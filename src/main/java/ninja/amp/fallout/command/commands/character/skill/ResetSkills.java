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
import ninja.amp.fallout.character.CharacterManager;
import ninja.amp.fallout.character.Skill;
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
 * A command that resets a character's skill levels.
 *
 * @author Austin Payne
 */
public class ResetSkills extends Command {

    public ResetSkills(FalloutCore fallout) {
        super(fallout, "resetskills");
        setDescription("Resets your or another fallout character's skill levels.");
        setCommandUsage("/fo character resetskills [character]");
        setPermission(new Permission("fallout.character.resetskills", PermissionDefault.OP));
        setArgumentRange(0, 1);
    }

    @Override
    public void execute(String command, CommandSender sender, List<String> args) {
        Player player = (Player) sender;

        Messenger messenger = fallout.getMessenger();
        CharacterManager characterManager = fallout.getCharacterManager();

        Character character;
        if (args.size() == 1) {
            String name = args.get(0);
            if (characterManager.isLoaded(name)) {
                character = characterManager.getCharacterByName(name);
            } else {
                messenger.sendErrorMessage(player, FOMessage.CHARACTER_DOESNTEXIST);
                return;
            }
        } else {
            UUID playerId = player.getUniqueId();
            if (characterManager.isOwner(playerId)) {
                character = characterManager.getCharacterByOwner(playerId);
            } else {
                messenger.sendErrorMessage(player, FOMessage.CHARACTER_NOTOWNER);
                return;
            }
        }

        for (Skill skill : Skill.class.getEnumConstants()) {
            character.setSkillLevel(skill, 0);
        }
        characterManager.saveCharacter(character);
        messenger.sendMessage(player, FOMessage.SKILLS_RESET, character.getCharacterName());
        messenger.sendMessage(character, FOMessage.SKILLS_RESETTED);
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
