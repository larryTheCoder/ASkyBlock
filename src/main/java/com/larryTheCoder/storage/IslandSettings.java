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

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

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
        Utils.sendDebug("DEBUG: asking for " + flag + " = " + igs.get(flag));
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
    public HashMap getIgsValues() {
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

    private void serializeIds(String defaultValue) {
        if (pd.isSpawn()) {
            setSpawnDefaults();
        } else {
            setIgsDefaults();
        }
        boolean value;
        ArrayList<String> bool = Utils.stringToArray(defaultValue, ", ");
        for (int i = 0; i < SettingsFlag.values().length; i++) {
            String pool = bool.get(i);
            value = Boolean.parseBoolean(pool);
            SettingsFlag[] set = SettingsFlag.values();
            igs.put(set[i], value);
        }
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

    /**
     * @return Serialized set of settings
     */
    public String getSettings() {
        String result;
        // Personal island protection settings - serialize enum into 1's and 0's representing the boolean values
        ASkyBlock.get().getLogger().info("DEBUG: igs = " + igs.toString());
        try {
            ArrayList<Boolean> val = new ArrayList<>(igs.values());
            result = Utils.arrayToString(val);
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }
        return result;
    }

    public enum SettingsFlag {
        /**
         * Water is acid above sea level
         */
        ACID_DAMAGE,
        /**
         * Anvil use
         * [Added]
         */
        ANVIL,
        /**
         * Armor stand use
         */
        ARMOR_STAND,
        /**
         * Beacon use
         */
        BEACON,
        /**
         * Bed use
         */
        BED,
        /**
         * Can break blocks
         * [Added]
         */
        BREAK_BLOCKS,
        /**
         * Can breed animals
         */
        BREEDING,
        /**
         * Can use brewing stand
         * [Added]
         */
        BREWING,
        /**
         * Can empty or fill buckets
         */
        BUCKET,
        /**
         * Can collect lava
         */
        COLLECT_LAVA,
        /**
         * Can collect water
         */
        COLLECT_WATER,
        /**
         * Can open chests or hoppers or dispensers
         * [Added]
         */
        CHEST,
        /**
         * Can eat and teleport with chorus fruit
         */
        CHORUS_FRUIT,
        /**
         * Can use the work bench
         * [Added]
         */
        CRAFTING,
        /**
         * Allow creepers to hurt players (but not damage blocks)
         */
        CREEPER_PAIN,
        /**
         * Can trample crops
         */
        CROP_TRAMPLE,
        /**
         * Can open doors or trapdoors
         */
        DOOR,
        /**
         * Chicken eggs can be thrown
         */
        EGGS,
        /**
         * Can use the enchanting table
         * [Added]
         */
        ENCHANTING,
        /**
         * Can throw ender pearls
         */
        ENDER_PEARL,
        /**
         * Can toggle enter/exit names to island
         */
        ENTER_EXIT_MESSAGES,
        /**
         * Fire use/placement in general
         */
        FIRE,
        /**
         * Can extinguish fires by punching them
         */
        FIRE_EXTINGUISH,
        /**
         * Allow fire spread
         */
        FIRE_SPREAD,
        /**
         * Can use furnaces
         */
        FURNACE,
        /**
         * Can use gates
         */
        GATE,
        /**
         * Can open horse or other animal inventories, e.g. llama
         */
        HORSE_INVENTORY,
        /**
         * Can ride an animal
         */
        HORSE_RIDING,
        /**
         * Can hurt friendly mobs, e.g. cows
         */
        HURT_MOBS,
        /**
         * Can hurt monsters
         */
        HURT_MONSTERS,
        /**
         * Can leash or unleash animals
         */
        LEASH,
        /**
         * Can use buttons or levers
         */
        LEVER_BUTTON,
        /**
         * Animals, etc. can spawn
         */
        MILKING,
        /**
         * Can do PVP in the nether
         */
        MOB_SPAWN,
        /**
         * Monsters can spawn
         */
        MONSTER_SPAWN,
        /**
         * Can operate jukeboxes, note boxes etc.
         */
        MUSIC,
        /**
         * Can place blocks
         */
        NETHER_PVP,
        /**
         * Can interact with redstone items, like diodes
         */
        PLACE_BLOCKS,
        /**
         * Can go through portals
         */
        PORTAL,
        /**
         * Will activate pressure plates
         */
        PRESSURE_PLATE,
        /**
         * Can do PVP in the overworld
         */
        PVP,
        /**
         * Cows can be milked
         */
        REDSTONE,
        /**
         * Spawn eggs can be used
         */
        SPAWN_EGGS,
        /**
         * Can shear sheep
         */
        SHEARING,
        /**
         * Can trade with villagers
         */
        VILLAGER_TRADING,
        /**
         * Visitors can drop items
         */
        VISITOR_ITEM_DROP,
        /**
         * Visitors can pick up items
         */
        VISITOR_ITEM_PICKUP
    }
}
