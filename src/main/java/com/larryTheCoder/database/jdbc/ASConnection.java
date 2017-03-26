/*
 * Copyright (C) 2016 larryTheHarry 
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
package com.larryTheCoder.database.jdbc;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.database.jdbc.variables.AbstractDatabase;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class of Database Supported Database is SQLite to-do: MySQL, homes
 *
 * @author larryTheCoder
 */
public final class ASConnection implements Database {

    private final AbstractDatabase db;
    private final Connection connection;
    private boolean closed = true;

    public ASConnection(AbstractDatabase database, boolean debug) throws SQLException, ClassNotFoundException {
        this.db = database;
        this.connection = database.openConnection();
        this.createTables();
    }

    public void createTables() throws SQLException {
        if (closed) {
            String[] tables = new String[]{"island", "worlds", "teams"};
            DatabaseMetaData meta = this.connection.getMetaData();
            int create = 0;
            for (String s : tables) {
                try (ResultSet set = meta.getTables(null, null, s, new String[]{"TABLE"})) {
                    if (!set.next()) {
                        create++;
                    }
                }
            }
            if (create == 0) {
                return;
            }
            try (Statement stmt = this.connection.createStatement()) {
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `island` (`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "`islandId` INTEGER NOT NULL,"
                        + "`x` INTEGER NOT NULL,"
                        + "`y` INTEGER NOT NULL,"
                        + "`z` INTEGER NOT NULL,"
                        + "`psize` INTEGER NOT NULL,"
                        + "`owner` VARCHAR NOT NULL,"
                        + "`name` VARCHAR NOT NULL,"
                        + "`world` VARCHAR NOT NULL,"
                        + "`biome` VARCHAR NOT NULL,"
                        + "`locked` INTEGER NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `worlds` (`world` VARCHAR)");
                // TODO Ranks
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `player` (`player` VARCHAR NOT NULL,"
                        + "`homes` INTEGER NOT NULL,"
                        + "`resetleft` INTEGER NOT NULL,"
                        + "`banlist` VARCHAR,"
                        + "`teamleader` VARCHAR,"
                        + "`teamislandlocation` VARCHAR,"
                        + "`inteam` BOOLEAN,"
                        + "`islandlvl` INTEGER,"
                        + "`members` VARCHAR,"
                        + "`name` VARCHAR)");
                stmt.executeBatch();
                stmt.clearBatch();
            }
            closed = false;
        } else if (closed == true && db != null) {
            Utils.ConsoleMsg("§cThis a problem. The SQLManager is closed but the Database is not...");
            Utils.ConsoleMsg("§cYou might to stop server for this kind of problem to fix this error");
            Utils.ConsoleMsg("&cError Code: 0x3f");
        } else if (closed == false && db == null) {
            Utils.ConsoleMsg("§cThis a problem. The SQLManager is open but the Database is not...");
            Utils.ConsoleMsg("§cYou might to stop server for this kind of problem to fix this error");
            Utils.ConsoleMsg("&cError Code: 0x4f");
        }
    }

    @Override
    public IslandData getIslandLocation(String levelName, int X, int Z) {
        IslandData database = new IslandData(levelName, X, Z, Settings.protectionrange);
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE(`world` = ? AND `islandId` = ?)")) {
            rt.setString(1, levelName);
            rt.setInt(2, ASkyBlock.get().getIsland().generateIslandKey(X, Z));
            ResultSet stmt = rt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                return new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"),stmt.getInt("psize"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getBoolean("locked"));
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #location #X" + X + "#Z" + Z);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return database;
    }

    @Override
    public ArrayList<IslandData> getIslands(String owner) {
        ArrayList<IslandData> pd = new ArrayList<>();
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE `owner` = ?")) {
            rt.setString(1, owner);
            ResultSet stmt = rt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                pd.add(new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"),stmt.getInt("psize"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getBoolean("locked")));
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #Player " + owner);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return pd;
    }

    @Override
    public IslandData getIsland(String name, int homes) {
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE `owner` = ? AND `id` = ?")) {
            rt.setString(1, name);
            rt.setInt(2, homes);
            ResultSet stmt = rt.executeQuery();
            if (stmt == null) {
                return null;
            }
            return new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"),stmt.getInt("psize"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getBoolean("locked"));
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean deleteIsland(IslandData pd) {
        boolean result = false;
        try {
            PreparedStatement stmt;
            if (pd.id >= 0) {
                stmt = connection.prepareStatement("DELETE FROM `island` WHERE `id` = ? AND `owner` = ?");
                stmt.setInt(1, pd.id);
                stmt.setString(2, pd.owner);
            } else {
                stmt = connection.prepareStatement("DELETE FROM `island` WHERE `world` = ? AND `x` = ? AND `z` = ?");
                stmt.setString(1, pd.levelName);
                stmt.setInt(2, pd.X);
                stmt.setInt(3, pd.Z);
            }
            result = stmt.execute();
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public IslandData getIslandById(int id) {
        IslandData database = null;
        try (PreparedStatement kt = connection.prepareStatement("SELECT * FROM `island` WHERE `islandId` = ?")) {
            kt.setInt(1, id);
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                return new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"),stmt.getInt("psize"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getBoolean("locked"));
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id " + id);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return database;
    }

    @Override
    public void close() {
        try {
            this.closed = true;
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean saveIsland(IslandData pd) {

        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO `island` (`id`, `islandId`, `x`, `y`, `z`, `psize`, `owner`, `name`, `world`, `biome`, `locked`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            stmt.setInt(1, pd.id);
            stmt.setInt(2, pd.islandId);
            stmt.setInt(3, pd.X);
            stmt.setInt(4, pd.Y);
            stmt.setInt(5, pd.Z);
            stmt.setInt(6, pd.getProtectionSize());
            stmt.setString(7, pd.owner);
            stmt.setString(8, pd.name);
            stmt.setString(9, pd.levelName);
            stmt.setString(10, pd.biome);
            stmt.setBoolean(11, pd.locked);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while saving SQLite: #id " + pd.id + " Error Code: " + ex.getErrorCode() + " AND " + ex.getMessage());
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public ArrayList<String> getWorlds() {
        ArrayList<String> world = new ArrayList<>();
        try (PreparedStatement kt = connection.prepareStatement("SELECT `world` FROM `worlds`")) {
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return world;
            }
            while (stmt.next()) {
                world.add(stmt.getString("world"));
            }
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return world;
    }

    @Override
    public boolean saveWorlds(ArrayList<String> pd) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO `worlds` (`world`) VALUES (?);")) {
            for (String pd2 : pd) {
                stmt.setString(1, pd2);
                stmt.executeUpdate();
            }
            stmt.close();
            return true;
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean setPosition(Position pos, int id, String owner) {
        int x = pos.getFloorX();
        int fy = pos.getFloorY();
        int z = pos.getFloorZ();
        String level = pos.getLevel().getName();
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE `island` SET `x` = ?, `y` = ?, `z` = ?, `world` = ? WHERE `id` = ? AND `owner` = ?")) {
            stmt.setInt(1, x);
            stmt.setInt(2, fy);
            stmt.setInt(3, z);
            stmt.setString(4, level);
            stmt.setInt(5, id);
            stmt.setString(6, owner);
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id" + id);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public PlayerData getPlayerData(Player pl) {
        try (PreparedStatement kt = connection.prepareStatement("SELECT * FROM `players`")) {
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return null;
            }
            return new PlayerData(stmt.getString("player"), stmt.getInt("homes"), Utils.stringToArray(stmt.getString("members"), ", "), (HashMap<String, Boolean>) Utils.stringToMap(stmt.getString("challengelist")), (HashMap<String, Integer>) Utils.stringToMap(stmt.getString("challengelisttimes")), stmt.getInt("islandlvl"), stmt.getBoolean("inTeam"), stmt.getString("teamLeader"), stmt.getString("teamIslandLocation"), stmt.getInt("resetleft"), Utils.stringToArray(stmt.getString("banList"), ", "));
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean createPlayer(Player p) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO `players` (*) VALUES (*);")) {
            PlayerData pd = new PlayerData(p.getName(), 0, Settings.reset);
            stmt.setString(1, pd.playerName);
            stmt.setInt(2, pd.homes);
            stmt.setInt(3, pd.resetleft);
            stmt.setString(4, Utils.arrayToString(pd.banList));
            stmt.setString(5, pd.teamLeader);
            stmt.setString(6, pd.teamIslandLocation);
            stmt.setBoolean(7, pd.inTeam);
            stmt.setString(8, Utils.arrayToString(pd.members));
            stmt.setString(5, pd.name);
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
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO `players` (`player`, `homes`, `resetleft`, `banlist`, `teamleader`, `teamislandlocation`, `inteam` , `islandlvl`, `members`, `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            stmt.setString(1, pd.playerName);
            stmt.setInt(2, pd.homes);
            stmt.setInt(3, pd.resetleft);
            stmt.setString(4, Utils.arrayToString(pd.banList));
            stmt.setString(5, pd.teamLeader);
            stmt.setString(6, pd.teamIslandLocation);
            stmt.setBoolean(7, pd.inTeam);
            stmt.setInt(8, pd.islandLevel);
            stmt.setString(9, Utils.arrayToString(pd.members));
            stmt.setString(10, pd.name);
            return true;
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
