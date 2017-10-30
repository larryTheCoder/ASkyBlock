/*
 * Copyright (C) 2017 Adam Matthew
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

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Renew the old sql database
 *
 * @author Adam Matthew
 */
public class ConnectionUpdater {

    private final Connection con;
    private final ArrayList<IndependantIsland> pd = new ArrayList<>();
    private final ASkyBlock plugin;

    public ConnectionUpdater(ASkyBlock plugin, Connection cone) {
        this.plugin = plugin;
        this.con = cone;

        checkStatement();
    }

    /**
     *
     */
    public void checkStatement() {
        // TODO: BE DONE OF ALL OF AOF THESE WAR!UHS@H@Q(A)$@*#P@*
        try (Statement stmt = con.createStatement()) {
            ResultSet set = stmt.executeQuery("SELECT * FROM `island`");
            if (set.isClosed()) {
                return;
            }
            while (set.next()) {
                IndependantIsland ipd = new IndependantIsland();
                try {
                    ipd.id = set.getInt("id");
                } catch (SQLException ex) {
                    ipd.id = 1;
                }
                try {
                    ipd.isSpawn = set.getBoolean("isSpawn");
                } catch (SQLException ex) {
                    ipd.isSpawn = false;
                }
                try {
                    ipd.islandId = set.getInt("islandId");
                } catch (SQLException ex) {
                    ipd.islandId = plugin.getIsland().generateIslandKey(set.getInt("x"), set.getInt("z"));
                }
                try {
                    ipd.protectionRange = set.getInt("psize");
                } catch (SQLException ex) {
                    ipd.protectionRange = Settings.protectionrange;
                }
                try {
                    ipd.name = set.getString("name");
                } catch (SQLException ex) {
                    ipd.name = "My Island";
                }
                try {
                    ipd.biome = set.getString("biome");
                } catch (SQLException ex) {
                    ipd.biome = Settings.defaultBiome.getName();
                }
                try {
                    ipd.locked = set.getBoolean("locked");
                } catch (SQLException ex) {
                    ipd.locked = false;
                }
                try {
                    ipd.reloadSettings(set.getString("protection"));
                } catch (SQLException ex) {
                    ipd.setIgsDefaults();
                }
                pd.add(ipd);
            }
        } catch (SQLException ex) {
        }
    }

    private class IndependantIsland {

        private final HashMap<IslandData.SettingsFlag, Boolean> igs = new HashMap<>();
        // This is how new island data is.
        public int islandId;
        public int id;
        public String levelName;
        // Coordinates of the island area
        // This should have before 1.0.3 commit
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

        public IndependantIsland() {
            prepareStatement();
        }

        /**
         * Prepare the variables
         */
        private void prepareStatement() {

        }

        private void reloadSettings(String string) {
            if (isSpawn) {
                setSpawnDefaults();
            } else {
                setIgsDefaults();
            }
            boolean value;
            ArrayList<String> bool = Utils.stringToArray(string, ", ");
            for (int i = 0; i < IslandData.SettingsFlag.values().length; i++) {
                String pool = bool.get(i);
                value = Boolean.parseBoolean(pool);
                IslandData.SettingsFlag[] set = IslandData.SettingsFlag.values();
                igs.put(set[i], value);
            }

        }

        /**
         * Resets the protection settings to their default as set in config.yml
         * for this island
         */
        public void setIgsDefaults() {
            for (IslandData.SettingsFlag flag : IslandData.SettingsFlag.values()) {
                if (!Settings.defaultIslandSettings.containsKey(flag)) {
                    // Default default
                    this.igs.put(flag, false);
                } else {
                    if (Settings.defaultIslandSettings.get(flag) == null) {
                        //plugin.getLogger().info("DEBUG: null flag " + flag);
                        this.igs.put(flag, false);
                    } else {
                        this.igs.put(flag, Settings.defaultIslandSettings.get(flag));
                    }
                }
            }
        }

        /**
         * Reset spawn protection settings to their default as set in config.yml
         * for this island
         */
        public void setSpawnDefaults() {
            for (IslandData.SettingsFlag flag : IslandData.SettingsFlag.values()) {
                if (!Settings.defaultSpawnSettings.containsKey(flag)) {
                    // Default default
                    this.igs.put(flag, false);
                } else {
                    if (Settings.defaultSpawnSettings.get(flag) == null) {
                        this.igs.put(flag, false);
                    } else {
                        this.igs.put(flag, Settings.defaultSpawnSettings.get(flag));
                    }
                }
            }
        }
    }
}
