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
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class to open a connection
 * database or resources
 * <p>
 * This class only stores island cache
 * and island spawn data
 */
public abstract class Database {

    ArrayList<IslandData> islandCache = new ArrayList<>();
    boolean enableFastCache;
    IslandData islandSpawn;

    Database() {
        this.enableFastCache = ASkyBlock.get().getConfig().getBoolean("fastCache");
    }

    /**
     * Release the array from system
     * Keep the server from lag
     */
    public void free() {
        islandCache.clear();
    }

    public void removeIslandFromCache(IslandData pd) {
        if (enableFastCache) {
            for (IslandData pde : islandCache) {
                if (pde.getIslandId() == pd.getIslandId()) {
                    islandCache.remove(pde);
                    break;
                }
            }
        }
    }

    public abstract void setSpawnPosition(Position pos);

    public abstract IslandData getIslandLocation(String levelName, int X, int Z);

    public abstract ArrayList<IslandData> getIslands(String owner);

    public abstract ArrayList<IslandData> getIslands(String owner, String levelName);

    public abstract IslandData getIsland(String name, int homes);

    public abstract IslandData getIsland(String name, String homeName);

    public abstract boolean deleteIsland(IslandData pd);

    public abstract IslandData getSpawn();

    public abstract IslandData getIslandById(int id);

    public abstract void close();

    public abstract boolean createIsland(IslandData pd);

    public abstract boolean saveIsland(IslandData pd);

    public abstract ArrayList<String> getWorlds();

    public abstract boolean saveWorlds(ArrayList<String> pd);

    public abstract List<String> getPlayersData();

    public abstract PlayerData getPlayerData(String st);

    public abstract void createPlayer(String p);

    public abstract void savePlayerData(PlayerData pd);
}
