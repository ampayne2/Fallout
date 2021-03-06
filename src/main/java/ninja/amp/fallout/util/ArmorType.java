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
package ninja.amp.fallout.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Minecraft armor types (where armor can be equipped).
 *
 * @author Austin Payne
 */
public enum ArmorType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS;

    /**
     * Checks if the slot of the armor type is empty.
     *
     * @param player The player
     * @return {@code true} if the slot is empty
     */
    public boolean canEquip(Player player) {
        switch (this) {
            case HELMET:
                return player.getInventory().getHelmet() == null;
            case CHESTPLATE:
                return player.getInventory().getChestplate() == null;
            case LEGGINGS:
                return player.getInventory().getLeggings() == null;
            case BOOTS:
                return player.getInventory().getBoots() == null;
            default:
                return false;
        }
    }

    /**
     * Gets the item stack a player has equipped in a slot.
     *
     * @param player The player
     * @return The item stack
     */
    public ItemStack getEquipped(Player player) {
        switch (this) {
            case HELMET:
                return player.getInventory().getHelmet();
            case CHESTPLATE:
                return player.getInventory().getChestplate();
            case LEGGINGS:
                return player.getInventory().getLeggings();
            case BOOTS:
                return player.getInventory().getBoots();
            default:
                return null;
        }
    }

    /**
     * Checks if a material is a piece of armor.
     *
     * @param material The material
     * @return {@code true} if the material is a piece of armor
     */
    public static boolean isArmor(Material material) {
        for (ArmorType armorType : ArmorType.class.getEnumConstants()) {
            if (material.name().contains(armorType.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the type of a piece of armor.
     *
     * @param material The piece of armor
     * @return The type of the piece of armor
     */
    public static ArmorType getArmorType(Material material) {
        for (ArmorType armorType : ArmorType.class.getEnumConstants()) {
            if (material.name().contains(armorType.name())) {
                return armorType;
            }
        }
        return null;
    }

}
