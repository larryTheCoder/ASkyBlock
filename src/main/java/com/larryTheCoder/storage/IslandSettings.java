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
package com.larryTheCoder.storage;

import com.larryTheCoder.utils.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Island Settings, generic settings to support
 * coop islands.
 * <p>
 * This class fix the truncated data on database,
 * which I cannot fix.
 * @author tastybento
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
