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
package com.larryTheCoder.database;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.variables.AbstractDatabase;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Full sql database
 *
 * @author larryTheCoder
 */
public final class ASConnection {

    private final AbstractDatabase db;
    private final Connection con;
    private boolean closed = true;

    public ASConnection(AbstractDatabase database, boolean debug) throws SQLException, ClassNotFoundException, InterruptedException {
        this.db = database;
        this.con = database.openConnection();
        this.createTables(true);
    }

    public void createTables(boolean updateCheck) throws SQLException, ClassNotFoundException, InterruptedException {
        if (closed) {
            String[] tables = new String[]{"island", "worlds", "players"};
            DatabaseMetaData meta = this.con.getMetaData();
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
            // A lot of updates will comming
            try (Statement set = this.con.createStatement()) {
                //createdDate updatedDate votes
                set.addBatch("CREATE TABLE IF NOT EXISTS `island` (`id` INTEGER,"
                        + "`islandId` INTEGER NOT NULL,"
                        + "`x` INTEGER NOT NULL,"
                        + "`y` INTEGER NOT NULL,"
                        + "`z` INTEGER NOT NULL,"
                        + "`isSpawn` BOOLEAN NOT NULL,"
                        + "`createdDate` LONG NOT NULL,"
                        + "`psize` INTEGER NOT NULL,"
                        + "`owner` VARCHAR NOT NULL,"
                        + "`name` VARCHAR NOT NULL,"
                        + "`world` VARCHAR NOT NULL,"
                        + "`protection` VARCHAR NOT NULL,"
                        + "`biome` VARCHAR NOT NULL,"
                        + "`locked` INTEGER NOT NULL)");
                set.addBatch("CREATE TABLE IF NOT EXISTS `worlds` (`world` VARCHAR)");
                set.addBatch("CREATE TABLE IF NOT EXISTS `players` (`player` VARCHAR NOT NULL,"
                        + "`homes` INTEGER NOT NULL,"
                        + "`resetleft` INTEGER NOT NULL,"
                        + "`banlist` VARCHAR,"
                        + "`teamleader` VARCHAR,"
                        + "`teamislandlocation` VARCHAR,"
                        + "`inteam` BOOLEAN,"
                        + "`islandlvl` INTEGER,"
                        + "`members` VARCHAR,"
                        + "`challengelist` VARCHAR,"
                        + "`challengelisttimes` VARCHAR,"
                        + "`name` VARCHAR)");
                set.executeBatch();
                set.clearBatch();
            }
            if (updateCheck == false) {

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

    public boolean setSpawnPosition(Position pos) {
        int x = pos.getFloorX();
        int y = pos.getFloorY();
        int z = pos.getFloorZ();
        String level = pos.getLevel().getName();
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("UPDATE `island` SET `x` = ?, `y` = ?, `z` = ?, `world` = ? WHERE `isSpawn` = true");
            if (set.isClosed()) {
                return false;
            }
            while (set.first()) {
                set.updateInt(1, x);
                set.updateInt(2, y);
                set.updateInt(3, z);
                set.updateString(4, level);
                set.updateRow();
            }
            stmt.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    public IslandData getIslandLocation(String levelName, int X, int Z) {
        // create a variables
        IslandData database = new IslandData(levelName, X, Z, Settings.protectionrange);
        try (Statement stmt = con.createStatement()) {
            String l = levelName;
            int z = ASkyBlock.get().getIsland().generateIslandKey(X, Z);
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE(`world` = '" + l + "' AND `islandId` = '" + z + "')");
            if (set.isClosed()) {
                return database;
            }
            while (set.next()) {
                database = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
                break;
            }
            stmt.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return database;
    }

    public ArrayList<IslandData> getIslands(String owner) {
        // safe block
        ArrayList<IslandData> pd = new ArrayList<>();
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `owner` = '" + owner + "'");
            if (set.isClosed()) {
                return pd;
            }
            while (set.next()) {
                pd.add(new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn")));
            }
            stmt.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return pd;
    }

    public IslandData getIsland(String name, int homes) {
        // safe block
        IslandData pd = null;
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE(`owner` = '" + name + "' AND `id` = '" + homes + "')");
            if (set.isClosed()) {
                return pd;
            }
            pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
            stmt.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return pd;
    }

    public boolean deleteIsland(IslandData pd) {
        // deprecated block
        boolean result = false;
        try {
            PreparedStatement set;
            if (pd.id >= 0) {
                set = con.prepareStatement("DELETE FROM `island` WHERE(`id` = ? AND `owner` = ?)");
                set.setInt(1, pd.id);
                set.setString(2, pd.owner);
            } else {
                set = con.prepareStatement("DELETE FROM `island` WHERE(`world` = ? AND `x` = ? AND `z` = ?)");
                set.setString(1, pd.levelName);
                set.setInt(2, pd.X);
                set.setInt(3, pd.Z);
            }
            result = set.execute();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return result;
    }

    public IslandData getSpawn() {
        // safe block
        IslandData pd = null;
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `isSpawn` = true");
            if (set.isClosed()) {
                return pd;
            }
            while (set.next()) {
                pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
                break;
            }
            stmt.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return pd;
    }

    public IslandData getIslandById(int id) {
        // safe block
        IslandData pd = null;
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `islandId` = '" + id + "'");
            if (set.isClosed()) {
                return pd;
            }
            while (set.next()) {
                pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
                break;
            }
            stmt.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return pd;
    }

    public void close() {
        try {
            this.closed = true;
            this.con.close();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
    }

    public boolean createIsland(IslandData pd) {
        // safe batch
        try (PreparedStatement set = con.prepareStatement("INSERT INTO `island` (`id`, `islandId`, `x`, `y`, `z`, `isSpawn`, `psize`, `owner`, `name`, `world`, `biome`, `locked`, `protection`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            set.setInt(1, pd.id);
            set.setInt(2, pd.islandId);
            set.setInt(3, pd.X);
            set.setInt(4, pd.Y);
            set.setInt(5, pd.Z);
            set.setBoolean(6, pd.isSpawn());
            set.setInt(7, pd.getProtectionSize());
            set.setString(8, pd.owner);
            set.setString(9, pd.name);
            set.setString(10, pd.levelName);
            set.setString(11, pd.biome);
            set.setBoolean(12, pd.locked);
            set.setString(13, pd.getSettings());
            set.addBatch();

            set.executeBatch();
            set.close();
            return true;
        } catch (BatchUpdateException b) {
            JDBCUtilities.printBatchUpdateException(b);
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    public boolean saveIsland(IslandData pd) {
        // safe update
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("UPDATE `island` SET `name` = ?, `biome` = ?, `locked` = ?,`isSpawn` = ?, `protection` = ? WHERE(`id` = '" + pd.id + "' AND `owner` = '" + pd.owner + "')");
            if (set.isClosed()) {
                return false;
            }
            while (set.next()) {
                set.updateString(1, pd.name);
                set.updateString(2, pd.biome);
                set.updateBoolean(3, pd.locked);
                set.updateBoolean(4, pd.isSpawn());
                set.updateString(5, pd.getSettings());
                set.updateRow();
            }
            stmt.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    public ArrayList<String> getWorlds() {
        // safe block JDBC_360
        ArrayList<String> world = new ArrayList<>();
        try (Statement kt = con.createStatement()) {
            ResultSet set = kt.executeQuery("SELECT `world` FROM `worlds`");
            if (set.isClosed()) {
                return world;
            }
            while (set.next()) {
                world.add(set.getString("world"));
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return world;
    }

    public boolean saveWorlds(ArrayList<String> pd) {
        // safeblock
        try (PreparedStatement set = con.prepareStatement("INSERT INTO `worlds` (`world`) VALUES (?);")) {
            for (String pd2 : pd) {
                set.setString(1, pd2);
                set.addBatch();
                set.executeBatch();
            }
            set.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    public boolean setPosition(Position pos, int id, String owner) {
        // safe block
        int x = pos.getFloorX();
        int fy = pos.getFloorY();
        int z = pos.getFloorZ();
        String level = pos.getLevel().getName();
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("UPDATE `island` SET `x` = ?, `y` = ?, `z` = ?, `world` = ? WHERE(`id` = '" + id + "' AND `owner` = '" + owner + "')");
            if (set.isClosed()) {
                return false;
            }
            while (set.first()) {
                set.updateInt("x", x);
                set.updateInt(2, fy);
                set.updateInt(3, z);
                set.updateString(4, level);
                set.updateInt(5, id);
                set.updateString(6, owner);
                set.updateRow();
            }
            stmt.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    public PlayerData getPlayerData(String st) {
        PlayerData pd = null;
        try (Statement kt = con.createStatement()) {
            ResultSet set = kt.executeQuery("SELECT * FROM `players` WHERE `player` = '" + st + "'");
            if (set.isClosed()) {
                return pd;
            }
            pd = new PlayerData(
                    set.getString("player"),
                    set.getInt("homes"),
                    Utils.stringToArray(set.getString("members"), ", "),
                    (HashMap<String, Boolean>) Utils.stringToMap(set.getString("challengelist")),
                    (HashMap<String, Integer>) Utils.stringToMap(set.getString("challengelisttimes")),
                    set.getInt("islandlvl"),
                    set.getBoolean("inTeam"),
                    set.getString("teamLeader"),
                    set.getString("teamIslandLocation"),
                    set.getInt("resetleft"),
                    Utils.stringToArray(set.getString("banList"), ", "));
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return pd;
    }

    public PlayerData getPlayerData(Player pl) {
        return getPlayerData(pl.getName());
    }

    public boolean createPlayer(Player p) {
        try (PreparedStatement set = con.prepareStatement("INSERT INTO `players` (`player`, `homes`, `resetleft`, `banlist`, `teamleader`, `teamislandlocation`, `inteam` , `islandlvl`, `members`,`challengelist` ,`challengelisttimes` , `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            PlayerData pd = new PlayerData(p.getName(), 0, Settings.reset);
            set.setString(1, pd.playerName);
            set.setInt(2, pd.homes);
            set.setInt(3, pd.resetleft);
            set.setString(4, Utils.arrayToString(pd.banList));
            set.setString(5, pd.teamLeader);
            set.setString(6, pd.teamIslandLocation);
            set.setBoolean(7, pd.inTeam);
            set.setInt(8, pd.islandLevel);
            set.setString(9, Utils.arrayToString(pd.members));
            set.setString(10, Utils.hashToString(pd.challengeList));
            set.setString(11, Utils.hashToString(pd.challengeListTimes));
            set.setString(12, pd.name);
            set.addBatch();

            set.executeBatch();
            set.close();
            return true;
        } catch (BatchUpdateException b) {
            JDBCUtilities.printBatchUpdateException(b);
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    public boolean savePlayerData(PlayerData pd) {
        // safe block
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("UPDATE `players` SET `player` = ?, `homes` = ?, `resetleft` = ?, `banlist` = ?, `teamleader` = ?, `teamislandlocation` = ?, `inteam` = ?, `islandlvl` = ?, `members` = ?, `challengelist` = ?, `challengelisttimes` = ?, `name` = ? WHERE `player = '" + pd.playerName + "'");
            if (set.isClosed()) {
                return false;
            }
            while (set.next()) {
                set.updateString(1, pd.playerName);
                set.updateInt(2, pd.homes);
                set.updateInt(3, pd.resetleft);
                set.updateString(4, Utils.arrayToString(pd.banList));
                set.updateString(5, pd.teamLeader);
                set.updateString(6, pd.teamIslandLocation);
                set.updateBoolean(7, pd.inTeam);
                set.updateInt(8, pd.islandLevel);
                set.updateString(9, Utils.arrayToString(pd.members));
                set.updateString(10, Utils.hashToString(pd.challengeList));
                set.updateString(11, Utils.hashToString(pd.challengeListTimes));
                set.updateString(12, pd.name);
                set.updateRow();
            }
            stmt.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

}
