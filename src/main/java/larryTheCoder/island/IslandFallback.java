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
package larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSapling;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.object.tree.ObjectTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import java.util.HashMap;
import java.util.Map;
import larryTheCoder.ASkyBlock;

/**
 * This function is for generating and regenerating island if .schematic file
 * doesn't exists and others, Fallback generation will be used to player island
 * so there wont be an Error will be thrown.
 * <p>
 * This method will leave a backup and more in .yml file and it will store
 * players islands in .DAT and will be more central also generating an island if
 * DEFAULT SCHEMATIC is gone!
 * </p>
 *
 * @author larryTheCoder
 */
public class IslandFallback {

    private ASkyBlock plugin = null;

    public IslandFallback(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    public void init() {
        //todo
    }

    public void saveIsland(){
        //todo
    }
    
    public void createIsland(Level world, int X, int Y, int Z, Player p) {
        int groundHeight = Y;
        // bedrock - ensures island are not overwritten
        for (int x = X + 13; x < X + 14; ++x) {
            for (int z = Z + 13; z < Z + 14; ++z) {
                world.setBlockIdAt(x, groundHeight, z, Block.BEDROCK);
            }
        }
        // Add some dirt and grass
        for (int x = X + 12; x < X + 15; ++x) {
            for (int z = X + 12; z < X + 15; ++z) {
                world.setBlockIdAt(x, groundHeight + 1, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 2, z, Block.DIRT);
            }
        }
        for (int x = X + 11; x < X + 16; ++x) {
            for (int z = Z + 11; z < Z + 16; ++z) {
                world.setBlockIdAt(x, groundHeight + 3, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 4, z, Block.DIRT);
            }
        }
        for (int x = X + 10; x < X + 17; ++x) {
            for (int z = Z + 10; z < Z + 17; ++z) {
                world.setBlockIdAt(x, groundHeight + 5, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 6, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 7, z, Block.GRASS);
            }
        }
        // Then cut off the corners to make it round-ish
        for (int x_space = X + 13 - 2; x_space <= X + 13 + 2; x_space += 4) {
            for (int z_space = Z + 13 - 2; z_space <= Z + 13 + 2; z_space += 4) {
                world.setBlockIdAt(x_space, groundHeight + 3, z_space, Block.AIR);
                world.setBlockIdAt(x_space, groundHeight + 4, z_space, Block.AIR);
            }
        }

        for (int y = groundHeight - 1; y < groundHeight + 8; ++y) {
            for (int x_space = X + 13 - 3; x_space <= X + 13 + 3; x_space += 6) {
                for (int z_space = Z + 13 - 3; z_space <= Z + 13 + 3; z_space += 6) {
                    world.setBlockIdAt(x_space, y, z_space, Block.AIR);
                }
            }
        }
        int Xt = X + 13;
        int Zt = X + 13;
        // First place
        world.setBlockIdAt(Xt - 1, groundHeight + 1, Zt + 1, Block.AIR);
        world.setBlockIdAt(Xt - 2, groundHeight + 1, Zt + 2, Block.AIR);
        world.setBlockIdAt(Xt - 1, groundHeight + 1, Zt - 1, Block.AIR);
        world.setBlockIdAt(Xt - 2, groundHeight + 1, Zt - 2, Block.AIR);
        // tree
        ObjectTree.growTree(world, X + 10, groundHeight + 8, Z + 11, new NukkitRandom(), BlockSapling.OAK);
        this.initChest(world, X, Y, Z, p);
    }
    
    private void initChest(Level lvl, int x, int y, int z, Player p) {
        // It works!
        lvl.setBlockIdAt(x, y, z, Block.CHEST);
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<>("Items"))
                .putString("id", BlockEntity.CHEST)
                .putInt("x", x)
                .putInt("y", y)
                .putInt("z", z);

        BlockEntity.createBlockEntity(BlockEntity.CHEST, p.chunk, nbt);
        BlockEntityChest e = new BlockEntityChest(p.chunk, nbt);
        // Items
        Map<Integer, Item> items = new HashMap<>();
        items.put(0, Item.get(Item.ICE, 0, 2));
        items.put(1, Item.get(Item.BUCKET, 10, 1));
        items.put(2, Item.get(Item.BONE, 0, 2));
        items.put(3, Item.get(Item.SUGARCANE, 0, 1));
        items.put(4, Item.get(Item.RED_MUSHROOM, 0, 1));
        items.put(5, Item.get(Item.BROWN_MUSHROOM, 0, 2));
        items.put(6, Item.get(Item.PUMPKIN_SEEDS, 0, 2));
        items.put(7, Item.get(Item.MELON, 0, 1));
        items.put(8, Item.get(Item.SAPLING, 0, 1));
        items.put(9, Item.get(Item.STRING, 0, 12));
        items.put(10, Item.get(Item.POISONOUS_POTATO, 0, 32));
        e.getInventory().setContents(items);
    }
}
