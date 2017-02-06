/*
 * Copyright (C) 2017 larryTheCoder
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
import java.util.ArrayList;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import java.util.HashMap;

/**
 *
 * @author larryTheCoder
 */
public class PlayerData implements Cloneable {

    public int homes;
    public int resetleft;
    public String playerName;
    private HashMap<String, Boolean> challengeList = new HashMap<>();
    public ArrayList<String> banList = new ArrayList<>();
    // Team Data
    public String teamLeader;
    public String teamIslandLocation;
    public boolean inTeam;
    public ArrayList<String> members = new ArrayList<>();
    public String name;
    public String leader;

    public PlayerData(String playerName, int homes, int resetleft) {
        this.playerName = playerName;
        this.homes = homes;
        this.resetleft = resetleft;
    }

    public PlayerData(String playerName, int homes, ArrayList<String> members, boolean inTeam, String teamleader, String teamIslandloc, int resetleft, ArrayList<String> banList) {
        this.homes = homes;
        this.members = members;
        this.inTeam = inTeam;
        this.teamLeader = teamleader;
        this.teamIslandLocation = teamIslandloc;
        this.resetleft = resetleft;
        this.playerName = playerName;
        this.banList = banList;
    }

    public boolean checkChallenge(String challenge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
