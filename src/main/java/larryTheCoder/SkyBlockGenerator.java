/*
 * Copyright (C) 2016 larryTheHarry 
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
package larryTheCoder;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSapling;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.object.tree.ObjectTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import java.util.Map;

/**
 * @author larryTheCoder
 */
public class SkyBlockGenerator extends Generator {

    public static final int TYPE_SKYBLOCK = 3;
    public ChunkManager level;
    public NukkitRandom random;
    private final Map<String, Object> options;

    public SkyBlockGenerator(Map<String, Object> options) {
        this.options = options;
    }
    
    @Override
    public int getId() {
        return TYPE_SKYBLOCK;
    }

    @Override
    public void init(ChunkManager cm, NukkitRandom nr) {
        level = cm;
        random = nr;
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = level.getChunk(chunkZ, chunkZ);
        int groundHeight = 60;
        // Bigger Islands
        if (chunkX % 100 == 0 && chunkZ % 100 == 0) {
            // bedrock - ensures island are not overwritten
            for (int x = 13; x < 14; ++x) {
                for (int z = 13; z < 14; ++z) {
                    chunk.setBlock(x, groundHeight, z, Block.BEDROCK);
                }
            }
            // Add some dirt and grass
            for (int x = 11; x < 16; ++x) {
                for (int z = 11; z < 16; ++z) {
                    chunk.setBlock(x, groundHeight + 1, z, Block.DIRT);
                    chunk.setBlock(x, groundHeight + 2, z, Block.DIRT);
                }
            }
            for (int x = 10; x < 17; ++x) {
                for (int z = 10; z < 17; ++z) {
                    chunk.setBlock(x, groundHeight + 3, z, Block.DIRT);
                    chunk.setBlock(x, groundHeight + 4, z, Block.DIRT);
                    chunk.setBlock(x, groundHeight + 5, z, Block.GRASS);
                }
            }
            // Then cut off the corners to make it round-ish
            for (int x = 10; x < 17; x += 2) {
                for (int z = 10; z < 17; z += 2) {
                    chunk.setBlockId(x, groundHeight + 1, z, Block.AIR);
                }
            }

            for (int y = groundHeight - 1; y < groundHeight + 6; ++y) {
                for (int x_space = 13 - 2; x_space <= 13 + 2; x_space += 4) {
                    for (int z_space = 13 - 2; z_space <= 13 + 2; z_space += 4) {
                        chunk.setBlockId(x_space, y, z_space, Block.AIR);
                    }
                }
            }
            ObjectTree.growTree(level, chunkX + 2, groundHeight + 6, chunkZ + 2, random, BlockSapling.OAK);
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                level.getChunk(chunkX, chunkZ).setBiomeColor(x, z, 133, 188, 86);
            }
        }
    }

    @Override
    public Map<String, Object> getSettings() {
        return options;
    }

    @Override
    public String getName() {
        return "skyblock";
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
