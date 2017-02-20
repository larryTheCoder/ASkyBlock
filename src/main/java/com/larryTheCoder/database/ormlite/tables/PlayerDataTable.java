/*
 * Copyright (C) 2017 larryTheHarry
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
package com.larryTheCoder.database.ormlite.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Utils;
import java.util.HashMap;

/**
 *
 * @author larryTheHarry
 */
@DatabaseTable(tableName = "player")
public class PlayerDataTable {

    @DatabaseField(canBeNull = false, columnName = "player")
    String player;
    @DatabaseField(canBeNull = false, columnName = "homes")
    int homes;
    @DatabaseField(canBeNull = false, columnName = "resetleft")
    int resetleft;
    @DatabaseField(canBeNull = true, columnName = "banlist")
    String banList;
    // Team Data
    @DatabaseField(canBeNull = true, columnName = "teamleader")
    String teamLeader;
    @DatabaseField(canBeNull = true, columnName = "teamislandlocation")
    String teamIslandLocation;
    @DatabaseField(canBeNull = true, columnName = "inteam")
    boolean inTeam;
    @DatabaseField(canBeNull = true, columnName = "members")
    String members;
    @DatabaseField(canBeNull = true, columnName = "name")
    String name;
    @DatabaseField(canBeNull = true, columnName = "challengelist")
    String challengeList;
    @DatabaseField(canBeNull = true, columnName = "challengelisttimes")
    String challengelistTimes;
    @DatabaseField(canBeNull = true, columnName = "islandlvl")
    int islandLevel;

    PlayerDataTable() {
    }

    public PlayerDataTable(PlayerData pd) {
        this.player = pd.playerName;
        this.homes = pd.homes;
        this.resetleft = pd.resetleft;
        this.banList = Utils.arrayToString(pd.banList);
        this.teamLeader = pd.teamLeader;
        this.teamIslandLocation = pd.teamIslandLocation;
        this.inTeam = pd.inTeam;
        this.members = Utils.arrayToString(pd.members);
        this.name = pd.name;
    }

    public PlayerData toData() {
        return new PlayerData(player, homes, Utils.stringToArray(members, ", "), (HashMap<String, Boolean>) Utils.stringToMap(challengeList), (HashMap<String, Integer>) Utils.stringToMap(challengelistTimes),islandLevel ,inTeam, teamLeader, teamIslandLocation, resetleft, Utils.stringToArray(banList, ", "));
    }

    public void save(PlayerData pd) {
        this.player = pd.playerName;
        this.homes = pd.homes;
        this.resetleft = pd.resetleft;
        this.banList = Utils.arrayToString(pd.banList);
        this.teamLeader = pd.teamLeader;
        this.teamIslandLocation = pd.teamIslandLocation;
        this.inTeam = pd.inTeam;
        this.members = Utils.arrayToString(pd.members);
        this.name = pd.name;
    }
}
