/*
 * Copyright (C) 2017 larryTheHarry
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
package com.larryTheCoder.task;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.intellectiualcrafters.TaskManager;
import java.util.HashMap;

/**
 *
 * @author larryTheHarry
 */
public class ChestPopulateTask implements TaskSkyBlock {

    private final int z;
    private final int y;
    private final int x;
    private final Level level;
    private final HashMap<Integer, Item> contents;

    public ChestPopulateTask(Location loc, HashMap<Integer, Item> chestContents) {
        this.x = loc.getFloorX();
        this.y = loc.getFloorY();
        this.z = loc.getFloorZ();
        this.level = loc.getLevel();
        this.contents = chestContents;
    }

    @Override
    public void run() {
        if (level.getBlockIdAt(x, y, z) != Block.CHEST) {
            // This should never happends
            level.setBlock(new Vector3(x, y, z), Block.get(Block.CHEST));
            TaskManager.runTaskLater(new ChestPopulateTask(new Location(x, y, z, level), contents), 5);
            return;
        }
        cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                .putString("id", BlockEntity.CHEST)
                .putInt("x", x)
                .putInt("y", y)
                .putInt("z", z);
        BlockEntity.createBlockEntity(BlockEntity.CHEST, level.getChunk(x >> 4, z >> 4), nbt);
        BlockEntityChest e = new BlockEntityChest(level.getChunk(x >> 4, z >> 4), nbt);
        e.getInventory().setContents(contents);
    }
}
