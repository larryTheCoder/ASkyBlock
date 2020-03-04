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

package com.larryTheCoder.cache.buider;


import cn.nukkit.level.Level;
import com.google.common.base.Preconditions;
import com.larryTheCoder.cache.settings.WorldSettings;

public class WorldSettingsBuilder {

    private WorldSettings realSettings = new WorldSettings(null);

    public WorldSettingsBuilder setPermission(String permission) {
        realSettings.permission = permission;
        return this;
    }

    public WorldSettingsBuilder setPlotMax(int plotMax) {
        realSettings.plotMax = plotMax;
        return this;
    }

    public WorldSettingsBuilder setPlotSize(int plotSize) {
        realSettings.plotSize = plotSize;
        return this;
    }

    public WorldSettingsBuilder setPlotRange(int plotRange) {
        realSettings.plotRange = plotRange;
        return this;
    }

    public WorldSettingsBuilder isStopTime(boolean isStopTime) {
        realSettings.stopTime = isStopTime;
        return this;
    }

    public WorldSettingsBuilder useDefaultChest(boolean defaultChest) {
        realSettings.useDefaultChest = defaultChest;
        return this;
    }

    public WorldSettingsBuilder setSeaLevel(int seaLevel) {
        realSettings.seaLevel = seaLevel;
        return this;
    }

    public WorldSettingsBuilder setLevel(Level level) {
        realSettings.level = level;
        return this;
    }


    public WorldSettings build() {
        Preconditions.checkState(realSettings.level != null, "World level cannot be null!");

        return realSettings;
    }
}
