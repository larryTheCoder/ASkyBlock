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
package com.larryTheCoder.player;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;

import java.util.ArrayList;

/**
 * This class is to handle Player's teammate
 *
 * @author larryTheCoder
 */
public class TeamManager {

    private final ASkyBlock plugin;

    public TeamManager(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Define the player has a team
     *
     * @param p The current player
     * @return {@code true} if not null
     */
    public boolean hasTeam(Player p) {
        return plugin.getPlayerInfo(p).inTeam;
    }

    public void addTeam(Player leader, Player member) {
        boolean done = false;
        PlayerData te = plugin.getPlayerInfo(leader);
        te.members.add(member.getName());
        for (String members : te.members) {
            if (members.equalsIgnoreCase(member.getName())) {
                done = true;
                break;
            }
        }
    }

    public boolean kickTeam(Player leader, Player member, String message) {
        String kickMessage = TextFormat.RED + plugin.getLocale(member).kickedFromTeam.replace("[name]", leader.getName());
        boolean done = false;
        if (!message.isEmpty()) {
            kickMessage = message;
        }
        PlayerData te = plugin.getPlayerInfo(leader);
        if (!te.members.contains(member.getName())) {
            leader.sendMessage(plugin.getPrefix() + plugin.getLocale(leader).errorOfflinePlayer);
            return true;
        }
        te.members.remove(member.getName());
        if (member.isOnline()) {
            member.sendMessage(plugin.getPrefix() + kickMessage);
        } else {
            plugin.getMessages().setMessage(member.getName(), message);
        }
        //todo: Store kick message if player doesnt exsits
        //todo: Kick the player if the player in owner's island
        return done;
    }

    public ArrayList<String> getPlayerMembers(String p) {
        PlayerData pd = plugin.getDatabase().getPlayerData(p);
        if (pd.members != null && !pd.members.isEmpty()) {
            return pd.members;
        }
        return null;
    }

    private boolean kick(Player p, PlayerData td) {
        if (plugin.level.contains(p.getLevel().getName())) {
            String st = td.leader;
            IslandData pd = plugin.getDatabase().getIsland(p.getName(), 1);
            IslandData p1 = plugin.getDatabase().getIsland(st, 1);
            if (plugin.getIsland().generateIslandKey(p.getLocation()) == p1.getIslandId()) {
                //kick the player
                p.teleport(new Location());
            }
        }
        return true;
    }

    public String getLeader(String p) {
        PlayerData pd = plugin.getDatabase().getPlayerData(p);
        return pd.leader;
    }

    public boolean inTeam(String team) {
        PlayerData pd = plugin.getDatabase().getPlayerData(team);
        return pd.inTeam;
    }
}
