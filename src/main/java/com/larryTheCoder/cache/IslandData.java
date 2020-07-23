/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
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
package com.larryTheCoder.cache;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import com.google.common.base.Preconditions;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.settings.IslandSettings;
import com.larryTheCoder.database.QueryInfo;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.sql2o.data.Row;

import java.util.Objects;

/**
 * @author larryTheCoder
 */
public class IslandData implements Cloneable {

    @Setter
    private int islandUniquePlotId = 0;
    @Setter
    private int homeCountId = 0;

    // Coordinates of the home spawn location
    @Getter
    private Vector3 homeCoordinates = new Vector3();
    private Vector2 gridCoordinates = new Vector2();

    // Plot metadata
    private boolean isLocked = false;

    // Island information
    private String levelName = "";
    private String plotOwner = "";
    private String plotBiome = "";
    private String islandName = "";

    // Protection size
    @Setter
    private int protectionRange = 0;

    @Getter @Setter
    private int levelHandicap = 0;
    @Getter @Setter
    private int islandLevel = 0;

    // CoopDatabase
    @Getter
    private CoopData coopData = null;

    // IslandSettings
    private IslandSettings settings = new IslandSettings(this);

    public IslandData() {
    }

    public IslandData(String levelName, int xCoord, int zCoord, int plotSize) {
        this.gridCoordinates = new Vector2(xCoord, zCoord);
        this.levelName = levelName;
        this.protectionRange = plotSize;

        this.settings = new IslandSettings(this);
    }

    private IslandData(String levelName, String plotOwner, String islandName, Vector2 gridPos, Vector3 spawnPos, int plotSize, int homeId, int islandUniquePlotId) {
        this.levelName = levelName;
        this.plotOwner = plotOwner;
        this.homeCoordinates = spawnPos;
        this.gridCoordinates = gridPos;
        this.protectionRange = plotSize;
        this.islandName = islandName;

        // The most crucial part of this plot
        this.homeCountId = homeId;
        this.islandUniquePlotId = islandUniquePlotId;
    }

    private IslandData(Row islandObj, Row dataObj) {
        this.levelName = islandObj.getString("levelName");
        this.plotOwner = islandObj.getString("playerName");
        this.gridCoordinates = Utils.unpairVector2(islandObj.getString("gridPosition"));
        this.homeCoordinates = Utils.unpairVector3(islandObj.getString("spawnPosition"));
        this.protectionRange = islandObj.getInteger("gridSize");
        this.islandName = islandObj.getString("islandName");

        // The most crucial part of this plot
        this.homeCountId = islandObj.getInteger("islandId");
        this.islandUniquePlotId = islandObj.getInteger("islandUniqueId");

        this.plotBiome = dataObj.getString("biome");
        this.isLocked = dataObj.getInteger("locked") == 1;
        this.levelHandicap = dataObj.getInteger("levelHandicap");
        this.islandLevel = dataObj.getInteger("islandLevel");
        this.settings = new IslandSettings(dataObj.getString("protectionData"));
    }

    static IslandData fromRows(Row row) {
        return new IslandData(
                row.getString("levelName"),
                row.getString("playerName"),
                row.getString("islandName"),
                Utils.unpairVector2(row.getString("gridPosition")),
                Utils.unpairVector3(row.getString("spawnPosition")),
                row.getInteger("gridSize"),
                row.getInteger("islandId"),
                row.getInteger("islandUniqueId"));
    }

    static IslandData fromRows(Row islandObject, Row dataObject) {
        return new IslandData(islandObject, dataObject);
    }

    void loadRelationData(Row relationData) {
        this.coopData = new CoopData(relationData);
    }

    /**
     * Get a valid home coordinates for this island.
     * This will return a safe-point for players to teleport into.
     *
     * @return {@link Location location} of the safe-point position.
     */
    public Location getHome() {
        return Location.fromObject(homeCoordinates, Server.getInstance().getLevelByName(levelName));
    }

    /**
     * This will return a valid coordinates for the center of the island.
     * This were used to plot a grid to islands so the code can make sense of the range
     * used by other islands.
     *
     * @return {@link Vector2 Vector} of 2-point grid coordinates
     */
    public Vector2 getCenter() {
        return gridCoordinates;
    }

    public void setCenter(Vector2 gridCoordinates) {
        Preconditions.checkState(gridCoordinates != null, "Grid coordinates cannot be null!");

        this.gridCoordinates = gridCoordinates;
    }

    public void setHomeLocation(Vector3 vector) {
        this.homeCoordinates = vector.clone();

        saveIslandData();
    }

    /**
     * Get a number of plots created by this player for this plot.
     * This is the plot number created by the player.
     *
     * @return {@code int} value, the house value.
     */
    public int getHomeCountId() {
        return homeCountId;
    }

    /**
     * The ID count of the island for player
     * this counts how much island player had
     *
     * @return int
     */
    public int getIslandUniquePlotId() {
        return islandUniquePlotId;
    }

    /**
     * Get the IslandSettings for this island
     *
     * @return IslandSettings
     */
    public IslandSettings getIgsSettings() {
        return settings;
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
        return (gridCoordinates.getFloorY() - (protectionRange / 2));
    }

    /**
     * Get minimum protected for value x
     *
     * @return int
     */
    public int getMinProtectedX() {
        return (gridCoordinates.getFloorX() - (protectionRange / 2));
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
        if (level == null || levelName == null) return false;

        return target.getLevel().getName().equalsIgnoreCase(levelName)
                && target.getFloorX() >= getMinProtectedX()
                && target.getFloorX() <= (getMinProtectedX() + protectionRange)
                && target.getFloorZ() >= getMinProtectedZ()
                && target.getFloorZ() <= (getMinProtectedZ() + protectionRange);
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
                && z >= getCenter().getFloorY() - protectionRange / 2
                && z < getCenter().getFloorY() + protectionRange / 2;
    }

    /**
     * Get the name for this island
     *
     * @return string
     */
    public String getIslandName() {
        return islandName;
    }

    /**
     * Set own name for this island
     * Player choice, do not duplicate
     *
     * @param islandName string
     */
    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    /**
     * This features is not being used until
     * we find out how to use this
     *
     * @return String
     */
    public String getPlotBiome() {
        return plotBiome;
    }

    public void setPlotBiome(String plotBiome) {
        this.plotBiome = plotBiome;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public String getPlotOwner() {
        return plotOwner;
    }

    public final void setPlotOwner(String plotOwner) {
        this.plotOwner = plotOwner;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    @Override
    public String toString() {
        return "IslandData(" +
                "homeId=" + homeCountId + ", " +
                "islandUId=" + islandUniquePlotId + ", " +
                "x=" + gridCoordinates.getFloorX() + ", " +
                "z=" + gridCoordinates.getFloorY() + ", " +
                "plotOwner=" + plotOwner + ", " +
                "gridSize=" + getProtectionSize() + ", " +
                "levelName=" + levelName + ")";
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
        hash = 61 * hash + this.islandUniquePlotId;
        hash = 61 * hash + this.homeCountId;
        hash = 61 * hash + Objects.hashCode(this.levelName);
        hash = 61 * hash + Objects.hashCode(this.plotOwner);
        return hash;
    }

    /**
     * Save island data asynchronously
     */
    public void saveIslandData() {
        ASkyBlock.get().getDatabase().processBulkUpdate(new QueryInfo("UPDATE island SET islandId = :islandId, gridPosition = :gridPos, spawnPosition = :spawnPos, gridSize = :gridSize, levelName = :levelName, playerName = :plotOwner, islandName = :islandName WHERE islandUniqueId = :islandUniqueId")
                        .addParameter("islandId", homeCountId)
                        .addParameter("islandUniqueId", islandUniquePlotId)
                        .addParameter("gridPos", Utils.getVector2Pair(gridCoordinates))
                        .addParameter("spawnPos", Utils.getVector3Pair(homeCoordinates))
                        .addParameter("gridSize", protectionRange)
                        .addParameter("levelName", levelName)
                        .addParameter("plotOwner", plotOwner)
                        .addParameter("islandName", islandName),
                new QueryInfo("UPDATE islandData SET biome = :plotBiome, locked = :isLocked, protectionData = :protectionData, levelHandicap = :levelHandicap, islandLevel = :islandLevel WHERE dataId = :islandUniqueId")
                        .addParameter("islandUniqueId", islandUniquePlotId)
                        .addParameter("plotBiome", plotBiome)
                        .addParameter("isLocked", isLocked ? 1 : 0)
                        .addParameter("protectionData", settings.getSettings())
                        .addParameter("levelHandicap", levelHandicap)
                        .addParameter("islandLevel", islandLevel));
    }
}
