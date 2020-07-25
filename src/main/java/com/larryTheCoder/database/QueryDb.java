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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * A query database that explicitly return specified database dialects
 * for cross-platform database operations. This class is formerly known as TableSet
 *
 * <h> This is to bind the returned dialects for sqlite and/or mysql. But in most cases,
 * the return value does not need any changes therefore only few variables were played
 * in this class.
 *
 * @author larryTheCoder
 * @since 0.5.3-BETA
 */
@Log4j2
public class QueryDb {

    @Getter
    public static QueryDb instance;

    // Init tables.
    public final String worldTable;
    public final String playerTable;
    public final String playerChallenges;
    public final String islandTable;
    public final String islandData;
    public final String islandRelations;
    public final String islandLimitCount;
    public final String metadata;

    public final String saveWorldData;
    public final String insertPlayerData;
    public final String insertChallengeData;
    public final String awaitStore;

    public QueryDb(boolean isMysql) {
        Preconditions.checkArgument(instance == null, "Query database has already been initiated");
        instance = this;

        if (isMysql) {
            metadata = "CREATE TABLE IF NOT EXISTS cacheMetadata(" +
                    "dbVersion VARCHAR(32) NOT NULL," +
                    "firstInit DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "cacheUniqueId VARCHAR(32) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            islandLimitCount = "CREATE TABLE IF NOT EXISTS lastExecution(" +
                    "playerUniqueId VARCHAR(64) PRIMARY KEY NOT NULL," +
                    "lastQueried BIGINT NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            islandRelations = "CREATE TABLE IF NOT EXISTS islandRelations(" +
                    "defaultIsland INT NOT NULL," +
                    "islandTeamName TEXT," +
                    "islandLeader VARCHAR(100)," +
                    "islandMembers TEXT," +
                    "FOREIGN KEY (defaultIsland) REFERENCES island(islandUniqueId) ON UPDATE CASCADE," +
                    "FOREIGN KEY (islandLeader) REFERENCES player(playerName) ON UPDATE CASCADE," +
                    "PRIMARY KEY (defaultIsland, islandLeader)) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            islandData = "CREATE TABLE IF NOT EXISTS islandData(" +
                    "dataId INT PRIMARY KEY," +
                    "biome VARCHAR(32) DEFAULT ''," +
                    "locked INTEGER DEFAULT 0," +
                    "protectionData TEXT," +
                    "levelHandicap INTEGER DEFAULT 0," +
                    "islandLevel INTEGER DEFAULT 0," +
                    "FOREIGN KEY (dataId) REFERENCES island(islandUniqueId) ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            islandTable = "CREATE TABLE IF NOT EXISTS island(" +
                    "islandUniqueId INTEGER PRIMARY KEY NOT NULL," +
                    "islandId INTEGER NOT NULL," +
                    "gridPosition TEXT," +
                    "spawnPosition TEXT," +
                    "islandName TEXT," +
                    "gridSize INTEGER NOT NULL," +
                    "levelName VARCHAR(256) NOT NULL," +
                    "playerName VARCHAR(100) NOT NULL," +
                    "FOREIGN KEY (levelName) REFERENCES worldList(worldName) ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            playerChallenges = "CREATE TABLE IF NOT EXISTS challenges(" +
                    "player VARCHAR(100) PRIMARY KEY NOT NULL," +
                    "challengesList TEXT," +
                    "challengesTimes TEXT," +
                    "FOREIGN KEY (player) REFERENCES player(playerName) ON UPDATE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            playerTable = "CREATE TABLE IF NOT EXISTS player(" +
                    "playerName VARCHAR(100)," +
                    "playerUUID VARCHAR(36)," +
                    "locale VARCHAR(4) NULL," +
                    "banList TEXT," +
                    "resetAttempts INTEGER NOT NULL," +
                    "lastLogin DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (playerName)) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            worldTable = "CREATE TABLE IF NOT EXISTS worldList(" +
                    "worldName VARCHAR(256) PRIMARY KEY," +
                    "levelId INT NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED";

            saveWorldData = "INSERT IGNORE INTO worldList (worldName, levelId) VALUES (:levelName, :levelId)";
            insertPlayerData = "INSERT IGNORE INTO player(playerName, playerUUID, locale, banList, resetAttempts) VALUES (:playerName, :playerUUID, :locale, :banList, :resetLeft)";
            insertChallengeData = "INSERT IGNORE INTO challenges(player, challengesList, challengesTimes) VALUES (:playerName, :challengesList, :challengesTimes)";
            awaitStore = "INSERT INTO lastExecution(playerUniqueId, lastQueried) VALUES (:plUniqueId, :timestamp) ON DUPLICATE KEY UPDATE lastQueried = :timestamp";
        } else {
            metadata = "CREATE TABLE IF NOT EXISTS cacheMetadata(" +
                    "dbVersion TEXT NOT NULL," +
                    "firstInit DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "cacheUniqueId TEXT NOT NULL)";

            islandLimitCount = "CREATE TABLE IF NOT EXISTS lastExecution(" +
                    "playerUniqueId VARCHAR(64) PRIMARY KEY NOT NULL," +
                    "lastQueried BIGINT NOT NULL)";

            islandRelations = "CREATE TABLE IF NOT EXISTS islandRelations(" +
                    "defaultIsland INT NOT NULL," +
                    "islandTeamName TEXT," +
                    "islandLeader VARCHAR(100)," +
                    "islandMembers TEXT DEFAULT ''," +
                    "FOREIGN KEY (defaultIsland) REFERENCES island(islandUniqueId) ON UPDATE CASCADE," +
                    "FOREIGN KEY (islandLeader) REFERENCES player(playerName) ON UPDATE CASCADE," +
                    "PRIMARY KEY (defaultIsland, islandLeader))";

            islandData = "CREATE TABLE IF NOT EXISTS islandData(" +
                    "dataId INT PRIMARY KEY," +
                    "biome INTEGER DEFAULT 0," +
                    "locked INTEGER DEFAULT 0," +
                    "protectionData TEXT DEFAULT ''," +
                    "levelHandicap INTEGER DEFAULT 0," +
                    "islandLevel INTEGER DEFAULT 0," +
                    "FOREIGN KEY (dataId) REFERENCES island(islandUniqueId) ON UPDATE CASCADE)";

            islandTable = "CREATE TABLE IF NOT EXISTS island(" +
                    "islandUniqueId INTEGER PRIMARY KEY NOT NULL," +
                    "islandId INTEGER NOT NULL," +
                    "gridPosition TEXT NOT NULL," +
                    "spawnPosition TEXT DEFAULT ''," +
                    "islandName TEXT DEFAULT ''," +
                    "gridSize INTEGER NOT NULL," +
                    "levelName TEXT NOT NULL," +
                    "playerName VARCHAR(100) NOT NULL," +
                    "FOREIGN KEY (levelName) REFERENCES worldList(worldName) ON UPDATE CASCADE)";

            playerChallenges = "CREATE TABLE IF NOT EXISTS challenges(" +
                    "player VARCHAR(100) PRIMARY KEY NOT NULL," +
                    "challengesList TEXT," +
                    "challengesTimes TEXT," +
                    "FOREIGN KEY (player) REFERENCES player(playerName) ON UPDATE CASCADE)";

            playerTable = "CREATE TABLE IF NOT EXISTS player(" +
                    "playerName VARCHAR(100)," +
                    "playerUUID VARCHAR(36)," +
                    "locale TEXT NOT NULL," +
                    "banList TEXT NOT NULL," +
                    "resetAttempts INTEGER NOT NULL," +
                    "lastLogin DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (playerName))";

            worldTable = "CREATE TABLE IF NOT EXISTS worldList(" +
                    "worldName TEXT PRIMARY KEY," +
                    "levelId INT NOT NULL)";

            saveWorldData = "INSERT OR IGNORE INTO worldList (worldName, levelId) VALUES (:levelName, :levelId)";
            insertPlayerData = "INSERT OR IGNORE INTO player(playerName, playerUUID, locale, banList, resetAttempts) VALUES (:playerName, :playerUUID, :locale, :banList, :resetLeft)";
            insertChallengeData = "INSERT OR IGNORE INTO challenges(player, challengesList, challengesTimes) VALUES (:playerName, :challengesList, :challengesTimes)";
            awaitStore = "INSERT OR REPLACE INTO lastExecution(playerUniqueId, lastQueried) VALUES (:plUniqueId, :timestamp)";
        }
    }
}
