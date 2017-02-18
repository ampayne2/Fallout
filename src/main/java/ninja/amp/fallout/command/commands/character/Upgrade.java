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
package ninja.amp.fallout.command.commands.character;

import ninja.amp.fallout.FalloutCore;
import ninja.amp.fallout.character.Character;
import ninja.amp.fallout.character.CharacterManager;
import ninja.amp.fallout.command.Command;
import ninja.amp.fallout.message.FOMessage;
import ninja.amp.fallout.message.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * A command that upgrades the level of a fallout character.
 *
 * @author Austin Payne
 */
public class Upgrade extends Command {

    public Upgrade(FalloutCore fallout) {
        super(fallout, "upgrade");
        setDescription("Upgrades the level of a fallout character.");
        setCommandUsage("/fo character upgrade <character>");
        setPermission(new Permission("fallout.character.upgrade", PermissionDefault.OP));
        setArgumentRange(1, 1);
        setPlayerOnly(false);
    }

    @Override
    public void execute(String command, CommandSender sender, List<String> args) {
        String name = args.get(0);

        Messenger messenger = fallout.getMessenger();
        CharacterManager characterManager = fallout.getCharacterManager();
        if (characterManager.isLoaded(name)) {
            Character character = characterManager.getCharacterByName(name);
            if (character.getLevel() > 4) {
                // Character is already max level
                messenger.sendErrorMessage(sender, FOMessage.CHARACTER_MAXLEVEL, character.getCharacterName());
            } else {
                // Upgrade level
                character.increaseLevel();

                // Save character to update level information
                characterManager.saveCharacter(character);

                messenger.sendMessage(sender, FOMessage.CHARACTER_UPGRADE, character.getCharacterName(), character.getLevel());
                messenger.sendMessage(character, FOMessage.CHARACTER_UPGRADED, character.getLevel());
            }
        } else {
            messenger.sendErrorMessage(sender, FOMessage.CHARACTER_DOESNTEXIST);
        }
    }

    @Override
    public List<String> tabComplete(List<String> args) {
        switch (args.size()) {
            case 1:
                return tabCompletions(args.get(0), fallout.getCharacterManager().getCharacterList());
            default:
                return EMPTY_LIST;
        }
    }

}
