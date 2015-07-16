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
package ninja.amp.fallout.command.commands.roll;

import ninja.amp.fallout.FalloutCore;
import ninja.amp.fallout.character.Skill;
import ninja.amp.fallout.character.Trait;
import ninja.amp.fallout.command.Command;
import ninja.amp.fallout.message.FOMessage;
import ninja.amp.fallout.message.Messenger;
import ninja.amp.fallout.util.DamageType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * A command that lets you roll a trait or skill globally.
 *
 * @author Austin Payne
 */
public class GlobalRoll extends Command {

    private final List<String> tabCompleteList = new ArrayList<>();

    public GlobalRoll(FalloutCore fallout) {
        super(fallout, "globalroll");
        setVisible(false);
        setPermission(new Permission("fallout.globalroll", PermissionDefault.TRUE));
        setArgumentRange(1, 2);

        tabCompleteList.addAll(Trait.getTraitNames());
        tabCompleteList.addAll(Skill.getSkillNames());
        tabCompleteList.add("Dice");
        tabCompleteList.add("Armor");
    }

    @Override
    public void execute(String command, CommandSender sender, List<String> args) {
        String target = args.get(0);

        Messenger messenger = fallout.getMessenger();
        RollManager rollManager = fallout.getRollManager();
        switch (target.toLowerCase()) {
            case "dice":
                if (args.size() == 2) {
                    rollManager.rollDice((Player) sender, args.get(1), RollManager.Distance.GLOBAL);
                } else {
                    messenger.sendErrorMessage(sender, FOMessage.COMMAND_USAGE, "/fo globalroll dice <amount>d<sides>[+/-modifier]");
                }
                break;
            case "armor":
                if (args.size() == 2) {
                    rollManager.rollArmor((Player) sender, args.get(1), RollManager.Distance.GLOBAL);
                } else {
                    messenger.sendErrorMessage(sender, FOMessage.COMMAND_USAGE, "/fo globalroll armor <damage type>[+/-modifier]");
                }
                break;
            default:
                rollManager.rollStandard((Player) sender, target, RollManager.Distance.GLOBAL);
        }
    }

    @Override
    public List<String> getTabCompleteList(List<String> args) {
        switch (args.size()) {
            case 1:
                return tabCompletions(args.get(0), tabCompleteList);
            case 2:
                if (args.get(0).equalsIgnoreCase("armor")) {
                    return tabCompletions(args.get(1), DamageType.getDamageTypeNames());
                }
            default:
                return EMPTY_LIST;
        }
    }

}
