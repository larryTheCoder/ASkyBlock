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
import com.larryTheCoder.utils.Settings;

public class WorldSettings {

    private Level level;
    private String permission;
    private int plotSize;
    private boolean stopTime;
    private int islandHeight;
    private int seaLevel;

    public WorldSettings(Level level) {
        this.level = level;
        // By using default parameters
        this.permission = "is.create";
        this.plotSize = Settings.protectionrange;
        this.stopTime = Settings.stopTime;
        this.islandHeight = Settings.islandHieght;
        this.seaLevel = Settings.seaLevel;
    }

    public WorldSettings(String permission, Level level, int plotSize, boolean stopTime, int islandHeight, int seaLevel) {
        this.permission = permission;
        this.level = level;
        this.plotSize = plotSize;
        this.stopTime = stopTime;
        this.islandHeight = islandHeight;
        this.seaLevel = seaLevel;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public int getIslandHeight() {
        return islandHeight;
    }

    public boolean isStopTime() {
        return stopTime;
    }

    public int getPlotSize() {
        return plotSize;
    }

    public String getPermission() {
        return permission;
    }

    public Level getLevel() {
        return level;
    }
}
