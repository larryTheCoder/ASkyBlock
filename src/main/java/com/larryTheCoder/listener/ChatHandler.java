/*
 * Copyright (C) 2017 Adam Matthew 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Adam Matthew
 */
public class ChatHandler implements Listener {

    private final ASkyBlock plugin;
    private final ConcurrentHashMap<Player, String> playerLevels;
    private final ConcurrentHashMap<UUID, String> playerChallengeLevels;
    private ConcurrentHashMap<Player, Boolean> teamChatUsers;

    public ChatHandler(ASkyBlock plugin) {
        this.plugin = plugin;
        this.playerLevels = new ConcurrentHashMap<>();
        this.teamChatUsers = new ConcurrentHashMap<>();
        this.playerChallengeLevels = new ConcurrentHashMap<>();
        // Add all online player Levels
        plugin.getServer().getOnlinePlayers().values().stream().map((player) -> {
            playerLevels.put(player, String.valueOf(plugin.getIslandLevel(player)));
            return player;
        }).forEachOrdered((player) -> {
            playerChallengeLevels.put(player.getUniqueId(), plugin.getChallenges().getChallengeLevel(player));
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final PlayerChatEvent event) {
        // Substitute variable - thread safe
        String level = "";
        if (playerLevels.containsKey(event.getPlayer())) {
            level = playerLevels.get(event.getPlayer());
        }
        String format = event.getFormat().replace("{ISLAND_LEVEL}", level);
        event.setFormat(format);
        if (Settings.teamChat && teamChatUsers.containsKey(event.getPlayer())) {
            // Cancel the event
            event.setCancelled(true);
            // Queue the sync task because you cannot use HashMaps asynchronously. Delaying to the next tick
            // won't be a major issue for synch events either.
            Server.getInstance().getScheduler().scheduleTask(plugin, () -> {
                teamChat(event, event.getMessage());
            });
        }
    }

    private void teamChat(PlayerChatEvent event, String message) {
        Player player = event.getPlayer();
        String playerUUID = player.getName();
        // Is team chat on for this player
        // Find out if this player is in a team (should be if team chat is on)
        // TODO: remove when player resets or leaves team
        if ((plugin.getTManager().getPlayerMembers(player.getName()).isEmpty()) == false) {
            ArrayList<String> teams = plugin.getTManager().getPlayerMembers(player.getName());
            // Tell only the team members if they are online
            boolean online = false;
            for (String teamMembers : teams) {
                Player teamPlayer = plugin.getServer().getPlayer(teamMembers);
                if (teamPlayer != null) {
                    teamPlayer.sendMessage(plugin.getPrefix() + message);
                    if (!teamMembers.equals(playerUUID)) {
                        online = true;
                    }
                }
            }
            // todo spy function
            if (!online) {
                player.sendMessage(plugin.getPrefix() + TextFormat.RED + plugin.getLocale(player).teamChatNoTeamAround);
                player.sendMessage(plugin.getPrefix() + TextFormat.RED + plugin.getLocale(player).teamChatStatusOff);
                teamChatUsers.remove(player);
            }
        } else {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + plugin.getLocale(player).teamChatNoTeamAround);
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + plugin.getLocale(player).teamChatStatusOff);
            // Not in a team any more so delete
            teamChatUsers.remove(player);
        }
    }

    /**
     * @param p adds player to chat
     */
    public void setPlayer(Player p) {
        this.teamChatUsers.put(p, true);
    }

    /**
     * Removes player from team chat
     *
     * @param p
     */
    public void unSetPlayer(Player p) {
        this.teamChatUsers.remove(p);
    }

    /**
     * Whether the player has team chat on or not
     *
     * @param p
     * @return true if team chat is on
     */
    public boolean isTeamChat(Player p) {
        return this.teamChatUsers.containsKey(p);
    }

    /**
     * Store the player's challenge level for use in their chat tag
     *
     * @param player
     */
    public void setPlayerChallengeLevel(Player player) {
        //plugin.getLogger().info("DEBUG: setting player's challenge level to " + plugin.getChallenges().getChallengeLevel(player));
        playerChallengeLevels.put(player.getUniqueId(), plugin.getChallenges().getChallengeLevel(player));
    }

    /**
     * Store the player's level for use in their chat tag
     *
     * @param playerUUID
     * @param level
     */
    public void setPlayerLevel(Player playerUUID, int level) {
        //plugin.getLogger().info("DEBUG: putting " + playerUUID.toString() + " Level " + level);
        playerLevels.put(playerUUID, String.valueOf(level));
    }

    /**
     * Return the player's level for use in chat - async safe
     *
     * @param playerUUID
     * @return Player's level as string
     */
    public String getPlayerLevel(Player playerUUID) {
        return playerLevels.get(playerUUID);
    }
}
