/*
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
package com.larryTheCoder.cache.settings;

import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import com.larryTheCoder.cache.builder.WorldSettingsBuilder;
import com.larryTheCoder.utils.Utils;

/**
 * @author larryTheCoder
 */
public class WorldSettings {

    public Level level;
    public String permission;
    public int plotMax;
    public int plotSize;
    public boolean stopTime;
    public int seaLevel;
    public int plotRange;
    public boolean useDefaultChest;

    public WorldSettings(Level level) {
        this.level = level;

        // By using default parameters
        this.permission = "is.create";
        this.plotMax = 5;
        this.plotSize = 200;
        this.stopTime = false;
        this.seaLevel = 0;
        this.plotRange = 100;
        this.useDefaultChest = false;
    }

    public void verifyWorldSettings() {
        if (isStopTime()) {
            level.setTime(1600);
            level.stopTime();
        }

        if (plotRange % 2 != 0) {
            plotRange--;
            Utils.send("&cThe protection range must be even, using " + plotRange);
        }

        if (plotRange > plotSize) {
            Utils.send("&cThe protection range cannot be bigger then the island distance. Setting them to be half equal.");
            plotRange = plotSize / 2; // Avoiding players from CANNOT break their island
        }

        if (plotRange < 0) {
            plotRange = 0;
        }
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

    public String getLevelName() {
        return level.getName();
    }

    public int getProtectionRange() {
        return plotRange;
    }

    public int getMaximumIsland() {
        return plotMax;
    }

    public void saveConfig(Config cfg) {
        String levelName = level.getName();

        cfg.set(levelName + ".permission", permission);
        cfg.set(levelName + ".maxHome", 5);
        cfg.set(levelName + ".plotSize", plotSize);
        cfg.set(levelName + ".protectionRange", plotRange);
        cfg.set(levelName + ".stopTime", false);
        cfg.set(levelName + ".seaLevel", seaLevel);
        cfg.set(levelName + ".useDefaultChest", true);
        cfg.save();
    }

    public static WorldSettingsBuilder builder() {
        return new WorldSettingsBuilder();
    }
}
