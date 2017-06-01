/*
 * Copyright (C) 2017 larryTheCoder
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

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

/**
 *
 * @author larryTheCoder
 */
public class DeleteIslandTask extends cn.nukkit.scheduler.PluginTask<ASkyBlock> implements TaskSkyBlock{

    private final IslandData pd;

    private Level level = null;

    public DeleteIslandTask(ASkyBlock owner, IslandData ps) {
        super(owner);
        this.pd = ps;
        level = getOwner().getServer().getLevelByName(pd.levelName);
    }

    @Override
    public void onRun(int currentTick) {
        // Check code
        // maxX = 50 + (200 / 2) = 150
        // maxZ = 50 + (200 / 2) = 150
        // minX = 50 - (200 / 2) = 50
        // minZ = 50 - (200 / 2) = 50
        //
        //
        int maxX = pd.X + (pd.getProtectionSize() / 2);
        int maxZ = pd.Z + (pd.getProtectionSize() / 2);
        int mineX = pd.X - (pd.getProtectionSize() / 2);
        int mineZ = pd.X - (pd.getProtectionSize() / 2);
        Utils.ConsoleMsg("Form: " + maxX + " " + maxZ + " to " + mineZ + " " + mineX);
        int blocks = 0;
        for (int minX = pd.X - (pd.getProtectionSize() / 2); minX < maxX; minX++) {
            for (int y = 0; y < 257; y++) {
                for (int minZ = pd.X - (pd.getProtectionSize() / 2); minZ < maxZ; minZ++) {
                    int block = Block.AIR;
                    if (y < Settings.seaLevel) {
                        block = Block.WATER;
                    }
                    level.setBlock(new Vector3(minX, y, minZ), Block.get(block), true, true);
                    blocks++;
                    if (blocks == 50) {
                        getOwner().getServer().getScheduler().scheduleDelayedTask(this, 2);
                        break;
                    }
                }
            }
        }
        Player p = getOwner().getServer().getPlayer(pd.owner);
        p.sendMessage(getOwner().getPrefix() +"Seccessfully cleared your island");
        ASkyBlock.get().getDatabase().deleteIsland(pd);
    }
}
