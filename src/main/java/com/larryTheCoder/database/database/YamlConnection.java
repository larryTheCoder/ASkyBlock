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
package com.larryTheCoder.database.database;

import cn.nukkit.level.Position;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;

import java.util.ArrayList;
import java.util.List;

public class YamlConnection extends Database {


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
