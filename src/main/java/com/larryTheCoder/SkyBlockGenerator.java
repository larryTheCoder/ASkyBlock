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
package com.larryTheCoder;

import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.larryTheCoder.storage.WorldSettings;

import java.util.Map;

/**
 * @author larryTheCoder
 */
public class SkyBlockGenerator extends Generator {

    public static final int TYPE_SKYBLOCK = 0x7eabbff;
    private final Map<String, Object> options;
    private ChunkManager level;

    public SkyBlockGenerator(Map<String, Object> options) {
        this.options = options;
    }

    @Override
    public int getId() {
        return TYPE_SKYBLOCK;
    }

    @Override
    public void init(ChunkManager cm, NukkitRandom nr) {
        this.level = cm;
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBiomeId(x, z, EnumBiome.PLAINS.id);
            }
        }

        WorldSettings settings = ASkyBlock.get().getSettings(chunk.getProvider().getLevel().getName());
        // Sometime it could be null.
        if (settings == null) {
            return;
        }
        // making island in this section has been removed
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < settings.getSeaLevel(); y++) {
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
        return new Vector3(15, 60, 15);
    }

    @Override
    public ChunkManager getChunkManager() {
        return level;
    }

    @Override
    public int getDimension() {
        return Level.DIMENSION_OVERWORLD;
    }
}
