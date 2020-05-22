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
package com.larryTheCoder.cache.settings;

import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import com.larryTheCoder.cache.builder.WorldSettingsBuilder;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;

/**
 * @author larryTheCoder
 */
public class WorldSettings {

    @Getter
    private int levelId;

    @Getter
    private Level level;
    @Getter
    private String permission = "is.create";
    @Getter
    private int maximumIsland = 5;
    @Getter
    private int islandDistance = 200;
    @Getter
    private int protectionRange = 100;
    @Getter
    private boolean stopTime = false;
    @Getter
    private int seaLevel = 0;
    @Getter
    private String[] signConfig = new String[]{"&aWelcome to", "&e[player]'s", "&aIsland! Enjoy.", ""};
    @Getter
    private boolean useDefaultChest = false;

    public WorldSettings(Level level) {
        this.level = level;

        this.levelId = Utils.generateLevelId();
    }

    public WorldSettings(WorldSettingsBuilder builder) {
        this.level = builder.getLevel();

        this.permission = builder.getPermission();
        this.maximumIsland = builder.getPlotMax();
        this.islandDistance = builder.getPlotSize();
        this.stopTime = builder.isStopTime();
        this.seaLevel = builder.getSeaLevel();
        this.protectionRange = builder.getPlotRange();
        this.useDefaultChest = builder.isUseDefaultChest();
        this.signConfig = builder.getSignConfig();

        this.levelId = builder.getLevelId();
    }

    public void verifyWorldSettings() {
        if (isStopTime()) {
            level.setTime(1600);
            level.stopTime();
        }

        if (protectionRange % 2 != 0) {
            protectionRange--;
            Utils.send("&cThe protection range must be even, using " + protectionRange);
        }

        if (protectionRange > islandDistance) {
            Utils.send("&cThe protection range cannot be bigger then the island distance. Setting them to be half equal.");
            protectionRange = islandDistance / 2; // Avoiding players from CANNOT break their island
        }

        if (protectionRange < 0) {
            protectionRange = 0;
        }
    }

    public void saveConfig(Config cfg) {
        String levelName = level.getName();

        cfg.set(levelName + ".permission", permission);
        cfg.set(levelName + ".maxHome", 5);
        cfg.set(levelName + ".plotSize", islandDistance);
        cfg.set(levelName + ".protectionRange", protectionRange);
        cfg.set(levelName + ".stopTime", stopTime);
        cfg.set(levelName + ".seaLevel", seaLevel);
        cfg.set(levelName + ".useDefaultChest", useDefaultChest);
        cfg.set(levelName + ".signConfig", new String[]{"&aWelcome to", "&e[player]'s", "&aIsland! Enjoy.", ""});
        cfg.save();
    }

    public static WorldSettingsBuilder builder() {
        return new WorldSettingsBuilder();
    }
}
