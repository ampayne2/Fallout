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
package ninja.amp.fallout.message;

import ninja.amp.fallout.Fallout;
import ninja.amp.fallout.character.Character;
import ninja.amp.fallout.config.ConfigAccessor;
import ninja.amp.fallout.config.FOConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages message sending, logging, and debugging.
 *
 * @author Austin Payne
 */
public class Messenger {

    private Fallout plugin;
    private boolean debug;
    private Logger log;
    private Map<Class<?>, RecipientHandler> recipientHandlers = new HashMap<>();

    /**
     * Basic color scheme in the fallout plugin.
     */
    public static ChatColor PRIMARY_COLOR = ChatColor.AQUA;
    public static ChatColor SECONDARY_COLOR = ChatColor.GRAY;
    public static ChatColor HIGHLIGHT_COLOR = ChatColor.DARK_GRAY;

    /**
     * Creates a new message manager.<br>
     * Must be created after the {@link ninja.amp.fallout.config.ConfigManager}!
     *
     * @param plugin The fallout plugin instance
     */
    public Messenger(Fallout plugin) {
        this.plugin = plugin;
        this.debug = plugin.getConfig().getBoolean("Debug", false);
        this.log = plugin.getLogger();

        registerMessages(EnumSet.allOf(FOMessage.class));

        // Register types of message recipients
        registerRecipient(CommandSender.class, new RecipientHandler() {
            @Override
            public void sendMessage(Object recipient, String message) {
                ((CommandSender) recipient).sendMessage(message);
            }
        });
        registerRecipient(Server.class, new RecipientHandler() {
            @Override
            public void sendMessage(Object recipient, String message) {
                ((Server) recipient).broadcastMessage(message);
            }
        });
        registerRecipient(Character.class, new RecipientHandler() {
            @Override
            public void sendMessage(Object recipient, String message) {
                Bukkit.getPlayer(((Character) recipient).getOwnerId()).sendMessage(message);
            }
        });
        int radius = plugin.getConfig().getInt("MessageRadius", 30);
        final int radiusSquared = radius * radius;
        registerRecipient(Location.class, new RecipientHandler() {
            @Override
            public void sendMessage(Object recipient, String message) {
                Location location = (Location) recipient;
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (location.getWorld().equals(player.getWorld()) && location.distanceSquared(player.getLocation()) <= radiusSquared) {
                        player.sendMessage(message);
                    }
                }
            }
        });

        // Load color theme of messages from config
        FileConfiguration config = plugin.getConfig();
        PRIMARY_COLOR = ChatColor.valueOf(config.getString("colors.primary", "AQUA"));
        SECONDARY_COLOR = ChatColor.valueOf(config.getString("colors.secondary", "GRAY"));
        HIGHLIGHT_COLOR = ChatColor.valueOf(config.getString("colors.highlights", "DARK_GRAY"));
    }

    /**
     * Adds the message defaults to the message config and loads them.
     *
     * @param messages The messages to register
     * @return The messenger
     */
    public Messenger registerMessages(EnumSet<? extends Message> messages) {
        // Add missing messages to message config
        ConfigAccessor messageConfig = plugin.getConfigManager().getConfigAccessor(FOConfig.MESSAGE);
        FileConfiguration messageConfigFile = messageConfig.getConfig();
        for (Message message : messages) {
            if (!messageConfigFile.isString(message.getPath())) {
                messageConfigFile.set(message.getPath(), message.getMessage());
            }
        }
        messageConfig.saveConfig();

        // Load messages from message config
        for (Message message : messages) {
            message.setMessage(ChatColor.translateAlternateColorCodes('&', messageConfigFile.getString(message.getPath())));
        }
        return this;
    }

    /**
     * Registers a recipient with a recipient handler.
     *
     * @param recipientClass   The recipient's class
     * @param recipientHandler The recipient handler
     * @return The messenger
     */
    public Messenger registerRecipient(Class recipientClass, RecipientHandler recipientHandler) {
        recipientHandlers.put(recipientClass, recipientHandler);
        return this;
    }

    /**
     * Sends a message to a recipient.
     *
     * @param recipient The recipient of the message. Type of recipient must be registered
     * @param message   The message
     * @param replace   Strings to replace any occurences of %s in the message with
     */
    public void sendMessage(Object recipient, Message message, Object... replace) {
        for (String s : (replace == null ? message.getMessage() : String.format(message.getMessage(), (Object[]) replace)).split("\\\\n")) {
            sendRawMessage(recipient, FOMessage.PREFIX + s);
        }
    }

    /**
     * Sends an error message to a recipient.
     *
     * @param recipient The recipient of the error message. Type of recipient must be registered
     * @param message   The error message
     * @param replace   Strings to replace any occurences of %s in the message with
     */
    public void sendErrorMessage(Object recipient, Message message, Object... replace) {
        for (String s : (replace == null ? message.getMessage() : String.format(message.getMessage(), (Object[]) replace)).split("\\\\n")) {
            sendRawMessage(recipient, FOMessage.PREFIX_ERROR + s);
        }
    }

    /**
     * Sends a raw message string to a recipient.
     *
     * @param recipient The recipient of the message. Type of recipient must be registered
     * @param message   The message
     */
    public void sendRawMessage(Object recipient, Object message) {
        if (recipient != null && message != null) {
            for (Class<?> recipientClass : recipientHandlers.keySet()) {
                if (recipientClass.isAssignableFrom(recipient.getClass())) {
                    recipientHandlers.get(recipientClass).sendMessage(recipient, message.toString());
                    break;
                }
            }
        }
    }

    /**
     * Logs one or more messages to the console.
     *
     * @param level    The level to log the message at
     * @param messages The message(s) to log
     */
    public void log(Level level, Object... messages) {
        for (Object message : messages) {
            log.log(level, message.toString());
        }
    }

    /**
     * Decides whether or not to print the stack trace of an exception.
     *
     * @param e The exception to debug
     */
    public void debug(Exception e) {
        if (debug) {
            e.printStackTrace();
        }
    }

    /**
     * Decides whether or not to print a debug message.
     *
     * @param message The message to debug
     */
    public void debug(Object message) {
        if (debug) {
            log.log(Level.INFO, message.toString());
        }
    }

    /**
     * Gets the logger.
     *
     * @return The logger
     */
    public Logger getLogger() {
        return log;
    }

    /**
     * Handles sending a message to a recipient.
     */
    public abstract class RecipientHandler {

        /**
         * Sends a message to the recipient.
         *
         * @param recipient The recipient
         * @param message   The message
         */
        public abstract void sendMessage(Object recipient, String message);

    }

}
