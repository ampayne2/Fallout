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
package ninja.amp.fallout.menu;

import ninja.amp.fallout.FalloutCore;
import ninja.amp.fallout.character.CharacterManager;
import ninja.amp.fallout.menu.events.ItemClickEvent;
import ninja.amp.fallout.menu.items.MenuItem;
import ninja.amp.fallout.menu.items.StaticMenuItem;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * A dynamic and interactive menu made up of an inventory and item stacks.
 *
 * @author Austin Payne
 */
public class ItemMenu {

    private FalloutCore fallout;
    private String name;
    private Size size;
    private MenuItem[] items;
    private ItemMenu parent;

    /**
     * The menu item that appears in empty slots if {@link ItemMenu#fillEmptySlots()} is called.
     */
    @SuppressWarnings("deprecation")
    private static final MenuItem EMPTY_SLOT_ITEM = new StaticMenuItem(" ", new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.GRAY.getDyeData()));

    /**
     * Creates an item menu.
     *
     * @param name    The name of the inventory
     * @param size    The item menu of the inventory
     * @param fallout The fallout plugin core
     * @param parent  The item menu's parent
     */
    public ItemMenu(String name, Size size, FalloutCore fallout, ItemMenu parent) {
        this.fallout = fallout;
        this.name = name;
        this.size = size;
        this.items = new MenuItem[size.toInt()];
        this.parent = parent;
    }

    /**
     * Creates an item menu with no parent.
     *
     * @param name    The name of the inventory
     * @param size    The size of the inventory
     * @param fallout The fallout plugin core
     */
    public ItemMenu(String name, Size size, FalloutCore fallout) {
        this(name, size, fallout, null);
    }

    /**
     * Gets the name of the item menu.
     *
     * @return The item menu's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the size of the item menu.
     *
     * @return The item menu's size
     */
    public Size getSize() {
        return size;
    }

    /**
     * Checks if the item menu has a parent.
     *
     * @return {@code true} if the item menu has a parent
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Gets the parent of the item menu.
     *
     * @return The item menu's parent
     */
    public ItemMenu getParent() {
        return parent;
    }

    /**
     * Sets the parent of the item menu.
     *
     * @param parent The item menu to set as parent
     */
    public void setParent(ItemMenu parent) {
        this.parent = parent;
    }

    /**
     * Sets the menu item of a slot.
     *
     * @param position The slot position
     * @param menuItem The menu item
     * @return The item menu
     */
    public ItemMenu setItem(int position, MenuItem menuItem) {
        items[position] = menuItem;
        return this;
    }

    /**
     * Fills all empty slots in the item menu with a certain menu item.
     *
     * @param menuItem The menu item to fill empty slots with
     * @return The item menu
     */
    public ItemMenu fillEmptySlots(MenuItem menuItem) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                items[i] = menuItem;
            }
        }
        return this;
    }

    /**
     * Fills all empty slots in the item menu with the default empty slot item.
     *
     * @return The item menu
     */
    public ItemMenu fillEmptySlots() {
        return fillEmptySlots(EMPTY_SLOT_ITEM);
    }

    /**
     * Opens the item menu for a player.
     *
     * @param player The player
     */
    public void open(Player player) {
        MenuHolder holder = MenuHolder.createInventory(this, size.toInt(), name);
        apply(holder.getInventory(), player);
        player.openInventory(holder.getInventory());
    }

    /**
     * Updates the item menu for a player.
     *
     * @param player The player to update the item menu for
     */
    @SuppressWarnings("deprecation")
    public void update(Player player) {
        if (player.getOpenInventory() != null) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getHolder() instanceof MenuHolder && ((MenuHolder) inventory.getHolder()).getMenu().equals(this)) {
                apply(inventory, player);
                player.updateInventory();
            }
        }
    }

    /**
     * Applies the item menu for a player to an inventory.<br>
     * This overrides the existing contents of the inventory.
     *
     * @param inventory The inventory to apply the item menu to
     * @param player    The player
     */
    public void apply(Inventory inventory, Player player) {
        CharacterManager characterManager = fallout.getCharacterManager();
        if (characterManager.isOwner(player.getUniqueId())) {
            Owner owner = new Owner(player, characterManager.getCharacterByOwner(player.getUniqueId()));
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    inventory.setItem(i, items[i].getFinalIcon(owner));
                }
            }
        } else {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    inventory.setItem(i, items[i].getFinalIcon(player));
                }
            }
        }
    }

    /**
     * Handles inventory click events for the item menu.
     */
    @SuppressWarnings("deprecation")
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.LEFT) {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size.toInt() && items[slot] != null) {
                Player player = (Player) event.getWhoClicked();
                final UUID playerId = player.getUniqueId();

                ItemClickEvent itemClickEvent;
                CharacterManager characterManager = fallout.getCharacterManager();
                if (characterManager.isOwner(playerId)) {
                    itemClickEvent = new ItemClickEvent(player, characterManager.getCharacterByOwner(playerId));
                } else {
                    itemClickEvent = new ItemClickEvent(player);
                }
                items[slot].onItemClick(itemClickEvent);
                if (itemClickEvent.willUpdate()) {
                    update(player);
                } else {
                    player.updateInventory();
                    if (itemClickEvent.willClose() || itemClickEvent.willGoBack()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(fallout.getPlugin(), () -> {
                            Player p = Bukkit.getPlayer(playerId);
                            if (p != null) {
                                p.closeInventory();
                            }
                        }, 1);
                    }
                    if (itemClickEvent.willGoBack() && hasParent()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(fallout.getPlugin(), () -> {
                            Player p = Bukkit.getPlayer(playerId);
                            if (p != null) {
                                parent.open(p);
                            }
                        }, 3);
                    }
                }
            }
        }
    }

    /**
     * Possible inventory sizes of an item menu.
     */
    public enum Size {
        ONE_LINE(9),
        TWO_LINE(18),
        THREE_LINE(27),
        FOUR_LINE(36),
        FIVE_LINE(45),
        SIX_LINE(54);

        private final int size;

        Size(int size) {
            this.size = size;
        }

        /**
         * Gets the size's amount of slots.
         *
         * @return The amount of slots
         */
        public int toInt() {
            return size;
        }

        /**
         * Gets the required size for an amount of slots.
         *
         * @param slots The amount of slots
         * @return The required size
         */
        public static Size fit(int slots) {
            if (slots < 10) {
                return ONE_LINE;
            } else if (slots < 19) {
                return TWO_LINE;
            } else if (slots < 28) {
                return THREE_LINE;
            } else if (slots < 37) {
                return FOUR_LINE;
            } else if (slots < 46) {
                return FIVE_LINE;
            } else {
                return SIX_LINE;
            }
        }

    }

}
