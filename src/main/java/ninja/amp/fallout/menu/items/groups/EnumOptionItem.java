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
package ninja.amp.fallout.menu.items.groups;

import ninja.amp.fallout.menu.events.ItemClickEvent;
import ninja.amp.fallout.menu.items.StaticMenuItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A menu item that is part of an {@link EnumOption}.
 *
 * @author Austin Payne
 */
public class EnumOptionItem<E extends Enum<E>> extends StaticMenuItem {

    private E e;
    private EnumOption<E> group;
    private ItemStack unselected;

    public EnumOptionItem(EnumOption<E> group, E e, String displayName, ItemStack selected, ItemStack unselected, String... lore) {
        super(displayName, selected, lore);

        this.group = group;
        this.e = e;
        this.unselected = unselected.clone();
        setNameAndLore(this.unselected, getDisplayName(), getLore());

        group.addOption(this);
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        if (this.equals(group.getSelected(player))) {
            return super.getFinalIcon(player);
        } else {
            return unselected;
        }
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        group.setSelected(event.getPlayer(), this);

        event.setWillUpdate(true);
    }

    /**
     * Gets the enum constant represented by the enum option item.
     *
     * @return The enum constant
     */
    public E getEnum() {
        return e;
    }

}
