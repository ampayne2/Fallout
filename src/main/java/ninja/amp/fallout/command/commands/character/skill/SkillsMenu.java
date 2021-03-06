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
package ninja.amp.fallout.command.commands.character.skill;

import ninja.amp.fallout.FalloutCore;
import ninja.amp.fallout.character.Character;
import ninja.amp.fallout.character.Skill;
import ninja.amp.fallout.menu.ItemMenu;
import ninja.amp.fallout.menu.Owner;
import ninja.amp.fallout.menu.events.ItemClickEvent;
import ninja.amp.fallout.menu.items.StaticMenuItem;
import ninja.amp.fallout.message.FOMessage;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An inventory menu used for viewing and increasing a player's skill levels.
 *
 * @author Austin Payne
 */
public class SkillsMenu extends ItemMenu {

    private FalloutCore fallout;
    private Map<UUID, Map<Skill, Integer>> pendingSkills = new HashMap<>();

    @SuppressWarnings("deprecation")
    public SkillsMenu(FalloutCore fallout) {
        super(ChatColor.AQUA + "Skill Allocation", Size.FIVE_LINE, fallout);

        this.fallout = fallout;

        setItem(41, new SkillsConfirmItem());
        setItem(40, new SkillPointsItem());
        setItem(39, new SkillsCancelItem());

        ItemStack icon = new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getWoolData());
        setItem(0, new SkillItem(Skill.BIG_GUNS, "Big Guns", icon));
        setItem(1, new SkillItem(Skill.CONVENTIONAL_GUNS, "Conventional Guns", icon));
        setItem(2, new SkillItem(Skill.ENERGY_WEAPONS, "Energy Weapons", icon));
        setItem(3, new SkillItem(Skill.MELEE_WEAPONS, "Melee Weapons", icon));

        setItem(5, new SkillItem(Skill.LOCKPICKING, "Lockpicking", icon));
        setItem(6, new SkillItem(Skill.SNEAK, "Sneak", icon));

        setItem(8, new SkillItem(Skill.SPEECH, "Speech", icon));

        setItem(18, new SkillItem(Skill.EXPLOSIVES, "Explosives", icon));
        setItem(19, new SkillItem(Skill.UNARMED, "Unarmed", icon));

        setItem(21, new SkillItem(Skill.FIRST_AID, "First Aid", icon));
        setItem(22, new SkillItem(Skill.SURGERY, "Surgery", icon));
        setItem(23, new SkillItem(Skill.REPAIR, "Repair", icon));

        setItem(25, new SkillItem(Skill.SCIENCE, "Science", icon));
        setItem(26, new SkillItem(Skill.LOGICAL_THINKING, "Logical Thinking", icon));
    }

    @Override
    public void open(Player player) {
        Character character = fallout.getCharacterManager().getCharacterByOwner(player.getUniqueId());
        pendingSkills.put(character.getOwnerId(), character.getSkillLevels());

        super.open(player);
    }

    /**
     * Gets the pending skill levels of a character.
     *
     * @param ownerId The uuid of the character's owner
     * @return The character's modified skill levels
     */
    public Map<Skill, Integer> getPendingSkills(UUID ownerId) {
        return pendingSkills.get(ownerId);
    }

    /**
     * Resets a character's pending skill levels.
     *
     * @param ownerId The uuid of the character's owner
     */
    public void resetPendingSkills(UUID ownerId) {
        pendingSkills.remove(ownerId);
    }

    /**
     * A menu item used in the skill allocation menu to indicate the current skill level.
     */
    private class SkillItem extends StaticMenuItem {

        private Skill skill;

        public SkillItem(Skill skill, String displayName, ItemStack icon, String... lore) {
            super(displayName, icon, lore);

            this.skill = skill;
        }

        @Override
        public ItemStack getFinalIcon(Player player) {
            ItemStack finalIcon = super.getFinalIcon(player).clone();

            Map<Skill, Integer> skills = getPendingSkills(player.getUniqueId());
            int amount = skills.get(skill);
            if (amount > 0) {
                finalIcon.setAmount(amount);
            } else {
                finalIcon.setType(Material.STONE_BUTTON);
            }

            return finalIcon;
        }

        @Override
        public void onItemClick(ItemClickEvent event) {
            Character character = event.getCharacter();
            Map<Skill, Integer> skills = getPendingSkills(character.getOwnerId());

            int totalPoints = (character.getLevel() + 1) * 5;
            int allocatedPoints = 0;
            for (int i : skills.values()) {
                allocatedPoints += i;
            }

            if (totalPoints > allocatedPoints && skills.get(skill) < 5) {
                skills.put(skill, skills.get(skill) + 1);
            }

            event.setWillUpdate(true);
        }

    }

    /**
     * A menu item used in the skill allocation menu to indicate available allocation points.
     */
    private class SkillPointsItem extends StaticMenuItem {

        private ItemStack noPoints;

        public SkillPointsItem() {
            super(ChatColor.AQUA + "Allocation Points",
                    new ItemStack(Material.GOLD_INGOT),
                    "The skill points",
                    "currently available",
                    "for allocation");

            noPoints = new ItemStack(Material.STONE_BUTTON);
            setNameAndLore(noPoints, "No Allocation Points", new ArrayList<>());
        }

        @Override
        public ItemStack getFinalIcon(Owner owner) {
            Character character = owner.getCharacter();
            Map<Skill, Integer> skills = getPendingSkills(character.getOwnerId());

            int totalPoints = (character.getLevel() + 1) * 5;
            int allocatedPoints = 0;
            for (int i : skills.values()) {
                allocatedPoints += i;
            }

            if (totalPoints > allocatedPoints) {
                ItemStack finalIcon = super.getFinalIcon(owner).clone();
                finalIcon.setAmount(totalPoints - allocatedPoints);

                return finalIcon;
            } else {
                return noPoints;
            }
        }

    }

    /**
     * A menu item used in the skill allocation menu to confirm skill allocation.
     */
    private class SkillsConfirmItem extends StaticMenuItem {

        @SuppressWarnings("deprecation")
        public SkillsConfirmItem() {
            super(ChatColor.GREEN + "Confirm Skill Allocation",
                    new ItemStack(Material.STAINED_GLASS, 1, DyeColor.LIGHT_BLUE.getWoolData()),
                    "THIS IS PERMANENT");
        }

        @Override
        public void onItemClick(ItemClickEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            Character character = event.getCharacter();
            Map<Skill, Integer> skills = getPendingSkills(playerId);

            for (Map.Entry<Skill, Integer> skill : skills.entrySet()) {
                character.setSkillLevel(skill.getKey(), skill.getValue());
            }
            fallout.getCharacterManager().saveCharacter(character);
            fallout.getMessenger().sendMessage(player, FOMessage.SKILLS_CONFIRM);

            resetPendingSkills(playerId);

            event.setWillClose(true);
        }

    }

    /**
     * A menu item used in the skill allocation menu to cancel skill allocation.
     */
    private class SkillsCancelItem extends StaticMenuItem {

        @SuppressWarnings("deprecation")
        public SkillsCancelItem() {
            super(ChatColor.DARK_RED + "Cancel Skill Allocation",
                    new ItemStack(Material.STAINED_GLASS, 1, DyeColor.MAGENTA.getWoolData()),
                    "Cancels the current",
                    "selection and exits",
                    "the menu.");
        }

        @Override
        public void onItemClick(ItemClickEvent event) {
            resetPendingSkills(event.getPlayer().getUniqueId());

            event.setWillClose(true);
        }

    }

}
