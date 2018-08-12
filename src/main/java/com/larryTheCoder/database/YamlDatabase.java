/*
 * Copyright (C) 2016-2018 Adam Matthew
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
package com.larryTheCoder.database;

import cn.nukkit.level.Position;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;

import java.util.ArrayList;
import java.util.List;

public class YamlDatabase extends Database {


    @Override
    public void setSpawnPosition(Position pos) {

    }

    @Override
    public IslandData getIslandLocation(String levelName, int X, int Z) {
        return null;
    }

    @Override
    public ArrayList<IslandData> getIslands(String owner) {
        return null;
    }

    @Override
    public ArrayList<IslandData> getIslands(String owner, String levelName) {
        return null;
    }

    @Override
    public IslandData getIsland(String name, int homes) {
        return null;
    }

    @Override
    public IslandData getIsland(String name, String homeName) {
        return null;
    }

    @Override
    public boolean deleteIsland(IslandData pd) {
        return false;
    }

    @Override
    public IslandData getSpawn() {
        return null;
    }

    @Override
    public IslandData getIslandById(int id) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean createIsland(IslandData pd) {
        return false;
    }

    @Override
    public boolean saveIsland(IslandData pd) {
        return false;
    }

    @Override
    public ArrayList<String> getWorlds() {
        return null;
    }

    @Override
    public boolean saveWorlds(ArrayList<String> pd) {
        return false;
    }

    @Override
    public List<String> getPlayersData() {
        return null;
    }

    @Override
    public PlayerData getPlayerData(String st) {
        return null;
    }

    @Override
    public void createPlayer(String p) {

    }

    @Override
    public void savePlayerData(PlayerData pd) {

    }
}
