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
import com.larryTheCoder.database.config.AbstractConfig;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main database provider that saves
 * Every saves all the data of the island.
 * This is the most stable type of database.
 *
 * @author larryTheCoder
 */
public final class SqlConnection extends Database {

    private final AbstractConfig db;
    private final ASkyBlock plugin;
    private Connection con;
    private boolean closed = true;

    public SqlConnection(ASkyBlock plugin, AbstractConfig database) throws SQLException, ClassNotFoundException {
        // Performance upgrade: Cache
        this.plugin = plugin;
        this.db = database;
        this.con = database.openConnection();
        this.verifyTable();
    }

    private void verifyTable() throws SQLException {
        String[] tables = new String[]{"island", "worlds", "players"};
        DatabaseMetaData meta = this.con.getMetaData();
        int create = 0;
        // Verify tables.
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
        // A lot of updates will coming
        try (Statement set = this.con.createStatement()) {
            //createdDate updatedDate votes
            set.addBatch("CREATE TABLE IF NOT EXISTS `island` (`id` INTEGER,"
                    + "`islandId` INTEGER NOT NULL,"
                    + "`x` INTEGER NOT NULL,"
                    + "`y` INTEGER NOT NULL,"
                    + "`z` INTEGER NOT NULL,"
                    + "`spawnX` INTEGER,"
                    + "`spawnY` INTEGER,"
                    + "`spawnZ` INTEGER,"
                    + "`isSpawn` BOOLEAN NOT NULL,"
                    + "`psize` INTEGER NOT NULL,"
                    + "`owner` VARCHAR NOT NULL,"
                    + "`name` VARCHAR NOT NULL,"
                    + "`world` VARCHAR NOT NULL,"
                    + "`protection` VARCHAR(780) NOT NULL,"
                    + "`biome` VARCHAR NOT NULL,"
                    + "`locked` INTEGER NOT NULL)");
            //+ "`active` INTEGER NOT NULL)");
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
                    + "`name` VARCHAR,"
                    + "`locale` VARCHAR NOT NULL)");
            set.executeBatch();
            set.clearBatch();
        }
        closed = false;
    }

    private boolean isValid() {
        try {
            if (con.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        try (PreparedStatement stmt = this.con.prepareStatement("SELECT 1")) {
            stmt.executeQuery();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private void reconnect() {
        try {
            close();
            closed = false;
            con = db.forceConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSpawnPosition(Position pos) {
        int x = pos.getFloorX();
        int y = pos.getFloorY();
        int z = pos.getFloorZ();
        try (PreparedStatement stmt = con.prepareStatement("UPDATE `island` SET `spawnX` = ?, `spawnY` = ?, `spawnZ` = ? WHERE `isSpawn` = '1'")) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, z);
            stmt.addBatch();
            stmt.executeBatch();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
    }

    @Override
    public IslandData getIslandLocation(String levelName, int X, int Z) {
        int id = plugin.getIsland().generateIslandKey(X, Z, levelName);
        IslandData database = new IslandData(levelName, X, Z, plugin.getSettings(levelName).getProtectionRange());
        if (enableFastCache) {
            // Get a list of island data on cache
            for (IslandData pd : islandCache) {
                if (pd.getIslandId() == id && pd.getLevelName().equalsIgnoreCase(levelName)) {
                    return pd;
                }
            }
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE(`world` = '" + levelName + "' AND `islandId` = '" + id + "')");
            if (set.isClosed()) {
                return database;
            }

            set.next();
            database = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
            if (enableFastCache) {
                islandCache.add(database);
            }

        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return database;
    }

    @Override
    public ArrayList<IslandData> getIslands(String owner) {
        ArrayList<IslandData> pd = new ArrayList<>();
        if (enableFastCache) {
            // get the data from cache
            islandCache.stream().filter(
                    (pd3) -> (pd3.getOwner().equalsIgnoreCase(owner)))
                    .forEachOrdered(pd::add);
            // Not empty: Data in list contains player islands
            if (!pd.isEmpty()) {
                return pd; // Return
            }
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `owner` = '" + owner + "'");
            if (set.isClosed()) {
                return pd;
            }
            while (set.next()) {
                pd.add(new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn")));
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        if (enableFastCache) {
            // Save the island into cache
            islandCache.addAll(pd);
        }
        return pd;
    }

    @Override
    public ArrayList<IslandData> getIslands(String owner, String levelName) {
        ArrayList<IslandData> pd = new ArrayList<>();
        if (enableFastCache) {
            // get the data from cache
            islandCache.stream().filter(
                    (pd3) -> (pd3.getOwner().equalsIgnoreCase(owner) && pd3.getLevelName().equalsIgnoreCase(levelName)))
                    .forEachOrdered(pd::add);
            // Not empty: Data in list contains player islands
            if (!pd.isEmpty()) {
                return pd; // Return
            }
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `owner` = '" + owner + "' AND `world` = '" + levelName + "'");
            if (set.isClosed()) {
                return pd;
            }
            while (set.next()) {
                pd.add(new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn")));
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        if (enableFastCache) {
            // Save the island into cache
            islandCache.addAll(pd);
        }
        return pd;
    }

    @Override
    public IslandData getIsland(String name, int homes) {
        // safe block
        IslandData pd = null;
        if (enableFastCache) {
            for (IslandData pda : islandCache) {
                if (pda.getOwner().equals(name) && pda.getId() == homes) {
                    return pda;
                }
            }
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE(`owner` = '" + name + "' AND `id` = '" + homes + "')");
            if (set.isClosed()) {
                return null;
            }
            pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        if (enableFastCache) {
            islandCache.add(pd);
        }
        return pd;
    }

    @Override
    public IslandData getIsland(String name, String homeName) {
        // safe block
        IslandData pd = null;
        if (enableFastCache) {
            for (IslandData pda : islandCache) {
                if (pda.getOwner().equals(name) && pda.getName().equalsIgnoreCase(homeName)) {
                    return pda;
                }
            }
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE(`owner` = '" + name + "' AND `name` = '" + homeName + "')");
            if (set.isClosed()) {
                return null;
            }
            pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        if (enableFastCache) {
            islandCache.add(pd);
        }
        return pd;
    }

    @Override
    public boolean deleteIsland(IslandData pd) {
        if (enableFastCache) {
            islandCache.remove(pd);
        }
        try (PreparedStatement set = con.prepareStatement("DELETE FROM `island` WHERE(`id` = ? AND `owner` = ?)")) {
            set.setInt(1, pd.getId());
            set.setString(2, pd.getOwner());

            set.execute();
            set.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    @Override
    public IslandData getSpawn() {
        // safe block
        IslandData pd = null;
        if (islandSpawn != null) {
            return islandSpawn;
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `isSpawn` = '1'");
            if (set.isClosed()) {
                return null;
            }
            set.next();
            pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        islandSpawn = pd;
        return pd;
    }

    @Override
    public IslandData getIslandById(int id) {
        // safe block
        IslandData pd = null;
        if (enableFastCache) {
            for (IslandData pde : islandCache) {
                if (pde.getIslandId() == id) {
                    return pde;
                }
            }
        }
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island` WHERE `islandId` = '" + id + "'");
            if (set.isClosed()) {
                return null;
            }
            set.next();
            pd = new IslandData(set.getString("world"), set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("spawnX"), set.getInt("spawnY"), set.getInt("spawnZ"), set.getInt("psize"), set.getString("name"), set.getString("owner"), set.getString("biome"), set.getInt("id"), set.getInt("islandId"), set.getBoolean("locked"), set.getString("protection"), set.getBoolean("isSpawn"));
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        if (enableFastCache) {
            islandCache.add(pd);
        }
        return pd;
    }

    @Override
    public void close() {
        Utils.send("&7Closing databases...");
        try {
            this.closed = true;
            this.con.close();
            // Clear all variables
            islandCache.clear();
            islandSpawn = null;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
    }

    @Override
    public boolean createIsland(IslandData pd) {
        try (PreparedStatement set = con.prepareStatement("INSERT INTO `island` (`id`, `islandId`, `x`, `y`, `z`, `isSpawn`, `psize`, `owner`, `name`, `world`, `biome`, `locked`, `protection`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            set.setInt(1, pd.getId());
            set.setInt(2, pd.getIslandId());
            set.setInt(3, pd.getCenter().getFloorX());
            set.setInt(4, pd.getCenter().getFloorY());
            set.setInt(5, pd.getCenter().getFloorZ());
            set.setBoolean(6, pd.isSpawn());
            set.setInt(7, pd.getProtectionSize());
            set.setString(8, pd.getOwner());
            set.setString(9, pd.getName());
            set.setString(10, pd.getLevelName());
            set.setString(11, pd.getBiome());
            set.setBoolean(12, pd.isLocked());
            set.setString(13, pd.getIgsSettings().getSettings());
            set.addBatch();
            set.executeBatch();
            set.close();
            if (enableFastCache) {
                islandCache.add(pd);
            }
            return true;
        } catch (BatchUpdateException b) {
            JDBCUtilities.printBatchUpdateException(b);
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    @Override
    public boolean saveIsland(IslandData pd) {
        if (enableFastCache) {
            for (IslandData pde : islandCache) {
                if (pde.getIslandId() == pd.getIslandId() && pde.getOwner().equalsIgnoreCase(pd.getOwner())) {
                    islandCache.remove(pde);
                    break;
                }
            }
        }
        try (PreparedStatement stmt = con.prepareStatement("UPDATE `island` SET `name` = ?, `biome` = ?, `locked` = ?,`isSpawn` = ?, `protection` = ?, `spawnX` = ?, `spawnY` = ?, `spawnZ` = ? WHERE(`id` = '" + pd.getId() + "' AND `owner` = '" + pd.getOwner() + "')")) {
            stmt.setString(1, pd.getName());
            stmt.setString(2, pd.getBiome());
            stmt.setBoolean(3, pd.isLocked());
            stmt.setBoolean(4, pd.isSpawn());
            stmt.setString(5, pd.getIgsSettings().getSettings());
            stmt.setInt(6, pd.homeX);
            stmt.setInt(7, pd.homeY);
            stmt.setInt(8, pd.homeZ);
            stmt.addBatch();
            stmt.executeBatch();
            stmt.close();
            if (enableFastCache) {
                islandCache.add(pd);
            }
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    @Override
    public ArrayList<String> getWorlds() {
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

    @Override
    public boolean saveWorlds(ArrayList<String> pd) {
        try (PreparedStatement set = con.prepareStatement("INSERT INTO `worlds` (`world`) VALUES (?);")) {
            ArrayList<String> second = getWorlds();
            for (String pd2 : pd) {
                if (!second.contains(pd2)) {
                    set.setString(1, pd2);
                    set.addBatch();
                    set.executeBatch();
                }
            }
            set.close();
            return true;
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return false;
    }

    @Override
    public List<String> getPlayersData() {
        List<String> playersData = new ArrayList<>();
        try (Statement kt = con.createStatement()) {
            ResultSet set = kt.executeQuery("SELECT * FROM `players` ");
            if (set.isClosed()) {
                return playersData;
            }
            while (set.next()) {
                playersData.add(set.getString("player"));
            }
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return playersData;
    }

    @Override
    public PlayerData getPlayerData(String st) {
        // TESTED SUCCESS
        PlayerData pd = null;
        try (Statement kt = con.createStatement()) {
            ResultSet set = kt.executeQuery("SELECT * FROM `players` WHERE `player` = '" + st + "'");
            if (set.isClosed()) {
                return null;
            }
            pd = new PlayerData(
                    set.getString("player"),
                    set.getInt("homes"),
                    Utils.stringToArray(set.getString("members"), ", "),
                    set.getString("challengelist"),
                    set.getString("challengelisttimes"),
                    set.getInt("islandlvl"),
                    set.getBoolean("inTeam"),
                    set.getString("teamLeader"),
                    set.getString("teamIslandLocation"),
                    set.getInt("resetleft"),
                    Utils.stringToArray(set.getString("banList"), ", "),
                    set.getString("locale"),
                    set.getString("name"));
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
        return pd;
    }

    @Override
    public void createPlayer(String p) {
        // TODO: Fix the casting issue on this
        try (PreparedStatement set = con.prepareStatement("INSERT INTO `players` ("
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
                + "`name`, "
                + "`locale`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            PlayerData pd = new PlayerData(p, 0, Settings.reset);
            set.setString(1, pd.getPlayerName());
            set.setInt(2, pd.getHomeNumber());
            set.setInt(3, pd.getPlayerReset());
            set.setString(4, Utils.arrayToString(pd.getBanList()));
            set.setString(5, pd.teamLeader);
            set.setString(6, pd.teamIslandLocation);
            set.setBoolean(7, pd.inTeam);
            set.setInt(8, pd.getIslandLevel());
            set.setString(9, Utils.arrayToString(pd.members));
            set.setString(10, pd.decodeChallengeList("cl"));
            set.setString(11, pd.decodeChallengeList("clt"));
            set.setString(12, pd.name);
            set.setString(13, pd.getLocale());
            set.addBatch();

            set.executeBatch();
        } catch (BatchUpdateException b) {
            JDBCUtilities.printBatchUpdateException(b);
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
    }

    @Override
    public void savePlayerData(PlayerData pd) {
        // TESTED SUCCESS
        try (PreparedStatement stmt = con.prepareStatement(
                "UPDATE `players` SET "
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
                        + "`name` = ?, "
                        + "`locale` = ? "
                        + "WHERE `player` = '" + pd.getPlayerName() + "'")) {
            stmt.setInt(1, pd.getHomeNumber());
            stmt.setInt(2, pd.getPlayerReset());
            stmt.setString(3, Utils.arrayToString(pd.getBanList()));
            stmt.setString(4, pd.teamLeader);
            stmt.setString(5, pd.teamIslandLocation);
            stmt.setBoolean(6, pd.inTeam);
            stmt.setInt(7, pd.getIslandLevel());
            stmt.setString(8, Utils.arrayToString(pd.members));
            stmt.setString(9, pd.decodeChallengeList("cl"));
            stmt.setString(10, pd.decodeChallengeList("clt"));
            stmt.setString(11, pd.name);
            stmt.setString(12, pd.getLocale());
            stmt.addBatch();
            stmt.executeBatch();
        } catch (SQLException ex) {
            JDBCUtilities.printSQLException(ex);
        }
    }
}
