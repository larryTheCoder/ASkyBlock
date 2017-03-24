/*
 * Copyright (C) 2017 larryTheCoder
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
package com.larryTheCoder.database.ormlite;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.intellectiualcrafters.TaskManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.database.ormlite.tables.IslandDataTable;
import com.larryTheCoder.database.ormlite.tables.WorldDataTable;
import com.larryTheCoder.database.ormlite.tables.PlayerDataTable;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.nukkit.dblib.DbLib;

/**
 *
 * @author larryTheCoder
 */
public class ORMLiteDatabase implements Database {

    private final ConnectionSource connection;
    private Dao<IslandDataTable, Object> islandDB;
    private Dao<WorldDataTable, Object> worldDB;
    private Dao<PlayerDataTable, Object> playerDB;
    private final String dbLocation;

    public ORMLiteDatabase(File data) {
        this(data, false);
    }

    public ORMLiteDatabase(File data, boolean mysql) {
        this.dbLocation = data.getAbsolutePath();
        File file = new File(this.dbLocation);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
                Utils.ConsoleMsg("&cFailed to connect to ORMLite");
            }
        }
        Config cfg = ASkyBlock.get().cfg;
        if (mysql) {
            connection = DbLib.getConnectionSource(DbLib.getMySqlUrl(cfg.getString("database.MySQL.host"), cfg.getInt("database.MySQL.port"), cfg.getString("database.MySQL.database")), cfg.getString("database.MySQL.username"), cfg.getString("database.MySQL.password"));
            Utils.ConsoleMsg(TextFormat.YELLOW + "Starting MySql-ORMLITE This will take a while...");
            TaskManager.runTaskLater(() -> {
                Utils.ConsoleMsg(TextFormat.YELLOW + "Connecting...");
            }, 20);

        } else {
            connection = DbLib.getConnectionSource("jdbc:sqlite:" + dbLocation, "", "");
            Utils.ConsoleMsg(TextFormat.YELLOW + "Starting Sqlite-ORMLITE This will take a while...");
        }
        try {
            Thread.sleep(Utils.secondsAsMillis(50));
        } catch (InterruptedException ex) {
        }
        if (connection == null) {
            Utils.ConsoleMsg("&cFailed to connect to " + (mysql ? "MySql" : "Sqlite"));
            return;
        }
        Utils.ConsoleMsg(TextFormat.GREEN + "Seccessfully connected into " + (mysql ? cfg.getString("database.MySQL.host") + ":" + cfg.getInt("database.MySQL.port") : "Sqlite-JDBC"));
        try {
            islandDB = DaoManager.createDao(connection, IslandDataTable.class);
            TableUtils.createTableIfNotExists(connection, IslandDataTable.class);
            worldDB = DaoManager.createDao(connection, WorldDataTable.class);
            TableUtils.createTableIfNotExists(connection, WorldDataTable.class);
            playerDB = DaoManager.createDao(connection, PlayerDataTable.class);
            TableUtils.createTableIfNotExists(connection, PlayerDataTable.class);
        } catch (SQLException ex) {
            Utils.ConsoleMsg("&cFailed to create table");
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public IslandData getIslandLocation(String levelName, int X, int Z) {
        List<IslandDataTable> records;
        Map<String, Object> ev = new HashMap<>();
        ev.put("world", levelName);
        ev.put("islandId", ASkyBlock.get().getIsland().generateIslandKey(X, Z));
        try {
            records = islandDB.queryForFieldValues(ev);
        } catch (SQLException ex) {
            return new IslandData(levelName, X, Z, Settings.protectionrange);
        }
        for (IslandDataTable island : records) {
            return island.toIsland();
        }
        return new IslandData(levelName, X, Z,Settings.protectionrange);
    }

    @Override
    public List<IslandData> getIslands(String owner) {
        List<IslandData> list = new ArrayList<>();
        List<IslandDataTable> records;
        try {
            records = islandDB.queryForEq("owner", owner);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return list;
        }
        records.stream().forEach((record) -> {
            list.add(record.toIsland());
        });
        return list;
    }

    @Override
    public IslandData getIsland(String name, int homes) {
        List<IslandDataTable> records;
        Map<String, Object> ev = new HashMap<>();
        ev.put("owner", name);
        ev.put("id", homes);
        try {
            records = islandDB.queryForFieldValues(ev);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return null;
        }
        for (IslandDataTable island : records) {
            return island.toIsland();
        }
        return null;
    }

    @Override
    public boolean deleteIsland(IslandData pd) {
        List<IslandDataTable> record;
        try {
            record = islandDB.queryForEq("owner", pd.owner);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
        IslandDataTable table = null;
        for (IslandDataTable rec : record) {
            table = rec;
        }
        if (table == null) {
            return false;
        }
        try {
            islandDB.delete(table);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public PlayerData getPlayerData(Player pl) {
        List<PlayerDataTable> records;
        try {
            records = playerDB.queryForEq("player", pl.getName());
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return null;
        }
        if (records == null) {
            return null;
        }
        for (PlayerDataTable recod : records) {
            return recod.toData();
        }
        return null;
    }

    @Override
    public boolean createPlayer(Player p) {
        PlayerData pd = new PlayerData(p.getName(), 0, Settings.reset);
        try {
            playerDB.create(new PlayerDataTable(pd));
            return true;
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean savePlayerData(PlayerData pd) {
        List<PlayerDataTable> record = null;
        try {
            // Check if player data exists
            record = playerDB.queryForEq("player", pd.playerName);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        if (record == null || record.isEmpty()) {
            // if not try to create a new Database
            try {
                playerDB.create(new PlayerDataTable(pd));
            } catch (SQLException ex) {
                if (ASkyBlock.get().isDebug()) {
                    ex.printStackTrace();
                }
            }
        }
        // The player has a table
        PlayerDataTable table = null;
        for (PlayerDataTable rec : record) {
            table = rec;
        }
        if (table == null) {
            return false;
        }
        table.save(pd);
        try {
            playerDB.update(table);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
        return false;
    }

    @Override
    public IslandData getIslandById(int id) {
        List<IslandDataTable> records;
        try {
            records = islandDB.queryForEq("islandId", id);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return null;
        }
        for (IslandDataTable record : records) {
            return record.toIsland();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (IOException ex) {
            Utils.ConsoleMsg("An error occurred while attempt to close database");
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean saveIsland(IslandData pd) {
        List<IslandDataTable> record = null;
        try {
            // Check if player data exists
            record = islandDB.queryForEq("owner", pd.owner);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        if (record == null) {
            // if not try to create a new Database
            try {
                islandDB.create(new IslandDataTable(pd));
            } catch (SQLException ex) {
                if (ASkyBlock.get().isDebug()) {
                    ex.printStackTrace();
                }
                return false;
            }
            return true;
        }
        // The player has an Island!
        IslandDataTable table = null;
        for (IslandDataTable rec : record) {
            table = rec;
        }
        if (table == null) {
            return false;
        }
        table.saveIslandData(pd);
        try {
            islandDB.update(table);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
        return false;
    }

    @Override
    public List<String> getWorlds() {
        List<WorldDataTable> records;
        try {
            records = worldDB.queryForAll();
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return null;
        }
        for (WorldDataTable record : records) {
            return record.toArray();
        }
        return null;
    }

    @Override
    public boolean saveWorlds(ArrayList<String> pd) {
        WorldDataTable records = new WorldDataTable(pd);
        try {
            worldDB.createOrUpdate(records);
            return true;
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean setPosition(Position pos, int id, String owner) {
        IslandData pd = getIsland(owner, id);
        IslandDataTable table = new IslandDataTable(pd, pos);
        try {
            islandDB.createOrUpdate(table);
            return true;
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}
