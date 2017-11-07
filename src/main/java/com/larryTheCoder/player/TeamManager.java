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
 * @author Adam Matthew
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

    public boolean addTeam(Player leader, Player member) {
        boolean done = false;
        PlayerData te = plugin.getPlayerInfo(leader);
        te.members.add(member.getName());
        for (String members : te.members) {
            if (members.equalsIgnoreCase(member.getName())) {
                done = true;
                break;
            }
        }
        return done;
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
        return pd.inTeam == true;
    }
}
