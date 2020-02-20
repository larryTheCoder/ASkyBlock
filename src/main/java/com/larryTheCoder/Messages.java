/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import com.larryTheCoder.player.CoopData;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Handles offline messaging to players and teams
 * <p>
 * @author tastybento
 * @author: larryTheCoder
 */
public class Messages {

    // TODO: REFACTOR THESE CODES

    // Offline Messages
    private final HashMap<String, List<String>> messages = new HashMap<>();
    private final ASkyBlock plugin;
    private Config messageStore;

    /**
     * @param plugin
     */
    Messages(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns what messages are waiting for the player or null if none
     *
     * @param p
     * @return List of messages
     */
    public List<String> getMessages(String p) {
        List<String> playerMessages = messages.get(p);
        return playerMessages;
    }

    /**
     * Clears any messages for player
     *
     * @param p
     */
    public void clearMessages(String p) {
        messages.remove(p);
    }

    void saveMessages() {
        if (messageStore == null) {
            return;
        }
        Utils.send("&7Saving offline messages...");
        try {
            // Convert to a serialized string
            final HashMap<String, Object> offlineMessages = new HashMap<>();
            messages.keySet().forEach((p) -> offlineMessages.put(p, messages.get(p)));
            // Convert to YAML
            messageStore.set("messages", offlineMessages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadMessages() {
        Utils.send("&7Loading offline messages...");
        try {
            messageStore = Utils.loadYamlFile("messages.yml");
            HashMap<String, Object> temp = messageStore.getSections("messages");
            temp.keySet().forEach((s) -> {
                List<String> messageList = messageStore.getStringList("messages." + s);
                if (!messageList.isEmpty()) {
                    messages.put(s, messageList);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Provides the messages for the player
     *
     * @param p
     * @return List of messages
     */
    private List<String> get(String p) {
        return messages.get(p);
    }

    /**
     * Stores a message for player
     *
     * @param p              The Player name
     * @param playerMessages Message to be given for the player
     */
    public void put(String p, List<String> playerMessages) {
        messages.put(p, playerMessages);
    }

    /**
     * Sends a message to every player in the team that is offline
     *
     * @param player
     * @param message
     */
    public void tellOfflineTeam(String player, String message) {
        if (!plugin.getTManager().hasTeam(player)) {
            return;
        }
        CoopData pd = plugin.getTManager().getLeaderCoop(player);
        List<String> teamMembers = pd.getMembers();
        for (String member : teamMembers) {
            if (plugin.getServer().getPlayer(player) == null) {
                // Offline player
                setMessage(member, message);
            }
        }
    }

    /**
     * Tells all online team members something happened
     *
     * @param player
     * @param message
     */
    public void tellTeam(String player, String message) {
        // getLogger().info("DEBUG: tell offline team called");
        if (!plugin.getTManager().hasTeam(player)) {
            // getLogger().info("DEBUG: player is not in a team");
            return;
        }
        CoopData pd = plugin.getTManager().getLeaderCoop(player);
        List<String> teamMembers = pd.getMembers();
        for (String member : teamMembers) {
            // getLogger().info("DEBUG: trying String " + member.toString());
            if (!member.equals(player) && plugin.getServer().getPlayer(member) != null) {
                // Online player
                plugin.getServer().getPlayer(member).sendMessage(message);
            }
        }
    }

    /**
     * Sets a message for the player to receive next time they login
     *
     * @param p
     * @param message
     * @return true if player is offline, false if online
     */
    public void setMessage(String p, String message) {
        // getLogger().info("DEBUG: received message - " + message);
        Player player = plugin.getServer().getPlayer(p);
        // Check if player is online
        if (player != null) {
            if (player.isOnline()) {
                player.sendMessage(message);
                return;
            }
        }
        // Player is offline so store the message
        // getLogger().info("DEBUG: player is offline - storing message");
        List<String> playerMessages = get(p);
        if (playerMessages != null) {
            playerMessages.add(message);
        } else {
            playerMessages = new ArrayList<>(Collections.singletonList(message));
        }
        put(p, playerMessages);
    }
}
