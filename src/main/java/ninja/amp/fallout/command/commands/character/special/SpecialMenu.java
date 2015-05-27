/*
 * This file is part of Fallout.
 *
 * Copyright (c) 2013-2015 <http://github.com/ampayne2/Fallout//>
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
package ninja.amp.fallout.command.commands.character.special;

import ninja.amp.fallout.Fallout;
import ninja.amp.fallout.characters.Character;
import ninja.amp.fallout.characters.CharacterManager;
import ninja.amp.fallout.characters.Special;
import ninja.amp.fallout.characters.Trait;
import ninja.amp.fallout.menus.ItemMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Inventory menu used for setting a character's SPECIAL.
 */
public class SpecialMenu extends ItemMenu {
    private CharacterManager characterManager;
    private Map<Character, Special> pendingSpecials = new HashMap<>();

    public SpecialMenu(Fallout plugin) {
        super("SPECIAL Modification", Size.FIVE_LINE, plugin);

        this.characterManager = plugin.getCharacterManager();

        setItem(41, new SpecialConfirmItem(plugin, this));
        setItem(39, new SpecialCancelItem(plugin, this));

        setItem(10, new SpecialItem(plugin, this, Trait.STRENGTH, new ItemStack(Material.DIAMOND_SWORD)));
        setItem(1, new SpecialAddItem(plugin, this, Trait.STRENGTH));
        setItem(19, new SpecialRemoveItem(plugin, this, Trait.STRENGTH));

        setItem(11, new SpecialItem(plugin, this, Trait.PERCEPTION, new ItemStack(Material.COMPASS)));
        setItem(2, new SpecialAddItem(plugin, this, Trait.PERCEPTION));
        setItem(20, new SpecialRemoveItem(plugin, this, Trait.PERCEPTION));

        setItem(12, new SpecialItem(plugin, this, Trait.ENDURANCE, new ItemStack(Material.GOLD_BOOTS)));
        setItem(3, new SpecialAddItem(plugin, this, Trait.ENDURANCE));
        setItem(21, new SpecialRemoveItem(plugin, this, Trait.ENDURANCE));

        setItem(13, new SpecialItem(plugin, this, Trait.CHARISMA, new ItemStack(Material.DIAMOND)));
        setItem(4, new SpecialAddItem(plugin, this, Trait.CHARISMA));
        setItem(22, new SpecialRemoveItem(plugin, this, Trait.CHARISMA));

        setItem(14, new SpecialItem(plugin, this, Trait.INTELLIGENCE, new ItemStack(Material.BOOK)));
        setItem(5, new SpecialAddItem(plugin, this, Trait.INTELLIGENCE));
        setItem(23, new SpecialRemoveItem(plugin, this, Trait.INTELLIGENCE));

        setItem(15, new SpecialItem(plugin, this, Trait.AGILITY, new ItemStack(Material.FEATHER)));
        setItem(6, new SpecialAddItem(plugin, this, Trait.AGILITY));
        setItem(24, new SpecialRemoveItem(plugin, this, Trait.AGILITY));

        setItem(16, new SpecialItem(plugin, this, Trait.LUCK, new ItemStack(Material.DIAMOND_SWORD)));
        setItem(7, new SpecialAddItem(plugin, this, Trait.LUCK));
        setItem(25, new SpecialRemoveItem(plugin, this, Trait.LUCK));
    }

    @Override
    public void open(Player player) {
        UUID playerId = player.getUniqueId();
        Character character = characterManager.getCharacterByOwner(playerId);
        pendingSpecials.put(character, new Special(character.getSpecial()));

        super.open(player);
    }

    /**
     * Gets the pending SPECIAL of a character.
     *
     * @param character The character.
     * @return The character's modified SPECIAL.
     */
    public Special getPendingSpecial(Character character) {
        return pendingSpecials.get(character);
    }

    /**
     * Resets a character's pending SPECIAL.
     *
     * @param character The character.
     */
    public void resetPendingSpecial(Character character) {
        pendingSpecials.remove(character);
    }
}