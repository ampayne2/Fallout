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

import ninja.amp.fallout.menu.ItemMenu;
import ninja.amp.fallout.menu.events.ItemClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * A menu item that opens a nested item menu.
 *
 * @author Austin Payne
 */
public class SubMenuItem extends StaticMenuItem {

    private final JavaPlugin plugin;
    private final ItemMenu menu;

    public SubMenuItem(JavaPlugin plugin, String displayName, ItemStack icon, ItemMenu menu, String... lore) {
        super(displayName, icon, lore);

        this.plugin = plugin;
        this.menu = menu;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onItemClick(ItemClickEvent event) {
        event.setWillClose(true);
        final UUID playerId = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                menu.open(p);
            }
        }, 3);
    }

}
