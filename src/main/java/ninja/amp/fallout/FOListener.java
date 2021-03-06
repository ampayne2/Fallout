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
package ninja.amp.fallout;

import ninja.amp.fallout.character.Character;
import ninja.amp.fallout.character.CharacterManager;
import ninja.amp.fallout.character.Information;
import ninja.amp.fallout.character.Race;
import ninja.amp.fallout.message.FOMessage;
import ninja.amp.fallout.util.ArmorMaterial;
import ninja.amp.fallout.util.ArmorType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles various fallout events.
 *
 * @author Austin Payne
 */
public class FOListener implements Listener {

    private Fallout plugin;
    private boolean preventCraftingDiamondArmor;
    private boolean preventMobsDroppingExp;
    private boolean preventSunlightCombust;
    private boolean preventZombieTargetGhoul;

    protected static Map<UUID, String> onlinePlayers = null;

    /**
     * Creates a new FOListener.
     *
     * @param plugin The fallout plugin instance
     */
    public FOListener(Fallout plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        FileConfiguration config = plugin.getConfig();
        preventCraftingDiamondArmor = config.getBoolean("PreventCraftingDiamondArmor", false);
        preventMobsDroppingExp = config.getBoolean("PreventMobsDroppingExp", false);
        preventSunlightCombust = config.getBoolean("PreventSunlightCombust", false);
        preventZombieTargetGhoul = config.getBoolean("PreventZombieTargetGhoul", false);

        onlinePlayers = new ConcurrentHashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayers.put(player.getUniqueId(), player.getName());
        }
    }

    /**
     * Loads the player's character into the character manager if the player has a character.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        onlinePlayers.put(player.getUniqueId(), player.getName());
        plugin.getCharacterManager().loadCharacter(player);
    }

    /**
     * Unloads the player's character from the character manager if the player has a character.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getCharacterManager().unloadCharacter(player);
        onlinePlayers.remove(player.getUniqueId());
    }

    /**
     * Lists the SPECIAL traits of the character of a player right clicked while crouching.
     */
    @EventHandler
    public void onPlayerClickPlayer(PlayerInteractEntityEvent event) {
        if (event.getPlayer().isSneaking() && event.getRightClicked() instanceof Player) {
            UUID clickedId = event.getRightClicked().getUniqueId();

            CharacterManager characterManager = plugin.getCharacterManager();
            if (characterManager.isOwner(clickedId)) {
                Character character = characterManager.getCharacterByOwner(clickedId);
                plugin.getMessenger().sendMessage(event.getPlayer(), FOMessage.SPECIAL_LIST, character.getCharacterName(), character.getSpecial());
            }
        }
    }

    /**
     * Stops players with characters who don't have the armor perk from right clicking to equip diamond armor.
     */
    /*
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerClickArmor(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack itemStack = player.getItemInHand();
            if (itemStack != null) {
                Material material = itemStack.getType();
                if (ArmorType.isArmor(material) && ArmorType.getArmorType(material).canEquip(player)) {
                    UUID playerId = player.getUniqueId();
                    CharacterManager characterManager = plugin.getCharacterManager();
                    if (characterManager.isOwner(playerId)) {
                        Character character = characterManager.getCharacterByOwner(playerId);
                        ArmorMaterial armorMaterial = ArmorMaterial.getArmorMaterial(material);
                        if (!ArmorMaterial.LEATHER.equals(armorMaterial) && character.getRace().equals(Race.SUPER_MUTANT)) { // Super Mutants can only wear leather armor
                            event.setCancelled(true);
                            player.updateInventory();
                            plugin.getMessenger().sendErrorMessage(player, FOMessage.RACE_ONLYLEATHER);
                        } else if (ArmorMaterial.DIAMOND.equals(armorMaterial) && !character.hasKnowledge(Information.POWER_ARMOR)) { // Wearing power armor requires permission
                            event.setCancelled(true);
                            player.updateInventory();
                            plugin.getMessenger().sendErrorMessage(player, FOMessage.INFORMATION_MISSING, Information.POWER_ARMOR);
                        }
                    }
                }
            }
        }
    }
    */

    /**
     * Stops players with characters who don't have the armor perk from manually equipping diamond armor.
     */
    /*
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (event.isLeftClick() || event.isRightClick()) {
                ItemStack itemStack = null;
                if (event.isShiftClick()) {
                    itemStack = event.getCurrentItem(); // Shift clicking will move the item in the slot
                } else if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                    itemStack = event.getCursor(); // Normal clicking will move the item in the cursor
                }
                if (itemStack != null) {
                    Material material = itemStack.getType();
                    if (ArmorType.isArmor(material)) {
                        Player player = (Player) event.getWhoClicked();
                        if (event.isShiftClick() && !ArmorType.getArmorType(material).canEquip(player)) { // The armor won't actually go into the armor slot, don't cancel
                            return;
                        }
                        UUID playerId = player.getUniqueId();
                        CharacterManager characterManager = plugin.getCharacterManager();
                        if (characterManager.isOwner(playerId)) {
                            Character character = characterManager.getCharacterByOwner(playerId);
                            ArmorMaterial armorMaterial = ArmorMaterial.getArmorMaterial(material);
                            if (!ArmorMaterial.LEATHER.equals(armorMaterial) && character.getRace().equals(Race.SUPER_MUTANT)) { // Super Mutants can only wear leather armor
                                event.setCancelled(true);
                                plugin.getMessenger().sendErrorMessage(player, FOMessage.RACE_ONLYLEATHER);
                            } else if (ArmorMaterial.DIAMOND.equals(armorMaterial) && !character.hasKnowledge(Information.POWER_ARMOR)) { // Wearing power armor requires permission
                                event.setCancelled(true);
                                plugin.getMessenger().sendErrorMessage(player, FOMessage.INFORMATION_MISSING, Information.POWER_ARMOR);
                            }
                        }
                    }
                }
            }
        }
    }
    */

    /**
     * Stops players with characters who don't have the armor perk from manually equipping diamond armor.
     */
    /*
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getOldCursor() != null) {
            Material material = event.getOldCursor().getType();
            if (ArmorType.isArmor(material)) {
                Player player = (Player) event.getWhoClicked();
                UUID playerId = player.getUniqueId();
                CharacterManager characterManager = plugin.getCharacterManager();
                if (characterManager.isOwner(playerId)) {
                    Character character = characterManager.getCharacterByOwner(playerId);
                    ArmorMaterial armorMaterial = ArmorMaterial.getArmorMaterial(material);
                    if (!ArmorMaterial.LEATHER.equals(armorMaterial) && character.getRace().equals(Race.SUPER_MUTANT)) { // Super Mutants can only wear leather armor
                        event.setCancelled(true);
                        plugin.getMessenger().sendErrorMessage(player, FOMessage.RACE_ONLYLEATHER);
                    } else if (ArmorMaterial.DIAMOND.equals(armorMaterial) && !character.hasKnowledge(Information.POWER_ARMOR)) { // Wearing power armor requires permission
                        event.setCancelled(true);
                        plugin.getMessenger().sendErrorMessage(player, FOMessage.INFORMATION_MISSING, Information.POWER_ARMOR);
                    }
                }
            }
        }
    }
    */

    /**
     * Stops mobs from dropping exp to prevent exp farming.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Creature && preventMobsDroppingExp) {
            event.setDroppedExp(0);
        }
    }

    /**
     * Stops mobs from burning due to sunlight.
     */
    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent || event.getEntityType() == EntityType.ARROW) {
            return;
        }
        if (preventSunlightCombust) {
            event.setCancelled(true);
        }
    }

    /**
     * Stops diamond armor from being crafted.
     */
    @EventHandler
    public void onDiamondArmorCraft(CraftItemEvent event) {
        if (preventCraftingDiamondArmor) {
            Material material = event.getRecipe().getResult().getType();
            if (ArmorType.isArmor(material) && ArmorMaterial.DIAMOND.equals(ArmorMaterial.getArmorMaterial(material))) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Stops zombies from targeting ghouls.
     */
    @EventHandler
    public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
        if (preventZombieTargetGhoul && event.getEntityType() == EntityType.ZOMBIE && event.getTarget() instanceof Player) {
            UUID playerId = event.getTarget().getUniqueId();
            CharacterManager characterManager = plugin.getCharacterManager();
            if (characterManager.isOwner(playerId)) {
                Character character = characterManager.getCharacterByOwner(playerId);
                if (character.getRace().equals(Race.GHOUL)) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
