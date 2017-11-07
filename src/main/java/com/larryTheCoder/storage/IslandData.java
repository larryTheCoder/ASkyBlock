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
package com.larryTheCoder.storage;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.google.common.collect.Lists;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Adam Matthew
 */
public class IslandData implements Cloneable {

    private final HashMap<SettingsFlag, Boolean> igs = new HashMap<>();
    // Coordinates of the home spawn location
    public int homeX = 0;
    public int homeY = 0;
    public int homeZ = 0;
    private int islandId = 0;
    private int id = 0;
    private String levelName;
    private String owner;
    private String biome;
    private boolean locked;
    // Island information
    private String name;
    // Coordinates of the island area
    private int centerX = 0;
    private int centerY = 0;
    private int centerZ = 0;
    // Set if this island is a spawn island
    private boolean isSpawn = false;
    // Protection size
    private int protectionRange = 0;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public IslandData(String levelName, int X, int Z, int PSize) {
        this.centerX = X;
        this.centerZ = Z;
        this.levelName = levelName;
        this.protectionRange = PSize;
        // Island Guard Settings
        setIgsDefaults();
    }

    @SuppressWarnings({"AssignmentToMethodParameter", "OverridableMethodCallInConstructor"})
    public IslandData(String levelName, int X, int Y, int Z, int homeX, int homeY, int homeZ, int PSize, String name, String owner, String biome, int id, int islandId, boolean locked, String defaultvalue, boolean isSpawn) {
        if (biome.isEmpty()) {
            biome = "PLAINS";
        }
        this.levelName = levelName;
        this.centerX = X;
        this.centerY = Y;
        this.centerZ = Z;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
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

    public Vector3 getCenter() {
        return new Vector3(centerX, centerY, centerZ);
    }

    public void setCenter(int centerX, int centerY, int centerZ) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
    }

    public void setHomeLocation(Vector3 vector) {
        this.homeX = vector.getFloorX();
        this.homeY = vector.getFloorY();
        this.homeZ = vector.getFloorZ();
        ASkyBlock.get().getDatabase().setSpawnPosition(getHome());
    }

    public Location getHome() {
        return new Location(homeX, homeY, homeZ, Server.getInstance().getLevelByName(levelName));
    }

    public boolean isSpawn() {
        return isSpawn;
    }

    public void setSpawn(boolean b) {
        isSpawn = b;
    }

    private void serilizeIgs(String defaultvalue) {
        if (isSpawn) {
            setSpawnDefaults();
        } else {
            setIgsDefaults();
        }
        boolean value;
        ArrayList<String> bool = Utils.stringToArray(defaultvalue, ", ");
        for (int i = 0; i < SettingsFlag.values().length; i++) {
            String pool = bool.get(i);
            value = Boolean.parseBoolean(pool);
            SettingsFlag[] set = SettingsFlag.values();
            igs.put(set[i], value);
        }

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
        return (centerZ - protectionRange / 2);
    }

    public int getMinProtectedX() {
        return (centerX - protectionRange / 2);
    }

    public int getProtectionSize() {
        return protectionRange;
    }

    public ArrayList<String> getMembers() {
        PlayerData pd = ASkyBlock.get().getDatabase().getPlayerData(this.owner);
        if (pd == null) {
            return Lists.newArrayList(new String());
        }
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
        if (level != null && levelName != null) {
            // If the new nether is being used, islands exist in the nether too
            //plugin.getLogger().info("DEBUG: target x = " + target.getBlockX() + " target z = " + target.getBlockZ());
            //plugin.getLogger().info("DEBUG: min prot x = " + getMinProtectedX() + " min z = " + minProtectedZ);
            //plugin.getLogger().info("DEBUG: max x = " + (getMinProtectedX() + protectionRange) + " max z = " + (minProtectedZ + protectionRange));

            if (target.getLevel().getName().equalsIgnoreCase(levelName)) {
                if (target.getFloorX() >= getMinProtectedX()
                    && target.getFloorX() <= (getMinProtectedX() + protectionRange)
                    && target.getFloorZ() >= getMinProtectedZ()
                    && target.getFloorZ() <= (getMinProtectedZ() + protectionRange)) {
                    return true;
                }
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
            ArrayList<Boolean> FANTASTIC = new ArrayList<>();
            for (Boolean f : igs.values()) {
                FANTASTIC.add(f);
            }
            result = Utils.arrayToString(FANTASTIC);
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
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

    public boolean inIslandSpace(int x, int z) {
        return x >= getCenter().getFloorX() - protectionRange / 2 && x < getCenter().getFloorX() + protectionRange / 2 && z >= getCenter().getFloorZ() - protectionRange / 2
            && z < getCenter().getFloorZ() + protectionRange / 2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getOwner() {
        return owner;
    }

    public final void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IslandData)) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.islandId;
        hash = 61 * hash + this.id;
        hash = 61 * hash + Objects.hashCode(this.levelName);
        hash = 61 * hash + Objects.hashCode(this.owner);
        return hash;
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
}
