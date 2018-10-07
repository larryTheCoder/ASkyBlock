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

import cn.nukkit.level.Level;

/**
 * @author larryTheCoder
 */
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
