/*
 * This file is part of Fallout.
 *
 * Copyright (c) 2013-2017 <http://github.com/ampayne2/Fallout//>
 *
 * Fallout is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fallout is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fallout.  If not, see <http://www.gnu.org/licenses/>.
 */
package ninja.amp.fallout.character;

import ninja.amp.fallout.Fallout;
import ninja.amp.fallout.config.ConfigAccessor;
import ninja.amp.fallout.config.ConfigManager;
import ninja.amp.fallout.config.FOConfig;
import ninja.amp.fallout.message.FOMessage;
import ninja.amp.fallout.message.Messenger;
import ninja.amp.fallout.util.FOUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Manages all of the fallout characters.
 *
 * @author Austin Payne
 */
public class CharacterManager {

    private Fallout plugin;
    private Map<UUID, Character> charactersByOwner = new HashMap<>();
    private Map<String, Character> charactersByName = new HashMap<>();
    private Map<UUID, Character.CharacterBuilder> characterBuilders = new HashMap<>();

    /**
     * Creates a new character manager.<br>
     * Must be created after the {@link ninja.amp.fallout.config.ConfigManager} and {@link ninja.amp.fallout.message.Messenger}!
     *
     * @param plugin The fallout plugin instance
     */
    public CharacterManager(Fallout plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();

        // Load character name requirement REGEX
        FOUtils.setNamePattern(config.isString("NameRequirement") ? Pattern.compile(config.getString("NameRequirement")) : null);

        // Players may already be online in case of reload
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadCharacter(player);
        }
    }

    /**
     * Loads a player's character if currently owning one.
     *
     * @param owner The player whose character to load
     * @return The player's character
     */
    public synchronized Character loadCharacter(Player owner) {
        Messenger messenger = plugin.getMessenger();
        ConfigManager configManager = plugin.getConfigManager();

        UUID ownerId = owner.getUniqueId();
        FileConfiguration playerConfig = configManager.getConfig(FOConfig.PLAYER);
        if (playerConfig.contains(ownerId.toString())) {
            // Find name of player's character
            String characterName = playerConfig.getString(ownerId.toString());

            // Load character from character config
            FileConfiguration characterConfig = configManager.getConfig(FOConfig.CHARACTER);

            Character character;
            try {
                character = new Character(characterConfig.getConfigurationSection(characterName.toLowerCase()));
                character.updateRadiationResistance();
            } catch (Exception e) {
                messenger.sendErrorMessage(owner, FOMessage.ERROR_CHARACTERLOAD, characterName, e.getMessage());
                messenger.debug("Failed to load character " + characterName + ". " + e.getMessage());
                return null;
            }
            messenger.debug("Loaded character " + characterName);

            // Save loaded character to update any information
            saveCharacter(character);

            // Add character to manager
            return addToManager(character);
        }

        return null;
    }

    /**
     * Loads an offline character.
     *
     * @param characterName The character's name
     * @return The offline character, or {@code} null if character doesn't exist
     */
    public synchronized Character loadOfflineCharacter(String characterName) {
        Messenger messenger = plugin.getMessenger();

        FileConfiguration characterConfig = plugin.getConfigManager().getConfig(FOConfig.CHARACTER);
        if (characterConfig.contains(characterName.toLowerCase())) {
            try {
                Character character = new Character(characterConfig.getConfigurationSection(characterName.toLowerCase()));
                messenger.debug("Loaded offline character " + characterName);
                return character;
            } catch (Exception e) {
                messenger.debug("Failed to load character " + characterName + ". " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Unloads a player's character if currently owning one.
     *
     * @param owner The player whose character to unload
     */
    public synchronized void unloadCharacter(Player owner) {
        UUID ownerId = owner.getUniqueId();
        if (isOwner(ownerId)) {
            removeFromManager(charactersByOwner.get(ownerId));
        }
        if (characterBuilders.containsKey(ownerId)) {
            characterBuilders.remove(ownerId);
            plugin.getMessenger().debug("Removed character builder for player " + owner.getName());
        }
    }

    /**
     * Saves a character to the character config.<br>
     * The character is saved to the path of the character's name in lowercase.
     *
     * @param character The character to save
     */
    public synchronized void saveCharacter(Character character) {
        ConfigAccessor characterConfig = plugin.getConfigManager().getConfigAccessor(FOConfig.CHARACTER);
        character.save(characterConfig.getConfig().getConfigurationSection(character.getCharacterName().toLowerCase()));
        characterConfig.saveConfig();
        plugin.getMessenger().debug("Saved character " + character.getCharacterName());
    }

    /**
     * Adds a character to the manager.
     *
     * @param character The character to add to the manager
     */
    private synchronized Character addToManager(Character character) {
        charactersByOwner.put(character.getOwnerId(), character);
        charactersByName.put(character.getCharacterName().toLowerCase(), character);
        plugin.getMessenger().debug("Added character " + character.getCharacterName() + " to character manager");
        return character;
    }

    /**
     * Removes a character from the manager.
     *
     * @param character The character to remove from the manager
     */
    private synchronized void removeFromManager(Character character) {
        charactersByOwner.remove(character.getOwnerId());
        charactersByName.remove(character.getCharacterName().toLowerCase());
        plugin.getMessenger().debug("Removed character " + character.getCharacterName() + " from character manager");
    }

    /**
     * Creates a character and adds it to the manager.<br>
     * Player must not already own a character.<br>
     * Player must have a valid builder in the manager, added with {@link CharacterManager#addCharacterBuilder}.
     *
     * @param owner The character's owner
     * @return The character created
     */
    public synchronized Character createCharacter(Player owner) {
        ConfigManager configManager = plugin.getConfigManager();

        // Create character from character builder and add to manager
        UUID ownerId = owner.getUniqueId();
        Character character = characterBuilders.get(ownerId).build();
        character.updateRadiationResistance();
        characterBuilders.remove(ownerId);
        plugin.getMessenger().debug("Created character " + character.getCharacterName());

        // Add owning player to players config
        ConfigAccessor playerConfig = configManager.getConfigAccessor(FOConfig.PLAYER);
        playerConfig.getConfig().set(ownerId.toString(), character.getCharacterName());
        playerConfig.saveConfig();

        // Add character to character config
        ConfigAccessor characterConfig = configManager.getConfigAccessor(FOConfig.CHARACTER);
        characterConfig.getConfig().createSection(character.getCharacterName().toLowerCase());
        characterConfig.saveConfig();
        saveCharacter(character);

        // Add character to manager
        addToManager(character);

        // Nickname player
        if (plugin.getConfig().getBoolean("NicknamePlayers", true)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "nick " + owner.getName() + " " + character.getCharacterName());
        }

        return character;
    }

    /**
     * Deletes a character.
     *
     * @param character The character to delete
     */
    public synchronized void deleteCharacter(Character character) {
        // Remove character from manager
        removeFromManager(character);

        ConfigManager configManager = plugin.getConfigManager();

        // Remove character from character config
        ConfigAccessor characterConfig = configManager.getConfigAccessor(FOConfig.CHARACTER);
        characterConfig.getConfig().set(character.getCharacterName().toLowerCase(), null);
        characterConfig.saveConfig();
        plugin.getMessenger().debug("Deleted character " + character.getCharacterName());

        // Remove owning player from players config
        ConfigAccessor playerConfig = configManager.getConfigAccessor(FOConfig.PLAYER);
        playerConfig.getConfig().set(character.getOwnerId().toString(), null);
        playerConfig.saveConfig();

        // Remove nickname from player if set
        if (plugin.getConfig().getBoolean("NicknamePlayers", true)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "nick " + character.getOwnerName() + " off");
        }
    }

    /**
     * Possesses a currently unowned character.<br>
     * In order to be possessed, the character must exist and have no current owner.
     *
     * @param owner         The character's new owner
     * @param characterName The character's name
     * @return The character possessed
     */
    public synchronized Character possessCharacter(Player owner, String characterName) {
        Messenger messenger = plugin.getMessenger();
        ConfigManager configManager = plugin.getConfigManager();

        // Load character from character config
        FileConfiguration characterConfig = configManager.getConfig(FOConfig.CHARACTER);
        Character character;
        try {
            character = new Character(characterConfig.getConfigurationSection(characterName.toLowerCase()));
            character.updateRadiationResistance();
        } catch (Exception e) {
            messenger.sendErrorMessage(owner, FOMessage.ERROR_CHARACTERLOAD, characterName, e.getMessage());
            messenger.debug("Failed to load character " + characterName + ". " + e.getMessage());
            return null;
        }
        if (character.getOwnerName() == null) {
            // Possess character
            character.possess(owner);
            messenger.debug("Possessed character " + character.getCharacterName());

            // Add owning player to players config
            ConfigAccessor playerConfig = configManager.getConfigAccessor(FOConfig.PLAYER);
            playerConfig.getConfig().set(owner.getUniqueId().toString(), character.getCharacterName());
            playerConfig.saveConfig();

            // Save loaded character to update owner information
            saveCharacter(character);

            // Nickname player
            if (plugin.getConfig().getBoolean("NicknamePlayers", true)) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "nick " + owner.getName() + " " + character.getCharacterName());
            }

            // Add character to manager
            return addToManager(character);
        }

        return null;
    }

    /**
     * Separates a character from its owner.<br>
     * Player must be the owner of the character.
     *
     * @param owner The character's owner
     */
    public synchronized Character abandonCharacter(Player owner) {
        ConfigManager configManager = plugin.getConfigManager();

        UUID ownerId = owner.getUniqueId();
        Character character = charactersByOwner.get(ownerId);

        // Remove character from manager (must be done before abandoning)
        removeFromManager(character);

        // Abandon character
        character.abandon();
        plugin.getMessenger().debug("Abandoned character " + character.getCharacterName());

        // Save character to update owner information
        saveCharacter(character);

        // Remove owning player from players config
        ConfigAccessor playerConfig = configManager.getConfigAccessor(FOConfig.PLAYER);
        playerConfig.getConfig().set(owner.getUniqueId().toString(), null);
        playerConfig.saveConfig();

        // Remove nickname from player if set
        if (plugin.getConfig().getBoolean("NicknamePlayers", true)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "nick " + character.getOwnerName() + " off");
        }

        return character;
    }

    /**
     * Adds a character builder to the manager for creation.
     *
     * @param player  The character's owner
     * @param builder The character builder
     */
    public synchronized void addCharacterBuilder(Player player, Character.CharacterBuilder builder) {
        characterBuilders.put(player.getUniqueId(), builder);
        plugin.getMessenger().debug("Added character builder for player " + player.getName());
    }

    /**
     * Gets a player's character builder.
     *
     * @param player The character's owner
     * @return The character builder, or {@code null} if the player has not begun to create a character
     */
    public synchronized Character.CharacterBuilder getCharacterBuilder(Player player) {
        return characterBuilders.get(player.getUniqueId());
    }

    /**
     * Checks if a player owns a character.
     *
     * @param playerId The player's uuid
     * @return {@code true} if the player is an owner
     */
    public synchronized boolean isOwner(UUID playerId) {
        return charactersByOwner.containsKey(playerId);
    }

    /**
     * Checks if the character of a certain name exists in the character config.
     *
     * @param characterName The character's name
     * @return {@code true} if the character exists
     */
    public synchronized boolean isCharacter(String characterName) {
        return plugin.getConfigManager().getConfig(FOConfig.CHARACTER).contains(characterName.toLowerCase());
    }

    /**
     * Checks if the character of a certain name is loaded in the character manager.
     *
     * @param characterName The character's name
     * @return {@code true} if the character is loaded
     */
    public synchronized boolean isLoaded(String characterName) {
        return charactersByName.containsKey(characterName.toLowerCase());
    }

    /**
     * Checks if the character of a certain name can be possessed.
     *
     * @param characterName The character's name
     * @return {@code true} if the character exists and has no current owner
     */
    public synchronized boolean canPossess(String characterName) {
        FileConfiguration characterConfig = plugin.getConfigManager().getConfig(FOConfig.CHARACTER);
        return characterConfig.contains(characterName.toLowerCase()) && !characterConfig.contains(characterName.toLowerCase() + ".ownerId");
    }

    /**
     * Gets the character currently owned by a player.
     *
     * @param ownerId The owner's UUID
     * @return The character owned by the player
     */
    public synchronized Character getCharacterByOwner(UUID ownerId) {
        return charactersByOwner.containsKey(ownerId) ? charactersByOwner.get(ownerId) : null;
    }

    /**
     * Gets the character of a given name. Character must have an online owner.
     *
     * @param characterName The character's name
     * @return The character with the given name
     */
    public synchronized Character getCharacterByName(String characterName) {
        return charactersByName.containsKey(characterName.toLowerCase()) ? charactersByName.get(characterName.toLowerCase()) : null;
    }

    /**
     * Gets the characters loaded in the manager.
     *
     * @return The characters in the manager
     */
    public Collection<Character> getCharacters() {
        return charactersByOwner.values();
    }

    /**
     * Gets the characters loaded in the manager and their owners.
     *
     * @return The map of owners and characters
     */
    public Map<UUID, Character> getCharactersByOwner() {
        return charactersByOwner;
    }

    /**
     * Gets the characters loaded in the manager and their names.
     *
     * @return The map of character names and characters
     */
    public Map<String, Character> getCharactersByName() {
        return charactersByName;
    }

    /**
     * Gets a list of the characters loaded in the manager.
     *
     * @return A list of the names of the manager's characters
     */
    public synchronized List<String> getCharacterList() {
        return new ArrayList<>(charactersByName.keySet());
    }

    /**
     * Gets a list of the characters that exist in the character config.
     *
     * @return A list of the names of the existing characters
     */
    public synchronized List<String> getExistingCharacters() {
        return new ArrayList<>(this.plugin.getConfigManager().getConfig(FOConfig.CHARACTER).getKeys(false));
    }

}
