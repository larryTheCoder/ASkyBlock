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

package com.larryTheCoder.cache.builder;


import cn.nukkit.level.Level;
import com.google.common.base.Preconditions;
import com.larryTheCoder.cache.settings.WorldSettings;
import lombok.Getter;

import java.util.List;

public class WorldSettingsBuilder {

    @Getter
    private int levelId;

    @Getter
    private Level level;
    @Getter
    private String permission;
    @Getter
    private int plotMax;
    @Getter
    private int plotSize;
    @Getter
    private boolean stopTime;
    @Getter
    private int seaLevel;
    @Getter
    private int plotRange;
    @Getter
    private boolean useDefaultChest;
    @Getter
    private String[] signConfig;

    public WorldSettingsBuilder setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public WorldSettingsBuilder setPlotMax(int plotMax) {
        this.plotMax = plotMax;
        return this;
    }

    public WorldSettingsBuilder setPlotSize(int plotSize) {
        this.plotSize = plotSize;
        return this;
    }

    public WorldSettingsBuilder setPlotRange(int plotRange) {
        this.plotRange = plotRange;
        return this;
    }

    public WorldSettingsBuilder isStopTime(boolean isStopTime) {
        this.stopTime = isStopTime;
        return this;
    }

    public WorldSettingsBuilder useDefaultChest(boolean defaultChest) {
        this.useDefaultChest = defaultChest;
        return this;
    }

    public WorldSettingsBuilder setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
        return this;
    }

    public WorldSettingsBuilder setLevel(Level level) {
        this.level = level;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public WorldSettingsBuilder setSignSettings(List section) {
        this.signConfig = (String[]) section.toArray(new String[0]);

        return this;
    }

    public WorldSettingsBuilder setLevelId(int value) {
        this.levelId = value;

        return this;
    }

    public WorldSettings build() {
        Preconditions.checkState(this.level != null, "World level cannot be null!");

        return new WorldSettings(this);
    }
}
