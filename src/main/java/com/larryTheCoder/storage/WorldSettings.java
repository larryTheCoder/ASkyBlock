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
 *
 */
package com.larryTheCoder.storage;

import cn.nukkit.level.Level;

public class WorldSettings {

    private final Level level;
    private final String permission;
    private final int plotMax;
    private final int plotSize;
    private final boolean stopTime;
    private final int seaLevel;
    private final int plotRange;

    public WorldSettings(Level level) {
        this.level = level;
        // By using default parameters
        this.permission = "is.create";
        this.plotMax = 5;
        this.plotSize = 200;
        this.stopTime = false;
        this.seaLevel = 0;
        this.plotRange = 100;
    }

    public WorldSettings(String permission, Level level, int plotMax, int plotSize, int plotRange, boolean stopTime, int seaLevel) {
        this.permission = permission;
        this.level = level;
        this.plotMax = plotMax;
        this.plotSize = plotSize;
        this.stopTime = stopTime;
        this.plotRange = plotRange;
        this.seaLevel = seaLevel;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public boolean isStopTime() {
        return stopTime;
    }

    public int getIslandDistance() {
        return plotSize;
    }

    public String getPermission() {
        return permission;
    }

    public Level getLevel() {
        return level;
    }

    public int getProtectionRange() {
        return plotRange;
    }

    public int getMaximumIsland() {
        return plotMax;
    }
}
