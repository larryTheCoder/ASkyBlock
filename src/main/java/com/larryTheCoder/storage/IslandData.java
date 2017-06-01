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
package com.larryTheCoder.storage;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Settings;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author larryTheCoder
 */
public class IslandData implements Cloneable {

    public int islandId;
    public int id;
    public String levelName;
    // Coordinates of the island area
    public int X = 0;
    public int Y = 0;
    public int Z = 0;
    // Island information
    public String name;
    public String owner;
    public String biome;
    public boolean locked;
    // Set if this island is a spawn island
    private boolean isSpawn = false;
    // Protection size
    private int protectionRange = 0; //Unaccessable

    private final HashMap<SettingsFlag, Boolean> igs = new HashMap<>();

    public Vector3 getCenter() {
        return new Vector3(X, Y, Z);
    }

    public boolean isSpawn() {
        return isSpawn;
    }

    private void serilizeIgs(String defaultvalue) {
        if(isSpawn){
            setSpawnDefaults();
        } else {
            setIgsDefaults();
        }
        String[] igsd = defaultvalue.split(" ");
        boolean value;
        for(int i = 0; i < SettingsFlag.values().length; i++){
            SettingsFlag[] set = SettingsFlag.values();
            value = !igsd[i].equalsIgnoreCase("0");
            igs.put(set[i], value);
        }
    }

    public void setSpawn(boolean b) {
        isSpawn = b;
    }

    /**
     * Island Guard Setting flags Covers island, spawn and system settings
     */
    public enum SettingsFlag {
        /**
         * Water is acid above sea level
         */
        ACID_DAMAGE,
        /**
         * Anvil use
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
         */
        BREAK_BLOCKS,
        /**
         * Can breed animals
         */
        BREEDING,
        /**
         * Can use brewing stand
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
         */
        CHEST,
        /**
         * Can eat and teleport with chorus fruit
         */
        CHORUS_FRUIT,
        /**
         * Can use the work bench
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

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public IslandData(String levelName, int X, int Z, int PSize) {
        this.X = X;
        this.Z = Z;
        this.levelName = levelName;
        this.protectionRange = PSize;
        // Island Guard Settings
        setIgsDefaults();
    }

    @SuppressWarnings({"AssignmentToMethodParameter", "OverridableMethodCallInConstructor"})
    public IslandData(String levelName, int X, int Y, int Z, int PSize, String name, String owner, String biome, int id, int islandId, boolean locked, String defaultvalue, boolean isSpawn) {
        if (biome.isEmpty()) {
            biome = "PLAINS";
        }
        this.levelName = levelName;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.protectionRange = PSize;
        this.name = name;
        this.owner = owner;
        this.biome = biome;
        this.id = id;
        this.islandId = islandId;
        this.locked = locked;
        this.serilizeIgs(defaultvalue);
        this.isSpawn = isSpawn;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    public int getMinProtectedZ() {
        return (Z - protectionRange / 2);
    }

    public int getMinProtectedX() {
        return (X - protectionRange / 2);
    }

    public int getProtectionSize() {
        return protectionRange;
    }

    public ArrayList<String> getMembers() {
        PlayerData pd = ASkyBlock.get().getDatabase().getPlayerData(this.owner);
        return pd.members;
    }

    /**
     * Checks if a location is within this island's protected area
     *
     * @param target
     * @return true if it is, false if not
     */
    public boolean onIsland(Location target) {
        Level level = Server.getInstance().getLevelByName(levelName);
        if (level != null) {
            // If the new nether is being used, islands exist in the nether too
            //plugin.getLogger().info("DEBUG: target x = " + target.getBlockX() + " target z = " + target.getBlockZ());
            //plugin.getLogger().info("DEBUG: min prot x = " + getMinProtectedX() + " min z = " + minProtectedZ);
            //plugin.getLogger().info("DEBUG: max x = " + (getMinProtectedX() + protectionRange) + " max z = " + (minProtectedZ + protectionRange));

            if (target.getLevel().equals(level)) {
                if (target.getFloorX() >= getMinProtectedX() && target.getFloorX() < (getMinProtectedX() + protectionRange)
                        && target.getFloorZ() >= getMinProtectedZ() && target.getFloorZ() < (getMinProtectedZ() + protectionRange)) {
                    return true;
                }
                /*
                if (target.getX() >= center.getBlockX() - protectionRange / 2 && target.getX() < center.getBlockX() + protectionRange / 2
                        && target.getZ() >= center.getBlockZ() - protectionRange / 2 && target.getZ() < center.getBlockZ() + protectionRange / 2) {

                    return true;
                }
                 */
            }
        }
        return false;
    }

    /**
     * Get the Island Guard flag status
     *
     * @param flag
     * @return true or false, or false if flag is not in the list
     */
    public boolean getIgsFlag(SettingsFlag flag) {
        //plugin.getLogger().info("DEBUG: asking for " + flag + " = " + igs.get(flag));
        if (this.igs.containsKey(flag)) {
            return igs.get(flag);
        }
        return false;
    }

    public HashMap getIgsValues() {
        return igs;
    }

    /**
     * Set the Island Guard flag
     *
     * @param flag
     * @param value
     */
    public void setIgsFlag(SettingsFlag flag, boolean value) {
        this.igs.put(flag, value);
    }

    /**
     * Resets the protection settings to their default as set in config.yml for
     * this island
     */
    public void setIgsDefaults() {
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
     * @return Serialized set of settings
     */
    public String getSettings() {
        String result = "";
        // Personal island protection settings - serialize enum into 1's and 0's representing the boolean values
        //plugin.getLogger().info("DEBUG: igs = " + igs.toString());
        try {
            for (SettingsFlag f : SettingsFlag.values()) {
                //plugin.getLogger().info("DEBUG: flag f = " + f);
                if (this.igs.containsKey(f)) {
                    //plugin.getLogger().info("DEBUG: contains key");
                    result += this.igs.get(f) ? "1" : "0";
                } else {
                    //plugin.getLogger().info("DEBUG: does not contain key");
                    result += "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }
        return result;
    }

    /**
     * Reset spawn protection settings to their default as set in config.yml for
     * this island
     */
    public void setSpawnDefaults() {
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
}
