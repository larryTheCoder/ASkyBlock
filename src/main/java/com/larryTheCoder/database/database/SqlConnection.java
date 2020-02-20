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

import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.config.AbstractConfig;
import com.larryTheCoder.database.config.SQLiteConfig;
import com.larryTheCoder.storage.IslandSettings;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A new and faster way to operate sql commands
 * and database through a relational database.
 * <p>
 * This makes sense when managing tables more easier
 * when its comes to create a new data and tables.
 * <p>
 * Legacy one will be automatically converted into a
 * new ones, depends, its could fails. Also more
 * slower...
 */
public class SqlConnection {

    private static final int TABLE_VERSION = 1;

    private AbstractConfig database;
    private Connection connection;

    private long lastTick = 0;
    private boolean messageDigest = false;

    public SqlConnection(AbstractConfig database) throws SQLException, ClassNotFoundException {
        this.database = database;
        this.connection = database.openConnection();

        this.startConnection();
    }

    /**
     * Starts a connection and creates a new table.
     * This also checks the table version and decide to
     * update it or not, like Windows Update Service.
     */
    private void startConnection() throws SQLException, ClassNotFoundException {
        Statement stmt = connection.createStatement();

        ResultSet tables = connection.getMetaData().getTables(null, null, "tableRevision", new String[]{"TABLE"});
        ResultSet legacy = connection.getMetaData().getTables(null, null, "worlds", new String[]{"TABLE"});

        if (tables.next()) {
            // Table exists
            ResultSet r = stmt.executeQuery("SELECT tableVersion from tableRevision");
            int revVersion = r.getInt(1);
            if (revVersion < TABLE_VERSION) {
                Utils.send("&7Updating sql tables from version " + revVersion + " into version " + TABLE_VERSION);
                Utils.send("&7Backing up your data before updating your tables.");

                startUpdate(revVersion);
            }
        } else if (legacy.next()) {
            database.closeConnection();
            Utils.send("&7Updating sql tables into version " + TABLE_VERSION);
            Utils.send("&7Backing up your data before updating your tables.");

            startUpdate(-1);
        } else {
            // Table revision and world name.
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tableRevision(tableVersion INT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS worldList (worldName TEXT PRIMARY KEY)");

            /*
             * Player data and challenges
             */

            // Player table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player (" +
                    "playerName VARCHAR(100) PRIMARY KEY," +
                    "locale TEXT NOT NULL," +
                    "banList TEXT NOT NULL," +
                    "resetAttempts INT NOT NULL)");

            // Challenges data
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS challenges(" +
                    "challengesList TEXT," +
                    "challengesTimes TEXT," +
                    "player VARCHAR(100)," +
                    "FOREIGN KEY (player) REFERENCES player(playerName))");

            /*
             * Island Tables and data
             */

            // Island table
            // Stores important data.
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS island (" +
                    "islandId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "islandUniqueId BIGINT," +
                    "gridPosition TEXT NOT NULL," +
                    "spawnPosition TEXT," +
                    "gridSize INTEGER NOT NULL," +
                    "player VARCHAR(100)," +
                    "FOREIGN KEY (player) REFERENCES player(playerName))");

            // Island data from table 'table island'
            // Store only island side data. Not important. well at least?
            // We can create new table if there is new data lol
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS islandData(" +
                    "dataId INT," +
                    "biome INT DEFAULT 0," +
                    "locked INTEGER DEFAULT 0," +
                    "protectionData TEXT DEFAULT ''," +
                    "levelName TEXT," +
                    "levelHandicap INT DEFAULT 0," +
                    "FOREIGN KEY (dataId) REFERENCES island(islandId)," +
                    "FOREIGN KEY (levelName) REFERENCES worldList(worldName))");

            // This table stores the home count, no more pain
            // This table will be updated every time player deletes their island.
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS islandId(" +
                    "dataId INT," +
                    "homeId INT," +
                    "FOREIGN KEY (dataId) REFERENCES island(islandId))");

            /*
             * Parties data and members
             */

            // PartyData table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS partyData (" +
                    "teamId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "defaultIsland INT NOT NULL," +
                    "player VARCHAR(100)," +
                    "FOREIGN KEY (defaultIsland) REFERENCES island(islandId)," +
                    "FOREIGN KEY (player) REFERENCES player(playerName))");

            // Members table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS members(" +
                    "memberId INT," +
                    "teamMembers TEXT DEFAULT ''," +
                    "FOREIGN KEY (memberId) REFERENCES partyData(teamId))");

            stmt.addBatch("INSERT INTO tableRevision(tableVersion) VALUES(" + TABLE_VERSION + ")");
            stmt.executeBatch();
            stmt.clearBatch();
        }
    }

    /**
     * Update a sql table from revision A to
     * revision B.
     *
     * @param from The table that need to be updated from.
     */
    private void startUpdate(int from) throws SQLException, ClassNotFoundException {
        // Windows Update much?
        switch (from) {
            // The table is from a very very legacy one
            // Like uh? no performance at all.
            case -1:
                if (database instanceof SQLiteConfig) {
                    UUID random = UUID.randomUUID(); // Random and unique, fix update loop
                    String sqlPath = ((SQLiteConfig) database).getAbsolutePath();
                    // Prepare to rename it and move it
                    File file = new File(sqlPath);
                    try {
                        file.renameTo(new File(ASkyBlock.get().getDataFolder(), random.toString() + ".db"));
                        file.createNewFile();
                    } catch (IOException ignored) {
                    }
                    SQLiteConfig oldDb = new SQLiteConfig(new File(ASkyBlock.get().getDataFolder(), random.toString() + ".db"));

                    this.database = new SQLiteConfig(file);
                    this.connection = database.openConnection();
                    Connection oldConn = oldDb.openConnection(); // Now move table A to B... uh more things to be done

                    Statement stmtOld = oldConn.createStatement();

                    // Start a new connection.
                    startConnection();

                    TaskManager.runTaskAsync(() -> {
                        long currentTime = System.currentTimeMillis();

                        Exception errorCode = null;
                        boolean failure = false;

                        try {
                            Statement stmt = connection.createStatement();

                            // The worlds table.
                            ResultSet worldSet = stmtOld.executeQuery("SELECT * FROM `worlds`");

                            while (worldSet.next()) {
                                stmt.addBatch("INSERT INTO worldList(worldName) VALUES ('" + worldSet.getString("world") + "')");
                                stmt.executeBatch();
                                stmt.clearBatch();
                            }
                            worldSet.close();

                            // The player data.
                            ResultSet playerSet = stmtOld.executeQuery("SELECT * FROM `players`");
                            while (playerSet.next()) {
                                tickProcessor(currentTime);

                                String playerName = playerSet.getString("player");
                                String localeName = playerSet.getString("locale");
                                String banList = playerSet.getString("banList");
                                int resetLeft = playerSet.getInt("resetleft");

                                // This will fix the challenge issue on legacies databases
                                String challengeList = Utils.checkChallenge(playerSet.getString("challengelist"), "cl");
                                String challengeListTimes = Utils.checkChallenge(playerSet.getString("challengelisttimes"), "clt");

                                try {
                                    stmt.addBatch("INSERT INTO player(playerName, locale, banList, resetAttempts) VALUES (" +
                                            "'" + playerName + "'," +
                                            "'" + localeName + "'," +
                                            "'" + banList + "'," +
                                            "'" + resetLeft + "')");

                                    stmt.executeBatch();
                                    stmt.clearBatch();

                                    stmt.addBatch("INSERT INTO challenges(challengesList, challengesTimes, player) VALUES (" +
                                            "'" + challengeList + "'," +
                                            "'" + challengeListTimes + "'," +
                                            "'" + playerName + "')");

                                    stmt.executeBatch();
                                    stmt.clearBatch();
                                } catch (SQLException e) {
                                    Utils.send("&cFailed to insert player table: " + playerName);
                                }
                            }

                            // The island table.
                            ResultSet set = stmtOld.executeQuery("SELECT * FROM `island`");
                            while (set.next()) {
                                tickProcessor(currentTime);

                                // Important set of data
                                String gridPosition = Utils.getVectorPair(new Vector3(set.getInt("x"), set.getInt("y"), set.getInt("z")));
                                String playerName = set.getString("owner"); // This is very very confusion on alpha databases
                                int islandUniqueId = set.getInt("islandId");
                                int gridSize = set.getInt("psize");

                                // Just an 'eh' set of data
                                int homeCount = set.getInt("id");
                                String biome = set.getString("biome");
                                String igsSettings = new IslandSettings(set.getString("protection")).getSettings();
                                String levelName = set.getString("world");
                                boolean isLocked = set.getBoolean("locked");

                                try {
                                    // Table 1: Important table
                                    stmt.addBatch("INSERT INTO island(islandUniqueId, gridPosition, gridSize, player) VALUES (" +
                                            "'" + islandUniqueId + "'," +
                                            "'" + gridPosition + "'," +
                                            "'" + gridSize + "'," +
                                            "'" + playerName + "')");

                                    stmt.executeBatch();
                                    stmt.clearBatch();

                                    int islandId;

                                    ResultSet setNew = stmt.executeQuery("SELECT * FROM island WHERE islandUniqueId = " + islandUniqueId);

                                    if (setNew.next()) {
                                        islandId = setNew.getInt("islandId");
                                    } else {
                                        Utils.send("&cUnable to process the islandId for " + playerName);
                                        return;
                                    }
                                    setNew.close();

                                    // Table 2: Island Data
                                    stmt.addBatch("INSERT INTO islandData(dataId, biome, locked, protectionData, levelName) VALUES (" +
                                            "'" + islandId + "'," +
                                            "'" + biome + "'," +
                                            "'" + (isLocked ? 0 : 1) + "'," +
                                            "'" + igsSettings + "'," +
                                            "'" + levelName + "')");

                                    stmt.executeBatch();
                                    stmt.clearBatch();

                                    // Table 3: Island homes
                                    stmt.addBatch("INSERT INTO islandId(dataId, homeId) VALUES (" +
                                            "'" + islandId + "', " +
                                            "'" + homeCount + "')");

                                    stmt.executeBatch();
                                    stmt.clearBatch();
                                } catch (SQLException e) {
                                    Utils.send("&cFailed to insert island table of player: " + playerName + " of IslandUniqueId: " + islandUniqueId);
                                }
                            }
                        } catch (SQLException | NullPointerException e) {
                            try {
                                database.closeConnection();
                                oldDb.closeConnection();
                            } catch (SQLException e1) {
                                failure = true;
                            }
                            errorCode = e;
                        }

                        final Exception errorRecord = errorCode;
                        final boolean crashedError = failure;

                        // Return to main thread.
                        new NukkitRunnable() {
                            @Override
                            public void run() {
                                if (errorRecord != null) {
                                    if (crashedError) {
                                        Utils.send("&cCRASHED WHILE UPDATING DATABASE...");
                                        Utils.send("&cCANNOT REVERT DATABASE TO ITS LAST STATE...");
                                        Utils.send("&cYOUR DATABASE COULD BE EFFECTED WITH CORRUPTION...");
                                        return;
                                    }
                                    Utils.send("&cAn unexpected error occured while updating database");
                                    Utils.send("&cReverting changes...");
                                    errorRecord.printStackTrace();
                                    // TODO: Revert the database
                                } else {
                                    double timeline = (double) (System.currentTimeMillis() - currentTime) / 1000;
                                    Utils.send("&aSuccessfully updated your database within (" + timeline + "s)!");
                                    Utils.send("&7Please restart your server to fully complete this update...");
                                }
                            }
                        }.runTask(ASkyBlock.get());
                    });
                }
                break;
            case 1:
            case 2:
            case 3:
                // All goes this kind of 'way'
                // Do not break it.
                // -1 -/-> 1 -> 2 -> 3 -> ...
                break;
        }
    }

    /**
     * Ticks the processor from SkyBlock Update Service
     * This is run trough async worker, please do not
     * exploit...
     *
     * @param currentTime The time of the worker elapsed.
     */
    private void tickProcessor(long currentTime) {
        if (lastTick == 0) {
            lastTick = currentTime;
        }
        long threadThreshold = System.currentTimeMillis() - lastTick;

        // 60 Seconds interval...
        if (threadThreshold >= TimeUnit.SECONDS.toMillis(30) && !messageDigest) {
            Utils.send("&eThis may take some time to update your database");
            lastTick = currentTime;
            messageDigest = true;
        }
    }
}
