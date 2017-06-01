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
package com.larryTheCoder.database;

import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Renew the old sql database
 *
 * @author larryTheCoder
 */
public class ConnectionUpdater {

    private final Connection con;
    private final HashMap<Integer, IndependantIsland> pd = new HashMap<>();

    public ConnectionUpdater(Connection cone) {
        this.con = cone;
    }

    public void updateRow() {
        int count = 0;
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island`");
            if (set.isClosed()) {
                return;
            }
            while (set.next()) {
                IndependantIsland ipd = new IndependantIsland();
                // try to check if the ROW exsits
                if (set.getRowId("id") == null) {
                    ipd.id = 1;
                }
                if (set.getRowId("isSpawn") == null) {
                    ipd.isSpawn = false;
                }
                if(set.getRowId("psize") == null){
                    ipd.protectionRange = Settings.protectionrange;
                }
                pd.put(count, ipd);
                count++;
            }
        } catch (SQLException ex) {
        }

    }

    private class IndependantIsland {

        public int islandId;
        public int id;
        public String levelName;
        // Coordinates of the island area
        public int X = 0;
        public int Y = 0;
        public int Z = 0;

        public String name;
        public String owner;
        public String biome;
        public boolean locked;
        // Set if this island is a spawn island
        private boolean isSpawn = false;
        // Protection size
        private int protectionRange = 0; //Unaccessable

        private final HashMap<IslandData.SettingsFlag, Boolean> igs = new HashMap<>();

        public IndependantIsland() {
            prepareStatement();
        }

        /**
         * Prepare the variables
         */
        private void prepareStatement() {

        }
    }
}
