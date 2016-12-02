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
import java.util.logging.Level;
import java.util.logging.Logger;
import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;
import larryTheCoder.Utils;
import larryTheCoder.database.helper.Database;
import larryTheCoder.island.Island;

/**
 * The class of Database Supported Database is SQLite
 *
 * @author larryTheCoder
 */
public final class ASConnection {

    private Database db;
    private Connection connection;
    private String prefix;

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
        String[] tables = new String[]{"island"};
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
                    + "`id` INTEGER,"
                    + "`x` INT(11) NOT NULL,"
                    + "`y` INT(11) NOT NULL,"
                    + "`z` INT(11) NOT NULL,"
                    + "`owner` VARCHAR(45) NOT NULL,"
                    + "`name` VARCHAR(45) NOT NULL,"
                    + "`world` VARCHAR(45) NOT NULL,"
                    + "`helpers` VARCHAR(45),"
                    + "`biome` VARCHAR(45) NOT NULL,"
                    + "`team` VARCHAR(45),"
                    + "`locked` VARCHAR(45) NOT NULL)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS "
                    + "`worlds` ("
                    + "`world` VARCHAR(45))");
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
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public IslandData getIslandLocation(String levelName, int X, int Z) {
        IslandData db = new IslandData(levelName, X, Z);
        try (Statement kt = connection.createStatement()) {   // 
            try (ResultSet stmt = kt.executeQuery("SELECT `id`, `x`, `y`, `z`, `owner`, `name`, `world`, `helpers`, `biome`, `team`,`locked` FROM `island`")) {
                ArrayList<String> helpers = new ArrayList<>();
                while (stmt.next()) {
                    // Initialize helpers
                    String st = stmt.getString("helpers");
                    String[] helper = st.split(" ");
                    helpers.addAll(Arrays.asList(helper));
                    int x = stmt.getInt(2);
                    int z = stmt.getInt(3);
                    if (Island.generateIslandKey(x, z) == stmt.getInt("id")) {
                        continue;
                    }
                    db = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getString("locked"));
                }
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #location #X" + X + "#Z" + Z);
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return db;
    }

    /**
     * Get the player island by name
     *
     * @param name - the player`s name
     * @return IslandDatabase
     */
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public IslandData getIsland(String name) {
        IslandData db = null;
        try (Statement kt = connection.createStatement()) {
            Utils.ConsoleMsg("1");
            try (ResultSet stmt = kt.executeQuery("SELECT `id`, `x`, `y`,`z`, `owner`, `name`, `world`, `helpers`, `biome`, `team`,`locked` FROM `island`")) {
                ArrayList<String> helpers = new ArrayList<>();
                Utils.ConsoleMsg("2");
                while (stmt.next()) {
                    Utils.ConsoleMsg("3");
                    // Initialize helpers
                    String st = stmt.getString("helpers");
                    Utils.ConsoleMsg("H1");
                    String[] helper = st.split(" ");
                    Utils.ConsoleMsg("H2");
                    helpers.addAll(Arrays.asList(helper));
                    Utils.ConsoleMsg("4");
                    if (stmt.getString(4) == null ? name == null : stmt.getString(4).equals(name)) {
                        continue;
                    }
                    Utils.ConsoleMsg("5");
                    db = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getString("locked"));
                }
            } catch (SQLException ex) {
                Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id ");
                if (ASkyBlock.get().isDebug()) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #player " + name);
            Utils.ConsoleMsg(TextFormat.GREEN + "Dont worry about this it might check #player database");
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        return db;
    }

    /**
     * Get the player island by id
     *
     * @param id - id that generated by getIslandkey(int)
     * @return IslandDatabase
     */
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public IslandData getIslandById(int id) {
        IslandData db = null;

        try (Statement kt = connection.createStatement()) {
            try (ResultSet stmt = kt.executeQuery("SELECT `id`, `x`, `y`, `z`, `owner`, `name`, `world`, `helpers`, `biome`, `team`,`locked` FROM `island`")) {
                ArrayList<String> helpers = new ArrayList<>();
                while (stmt.next()) {
                    // Initialize helpers
                    String st = stmt.getString("helpers");
                    String[] helper = st.split(" ");
                    helpers.addAll(Arrays.asList(helper));
                    if (stmt.getInt(1) == id) {
                        continue;
                    }
                    db = new IslandData(stmt.getString("world"), stmt.getInt("x"), stmt.getInt("y"), stmt.getInt("z"), stmt.getString("name"), stmt.getString("owner"), stmt.getString("team"), helpers, stmt.getString("biome"), stmt.getInt("id"), stmt.getString("locked"));
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
        return db;
    }

    /**
     * Save player Island
     *
     * @param pd - IslandData that contains every actions
     * @return boolean - If it running correctly
     */
    public boolean saveIsland(IslandData pd) {
        Utils.ConsoleMsg("1");
        try (PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO "
                + "`island`"
                + "(`id`, `x`, `y`, `z`, `owner`, `name`, `world`, `helpers`, `biome`, `team`,`locked`) VALUES"
                + "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            Utils.ConsoleMsg("1");
            stmt.setInt(1, pd.id);
            Utils.ConsoleMsg("1");
            stmt.setInt(2, pd.X);
            Utils.ConsoleMsg("12");
            stmt.setInt(3, pd.floor_y);
            Utils.ConsoleMsg("3");
            stmt.setInt(4, pd.Z);
            Utils.ConsoleMsg("14");
            stmt.setString(5, pd.owner);
            Utils.ConsoleMsg("51");
            stmt.setString(6, pd.name);
            Utils.ConsoleMsg("16");
            stmt.setString(7, pd.levelName);
            Utils.ConsoleMsg("17");
            // TO-DO Helpers
            stmt.setString(8, "");

            Utils.ConsoleMsg("18");
            stmt.setString(9, pd.biome);
            Utils.ConsoleMsg("19");
            stmt.setString(10, pd.team);
            Utils.ConsoleMsg("28");
            stmt.setString(11, pd.locked);
            Utils.ConsoleMsg("38");
            stmt.executeUpdate();
            Utils.ConsoleMsg("DONE1");
            Utils.ConsoleMsg("DONE1");
            return true;
        } catch (SQLException ex) {
            Utils.ConsoleMsg(TextFormat.RED + "An error occured while fecthing SQLite: #id ?");
            ex.printStackTrace();
            ex.fillInStackTrace();
            ex.getNextException();
        }
        return true;
    }

    public ArrayList<String> getWorlds() {
        ArrayList<String> world = null;
        try (Statement kt = connection.createStatement()) {
            try (ResultSet stmt = kt.executeQuery("SELECT `world` FROM `worlds`")) {
                world = new ArrayList<>();
                while (stmt.next()) {
                    // Initialize helpers
                    String st = stmt.getString("helpers");
                    String[] helper = st.split(" ");
                    world.addAll(Arrays.asList(helper));
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
            Logger.getLogger(ASConnection.class.getName()).log(Level.SEVERE, null, ex);
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

    public boolean setOwner(String owner, int id) {
        return false;
    }

}
