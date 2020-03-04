/*
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
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.DatabaseManager;
import lombok.Getter;
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

    // FastCache Theory:
    //    This FC, which is an abbreviation for FastCache, is used to fetch
    //    PlayerData and IslandData as fast as it could. In order to achieve this, a hierarchy
    //    of 0-5 is recorded in a table as follows where the data is tabulated after the player left the game:

    //    ID      INFO              TIME                                 DESCRIPTION
    //    0 - Extremely active [Less than 12h]                    This data loss rate is 0%
    //    1 - Casually active  [Average of 12-24h]      Data loss rate for this section is about 15%
    //    2 - Active           [Average of 24-48h]              30% chances of data loss rate
    //    3 - Less active      [Average of 48-96h]              55% chances of data loss rate
    //    4 - Not active       [Average of 96-144h]       70% chances for the data to destruct itself
    //    5 - Rarely active    [More than 144h]          95% overall chances of data to destruct itself

    //    DATA LOSS RATE: 1/10000s * [DATA LOSS RATE PERCENTAGE]

    //    So in order to achieve this, a HashMap consisting of Level Name, Player name, and home UNIQUE were placed.
    //    The HashMap however, consisting of 6 HashMap functions, where those functions are aligned according to the
    //    theory above.

    //    In addition, if the data doesn't exists in the map, we need to fetch it from the database.
    //    But before that happens, the HashMap must store their data during startup. By this way, FC
    //    Can be used right after the server started, without having to query any database during in-game.

    //    Diagram of how this code executed:

    //    [From] --> {Asks for IslandData [Player, Home]}
    //                         (lambda) --> [FastCacheData] --> (Consumer)
    //                               (Verifies data and its variables)
    //                                       (Not in cache) --> [Mysql Fetch] --> Consumer

    // Consumer will always run in MAIN THREAD!

    private final ASkyBlock plugin;

    // Since ArrayList is modifiable, there's no need to replace the cache again into the list.
    private final List<FastCacheData> dataCache = new ArrayList<>();

    public FastCache(ASkyBlock plugin) {
        this.plugin = plugin;

        this.loadFastCache();
    }

    public void loadFastCache() {

    }

    /**
     * Retrieves an information of an island with a given position.
     * This is a thread blocking operation if the information were not found in cache.
     *
     * @param pos The position of an island.
     * @return The desired IslandData object.
     */
    public IslandData getIslandData(Position pos) {
        int id = plugin.getIslandManager().generateIslandKey(pos.getFloorX(), pos.getFloorZ(), pos.getLevel().getName());

        FastCacheData result = dataCache.stream().filter(i -> i.anyIslandMatch(id)).findFirst().orElse(null);
        if (result == null) {
            Connection connection = plugin.getDatabase().getConnection();

            List<IslandData> islandList = parseData(connection.createQuery(FETCH_LEVEL_PLOT.getQuery())
                    .addParameter("pName", id)
                    .addParameter("levelName", pos.getLevel().getName())
                    .executeAndFetchTable().rows(), connection);

            putIslandUnspecified(islandList);

            result = dataCache.stream().filter(i -> i.anyIslandMatch(id)).findFirst().orElse(null);

            return result == null ? null : result.getIslandById(1);
        }

        return result.getIslandById(1);
    }

    /**
     * Retrieves an information of an island with a given position.
     *
     * @param pos          The position of an island.
     * @param resultOutput Result of the search query.
     */
    public void getIslandData(Position pos, Consumer<IslandData> resultOutput) {
        int id = plugin.getIslandManager().generateIslandKey(pos.getFloorX(), pos.getFloorZ(), pos.getLevel().getName());

        FastCacheData result = dataCache.stream().filter(i -> i.anyIslandMatch(id)).findFirst().orElse(null);
        if (result == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                @Override
                public void executeQuery(Connection connection) {
                    List<IslandData> islandList = parseData(connection.createQuery(FETCH_LEVEL_PLOT.getQuery())
                            .addParameter("pName", id)
                            .addParameter("levelName", pos.getLevel().getName())
                            .executeAndFetchTable().rows(), connection);

                    putIslandUnspecified(islandList);
                }

                @Override
                public void onCompletion(Exception err) {
                    FastCacheData result = dataCache.stream().filter(i -> i.anyIslandMatch(id)).findFirst().orElse(null);

                    IslandData pd = result == null ? null : result.getIslandById(1);

                    resultOutput.accept(pd);
                }
            });
            return;
        }

        resultOutput.accept(result.getIslandById(1));
    }

    public IslandData getIslandData(String playerName) {
        return getIslandData(playerName, 1);
    }

    public IslandData getIslandData(String playerName, int homeNum) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(playerName) && i.anyIslandMatch(homeNum)).findFirst().orElse(null);
        if (result == null) {
            Connection connection = plugin.getDatabase().getConnection();

            List<IslandData> islandList = parseData(connection.createQuery(FETCH_ISLAND_PLOT.getQuery())
                    .addParameter("pName", playerName)
                    .executeAndFetchTable().rows(), connection);

            createIntoDb(playerName, islandList);

            return islandList.stream().filter(i -> i.getHomeCountId() == homeNum).findFirst().orElse(null);
        }

        return result.getIslandById(homeNum);
    }

    public void getIslandData(String playerName, Consumer<IslandData> resultOutput) {
        getIslandData(playerName, 1, resultOutput);
    }

    public void getIslandData(String playerName, int homeNum, Consumer<IslandData> resultOutput) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(playerName) && i.anyIslandMatch(homeNum)).findFirst().orElse(null);
        if (result == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                @Override
                public void executeQuery(Connection connection) {
                    List<IslandData> islandList = parseData(connection.createQuery(FETCH_ISLAND_PLOT.getQuery())
                            .addParameter("pName", playerName)
                            .executeAndFetchTable().rows(), connection);

                    createIntoDb(playerName, islandList);
                }
            });
            return;
        }

        resultOutput.accept(result.getIslandById(homeNum));
    }

    public void getPlayerData(Player pl, Consumer<PlayerData> resultOutput) {
        getPlayerData(pl.getName(), resultOutput);
    }

    /**
     * Fetch a player data, this is an asynchronous operation.
     * The result output is being stored as it works as a function for the command.
     *
     * @param player       The target player
     * @param resultOutput The result
     */
    public void getPlayerData(String player, Consumer<PlayerData> resultOutput) {
        FastCacheData result = dataCache.stream().filter(i -> i.anyMatch(player)).findFirst().orElse(null);
        if (result == null) {
            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                private PlayerData playerData = null;

                @Override
                public void executeQuery(Connection connection) {
                    Table data = connection.createQuery(FETCH_PLAYER_MAIN.getQuery())
                            .addParameter("plotOwner", player)
                            .executeAndFetchTable();

                    if (data.rows().isEmpty()) {
                        resultOutput.accept(null);
                        return;
                    }

                    PlayerData pd = PlayerData.fromRows(data.rows().get(0));
                    pd.fetchChallengeBody(); // TODO: Remove this after asynchronous target achieved.

                    playerData = pd;
                }

                @Override
                public void onCompletion(Exception err) {
                    resultOutput.accept(playerData);
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
        final String[] playerNames = new String[32]; // (n + 1) for a length
        final FastCacheData[] cacheObject = new FastCacheData[32];

        for (IslandData pd : data) {
            int keyVal = 0;

            if (playerNames[0].isEmpty()) {
                playerNames[0] = pd.getPlotOwner();
                cacheObject[0] = new FastCacheData(pd.getPlotOwner());
            } else {
                while (playerNames[keyVal].equalsIgnoreCase(pd.getPlotOwner()) || playerNames[keyVal].isEmpty()) {
                    if (keyVal++ > 30) break;
                }

                // If the array of this key is empty, then there is no player in this value.
                if (playerNames[keyVal].isEmpty()) {
                    playerNames[keyVal] = pd.getPlotOwner();
                    cacheObject[keyVal] = new FastCacheData(pd.getPlotOwner());
                }
            }

            cacheObject[keyVal].addIslandData(pd);
        }

        dataCache.addAll(Arrays.asList(cacheObject));
    }

    public void addIslandIntoDb(String playerName, IslandData newData) {
        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(playerName)).findFirst().orElse(null);
        if (object == null) {
            object = new FastCacheData(playerName);
            object.addIslandData(newData);

            dataCache.add(object);
            return;
        }

        object.addIslandData(newData);
        object.updateTime();
    }

    /**
     * Stores a data of this player into a database.
     * This field REPLACES this data into the cache.
     *
     * @param playerName The name of the player, or XUID if preferred.
     * @param data       List of IslandData for this player
     */
    public void createIntoDb(String playerName, List<IslandData> data) {
        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(playerName)).findFirst().orElse(null);
        if (object == null) {
            object = new FastCacheData(playerName);
            object.setIslandData(data);

            dataCache.add(object);
            return;
        }

        object.setIslandData(data);
        object.updateTime();
    }

    /**
     * Stores a data of this player into a database.
     * This field REPLACES this data into the cache.
     *
     * @param playerName The name of the player, or XUID if preferred.
     * @param data       PlayerData for this player
     */
    public void createIntoDb(String playerName, PlayerData data) {
        FastCacheData object = dataCache.stream().filter(i -> i.anyMatch(playerName)).findFirst().orElse(null);
        if (object == null) {
            object = new FastCacheData(playerName);
            object.setPlayerData(data);

            dataCache.add(object);
            return;
        }

        // Since ArrayList is modifiable, there is no need to replace the cache again into the list.
        object.setPlayerData(data);
        object.updateTime();
    }

    private List<IslandData> parseData(List<Row> data, Connection connection) {
        List<IslandData> islandList = new ArrayList<>();

        for (Row o : data) {
            List<Row> row = connection.createQuery(FETCH_ISLAND_DATA.getQuery())
                    .addParameter("islandUniquePlotId", o.getInteger("islandUniqueId"))
                    .executeAndFetchTable().rows();

            if (row.isEmpty()) {
                islandList.add(IslandData.fromRows(o));
                continue;
            }

            islandList.add(IslandData.fromRows(o, row.get(0)));
        }

        return islandList;
    }

    public static class FastCacheData {

        private Timestamp lastUpdatedQuery;

        private String ownedBy = "";

        @Getter
        private Map<Integer, IslandData> islandData = new HashMap<>();
        @Getter
        private PlayerData playerData = null;

        FastCacheData(String playerName) {
            this.ownedBy = playerName;
        }

        void setIslandData(List<IslandData> dataList) {
            Map<Integer, IslandData> islandData = new HashMap<>();
            dataList.forEach(i -> islandData.put(i.getIslandUniquePlotId(), i));

            this.islandData = islandData;
        }

        public void addIslandData(IslandData newData) {
            islandData.put(newData.getIslandUniquePlotId(), newData);
        }

        void setPlayerData(PlayerData playerData) {
            this.playerData = playerData;
        }

        boolean anyMatch(String pl) {
            return playerData == null ? ownedBy.equalsIgnoreCase(pl) : playerData.getPlayerName().equalsIgnoreCase(pl);
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

        void updateTime() {
            lastUpdatedQuery = Timestamp.from(Instant.now());
        }
    }
}
