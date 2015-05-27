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

import ninja.amp.fallout.Fallout;
import ninja.amp.fallout.characters.Character;
import ninja.amp.fallout.characters.CharacterManager;
import ninja.amp.fallout.characters.Skill;
import ninja.amp.fallout.characters.Trait;
import ninja.amp.fallout.message.FOMessage;
import ninja.amp.fallout.utils.ArmorMaterial;
import ninja.amp.fallout.utils.DamageType;
import ninja.amp.fallout.utils.FOArmor;
import ninja.amp.fallout.utils.FOUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RollManager {
    private Fallout plugin;

    public RollManager(Fallout plugin) {
        this.plugin = plugin;
    }

    public void rollDefault(Player player, String value, Distance distance) {
        UUID playerId = player.getUniqueId();
        CharacterManager characterManager = plugin.getCharacterManager();
        if (characterManager.isOwner(playerId)) {
            Character character = characterManager.getCharacterByOwner(playerId);

            // Parse trait/skill name from modifier if one exists
            String rolling = value;
            int modifier = 0;
            try {
                String[] arg = value.split("\\+");
                if (arg.length > 1) {
                    rolling = arg[0];
                    modifier += Integer.parseInt(arg[1]);
                } else {
                    arg = value.split("-");
                    if (arg.length > 1) {
                        rolling = arg[0];
                        modifier -= Integer.parseInt(arg[1]);
                    }
                }
            } catch (NumberFormatException e) {
                plugin.getMessenger().sendMessage(player, FOMessage.ERROR_MODIFIERSYNTAX);
                return;
            }

            // Perform the roll
            Trait trait = Trait.fromName(rolling);
            if (trait == null) {
                Skill skill = Skill.fromName(rolling);
                if (skill == null) {
                    plugin.getMessenger().sendMessage(player, FOMessage.ROLL_CANTROLL, value);
                } else {
                    int roll = FOUtils.random(1, 20);
                    Result outcome = getResult(roll, skillModifier(character, skill, modifier), character.getSpecial().get(Trait.LUCK));
                    switch (distance) {
                        case GLOBAL:
                            plugin.getMessenger().sendMessage(plugin.getServer(), FOMessage.ROLL_BROADCAST, character.getCharacterName(), roll, skill.getName(), modifier, outcome.getName());
                            break;
                        case LOCAL:
                            plugin.getMessenger().sendMessage(player.getLocation(), FOMessage.ROLL_BROADCAST, character.getCharacterName(), roll, skill.getName(), modifier, outcome.getName());
                            break;
                        case PRIVATE:
                            plugin.getMessenger().sendMessage(player, FOMessage.ROLL_MESSAGE, roll, skill.getName(), modifier, outcome.getName());
                            break;
                    }
                }
            } else {
                int roll = FOUtils.random(1, 20);
                Result outcome = getResult(roll, specialModifier(character, trait, modifier), character.getSpecial().get(Trait.LUCK));
                switch (distance) {
                    case GLOBAL:
                        plugin.getMessenger().sendMessage(plugin.getServer(), FOMessage.ROLL_BROADCAST, character.getCharacterName(), roll, trait.getName(), modifier, outcome.getName());
                        break;
                    case LOCAL:
                        plugin.getMessenger().sendMessage(player.getLocation(), FOMessage.ROLL_BROADCAST, character.getCharacterName(), roll, trait.getName(), modifier, outcome.getName());
                        break;
                    case PRIVATE:
                        plugin.getMessenger().sendMessage(player, FOMessage.ROLL_MESSAGE, roll, trait.getName(), modifier, outcome.getName());
                        break;
                }
            }
        } else {
            plugin.getMessenger().sendMessage(player, FOMessage.CHARACTER_NOTOWNER);
        }
    }

    public void rollArmor(Player player, String value, Distance distance) {
        UUID playerId = player.getUniqueId();
        CharacterManager characterManager = plugin.getCharacterManager();
        if (characterManager.isOwner(playerId)) {
            Character character = characterManager.getCharacterByOwner(playerId);

            // Parse damage type from modifier if one exists
            String rolling = value;
            int modifier = 0;
            try {
                String[] arg = value.split("\\+");
                if (arg.length > 1) {
                    rolling = arg[0];
                    modifier += Integer.parseInt(arg[1]);
                } else {
                    arg = value.split("-");
                    if (arg.length > 1) {
                        rolling = arg[0];
                        modifier -= Integer.parseInt(arg[1]);
                    }
                }
            } catch (NumberFormatException e) {
                plugin.getMessenger().sendMessage(player, FOMessage.ERROR_MODIFIERSYNTAX);
                return;
            }

            // Perform the roll
            DamageType damageType = DamageType.fromName(rolling);
            if (damageType == null) {
                // TODO: error u fukd up syntax
                return;
            }
            boolean blocked;
            if (ArmorMaterial.isWearingFullSet(player)) {
                FOArmor foArmor = ArmorMaterial.getArmorMaterial(player.getInventory().getHelmet().getType()).getFOVersion();
                int roll = FOUtils.random(1, 20) + modifier;
                blocked = foArmor.canBlock(damageType, roll);
            } else {
                blocked = false;
            }
            if (blocked) {
                // TODO: blocked messages
                switch (distance) {
                    case GLOBAL:
                        break;
                    case LOCAL:
                        break;
                    case PRIVATE:
                        break;
                }
            } else {
                // TODO: not blocked messages
                switch (distance) {
                    case GLOBAL:
                        break;
                    case LOCAL:
                        break;
                    case PRIVATE:
                        break;
                }
            }
        } else {
            plugin.getMessenger().sendMessage(player, FOMessage.CHARACTER_NOTOWNER);
        }
    }

    public void rollDice(Player player, String value, Distance distance) {
        UUID playerId = player.getUniqueId();
        CharacterManager characterManager = plugin.getCharacterManager();
        if (characterManager.isOwner(playerId)) {
            Character character = characterManager.getCharacterByOwner(playerId);

            // Parse amount and sides from modifier if one exists
            int amount;
            int sides;
            int modifier = 0;
            try {
                String[] arg = value.split("d");
                amount = Integer.parseInt(arg[0]);
                String[] arg2 = arg[1].split("\\+");
                if (arg2.length > 1) {
                    sides = Integer.parseInt(arg2[0]);
                    modifier += Integer.parseInt(arg2[1]);
                } else {
                    arg2 = arg[1].split("-");
                    if (arg2.length > 1) {
                        sides = Integer.parseInt(arg2[0]);
                        modifier -= Integer.parseInt(arg2[1]);
                    } else {
                        sides = Integer.parseInt(arg[1]);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // TODO: error u fukd up syntax
                return;
            } catch (NumberFormatException e) {
                plugin.getMessenger().sendMessage(player, FOMessage.ERROR_MODIFIERSYNTAX);
                return;
            }

            // Perform the roll
            // TODO: amount?
            int roll = FOUtils.random(1, sides) + modifier;
            // TODO: messages
            switch (distance) {
                case GLOBAL:
                    break;
                case LOCAL:
                    break;
                case PRIVATE:
                    break;
            }
        } else {
            plugin.getMessenger().sendMessage(player, FOMessage.CHARACTER_NOTOWNER);
        }
    }

    public int specialModifier(ninja.amp.fallout.characters.Character character, Trait trait, int modifier) {
        return character.getSpecial().get(trait) + modifier;
    }

    public int skillModifier(Character character, Skill skill, int modifier) {
        return character.skillLevel(skill) + skill.getRollModifier(character.getSpecial()) + modifier;
    }

    public Result getResult(int roll, int modifier, int luck) {
        // 1 or 20 is always critical
        if (roll == 1) {
            return Result.CRITICAL_FAILURE;
        } else if (roll == 20) {
            return Result.CRITICAL_SUCCESS;
        }

        int nearSuccess = 18 - modifier;
        if (roll > nearSuccess) {
            // Check if result should be critical
            int criticals = Math.max(1, Math.min(4, luck - 5));
            if (roll + criticals > 20) {
                return Result.CRITICAL_SUCCESS;
            } else {
                return Result.SUCCESS;
            }
        } else if (roll >= nearSuccess) {
            return Result.NEAR_SUCCESS;
        } else {
            // Check if result should be critical
            int criticals = Math.max(1, 5 - luck);
            if (roll - criticals < 1) {
                return Result.CRITICAL_FAILURE;
            } else {
                return Result.FAILURE;
            }
        }
    }

    /**
     * Results of a roll.
     */
    public enum Result {
        CRITICAL_FAILURE("Critical Failure"),
        FAILURE("Failure"),
        NEAR_SUCCESS("Near Success"),
        SUCCESS("Success"),
        CRITICAL_SUCCESS("Critical Success");

        private final String name;

        private Result(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum Distance {
        GLOBAL,
        LOCAL,
        PRIVATE
    }
}