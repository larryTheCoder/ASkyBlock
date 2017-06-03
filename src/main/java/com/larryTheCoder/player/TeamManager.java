/*
 * Copyright (C) 2016 larryTheHarry 
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
import java.util.ArrayList;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;

/**
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
     * @return {@code true} if {@link #getIsland} not null
     */
    public boolean hasTeam(Player p) {
        return plugin.getDatabase().getPlayerData(p).inTeam;
    }

    public boolean addTeam(Player leader, Player member) {
        boolean done = false;
        PlayerData te = plugin.getDatabase().getPlayerData(leader);
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
        String kickMessage = TextFormat.RED + "You has been kicked from your team";
        boolean done = false;
        if (!message.isEmpty()) {
            kickMessage = message;
        }
        PlayerData te = plugin.getDatabase().getPlayerData(leader);
        if (!te.members.contains(member.getName())) {
            leader.sendMessage(plugin.getPrefix() + plugin.getMsg(leader).errorOfflinePlayer);
            return true;
        }
        te.members.remove(member.getName());
        if (member.isOnline()) {
            member.sendMessage(plugin.getPrefix() + kickMessage);
        }
        //todo: Store kick message if player doesnt exsits
        //todo: Kick the player if the player in player team area level
        return done;
    }

    public ArrayList<String> getPlayerMembers(Player p) {
        PlayerData pd = plugin.getDatabase().getPlayerData(p);
        if (pd.members != null && !pd.members.isEmpty()) {
            return pd.members;
        }
        return null;
    }

    private boolean kick(Player p, PlayerData td) {
        if (p.getLevel().getName().equalsIgnoreCase("SkyBlock")) {
            String st = td.leader;
            IslandData pd = plugin.getDatabase().getIsland(p.getName(), 1);
            IslandData p1 = plugin.getDatabase().getIsland(st, 1);
            if (plugin.getIsland().generateIslandKey(p.getLocation()) == p1.islandId) {
                //kick the player
                p.teleport(new Location());
            }
        }
        return true;
    }
}
