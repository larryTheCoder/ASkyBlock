/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
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

package com.larryTheCoder.cache;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import com.google.common.base.Preconditions;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.DatabaseManager;
import com.larryTheCoder.database.TableSet;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;
import lombok.NonNull;
import org.sql2o.Connection;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static com.larryTheCoder.database.TableSet.*;

/**
 * Caches information for an object.
 */
public class FastCache {

    private final ASkyBlock plugin;

    // Since ArrayList is modifiable, there's no need to replace the cache again into the list.
    private final List<FastCacheData> dataCache = new ArrayList<>();
    private final List<String> uniqueId = new ArrayList<>();

    public FastCache(ASkyBlock plugin) {
        this.plugin = plugin;

        this.loadFastCache();
    }

    private void addAllCacheData(List<FastCacheData> list) {
        dataCache.addAll(list);
    }

    private void loadFastCache() {
        plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
            private final List<FastCacheData> dataCache = new ArrayList<>();

            @Override
            public void executeQuery(Connection connection) {
                // If the table FETCH ALL players are empty, it means that the server doesn't
                // have any players played in this game "yet"
                Table stmt = connection.createQuery(FETCH_ALL_PLAYERS.getQuery()).executeAndFetchTable();
                if (stmt.rows().size() == 0) return;

                for (Row plData : stmt.rows()) {
                    // Just an oopsies.
                    try {
                        Date date = Date.from(Timestamp.valueOf(plData.getString("lastLogin")).toInstant());
                        if (date.compareTo(Date.from(Instant.now())) >= Settings.loadCacheBefore) continue;
                    } catch (Throwable ignored) {
                    }

                    String userName = plData.getString("playerName");

                    FastCacheData data = new FastCacheData(userName);
                    Row clData;

                    // Fetch user info
                    Table table = connection.createQuery(FETCH_PLAYER_DATA.getQuery())
                            .addParameter("playerName", userName)
                            .executeAndFetchTable();

                    if (!table.rows().isEmpty()) {
                        clData = table.rows().get(0);
                    } else {
                        clData = null;
                    }

                    data.setPlayerData(PlayerData.fromRows(plData, clData));

                    // Then we fetch IslandData.
                    table = connection.createQuery(FETCH_ISLANDS_PLOT.getQuery())
                            .addParameter("pName", userName)
                            .executeAndFetchTable();

                    for (Row islandRow : table.rows()) {
                        int islandId = islandRow.getInteger("islandUniqueId");

                        table = connection.createQuery(FETCH_ISLAND_DATA.getQuery())
                                .addParameter("islandUniquePlotId", islandId)
                                .executeAndFetchTable();

                        if (table.rows().isEmpty()) {
                            data.addIslandData(IslandData.fromRows(islandRow));
                            continue;
                        }

                        Row dataRow = table.rows().get(0);
                        data.addIslandData(IslandData.fromRows(islandRow, dataRow));
                    }

                    dataCache.add(data);
                }

                Table table = connection.createQuery(TableSet.FETCH_ALL_ISLAND_UNIQUE.getQuery()).executeAndFetchTable();

                for (Row rows : table.rows()) uniqueId.add(rows.getString("islandUniqueId"));
            }

            @Override
            public void onCompletion(Exception err) {
                if (err != null) {
                    return;
                }
                Utils.sendDebug(String.format("Loaded %s cache data.", dataCache.size()));

                ASkyBlock.get().getFastCache().addAllCacheData(dataCache);
            }
        });
    }

    public boolean containsId(String value) {
        return uniqueId.contains(value);
    }

    /**
     * Gets the island relations data for this position,
     * If there is not relationship with this island, it will return null.
     *
     * @param pos The position of the level.
     * @return The coop data that are related to this island.
     */
    public CoopData getRelations(Position pos) {
        int id = plugin.getIslandManager().generateIslandKey(pos.getFloorX(), pos.getFloorZ(), pos.getLevel().getName());

        FastCacheData data = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);
        if (data == null) {
            // Check again this island, the cache might not have yet loaded.
            if (getIslandData(pos) != null) {
                data = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);
                if (data != null) return data.getIslandByUId(id).getCoopData();
            }

            return null;
        }

        return data.getIslandByUId(id).getCoopData();
    }

    /**
     * Retrieves an information of an island with a given position.
     * This is a thread blocking operation if the information were not found in cache.
     */
    public IslandData getIslandData(Position pos) {
        int id = plugin.getIslandManager().generateIslandKey(pos.getFloorX(), pos.getFloorZ(), pos.getLevel().getName());

        FastCacheData result = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);
        if (result == null) {
            Connection connection = plugin.getDatabase().getConnection();

            List<IslandData> islandList = parseData(connection.createQuery(FETCH_LEVEL_PLOT.getQuery())
                    .addParameter("islandId", id)
                    .executeAndFetchTable().rows(), connection);

            putIslandUnspecified(islandList);

            result = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);

            return result == null ? null : result.getIslandByUId(id);
        }

        return result.getIslandByUId(id);
    }

    public IslandData getIslandData(String playerName) {
        return getIslandData(playerName, 1);
    }

    public IslandData getIslandData(String playerName, int homeNum) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(playerName) && i.anyIslandMatch(homeNum)).findFirst().orElse(null);
        if (result == null) {
            Connection connection = plugin.getDatabase().getConnection();

            List<Row> data = connection.createQuery(FETCH_ISLAND_PLOT.getQuery())
                    .addParameter("pName", playerName)
                    .addParameter("islandId", homeNum)
                    .executeAndFetchTable().rows();

            if (data == null || data.isEmpty()) {
                return null;
            }

            List<IslandData> islandList = parseData(data, connection);

            saveIntoDb(playerName, islandList);

            return islandList.stream().filter(i -> i.getHomeCountId() == homeNum).findFirst().orElse(null);
        }

        return result.getIslandById(homeNum);
    }

    /**
     * Retrieves all islands from this player.
     * This method is a thread blocking operation if the given name is not found in the cache.
     */
    @NonNull
    public List<IslandData> getIslandsFrom(String plName) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(plName)).findFirst().orElse(null);
        if (result == null) {
            Connection connection = plugin.getDatabase().getConnection();

            List<IslandData> islandList = parseData(connection.createQuery(FETCH_ISLAND_PLOTS.getQuery())
                    .addParameter("pName", plName)
                    .executeAndFetchTable().rows(), connection);

            putIslandUnspecified(islandList);

            return islandList;
        }

        return new ArrayList<>(result.islandData.values());
    }

    /**
     * Gets the island relations data for this position,
     * If there is not relationship with this island, it will return null.
     *
     * @param pos          The position of the level.
     * @param resultOutput The coop data that are related to this island.
     */
    public void getRelations(Position pos, Consumer<CoopData> resultOutput) {
        int id = plugin.getIslandManager().generateIslandKey(pos.getFloorX(), pos.getFloorZ(), pos.getLevel().getName());

        FastCacheData data = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);
        if (data == null) {
            // Check again this island, the cache might not have yet loaded.
            getIslandData(pos, pd -> {
                if (pd != null) {
                    FastCacheData rData = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);
                    if (rData != null) {
                        resultOutput.accept(rData.getIslandByUId(id).getCoopData());
                        return;
                    }
                }

                resultOutput.accept(null);
            });

            return;
        }

        resultOutput.accept(data.getIslandByUId(id).getCoopData());
    }

    /**
     * Gets the island relations data for a player,
     * If there is not relationship with this player, it will return null.
     *
     * @param plName       The name of the player itself.
     * @param resultOutput The coop data that are related to this island.
     */
    public void getRelations(String plName, Consumer<CoopData> resultOutput) {
        FastCacheData data = dataCache.stream().filter(i -> i.getIslandData().values().stream().anyMatch(v -> ((v.getCoopData() != null) && v.getCoopData().isMember(plName)))).findFirst().orElse(null);

        if (data == null) {
            // Check again this island, the cache might not have yet loaded.
            getIslandData(plName, pd -> {
                if (pd != null) {
                    FastCacheData rData = dataCache.stream().filter(i -> i.getIslandData().values().stream().anyMatch(v -> ((v.getCoopData() != null) && v.getCoopData().isMember(plName)))).findFirst().orElse(null);
                    if (rData != null) {
                        IslandData iPlData = rData.getIslandData().values().stream().filter(v -> ((v.getCoopData() != null) && v.getCoopData().isMember(plName))).findFirst().orElse(null);
                        if (iPlData == null) {
                            resultOutput.accept(null);
                            return;
                        }

                        resultOutput.accept(iPlData.getCoopData());
                        return;
                    }
                }

                resultOutput.accept(null);
            });

            return;
        }

        IslandData iPlData = data.getIslandData().values().stream().filter(v -> ((v.getCoopData() != null) && v.getCoopData().isMember(plName))).findFirst().orElse(null);
        if (iPlData == null) {
            resultOutput.accept(null);
            return;
        }

        resultOutput.accept(iPlData.getCoopData());
    }

    /**
     * Retrieves an information of an island with a given position.
     */
    public void getIslandData(Position pos, Consumer<IslandData> resultOutput) {
        int id = plugin.getIslandManager().generateIslandKey(pos.getFloorX(), pos.getFloorZ(), pos.getLevel().getName());
        getIslandData(id, resultOutput);
    }

    public void getIslandData(int id, Consumer<IslandData> resultOutput) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);
        if (result == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                List<IslandData> islandList = null;

                @Override
                public void executeQuery(Connection connection) {
                    islandList = parseData(connection.createQuery(FETCH_LEVEL_PLOT.getQuery())
                            .addParameter("islandId", id)
                            .executeAndFetchTable().rows(), connection);
                }

                @Override
                public void onCompletion(Exception err) {
                    if (err != null) {
                        resultOutput.accept(null);
                        return;
                    }
                    putIslandUnspecified(islandList);

                    FastCacheData result = dataCache.stream().filter(i -> i.anyIslandUidMatch(id)).findFirst().orElse(null);

                    IslandData pd = result == null ? null : result.getIslandById(id);
                    resultOutput.accept(pd);
                }
            });
            return;
        }

        resultOutput.accept(result.getIslandByUId(id));
    }

    public void getIslandData(String playerName, Consumer<IslandData> resultOutput) {
        getIslandData(playerName, 1, resultOutput);
    }

    public void getIslandData(String playerName, int homeNum, Consumer<IslandData> resultOutput) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(playerName) && i.anyIslandMatch(homeNum)).findFirst().orElse(null);
        if (result == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {

                private List<IslandData> islandList;

                @Override
                public void executeQuery(Connection connection) {
                    islandList = parseData(connection.createQuery(FETCH_ISLAND_PLOT.getQuery())
                            .addParameter("pName", playerName)
                            .addParameter("islandId", homeNum)
                            .executeAndFetchTable().rows(), connection);
                }

                @Override
                public void onCompletion(Exception exception) {
                    if (exception != null) {
                        resultOutput.accept(null);

                        exception.printStackTrace();
                        return;
                    }
                    saveIntoDb(playerName, islandList);

                    FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(playerName) && i.anyIslandMatch(homeNum)).findFirst().orElse(null);
                    resultOutput.accept(result != null ? result.getIslandById(homeNum) : null);
                }
            });
            return;
        }

        resultOutput.accept(result.getIslandById(homeNum));
    }

    /**
     * Retrieves all islands from this player
     * This method is not a thread-blocking operation.
     */
    public void getIslandsFrom(String plName, Consumer<List<IslandData>> resultOutput) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(plName)).findFirst().orElse(null);
        if (result == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                List<IslandData> islandList = null;

                @Override
                public void executeQuery(Connection connection) {
                    islandList = parseData(connection.createQuery(FETCH_ISLAND_PLOTS.getQuery())
                            .addParameter("pName", plName)
                            .executeAndFetchTable().rows(), connection);
                }

                @Override
                public void onCompletion(Exception exception) {
                    if (exception != null) {
                        resultOutput.accept(null);

                        exception.printStackTrace();
                        return;
                    }
                    putIslandUnspecified(islandList);

                    resultOutput.accept(islandList);
                }
            });

            return;
        }

        resultOutput.accept(new ArrayList<>(result.islandData.values()));
    }

    /**
     * Fetch a player data, this is an asynchronous operation.
     * The result output is being stored as it works as a function for the command.
     *
     * @param pl           The target player class
     * @param resultOutput The result
     */
    public void getPlayerData(Player pl, Consumer<PlayerData> resultOutput) {
        getPlayerData(pl.getName(), resultOutput);
    }

    /**
     * Fetch a player data, this is an asynchronous operation.
     * The result output is being stored as it works as a function for the command.
     *
     * @param player       The target player name
     * @param resultOutput The result
     */
    public void getPlayerData(String player, Consumer<PlayerData> resultOutput) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(player)).findFirst().orElse(null);
        if (result == null || result.getPlayerData() == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                private PlayerData playerData = null;

                @Override
                public void executeQuery(Connection connection) {
                    Row plData;
                    Row clData;

                    Table data = connection.createQuery(FETCH_PLAYER_MAIN.getQuery())
                            .addParameter("plotOwner", player)
                            .executeAndFetchTable();

                    if (data.rows().isEmpty()) {
                        return;
                    }

                    plData = data.rows().get(0);

                    data = connection.createQuery(FETCH_PLAYER_DATA.getQuery())
                            .addParameter("playerName", player)
                            .executeAndFetchTable();

                    if (!data.rows().isEmpty()) {
                        clData = data.rows().get(0);
                    } else {
                        clData = null;
                    }

                    playerData = PlayerData.fromRows(plData, clData);
                }

                @Override
                public void onCompletion(Exception err) {
                    if (err != null) {
                        resultOutput.accept(playerData);

                        return;
                    }

                    resultOutput.accept(playerData);
                    saveIntoDb(playerData);
                }
            });

            return;
        }

        resultOutput.accept(result.getPlayerData());
    }

    /**
     * Inserts an island data arrays into the cache.
     * This operation queries all island list given in a variable and create a mutable
     * array which each data will be stored in there.
     * <p>
     * This method <bold>REMOVES</bold> all island data in the cache and <bold>REPLACES</bold> old
     * data with the given array. This method is also be used during startup.
     *
     * @param data The entire list of an island data.
     */
    private void putIslandUnspecified(List<IslandData> data) {
        final String[] playerNames = new String[data.size()]; // (n + 1) for a length
        final FastCacheData[] cacheObject = new FastCacheData[data.size()];

        for (IslandData pd : data) {
            int keyVal = 0;

            if (playerNames[0] == null || playerNames[0].isEmpty()) {
                playerNames[0] = pd.getPlotOwner();
                cacheObject[0] = new FastCacheData(pd.getPlotOwner());
            } else {
                // If the array of this key is null, then there is no player in this value.
                while (playerNames[keyVal] != null) {
                    keyVal++;
                }

                playerNames[keyVal] = pd.getPlotOwner();
                cacheObject[keyVal] = new FastCacheData(pd.getPlotOwner());
            }

            cacheObject[keyVal].addIslandData(pd);
        }

        dataCache.addAll(Arrays.asList(cacheObject));
    }

    /**
     * Adds an island data into this player cache data.
     * This method is used internally. Using this function may lead to severe critical errors.
     */
    public void addIslandIntoDb(String playerName, IslandData newData) {
        uniqueId.add(Integer.toString(newData.getIslandUniquePlotId()));

        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(playerName)).findFirst().orElse(null);
        if (object == null) {
            object = new FastCacheData(playerName);
            object.addIslandData(newData);

            dataCache.add(object);
            return;
        }

        object.addIslandData(newData);
    }

    /**
     * Deletes an island from the data given.
     */
    public void deleteIsland(IslandData islandData) {
        FastCacheData object = dataCache.stream().filter(i -> i.anyIslandUidMatch(islandData.getIslandUniquePlotId())).findFirst().orElse(null);
        if (object == null) {
            Utils.sendDebug("Attempting to delete an unknown player island");
            return;
        }

        plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {

            @Override
            public void executeQuery(Connection connection) {
                Utils.sendDebug("Deleting attempt");

                connection.createQuery(DELETE_ISLAND_DATA.getQuery())
                        .addParameter("islandUniqueId", islandData.getIslandUniquePlotId())
                        .executeUpdate();

                Utils.sendDebug("Executed delete #1");

                connection.createQuery(DELETE_ISLAND_MAIN.getQuery())
                        .addParameter("islandUniqueId", islandData.getIslandUniquePlotId())
                        .executeUpdate();

                Utils.sendDebug("Executed delete #2");
            }

            @Override
            public void onCompletion(Exception err) {
                object.removeIsland(islandData);
            }
        });
    }

    /**
     * Stores a data of this player into a database.
     * This field REPLACES this data into the cache.
     *
     * @param playerName The name of the player, or XUID if preferred.
     * @param data       List of IslandData for this player
     */
    public void saveIntoDb(String playerName, List<IslandData> data) {
        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(playerName)).findFirst().orElse(null);
        if (object == null) {
            object = new FastCacheData(playerName);
            object.setIslandData(data);

            dataCache.add(object);
            return;
        }

        object.setIslandData(data);
    }

    /**
     * Stores a data of this player into a database.
     * This field REPLACES this data into the cache.
     *
     * @param data PlayerData for this player
     */
    public void saveIntoDb(PlayerData data) {
        if (data == null) return;

        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(data.getPlayerName())).findFirst().orElse(null);
        if (object == null) {
            object = new FastCacheData(data.getPlayerName());
            object.setPlayerData(data);

            dataCache.add(object);
            return;
        }

        // Since ArrayList is modifiable, there is no need to replace the cache again into the list.
        object.setPlayerData(data);
    }

    private List<IslandData> parseData(List<Row> data, Connection connection) {
        List<IslandData> islandList = new ArrayList<>();

        for (Row o : data) {
            //relationData
            List<Row> sharedRows = connection.createQuery(FETCH_ISLAND_DATA.getQuery())
                    .addParameter("islandUniquePlotId", o.getInteger("islandUniqueId"))
                    .executeAndFetchTable().rows();

            IslandData pd;
            if (sharedRows.isEmpty()) {
                pd = IslandData.fromRows(o);
            } else {
                pd = IslandData.fromRows(o, sharedRows.get(0));
            }

            sharedRows = connection.createQuery(FETCH_RELATION_DATA.getQuery())
                    .addParameter("islandId", o.getInteger("islandUniqueId"))
                    .executeAndFetchTable().rows();

            if (!sharedRows.isEmpty()) {
                pd.loadRelationData(sharedRows.get(0));
            }

            islandList.add(pd);
        }

        return islandList;
    }

    /**
     * A method to retrieve default locale of a player.
     * This method is considered thread safe, while a runnable object will executed when
     * this player data were not found in cache.
     *
     * @param plName The player name
     * @return Default locale of the player.
     */
    public String getDefaultLocale(String plName) {
        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(plName)).findFirst().orElse(null);
        if (object == null || object.getPlayerData() == null) {
            // Forces the query to fetch all available information about this player.
            getPlayerData(plName, o -> {
            });

            return Settings.defaultLanguage;
        }

        return object.getPlayerData().getLocale();
    }

    public void clearSavedCaches() {
        dataCache.clear();
    }

    public void dumpAllCaches() {
        Utils.send(dataCache.toString());
    }

    private static class FastCacheData {

        @Getter
        private Map<Integer, IslandData> islandData = new HashMap<>();
        @Getter
        private PlayerData playerData = null;

        private final String ownedBy;

        FastCacheData(String playerName) {
            this.ownedBy = playerName;
        }

        void setIslandData(List<IslandData> dataList) {
            Map<Integer, IslandData> islandData = new HashMap<>();
            dataList.forEach(i -> islandData.put(i.getIslandUniquePlotId(), i));

            this.islandData = islandData;
        }

        public void addIslandData(IslandData newData) {
            Preconditions.checkState(!islandData.containsKey(newData.getIslandUniquePlotId()), "IslandData already exists in this cache.");

            islandData.put(newData.getIslandUniquePlotId(), newData);
        }

        void setPlayerData(PlayerData playerData) {
            Preconditions.checkState(playerData != null, "PlayerData cannot be null");

            this.playerData = playerData;
        }

        public void removeIsland(IslandData pd) {
            islandData.remove(pd.getIslandUniquePlotId());
        }

        boolean anyMatch(String pl) {
            return playerData == null ? ownedBy.equalsIgnoreCase(pl) : playerData.getPlayerName().equalsIgnoreCase(pl);
        }

        boolean anyIslandUidMatch(int islandUId) {
            return islandData.values().stream().anyMatch(o -> o.getIslandUniquePlotId() == islandUId);
        }

        boolean anyIslandMatch(int islandId) {
            return islandData.values().stream().anyMatch(o -> o.getHomeCountId() == islandId);
        }

        IslandData getIslandByUId(int homeUIdKey) {
            return islandData.values().stream().filter(i -> i.getIslandUniquePlotId() == homeUIdKey).findFirst().orElse(null);
        }

        IslandData getIslandById(int homeId) {
            return islandData.values().stream().filter(i -> i.getHomeCountId() == homeId).findFirst().orElse(null);
        }

        @Override
        public String toString() {
            return "FastCacheData{" +
                    "islandData=" + islandData +
                    ", playerData=" + playerData +
                    ", ownedBy='" + ownedBy + '\'' +
                    '}';
        }
    }
}
