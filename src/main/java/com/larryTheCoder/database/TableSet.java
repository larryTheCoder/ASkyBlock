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

package com.larryTheCoder.database;

/**
 * A set of queries.
 */
public enum TableSet {

    // Default tables
    WORLD_TABLE("CREATE TABLE IF NOT EXISTS worldList(" +
            "worldName TEXT PRIMARY KEY) %OPTIMIZE"),

    PLAYER_TABLE("CREATE TABLE IF NOT EXISTS player(" +
            "playerName VARCHAR(100)," +
            "playerUUID VARCHAR(36)," +
            "locale TEXT NOT NULL," +
            "banList TEXT NOT NULL," +
            "resetAttempts INTEGER NOT NULL," +
            "islandLevels INTEGER DEFAULT 0," +
            "PRIMARY KEY (playerName)) %OPTIMIZE"),

    PLAYER_CHALLENGES("CREATE TABLE IF NOT EXISTS challenges(" +
            "player VARCHAR(100) PRIMARY KEY NOT NULL," +
            "challengesList TEXT," +
            "challengesTimes TEXT," +
            "FOREIGN KEY (player) REFERENCES player(playerName) ON UPDATE CASCADE) %OPTIMIZE"),

    ISLAND_TABLE("CREATE TABLE IF NOT EXISTS island(" +
            "islandUniqueId INTEGER PRIMARY KEY NOT NULL," +
            "islandId INTEGER NOT NULL," +
            "gridPosition TEXT NOT NULL," +
            "spawnPosition TEXT DEFAULT ''," +
            "gridSize INTEGER NOT NULL," +
            "levelName TEXT NOT NULL," +
            "islandName TEXT DEFAULT ''," +
            "playerName VARCHAR(100) NOT NULL," +
            "FOREIGN KEY (levelName) REFERENCES worldList(worldName) ON UPDATE CASCADE) %OPTIMIZE"),

    ISLAND_DATA("CREATE TABLE IF NOT EXISTS islandData(" +
            "dataId INT PRIMARY KEY," +
            "biome INTEGER DEFAULT 0," +
            "locked INTEGER DEFAULT 0," +
            "protectionData TEXT DEFAULT ''," +
            "levelHandicap INTEGER DEFAULT 0," +
            "FOREIGN KEY (dataId) REFERENCES island(islandUniqueId) ON UPDATE CASCADE) %OPTIMIZE"),

    ISLAND_RELATIONS("CREATE TABLE IF NOT EXISTS islandRelations(" +
            "defaultIsland INT NOT NULL," +
            "islandLeader VARCHAR(100)," +
            "islandMembers TEXT DEFAULT ''," +
            "FOREIGN KEY (defaultIsland) REFERENCES island(islandUniqueId) ON UPDATE CASCADE," +
            "FOREIGN KEY (islandLeader) REFERENCES player(playerName) ON UPDATE CASCADE," +
            "PRIMARY KEY (defaultIsland, islandLeader)) %OPTIMIZE"),

    METADATA_TABLE("CREATE TABLE IF NOT EXISTS cacheMetadata(" +
            "dbVersion INT NOT NULL," +
            "firstInit DEFAULT CURRENT_TIMESTAMP," +
            "cacheUniqueId VARCHAR(32) NOT NULL) %OPTIMIZE"),

    // Note: Enabling this will make sure that everything will be strict.
    //       Better be strict than having flaws.
    SQLITE_PRAGMA_ON("PRAGMA foreign_keys = ON"),
    FOR_TABLE_OPTIMIZE_A("SET GLOBAL innodb_file_per_table=1"),
    FOR_TABLE_OPTIMIZE_B("SET GLOBAL innodb_file_format=Barracuda"),

    FETCH_WORLDS("SELECT worldName FROM worldList"),
    FETCH_PLAYER_MAIN("SELECT * FROM player WHERE playerName = :plotOwner"),
    FETCH_PLAYER_DATA("SELECT * FROM challenges WHERE player = :playerName"),
    FETCH_ISLAND_UNIQUE("SELECT * FROM island WHERE islandUniqueId = :islandUniqueId AND levelName = :levelName"),
    FETCH_LEVEL_PLOT("SELECT * FROM island WHERE levelName = :levelName AND islandUniqueId = :islandId"),
    FETCH_ISLAND_PLOTS("SELECT * FROM island WHERE playerName = :pName"),
    FETCH_ISLAND_PLOT("SELECT * FROM island WHERE playerName = :pName AND islandId = :islandId"),
    FETCH_ISLAND_NAME("SELECT * FROM island WHERE playerName = :pName AND islandName = :islandName"),
    FETCH_ISLAND_DATA("SELECT * FROM islandData WHERE dataId = :islandUniquePlotId"),
    TABLE_FETCH_CACHE("SELECT * FROM cacheMetadata WHERE dbVersion = :dbVersion"),
    FETCH_ISLANDS_PLOT("SELECT * FROM island WHERE playerName = :pName"),
    FETCH_ALL_ISLAND_UNIQUE("SELECT islandUniqueId FROM island"),

    // Mysql and SQLite database syntax are very different.
    // Therefore we must INSERT data precisely.
    TABLE_INSERT_CACHE("INSERT INTO cacheMetadata(dbVersion, cacheUniqueId) VALUES (:dbVersion, :cacheUniqueId)"),
    ISLAND_INSERT_MAIN("INSERT INTO island(islandId, islandUniqueId, gridPosition, spawnPosition, gridSize, levelName, playerName, islandName) VALUES (:islandId, :islandUniqueId, :gridPos, :spawnPos, :gridSize, :levelName, :playerName, :islandName)"),
    ISLAND_INSERT_DATA("INSERT INTO islandData(dataId, biome, locked, protectionData, levelHandicap) VALUES (:islandUniqueId, :plotBiome, :isLocked, :protectionData, :levelHandicap)"),
    PLAYER_INSERT_MAIN("INSERT %IGNORE INTO player(playerName, playerUUID, locale, banList, resetAttempts) VALUES (:playerName, :playerUUID, :locale, :banList, :resetLeft)"),
    PLAYER_INSERT_DATA("INSERT %IGNORE INTO challenges(player, challengesList, challengesTimes) VALUES (:playerName, :challengesList, :challengesTimes)"),

    ISLAND_UPDATE_MAIN("UPDATE island SET islandId = :islandId, gridPosition = :gridPos, spawnPosition = :spawnPos, gridSize = :gridSize, levelName = :levelName, playerName = :plotOwner, islandName = :islandName WHERE islandUniqueId = :islandUniqueId"),
    ISLAND_UPDATE_DATA("UPDATE islandData SET biome = :plotBiome, locked = :isLocked, protectionData = :protectionData, levelHandicap = :levelHandicap WHERE dataId = :islandUniqueId"),
    PLAYER_UPDATE_MAIN("UPDATE player SET locale = :locale, banList = :banList, resetAttempts = :resetLeft, islandLevels = :islandLevels WHERE playerName = :playerName"),
    PLAYER_UPDATE_DATA("UPDATE challenges SET challengesList = :challengesList, challengesTimes = :challengesTimes WHERE player = :playerName"),

    WORLDS_INSERT("INSERT %IGNORE INTO worldList (worldName) VALUES (:levelName)");

    private final String query;

    TableSet(String query) {
        this.query = query;
    }

    public String getQuery() {
        String resultPoint = query;

        boolean isMysql = DatabaseManager.isMysql;

        if (isMysql) {
            resultPoint = resultPoint.replace("%IGNORE", "IGNORE");
            resultPoint = resultPoint.replace("%OPTIMIZE", "ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED");
        } else {
            resultPoint = resultPoint.replace("%IGNORE", "OR IGNORE");
            resultPoint = resultPoint.replace("%OPTIMIZE", "");
        }

        return resultPoint;
    }
}
