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
 */
package com.larryTheCoder;

import com.larryTheCoder.utils.Settings;
import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import java.util.Map;

/**
 * @author Adam Matthew
 */
public class SkyBlockGenerator extends Generator {

    public static final int TYPE_SKYBLOCK = 0x7fffffff;
    private ChunkManager level;
    private NukkitRandom random;
    private final Map<String, Object> options;

    public SkyBlockGenerator(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public int getDimension() {
        return Level.DIMENSION_OVERWORLD;
    }
    
    @Override
    public int getId() {
        return TYPE_SKYBLOCK;
    }

    @Override
    public void init(ChunkManager cm, NukkitRandom nr) {
        this.level = cm;
        this.random = nr;
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBiomeId(x, z, Biome.PLAINS);
            }
        }
        // making island in this section has been removed
        // reason why blocks not appear BECAUSE OF THIS @#*~+ AIR!!
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < Settings.seaLevel; y++) {
                    chunk.setBlock(x, y, z, Block.STILL_WATER); // Water Allows stuff 
                    // to fall through into oblivion, thus keeping lag to a minimum
                }
            }
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        // ??? do wut Xd
    }

    @Override
    public Map<String, Object> getSettings() {
        return options;
    }

    @Override
    public String getName() {
        return "island";
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(14, 60, 14);
    }

    @Override
    public ChunkManager getChunkManager() {
        return level;
    }

}
