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

import cn.nukkit.level.Location;
import larryTheCoder.Providers;
import larryTheCoder.island.IslandData;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sqlite.SQLite;

/**
 * @author larryTheCoder
 */
public class DatabaseProvider implements Database {

    private boolean enabled;
    private Sql2o db;

    public DatabaseProvider() {
        enabled = false;
        db = Providers.getSql2o();
        try (Connection con = db.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
            con.createQuery("CREATE TABLE IF NOT EXISTS islands (id INTEGER PRIMARY KEY AUTOINCREMENT, level TEXT, X INTEGER, Z INTEGER, name TEXT,owner TEXT, helpers TEXT, biome TEXT)").executeUpdate();               
        }
        enabled = true;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }

    @Override
    public boolean saveIsland(IslandData pd) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public IslandData getIsland(String pd) {
        try(Connection con = db.open()){
           con.createQuery("SELECT id, name, owner, helpers, biome FROM islands WHERE level = :level");
        }
        return null;
    }

    @Override
    public IslandData getIslandByOwner(String owner, String levelName) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Location teleportToIsland(String owner, String levelName) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    @Override
    public void close() {
        //ZZZZzzz...
    }
}