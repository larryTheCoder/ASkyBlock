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
package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.CoopData;
import com.larryTheCoder.utils.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tastybento
 * @author larryTheCoder
 */
public class ChatHandler implements Listener {

    private final ASkyBlock plugin;
    private final ConcurrentHashMap<Player, String> playerLevels;
    private final ConcurrentHashMap<UUID, String> playerChallengeLevels;
    private final ConcurrentHashMap<Player, Boolean> teamChatUsers;

    public ChatHandler(ASkyBlock plugin) {
        this.plugin = plugin;
        this.playerLevels = new ConcurrentHashMap<>();
        this.teamChatUsers = new ConcurrentHashMap<>();
        this.playerChallengeLevels = new ConcurrentHashMap<>();
        // Add all online player Levels
        plugin.getServer().getOnlinePlayers().values().stream().peek((player) -> playerLevels.put(player, String.valueOf(plugin.getIslandLevel(player)))).forEachOrdered((player) -> playerChallengeLevels.put(player.getUniqueId(), plugin.getChallenges().getChallengeLevel(player)));
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
            Server.getInstance().getScheduler().scheduleTask(plugin, () -> teamChat(event, event.getMessage()));
        }
    }

    private void teamChat(PlayerChatEvent event, String message) {
        Player player = event.getPlayer();
        String playerUUID = player.getName();
        // Is team chat on for this player
        // Find out if this player is in a team (should be if team chat is on)
        // TODO: remove when player resets or leaves team
        CoopData pd = plugin.getTManager().getPlayerCoop(player.getName());
        if (!pd.getMembers().isEmpty()) {
            List<String> teams = pd.getMembers();
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
