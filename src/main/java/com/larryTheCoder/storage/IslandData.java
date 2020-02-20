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

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.larryTheCoder.ASkyBlock;

import java.util.Objects;

/**
 * @author larryTheCoder
 */
public class IslandData implements Cloneable {

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
    private int centerX;
    private int centerY;
    private int centerZ;
    // Set if this island is a spawn island
    private boolean isSpawn = false;
    // Protection size
    private int protectionRange;
    // IslandSettings
    private IslandSettings settings;
    private int levelHandicap;
    private int deaths;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public IslandData(String levelName, int X, int Z, int PSize) {
        this.centerX = X;
        this.centerY = 0;
        this.centerZ = Z;
        this.levelName = levelName;
        this.protectionRange = PSize;
        // Island Guard Settings
        this.settings = new IslandSettings(this);
    }

    @SuppressWarnings({"AssignmentToMethodParameter", "OverridableMethodCallInConstructor"})
    public IslandData(String levelName, int X, int Y, int Z, int homeX, int homeY, int homeZ, int PSize, String name, String owner, String biome, int id, int islandId, boolean locked, String defaultvalue, boolean isSpawn, int levelHandicap, int deaths) {
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
        this.settings = new IslandSettings(this, defaultvalue);
        this.isSpawn = isSpawn;
        this.levelHandicap = levelHandicap;
        this.deaths = deaths;
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
    }

    public Location getHome() {
        return new Location(homeX, homeY, homeZ, Server.getInstance().getLevelByName(levelName));
    }

    /**
     * Get generic ID for this island
     * every islands generate own unique
     * ID
     *
     * @return integer, double
     */
    public int getId() {
        return id;
    }

    /**
     * Do not use this in server production
     * Unless you want to mess up the plugin
     *
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * The ID count of the island for player
     * this counts how much island player had
     *
     * @return int
     */
    public int getIslandId() {
        return islandId;
    }

    /**
     * Set the player island ID
     * Please do not use this in any production
     * server, this method is mean to be final only
     *
     * @param islandId int
     */
    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    /**
     * Get the IslandSettings for this island
     *
     * @return IslandSettings
     */
    public IslandSettings getIgsSettings() {
        return settings;
    }

    /**
     * Return if the island is the spawn
     *
     * @return bool
     */
    public boolean isSpawn() {
        return isSpawn;
    }

    /**
     * Set the safe spawn for this island
     * Note: This is not mean setting the island binding
     * location
     *
     * @param vec boolean
     */
    public void setSpawn(boolean vec) {
        isSpawn = vec;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    /**
     * Get minimum protected for value z
     *
     * @return int
     */
    public int getMinProtectedZ() {
        return (centerZ - protectionRange / 2);
    }

    /**
     * Get minimum protected for value x
     *
     * @return int
     */
    public int getMinProtectedX() {
        return (centerX - protectionRange / 2);
    }

    /**
     * Get protection size that used for this island
     *
     * @return int
     */
    public int getProtectionSize() {
        return protectionRange;
    }

    /**
     * Checks if a location is within this island's protected area
     *
     * @param target The location to be checked
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
                return target.getFloorX() >= getMinProtectedX()
                        && target.getFloorX() <= (getMinProtectedX() + protectionRange)
                        && target.getFloorZ() >= getMinProtectedZ()
                        && target.getFloorZ() <= (getMinProtectedZ() + protectionRange);
            }
        }
        return false;
    }

    /**
     * Check either the location is inside the island
     * protection range
     *
     * @param x x-range offset
     * @param z z-range offset
     * @return true if the location is inside the protection range
     */
    public boolean inIslandSpace(int x, int z) {
        return x >= getCenter().getFloorX() - protectionRange / 2
                && x < getCenter().getFloorX() + protectionRange / 2
                && z >= getCenter().getFloorZ() - protectionRange / 2
                && z < getCenter().getFloorZ() + protectionRange / 2;
    }

    /**
     * Get the name for this island
     *
     * @return string
     */
    public String getName() {
        return name;
    }

    /**
     * Set own name for this island
     * Player choice, do not duplicate
     *
     * @param name string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This features is not being used until
     * we find out how to use this
     *
     * @return String
     */
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
    public String toString() {
        return "IslandData(x=" + centerX + ", y=" + centerY + ", z= " + centerZ + ")";
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

    public int getLevelHandicap() {
        return levelHandicap;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public void saveData() {
        ASkyBlock.get().getDatabase().saveIsland(this);
    }

}
