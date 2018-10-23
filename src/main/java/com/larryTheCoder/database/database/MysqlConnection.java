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
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.database.JDBCUtilities;
import com.larryTheCoder.database.config.MySQLConfig;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.EXECUTE_FAILED;

/**
 * This class is used for Mysql database
 * which is currently under maintain and refurbish.
 * Also syncs the connection with the server, maintaining
 * the ping and connectivity so it won't lost its connection
 *
 * @author larryTheCoder
 */
public class MysqlConnection extends Database {

    private final ASkyBlock plugin;
    private final MySQLConfig database;
    private final String prefix;
    private Connection connection;
    private boolean closed;
    private boolean connected;

    /**
     * Constructor that provides a mysql connection and database management
     * that will always sync the connection trough the mysql-server and the
     * server itself providing best latency and low data corruption
     *
     * @param plugin   The plugin itself
     * @param database Database configuration
     * @param prefix   The prefix of this database, used in multi server
     * @throws SQLException           SQL Exception will be thrown when there is an error while creating database
     *                                for the first time.
     * @throws ClassNotFoundException The class of the JDBC not found to complete the operation, check your
     *                                plugins and make sure JDBC or DbLib is installed.
     */
    public MysqlConnection(ASkyBlock plugin, MySQLConfig database, String prefix) throws SQLException, ClassNotFoundException {
        this.plugin = plugin;
        this.database = database;
        this.prefix = prefix;
        this.connection = database.openConnection();
        this.closed = this.connection == null;
        this.connected = !this.closed;
        createTables();
        syncWithServer();
    }

    private void createTables() throws SQLException {
        // Beep Boop, Error 404
        if (closed) {
            return;
        }
        // Verify tables integrity
        String[] tables = new String[]{"_island", "_worlds", "_players"};
        DatabaseMetaData meta = connection.getMetaData();
        int create = 0;
        for (String s : tables) {
            try (ResultSet set = meta.getTables(null, null, prefix + s, new String[]{"TABLE"})) {
                if (!set.next()) {
                    create++;
                }
            }
        }
        if (create == 0) {
            return;
        }

        // Prefix allowing server to choose either this database in multichannel
        // Connections. There is still some data to be added
        try (Statement set = connection.createStatement()) {
            // Final database
            set.addBatch("CREATE TABLE IF NOT EXISTS `" + prefix + "_island` ("
                    + "`id` INTEGER NOT NULL AUTO_INCREMENT,"
                    + "`islandId` INTEGER NOT NULL,"
                    + "`x` INTEGER NOT NULL,"
                    + "`y` INTEGER NOT NULL,"
                    + "`z` INTEGER NOT NULL,"
                    + "`spawnX` INTEGER,"
                    + "`spawnY` INTEGER,"
                    + "`spawnZ` INTEGER,"
                    + "`isSpawn` BOOLEAN NOT NULL,"
                    + "`psize` INTEGER NOT NULL,"
                    + "`playerName` VARCHAR(64) NOT NULL,"
                    + "`islandName` VARCHAR(64) NOT NULL,"
                    + "`level` VARCHAR(1024) NOT NULL,"
                    + "`protection` VARCHAR(1024) NOT NULL,"
                    + "`biome` VARCHAR(64) NOT NULL,"
                    + "`locked` INTEGER NOT NULL,"
                    + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "  PRIMARY KEY (`id`, `playerName`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1");
            set.addBatch("CREATE TABLE IF NOT EXISTS `" + prefix + "_worlds` ("
                    + "`world` VARCHAR(4096)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            set.addBatch("CREATE TABLE IF NOT EXISTS `" + prefix + "_players` ("
                    + "`id` INTEGER NOT NULL AUTO_INCREMENT,"
                    + "`homes` INTEGER NOT NULL,"
                    + "`resetleft` INTEGER NOT NULL,"
                    + "`islandlvl` INTEGER,"
                    + "`player` VARCHAR(64) NOT NULL,"
                    + "`locale` VARCHAR(64) NOT NULL,"
                    + "`banlist` VARCHAR(1024),"
                    + "`teamleader` VARCHAR(1024),"
                    + "`deaths` INTEGER DEFAULT 0,"
                    + "`teamislandlocation` VARCHAR(1024),"
                    + "`members` VARCHAR(8192),"
                    + "`challengelist` VARCHAR(8192),"
                    + "`challengelisttimes` VARCHAR(8192),"
                    + "`teamName` VARCHAR(128),"
                    + "`inteam` BOOL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1");
            set.executeBatch();
            set.clearBatch();

            Utils.sendDebug("&eSuccessfully created database tables");
        } catch (BatchUpdateException ex) {
            JDBCUtilities.printBatchUpdateException(ex);
        }
    }

    private void syncWithServer() {
        TaskManager.runTaskRepeatAsync(() -> {
            if (!closed && !isValid()) {
                connected = false;
                reconnect();
            } else {
                connected = true;
            }

        }, 60);
    }

    private boolean isValid() {
        try {
            connection.prepareStatement("SELECT 1 FROM `" + prefix + "_island`").executeQuery();
            return true;
        } catch (SQLException e) {
            Utils.send("&eLosing connection heartbeat with MySQL server...");
        }
        return false;
    }

    private void reconnect() {
        try {
            connection.close();
            database.closeConnection();
            connection = database.forceConnection();
        } catch (SQLException | ClassNotFoundException e) {
            Utils.send("&eReconnection failed... Retrying");
        }
    }

    @Override
    public void setSpawnPosition(Position pos) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return;
        }
        int x = pos.getFloorX();
        int y = pos.getFloorY();
        int z = pos.getFloorZ();

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("UPDATE `" + prefix + "_island` SET `spawnX` = ?, `spawnY` = ?, `spawnZ` = ? WHERE `isSpawn` = '1'");
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.addBatch();
            stmt.executeBatch();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
    }

    @Override
    public IslandData getIslandLocation(String levelName, int x, int z) {
        int id = plugin.getIsland().generateIslandKey(x, z, levelName);
        IslandData database = new IslandData(levelName, x, z, plugin.getSettings(levelName).getProtectionRange());
        if (enableFastCache) {
            // Get a list of island data on cache
            for (IslandData pd : islandCache) {
                if (pd.getIslandId() == id && pd.getLevelName().equalsIgnoreCase(levelName)) {
                    return pd;
                }
            }
        }
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return null;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE(`level` = '" + levelName + "' AND `islandId` = '" + id + "')");
            if (!set.isClosed()) {
                database = new IslandData(
                        set.getString("level"),
                        set.getInt("x"),
                        set.getInt("y"),
                        set.getInt("z"),
                        set.getInt("spawnX"),
                        set.getInt("spawnY"),
                        set.getInt("spawnZ"),
                        set.getInt("psize"),
                        set.getString("islandName"),
                        set.getString("playerName"),
                        set.getString("biome"),
                        set.getInt("id"),
                        set.getInt("islandId"),
                        set.getBoolean("locked"),
                        set.getString("protection"),
                        set.getBoolean("isSpawn")
                );

                // The put in the cache
                if (enableFastCache) {
                    islandCache.add(database);
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return database;
    }

    @Override
    public ArrayList<IslandData> getIslands(String playerName) {
        ArrayList<IslandData> list = new ArrayList<>();

        // Get data from the cache
        if (enableFastCache) {
            islandCache.stream().filter((pd3) -> (pd3.getOwner().equalsIgnoreCase(playerName))).forEachOrdered(list::add);
            if (!list.isEmpty()) {
                return list;
            }
        }
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return list;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE(`playerName` = '" + playerName + "')");
            if (!set.isClosed()) {
                while (set.next()) {
                    IslandData island = new IslandData(
                            set.getString("level"),
                            set.getInt("x"),
                            set.getInt("y"),
                            set.getInt("z"),
                            set.getInt("spawnX"),
                            set.getInt("spawnY"),
                            set.getInt("spawnZ"),
                            set.getInt("psize"),
                            set.getString("islandName"),
                            set.getString("playerName"),
                            set.getString("biome"),
                            set.getInt("id"),
                            set.getInt("islandId"),
                            set.getBoolean("locked"),
                            set.getString("protection"),
                            set.getBoolean("isSpawn")
                    );
                    list.add(island);

                    // The put in the cache
                    if (enableFastCache) {
                        islandCache.add(island);
                    }
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return list;
    }

    @Override
    public ArrayList<IslandData> getIslands(String playerName, String levelName) {
        ArrayList<IslandData> list = new ArrayList<>();

        // Get data from the cache
        if (enableFastCache) {
            islandCache.stream().filter((list3) -> (list3.getOwner().equalsIgnoreCase(playerName) && list3.getLevelName().equalsIgnoreCase(levelName))).forEachOrdered(list::add);
            if (!list.isEmpty()) {
                return list;
            }
        }

        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return list;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE(`level` = '" + levelName + "' AND `playerName` = '" + playerName + "')");
            if (!set.isClosed()) {
                while (set.next()) {
                    IslandData island = new IslandData(
                            set.getString("level"),
                            set.getInt("x"),
                            set.getInt("y"),
                            set.getInt("z"),
                            set.getInt("spawnX"),
                            set.getInt("spawnY"),
                            set.getInt("spawnZ"),
                            set.getInt("psize"),
                            set.getString("islandName"),
                            set.getString("playerName"),
                            set.getString("biome"),
                            set.getInt("id"),
                            set.getInt("islandId"),
                            set.getBoolean("locked"),
                            set.getString("protection"),
                            set.getBoolean("isSpawn")
                    );
                    list.add(island);

                    // Save the island into cache
                    if (enableFastCache) {
                        islandCache.add(island);
                    }
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return list;
    }

    @Override
    public IslandData getIsland(String name, int homes) {
        IslandData pd = null;

        // Get the data from the cache first
        if (enableFastCache) {
            for (IslandData pda : islandCache) {
                if (!pda.getOwner().equals(name) && pda.getId() != homes) {
                    continue;
                }
                return pda;
            }
        }

        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return null;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE(`playerName` = '" + name + "' AND `id` = '" + homes + "')");

            // Check if its not closed, otherwise just move trough
            if (!set.isClosed()) {
                pd = new IslandData(
                        set.getString("level"),
                        set.getInt("x"),
                        set.getInt("y"),
                        set.getInt("z"),
                        set.getInt("spawnX"),
                        set.getInt("spawnY"),
                        set.getInt("spawnZ"),
                        set.getInt("psize"),
                        set.getString("islandName"),
                        set.getString("playerName"),
                        set.getString("biome"),
                        set.getInt("id"),
                        set.getInt("islandId"),
                        set.getBoolean("locked"),
                        set.getString("protection"),
                        set.getBoolean("isSpawn")
                );

                // Save into the cache
                if (enableFastCache) {
                    islandCache.add(pd);
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return pd;
    }

    @Override
    public IslandData getIsland(String playerName, String homeName) {
        IslandData pd = null;
        // Get the data from the cache first
        if (enableFastCache) {
            for (IslandData pda : islandCache) {
                if (!pda.getOwner().equals(playerName) && !pda.getName().equalsIgnoreCase(homeName)) {
                    continue;
                }
                return pda;
            }
        }
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return null;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE(`playerName` = '" + playerName + "' AND `islandName` = '" + homeName + "')");

            // Check if its not closed, otherwise just move trough
            if (!set.isClosed()) {
                pd = new IslandData(set.getString("level"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("islandName"), set.getString("playerName"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));

                // Save into the cache
                if (enableFastCache) {
                    islandCache.add(pd);
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            // Maybe there was an error while saving the data
            // Close the connection so it won't create too much connections
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return pd;
    }

    @Override
    public boolean deleteIsland(IslandData pd) {
        if (enableFastCache) {
            islandCache.remove(pd);
        }
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return false;
        }

        boolean result = false;
        PreparedStatement set = null;
        try {
            set = connection.prepareStatement("DELETE FROM `" + prefix + "_island` WHERE(`id` = ? AND `playerName` = ?)");
            set.setInt(1, pd.getId());
            set.setString(2, pd.getOwner());

            result = set.execute();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            // Maybe there was an error while saving the data
            // Close the connection so it won't create too much connections
            try {
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return result;
    }

    @Override
    public IslandData getSpawn() {
        IslandData pd = null;
        if (islandSpawn != null) {
            return islandSpawn;
        }
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return null;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE `isSpawn` = '1'");

            if (!set.isClosed()) {
                pd = new IslandData(set.getString("level"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("islandName"), set.getString("playerName"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
                islandSpawn = pd;
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            // Maybe there was an error while saving the data
            // Close the connection so it won't create too much connections
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return pd;
    }

    @Override
    public IslandData getIslandById(int id) {
        IslandData pd = null;

        if (enableFastCache) {
            for (IslandData list : islandCache) {
                if (list.getIslandId() != id) {
                    continue;
                }
                return list;
            }
        }
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return null;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_island` WHERE `islandId` = '" + id + "'");

            if (!set.isClosed()) {
                pd = new IslandData(set.getString("level"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("islandName"), set.getString("playerName"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
                if (enableFastCache) {
                    islandCache.add(pd);
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            // Maybe there was an error while saving the data
            // Close the connection so it won't create too much connections
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return pd;
    }

    @Override
    public void close() {
        Utils.send("&7Closing databases...");
        try {
            connection.close();
            database.closeConnection();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }

        free();
        closed = true;
    }

    @Override
    public boolean createIsland(IslandData pd) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return false;
        }
        boolean result = true;
        PreparedStatement set = null;
        try {
            set = connection.prepareStatement("INSERT INTO `" + prefix + "_island` (`islandId`, `x`, `y`, `z`, `isSpawn`, `psize`, `playerName`, `islandName`, `level`, `biome`, `locked`, `protection`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            set.setInt(1, pd.getIslandId());
            set.setInt(2, pd.getCenter().getFloorX());
            set.setInt(3, pd.getCenter().getFloorY());
            set.setInt(4, pd.getCenter().getFloorZ());
            set.setBoolean(5, pd.isSpawn());
            set.setInt(6, pd.getProtectionSize());
            set.setString(7, pd.getOwner());
            set.setString(8, pd.getName());
            set.setString(9, pd.getLevelName());
            set.setString(10, pd.getBiome());
            set.setBoolean(11, pd.isLocked());
            set.setString(12, pd.getIgsSettings().getSettings());
            set.addBatch();

            for (int batches : set.executeBatch()) {
                if (batches == EXECUTE_FAILED) {
                    Utils.send("&cFailed to create data for island: " + pd.getOwner());

                    result = false;
                    break;
                }
            }
        } catch (SQLException ex) {
            try {
                if (set != null) {
                    set.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        }

        return result;
    }

    @Override
    public boolean saveIsland(IslandData pd) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return false;
        }
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("INSERT INTO `" + prefix + "_island` (`id`, `islandId`, `x`, `y`, `z`, `isSpawn`, `psize`, `islandOwner`, `islandName`, `level`, `biome`, `locked`, `protection`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            stmt.setString(1, pd.getName());
            stmt.setString(2, pd.getBiome());
            stmt.setBoolean(3, pd.isLocked());
            stmt.setBoolean(4, pd.isSpawn());
            stmt.setString(5, pd.getIgsSettings().getSettings());
            stmt.setInt(6, pd.homeX);
            stmt.setInt(7, pd.homeY);
            stmt.setInt(8, pd.homeZ);
            stmt.addBatch();

            for (int batches : stmt.executeBatch()) {
                if (batches == EXECUTE_FAILED) {
                    Utils.send("&cFailed to save data for island: " + pd.getOwner());
                    stmt.close();

                    return false;
                }
            }
            stmt.close();
        } catch (SQLException ex) {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }

            return false;
        }

        // Automatically remove the island from cache
        if (enableFastCache) {
            for (IslandData pde : islandCache) {
                if (pde.getIslandId() != pd.getIslandId() && !pde.getOwner().equalsIgnoreCase(pd.getOwner())) {
                    continue;
                }
                islandCache.remove(pde);
            }
            islandCache.add(pd);
        }
        return false;
    }

    @Override
    public ArrayList<String> getWorlds() {
        ArrayList<String> world = new ArrayList<>();
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return world;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT `world` FROM `" + prefix + "_worlds`");

            // Simple and ease
            while (!set.isClosed() && set.next()) {
                world.add(set.getString("world"));
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            // Maybe there was an error while saving the data
            // Close the connection so it won't create too much connections
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        }
        return world;
    }

    @Override
    public boolean saveWorlds(ArrayList<String> pd) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return false;
        }
        ArrayList<String> duplicate = getWorlds();

        boolean result = true;
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("INSERT INTO `" + prefix + "_worlds` (`world`) VALUES (?);");
            for (String pd2 : pd) {
                // Sometimes, they could go duplicate
                if (duplicate.contains(pd2)) {
                    continue;
                }
                stmt.setString(1, pd2);
                stmt.addBatch();

                for (int batches : stmt.executeBatch()) {
                    if (batches == EXECUTE_FAILED) {
                        Utils.send("&cFailed to save world for world: " + pd2);

                        result = false;
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            }
        }
        return result;
    }

    @Override
    public List<String> getPlayersData() {
        List<String> playersData = new ArrayList<>();
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return playersData;
        }

        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_worlds`");


            while (!set.isClosed() && set.next()) {
                playersData.add(set.getString("player"));
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        }
        return playersData;
    }

    @Override
    public PlayerData getPlayerData(String st) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return null;
        }
        PlayerData pd = null;
        Statement stmt = null;
        ResultSet set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery("SELECT * FROM `" + prefix + "_players` WHERE `player` = '" + st + "'");

            if (!set.isClosed()) {
                pd = new PlayerData(set.getString("player"), set.getInt("homes"), Utils.stringToArray(set.getString("members"), ", "), set.getString("challengelist"), set.getString("challengelisttimes"), set.getInt("islandlvl"), set.getBoolean("inTeam"), set.getInt("deaths"), set.getString("teamLeader"), set.getString("teamIslandLocation"), set.getInt("resetleft"), Utils.stringToArray(set.getString("banList"), ", "), set.getString("locale"), set.getString("teamName"));
            }
        } catch (SQLException ex) {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        }
        return pd;
    }

    @Override
    public void createPlayer(String p) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("INSERT INTO `" + prefix + "_players` ("
                    + "`player`, "
                    + "`homes`, "
                    + "`resetleft`, "
                    + "`banlist`, "
                    + "`teamleader`, "
                    + "`teamislandlocation`, "
                    + "`inteam` , "
                    + "`islandlvl`, "
                    + "`members`,"
                    + "`challengelist`, "
                    + "`challengelisttimes`, "
                    + "`teamName`, "
                    + "`locale`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            PlayerData pd = new PlayerData(p, 0, Settings.reset);
            stmt.setString(1, pd.getPlayerName());
            stmt.setInt(2, pd.getHomeNumber());
            stmt.setInt(3, pd.getPlayerReset());
            stmt.setString(4, Utils.arrayToString(pd.getBanList()));
            stmt.setString(5, pd.teamLeader);
            stmt.setString(6, pd.teamIslandLocation);
            stmt.setBoolean(7, pd.hasTeam());
            stmt.setInt(8, pd.getIslandLevel());
            stmt.setString(9, Utils.arrayToString(pd.members));
            stmt.setString(10, pd.decodeChallengeList("cl"));
            stmt.setString(11, pd.decodeChallengeList("clt"));
            stmt.setString(12, pd.name);
            stmt.setString(13, pd.getLocale());
            stmt.addBatch();

            for (int batches : stmt.executeBatch()) {
                if (batches == EXECUTE_FAILED) {
                    Utils.send("&cFailed to create PlayerData for: " + p);
                }
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        }
    }

    @Override
    public void savePlayerData(PlayerData pd) {
        if (!connected) {
            Utils.send("&cUnable to process any requests for MySQL due to no connectivity within the servers");
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("UPDATE `" + prefix + "_players` SET "
                    + "`homes` = ?, "
                    + "`resetleft` = ?, "
                    + "`banlist` = ?, "
                    + "`teamleader` = ?, "
                    + "`teamislandlocation` = ?, "
                    + "`inteam` = ?, "
                    + "`islandlvl` = ?, "
                    + "`members` = ?, "
                    + "`challengelist` = ?, "
                    + "`challengelisttimes` = ?, "
                    + "`teamName` = ?, "
                    + "`locale` = ? "
                    + "WHERE `player` = '" + pd.getPlayerName() + "'");
            stmt.setInt(1, pd.getHomeNumber());
            stmt.setInt(2, pd.getPlayerReset());
            stmt.setString(3, Utils.arrayToString(pd.getBanList()));
            stmt.setString(4, pd.teamLeader);
            stmt.setString(5, pd.teamIslandLocation);
            stmt.setBoolean(6, pd.hasTeam());
            stmt.setInt(7, pd.getIslandLevel());
            stmt.setString(8, Utils.arrayToString(pd.members));
            stmt.setString(9, pd.decodeChallengeList("cl"));
            stmt.setString(10, pd.decodeChallengeList("clt"));
            stmt.setString(11, pd.name);
            stmt.setString(12, pd.getLocale());
            stmt.addBatch();
            for (int batches : stmt.executeBatch()) {
                if (batches == EXECUTE_FAILED) {
                    Utils.send("&cFailed to save PlayerData for: " + pd.getPlayerName());
                }
            }

            stmt.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                JDBCUtilities.printSQLException(e);
            }
        }
    }

}
