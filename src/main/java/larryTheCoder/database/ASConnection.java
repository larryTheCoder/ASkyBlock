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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;
import larryTheCoder.Utils;
import larryTheCoder.database.helper.Database;

/**
 * The class of Database Supported Database is SQLite to-do: MySQL, homes
 *
 * @author larryTheCoder
 */
public final class ASConnection {

    private Database db;
    private Connection connection;
    private String prefix;
    private boolean closed;

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
        String[] tables = new String[]{"island", "worlds", "helpers"};
        DatabaseMetaData meta = this.connection.getMetaData();
        int create = 0;
        for (String s : tables) {
            try (ResultSet set = meta.getTables(null, null, this.prefix + s, new String[]{"TABLE"})) {
                if (!set.next()) {
                    create++;
                }
            }
        }
        if (create == 0) {
            return;
        }
        try (Statement stmt = this.connection.createStatement()) {
            stmt.addBatch("CREATE TABLE IF NOT EXISTS "
                    + "`island` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "`islandId` INT(11) NOT NULL,"
                    + "`x` INT(11) NOT NULL,"
                    + "`y` INT(11) NOT NULL,"
                    + "`z` INT(11) NOT NULL,"
                    + "`owner` VARCHAR(45) NOT NULL,"
                    + "`name` VARCHAR(45) NOT NULL,"
                    + "`world` VARCHAR(45) NOT NULL,"
                    + "`biome` VARCHAR(45) NOT NULL,"
                    + "`team` VARCHAR(45),"
                    + "`locked` INTEGER NOT NULL)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS "
                    + "`worlds` ("
                    + "`world` VARCHAR(45))");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS "
                    + "`helpers` ("
                    + "`islandId` INT(11) NOT NULL,"
                    + "`user` VARCHAR(45))");
            stmt.executeBatch();
            stmt.clearBatch();
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
        try (Statement kt = connection.createStatement()) {
            // Get helpers
            ResultSet resultSet = kt.executeQuery("SELECT `user`, `islandId` FROM `helpers`");
            HashMap<Integer, String> helpers = new HashMap<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("islandId");
                String helper = resultSet.getString("user");
                helpers.put(id, helper);
            }
            try (ResultSet stmt = kt.executeQuery("SELECT  `id`, `islandId`, `x`, `y`, `z`, `owner`, `name`, `world`, `biome`, `team`,`locked` FROM `island`")) {
                while (stmt.next()) {
                    int x = stmt.getInt("x");
                    int z = stmt.getInt("z");
                    if (ASkyBlock.get().getIsland().generateIslandKey(x, z) == stmt.getInt("islandId")) {
                        continue;
                    }
                    database = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked"));
                }
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
        try (PreparedStatement rt = connection.prepareStatement("SELECT * FROM `island` WHERE `owner` = ?")) {   // 
            rt.setString(1, owner);
            try (ResultSet stmt = rt.executeQuery()) {
                // Get helpers
                ResultSet resultSet = rt.executeQuery("SELECT `user`, `islandId` FROM `helpers`");
                HashMap<Integer, String> helpers = new HashMap<>();
                while (resultSet.next()) {
                    int id = resultSet.getInt("islandId");
                    String helper = resultSet.getString("user");
                    helpers.put(id, helper);
                }
                while (stmt.next()) {
                    pd.add(new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked")));
                }
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #Player " + owner);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return pd;
    }

    public IslandData getIsland(String name){
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
        try (Statement kt = connection.createStatement()) {
            try (ResultSet stmt = kt.executeQuery("SELECT  `id`,`islandId`, `x`, `y`,`z`, `owner`, `name`, `world`, `biome`, `team`,`locked` FROM `island`")) {
                // Get helpers
                ResultSet resultSet = kt.executeQuery("SELECT `user`, `islandId` FROM `helpers`");
                HashMap<Integer, String> helpers = new HashMap<>();
                while (resultSet.next()) {
                    int id = resultSet.getInt("islandId");
                    String helper = resultSet.getString("user");
                    helpers.put(id, helper);
                }
                
                while (stmt.next()) {
                    if (stmt.getString("owner").equals(name) && stmt.getInt("id") == homes) {
                        continue;
                    }
                    database = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked"));
                    // Stop the loop so there wont be an error while teleporting
                    break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ASConnection.class.getName()).log(Level.SEVERE, null, ex);
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
                stmt = connection.prepareStatement("DELETE FROM `island` WHERE `id` = ?");
                stmt.setInt(1, pd.id);
            } else {
                stmt = connection.prepareStatement("DELETE FROM `island` WHERE `world` = ? AND `x` = ? AND `z` = ?");
                stmt.setString(1, pd.levelName);
                stmt.setInt(2, pd.X);
                stmt.setInt(3, pd.Z);
            }
            result = stmt.execute();

        } catch (SQLException ex) {

        }
        return result != false;
    }

    /**
     * Get the player island by id
     *
     * @param id - id that generated by getIslandkey(int)
     * @return IslandDatabase
     */
    public IslandData getIslandById(int id) {
        IslandData database = null;

        try (Statement kt = connection.createStatement()) {
            try (ResultSet stmt = kt.executeQuery("SELECT  `id`, `islandId`, `x`, `y`, `z`, `owner`, `name`, `world`, `biome`, `team`,`locked` FROM `island`")) {
                // Get helpers
                ResultSet resultSet = kt.executeQuery("SELECT `user`, `islandId` FROM `helpers`");
                HashMap<Integer, String> helpers = new HashMap<>();
                while (resultSet.next()) {
                    int ide = resultSet.getInt("islandId");
                    String helper = resultSet.getString("user");
                    helpers.put(ide, helper);
                }
                while (stmt.next()) {
                    if (stmt.getInt("islandId") == id) {
                        continue;
                    }
                    database = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getInt("islandId"), stmt.getInt("locked"));
                    break;
                }
            } catch (SQLException ex) {
                Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id " + id);
                if (ASkyBlock.get().isDebug()) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id " + id);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return database;
    }

    /**
     * Close the database. Generally not recommended to be used by add-ons.
     */
    public void close(){
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
    public boolean saveIsland(IslandData pd) {
        boolean result = false;
        boolean result2 = false;
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO "
                + "`island`"
                + "(`id`,`islandId`, `x`, `y`, `z`, `owner`, `name`, `world`, `biome`, `team`,`locked`) VALUES"
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
            result = stmt.execute();
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while saving SQLite: #id ?");
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO `helpers` (`islandId`, `user`) VALUES (?, ?);")) {
            stmt.setInt(1, pd.islandId);
            for (String member : pd.members) {
                stmt.setString(2, member);
            }
            stmt.executeUpdate();
            result2 = stmt.execute();
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while saving SQLite: #id ?");
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return result && result2;
    }

    public ArrayList<String> getWorlds() {
        ArrayList<String> world = null;
        try (Statement kt = connection.createStatement()) {
            try (ResultSet stmt = kt.executeQuery("SELECT `world` FROM `worlds`")) {
                world = new ArrayList<>();
                while (stmt.next()) {
                    world.add(stmt.getString(1));
                }
            }
        } catch (SQLException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return world;
    }

    public boolean saveWorlds(ArrayList<String> pd) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO `worlds` (`world`) VALUES (?);")) {
            for (String pd1 : pd) {
                stmt.setString(1, pd1);
            }
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
     * @return boolean - If it running correctly
     */
    public boolean setPosition(Position pos, int id) {
        int x = pos.getFloorX();
        int fy = pos.getFloorY();
        int z = pos.getFloorZ();
        String level = pos.getLevel().getName();
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE `island` SET `x` = ?, `y` = ?, `z` = ?, `world` = ? WHERE `id` = ?")) {
            stmt.setInt(1, x);
            stmt.setInt(2, fy);
            stmt.setInt(3, z);
            stmt.setString(4, level);
            stmt.setInt(5, id);
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

    public boolean setOwner(String oldOwner, String newOwner, int id) {
        return false;
    }

}
