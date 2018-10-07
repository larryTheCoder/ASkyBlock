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
 *
 * @author larryTheCoder
 */
public abstract class Database {

    protected ArrayList<IslandData> islandCache = new ArrayList<>();
    protected boolean enableFastCache;
    protected IslandData islandSpawn;

    protected Database() {
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
