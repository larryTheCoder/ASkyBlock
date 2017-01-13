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
package larryTheCoder.database;

import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import larryTheCoder.ASkyBlock;
import larryTheCoder.database.purger.IslandData;
import larryTheCoder.utils.Utils;
import larryTheCoder.database.helper.Database;
import larryTheCoder.database.purger.TeamData;

/**
 * The class of Database Supported Database is SQLite to-do: MySQL, homes
 *
 * @author larryTheCoder
 */
public class ASConnection {

    private final Database db;
    private final Connection connection;
    private final String prefix;
    private boolean closed = true;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ASConnection(final Database database, String prefix, boolean debug) throws SQLException, ClassNotFoundException {
        this.db = database;
        this.connection = database.openConnection();
        this.prefix = prefix;
        this.createTables();
    }

    /**
     * Create tables.
     *
     * @throws SQLException
     */
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
                //  CREATE TABLE IF NOT EXISTS plots
                //(id INTEGER PRIMARY KEY AUTOINCREMENT, level TEXT, X INTEGER, Z INTEGER, name TEXT,
                //owner TEXT, helpers TEXT, biome TEXT)
                stmt.addBatch("CREATE TABLE IF NOT EXISTS "
                        + "`island` ("
                        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "`islandId` INTEGER,"
                        + "`x` INT(11) NOT NULL,"
                        + "`y` INT(11) NOT NULL,"
                        + "`z` INT(11) NOT NULL,"
                        + "`owner` VARCHAR(45) NOT NULL,"
                        + "`name` VARCHAR(45) NOT NULL,"
                        + "`world` VARCHAR(45) NOT NULL,"
                        + "`biome` VARCHAR(45) NOT NULL,"
                        + "`team` VARCHAR(45) NOT NULL,"
                        + "`helpers` VARCHAR(45),"
                        + "`locked` INT(11) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS "
                        + "`worlds` ("
                        + "`world` VARCHAR(45))");
                // TODO Ranks
                stmt.addBatch("CREATE TABLE IF NOT EXISTS"
                        + "`teams` ("
                        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "`name` VARCHAR(45) NOT NULL,"
                        + "`leader` VARCHAR(45) NOT NULL,"
                        + "`teams` VARCHAR(45))");
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

    /**
     * Get the player island by Location
     *
     * @param levelName - World location
     * @param X - Location X
     * @param Z - Location Z
     * @return IslandDatabase
     */
    public IslandData getIslandLocation(String levelName, int X, int Z) {
        IslandData database = new IslandData(levelName, X, Z);
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE(`world` = ? AND `islandId` = ?)")) {
            rt.setString(1, levelName);
            rt.setInt(2, ASkyBlock.get().getIsland().generateIslandKey(X, Z));
            ResultSet stmt = rt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                database = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), getTeamMetaData(stmt.getString("owner")), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked"));
            }

        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #location #X" + X + "#Z" + Z);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return database;
    }

    public ArrayList<IslandData> getIslands(String owner) {
        ArrayList<IslandData> pd = new ArrayList<>();
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE `owner` = ?")) {
            rt.setString(1, owner);
            ResultSet stmt = rt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                pd.add(new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), getTeamMetaData(stmt.getString("owner")), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked")));
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #Player " + owner);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return pd;
    }

    public IslandData getIsland(String name) {
        return getIslandAndId(name, 1);
    }

    /**
     * Get the player island by name and its ID
     *
     * @param name - the player`s name
     * @param homes - the number of player island
     * @return IslandDatabase
     */
    public IslandData getIslandAndId(String name, int homes) {
        IslandData database = null;
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE `owner` = ? AND `id` = ?")) {
            rt.setString(1, name);
            rt.setInt(2, homes);
            ResultSet stmt = rt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                database = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), getTeamMetaData(stmt.getString("owner")), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked"));
            }
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return database;
    }

    /**
     * Delete an existing player island.
     *
     * @param pd - the player`s IslandData
     * @return IslandDatabase
     */
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

    public boolean deleteTeam(TeamData pd) {
        boolean result = false;
        try {
            PreparedStatement stmt;
            stmt = connection.prepareStatement("DELETE FROM `teams` WHERE`leader` = ?");
            stmt.setString(1, pd.leader);
            result = stmt.execute();
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public void tick() {
//        try (PreparedStatement kt = connection.prepareStatement("UPDATE `island` SET `team` = ?")) {
//            ResultSet stmt = kt.executeQuery();
//            if(stmt == null){
//                return;
//            }
//            while(stmt.next()) {
//                if (getTeamMetaDataByName(stmt.getString("team")) == null && !stmt.getString("team").isEmpty()) {
//                    // try Remove them from Database
//                    kt.setString(1, "");
//                    kt.executeUpdate();
//                }
//            }
//        } catch (SQLException ex) {
//            if (ASkyBlock.get().isDebug()) {
//                ex.printStackTrace();
//            }
//        }
    }

    /**
     * Get the player island by id
     *
     * @param id - id that generated by getIslandkey(int)
     * @return IslandDatabase
     */
    public IslandData getIslandById(int id) {
        IslandData database = null;
        try (PreparedStatement kt = connection.prepareStatement("SELECT * FROM `island` WHERE `islandId` = ?")) {
            kt.setInt(1, id);
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                database = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), getTeamMetaData(stmt.getString("owner")), stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked"));
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id " + id);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return database;
    }

    public TeamData getTeamMetaData(String leader) {
        TeamData pd = null;
        try (PreparedStatement kt = connection.prepareStatement("SELECT * FROM `teams` WHERE `id` = ? AND `leader` = ?")) {
            kt.setString(1, leader);
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                pd = new TeamData(stmt.getString("name"), stmt.getString("leader"), stmt.getString("members"));
            }
        } catch (SQLException ex) {

        }

        return pd;
    }

    public TeamData getTeamMetaDataByName(String name) {
        TeamData pd = null;
        try (PreparedStatement kt = connection.prepareStatement("SELECT * FROM `teams` WHERE `id` = ? AND `name` = ?")) {
            kt.setString(1, name);
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return null;
            }
            while (stmt.next()) {
                pd = new TeamData(stmt.getString("name"), stmt.getString("leader"), stmt.getString("members"));
            }
        } catch (SQLException ex) {

        }

        return pd;
    }

    /**
     * Close the database. Generally not recommended to be used by add-ons.
     */
    public void close() {
        try {
            this.closed = true;
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save player Island
     *
     * @param pd - IslandData that contains every actions
     * @return boolean - If it running correctly
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    public boolean saveIsland(IslandData pd) {
        boolean result = false;
        boolean result2 = false;
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO "
                + "`island`"
                + "(`id`, `islandId`, `x`, `y`, `z`, `owner`, `name`, `world`, `biome`, `team`,`locked`) VALUES"
                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            stmt.setInt(1, pd.id);
            stmt.setInt(2, pd.islandId);
            stmt.setInt(3, pd.X);
            stmt.setInt(4, pd.floor_y);
            stmt.setInt(5, pd.Z);
            stmt.setString(6, pd.owner);
            stmt.setString(7, pd.name);
            stmt.setString(8, pd.levelName);
            stmt.setString(9, pd.biome);
            stmt.setString(10, pd.team);
            stmt.setInt(11, pd.locked);
            stmt.executeUpdate();
            stmt.close();
            result = true;
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while saving SQLite: #id " + pd.id + "#Text 1 Cause of: " + ex.getErrorCode() + "AND " + ex.getSQLState());
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        if (pd.members == null) {
            result2 = true;
        } else {

        }
        return result && result2;
    }

    @SuppressWarnings("IncompatibleEquals")
    public ArrayList<String> getWorlds() {
        ArrayList<String> world = null;
        try (PreparedStatement kt = connection.prepareStatement("SELECT * FROM `worlds`")) {
            ResultSet stmt = kt.executeQuery();
            if (stmt == null) {
                return null;
            }
            world = new ArrayList<>();
            String weirdo = "";
            while (stmt.next()) {
                if (!world.equals("")) {
                    weirdo += " ";
                }
                weirdo += stmt.getString("world");
            }
            String[] array = weirdo.split(" ");
            world.addAll(Arrays.asList(array));
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return world;
    }

    public boolean saveWorlds(ArrayList<String> pd) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO `worlds` (`world`) VALUES (?);")) {
            String world = "";
            for (String arg : pd) {
                if (!world.equals("")) {
                    world += " ";
                }
                world += arg;
            }
            stmt.setString(1, world);
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Set the Player Island location
     *
     * @param pos - Position of the target location
     * @param id - Island id
     * @param owner
     * @return boolean - If it running correctly
     */
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
}
