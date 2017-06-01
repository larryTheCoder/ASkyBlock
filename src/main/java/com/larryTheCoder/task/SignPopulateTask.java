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
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.intellectiualcrafters.TaskManager;
import java.util.List;

/**
 *
 * @author larryTheHarry
 */
public class SignPopulateTask implements TaskSkyBlock {

    private final int z;
    private final int y;
    private final int x;
    private final Level level;
    private final List<String> signText;

    public SignPopulateTask(Location loc, List<String> chestContents) {
        this.x = loc.getFloorX();
        this.y = loc.getFloorY();
        this.z = loc.getFloorZ();
        this.level = loc.getLevel();
        this.signText = chestContents;
    }

    @Override
    public void run() {
        if (level.getBlockIdAt(x, y, z) != Block.SIGN_POST || level.getBlockIdAt(x, y, z) != Block.WALL_SIGN) {
            // This should never happends
            level.setBlock(new Vector3(x, y, z), Block.get(Block.SIGN_POST));
            TaskManager.runTaskLater(new SignPopulateTask(new Location(x, y, z, level), signText), 5);
            return;
        }
        cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                .putString("id", BlockEntity.SIGN)
                .putInt("x", (int) x)
                .putInt("y", (int) y)
                .putInt("z", (int) z)
                .putString("Text1", signText.get(0))
                .putString("Text2", signText.get(1))
                .putString("Text3", signText.get(2))
                .putString("Text4", signText.get(3));
        new BlockEntitySign(level.getChunk((int) x >> 4, (int) z >> 4), nbt);
    }

}
