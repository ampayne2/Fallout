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
package ninja.amp.fallout.command.commands.character;

import ninja.amp.fallout.FalloutCore;
import ninja.amp.fallout.character.CharacterManager;
import ninja.amp.fallout.command.Command;
import ninja.amp.fallout.message.FOMessage;
import ninja.amp.fallout.message.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.UUID;

/**
 * A command that deletes the senders fallout character.
 *
 * @author Austin Payne
 */
public class Delete extends Command {

    public Delete(FalloutCore fallout) {
        super(fallout, "delete");
        setDescription("Deletes your fallout character.");
        setCommandUsage("/fo character delete");
        setPermission(new Permission("fallout.character.delete", PermissionDefault.TRUE));
    }

    @Override
    public void execute(String command, CommandSender sender, List<String> args) {
        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        Messenger messenger = fallout.getMessenger();
        CharacterManager characterManager = fallout.getCharacterManager();
        if (characterManager.isOwner(playerId)) {
            characterManager.deleteCharacter(characterManager.getCharacterByOwner(playerId));
            messenger.sendMessage(player, FOMessage.CHARACTER_DELETE);
        } else {
            messenger.sendErrorMessage(player, FOMessage.CHARACTER_NOTOWNER);
        }
    }

}
