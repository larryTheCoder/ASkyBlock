/*
 * Copyright (C) 2016-2018 Adam Matthew
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
package com.larryTheCoder.storage;

import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Island Settings, generic settings to support
 * coop islands.
 * <p>
 * This class fix the truncated data on database,
 * which I cannot fix.
 */
public class IslandSettings {

    private final HashMap<SettingsFlag, Boolean> igs = new HashMap<>();
    private IslandData pd;

    IslandSettings(IslandData pd, String defVal) {
        this.pd = pd;

        // Sometimes this shit could be empty
        if (defVal.isEmpty()) {
            this.setIgsDefaults();
        } else {
            this.serializeIds(defVal);
        }
    }

    public IslandSettings(IslandData pd) {
        this.pd = pd;
        this.setIgsDefaults();
    }

    /**
     * Get the Island Guard flag status
     *
     * @param flag The flag to be checked
     * @return true or false, or false if flag is not in the list
     */
    public boolean getIgsFlag(SettingsFlag flag) {
        //Utils.sendDebug"DEBUG: asking for " + flag + " = " + igs.get(flag));
        if (this.igs.containsKey(flag)) {
            return igs.get(flag);
        }
        return false;
    }

    /**
     * Get all the island settings values
     *
     * @return java.util.HashMap
     */
    public HashMap<SettingsFlag, Boolean> getIgsValues() {
        return igs;
    }

    /**
     * Set the Island Guard flag
     *
     * @param flag  Settings flag
     * @param value The value to be set on that flag
     */
    public void setIgsFlag(SettingsFlag flag, boolean value) {
        igs.put(flag, value);
    }

    /**
     * Resets the protection settings to their default as set in config.yml for
     * this island
     */
    private void setIgsDefaults() {
        for (SettingsFlag flag : SettingsFlag.values()) {
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
     * Reset spawn protection settings to their default as set in config.yml for
     * this island
     */
    private void setSpawnDefaults() {
        for (SettingsFlag flag : SettingsFlag.values()) {
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

    private void serializeIds(String defaultValue) {
        if (pd.isSpawn()) {
            setSpawnDefaults();
        } else {
            setIgsDefaults();
        }
        try {
            String[] at = defaultValue.split(", ");
            for (String string : at) {
                String[] at2 = string.split(":");
                ArrayList<String> list = new ArrayList<>(Arrays.asList(at2));

                boolean value = list.get(1).equalsIgnoreCase("1");
                igs.put(SettingsFlag.getFlag(list.get(0)), value);
            }
        } catch (Exception ignored) {
            //Utils.sendDebug"Protection settings is outdated.");
        }
    }

    /**
     * @return Serialized set of settings
     */
    public String getSettings() {
        StringBuilder buf = new StringBuilder();
        // Personal island protection settings - serialize enum into 1's and 0's representing the boolean values
        try {
            HashMap<SettingsFlag, Boolean> set = getIgsValues();
            // Highly efficient data store system
            set.forEach((key, value) -> {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(key.getId()).append(":").append(value ? "1" : "0");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buf.toString();
    }
}
