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
package ninja.amp.fallout.menu.items;

import ninja.amp.fallout.menu.Owner;
import ninja.amp.fallout.menu.events.ItemClickEvent;
import ninja.amp.fallout.menu.events.ItemClickEventHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * An item inside an {@link ninja.amp.fallout.menu.ItemMenu}.
 *
 * @author Austin Payne
 */
public class MenuItem implements ItemClickEventHandler {

    private final String displayName;
    private final ItemStack icon;
    private final List<String> lore;

    public MenuItem(String displayName, ItemStack icon, String... lore) {
        this.displayName = displayName;
        if (icon == null) {
            this.icon = new ItemStack(Material.STONE);
        } else {
            this.icon = icon.clone();
        }
        this.lore = Arrays.asList(lore);
    }

    /**
     * Gets the display name of the menu item.
     *
     * @return The menu item's display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the icon of the menu item.
     *
     * @return The menu item's icon
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Gets the lore of the menu item.
     *
     * @return The menu item's lore
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * Gets the item stack for a certain player.
     *
     * @param player The player
     * @return The final icon
     */
    public ItemStack getFinalIcon(Player player) {
        return setNameAndLore(getIcon().clone(), getDisplayName(), getLore());
    }

    /**
     * Gets the item stack for a certain owner.
     *
     * @param owner The owner of a character
     * @return The final icon
     */
    public ItemStack getFinalIcon(Owner owner) {
        return getFinalIcon(owner.getPlayer());
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        // Do nothing by default
    }

    /**
     * Sets the display name and lore of an ItemStack.
     *
     * @param itemStack   The item stack
     * @param displayName The display name
     * @param lore        The lore
     * @return The item stack
     */
    public static ItemStack setNameAndLore(ItemStack itemStack, String displayName, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
