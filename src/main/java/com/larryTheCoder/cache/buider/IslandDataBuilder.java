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

import cn.nukkit.math.Vector2;
import com.larryTheCoder.cache.IslandData;

public class IslandDataBuilder {

    private IslandData data = new IslandData();

    public IslandDataBuilder setGridCoordinates(Vector2 vec) {
        data.setCenter(vec);

        return this;
    }

    public IslandDataBuilder setIslandUniquePlotId(int generatedData) {
        data.setIslandUniquePlotId(generatedData);

        return this;
    }

    public IslandDataBuilder setPlotOwner(String plotOwner) {
        data.setPlotOwner(plotOwner);

        return this;
    }

    public IslandDataBuilder setLevelName(String levelName) {
        data.setLevelName(levelName);

        return this;
    }

    public IslandDataBuilder setLocked(boolean isLocked) {
        data.setLocked(isLocked);
        return this;
    }

    public IslandDataBuilder setPlotBiome(String biomeName) {
        data.setPlotBiome(biomeName);
        return this;
    }

    public IslandDataBuilder setIslandName(String islandName) {
        data.setIslandName(islandName);
        return this;
    }

    public IslandData build() {
        return data;
    }

}
