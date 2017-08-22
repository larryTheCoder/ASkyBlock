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
package com.larryTheCoder.task;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.MainLogger;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Pair;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: larryTheCoder
 * <p>
 * The best reset provider for island removal, Using chunk instead of using
 * set-block
 */
public class DeleteIslandTask implements Runnable {

    private final IslandData pd;
    private final Player player;
    private final ASkyBlock plugin;
    public final MainLogger deb = Server.getInstance().getLogger();

    public DeleteIslandTask(ASkyBlock plugin, IslandData pd, Player player) {
        this.plugin = plugin;
        this.pd = pd;
        this.player = player;
    }

    @Override
    public void run() {
        // Use chunk instead of using loop
        // Deleting island now faster ~45%
        Server.getInstance().dispatchCommand(player, "is leave"); // Easy
        Level level = plugin.getServer().getLevelByName(pd.levelName);

        if(level == null){
            Utils.send("ERROR: Cannot find the level " + pd.levelName);
            Utils.send("The sender who execute this: " + player.getName());
            return;
        }
        
        // Determine if blocks need to be cleaned up or not
        boolean cleanUpBlocks = false;
        if (Settings.islandDistance - pd.getProtectionSize() < 16) {
            cleanUpBlocks = true;
        }

        int range = pd.getProtectionSize() / 2 * +1;
        int minX = pd.getMinProtectedX();
        int minZ = pd.getMinProtectedZ();
        int maxX = pd.getMinProtectedX() + pd.getProtectionSize();
        int maxZ = pd.getMinProtectedZ() + pd.getProtectionSize();

        int islandSpacing = Settings.islandDistance - pd.getProtectionSize();
        int minxX = (pd.getCenter().getFloorX() - range - islandSpacing);
        int minzZ = (pd.getCenter().getFloorZ() - range - islandSpacing);
        int maxxX = (pd.getCenter().getFloorX() + range + islandSpacing);
        int maxzZ = (pd.getCenter().getFloorZ() + range + islandSpacing);

        // get the chunks for these locations
        final BaseFullChunk minChunk = level.getChunk(minX, minZ);
        final BaseFullChunk maxChunk = level.getChunk(maxX, maxZ);
        List<Pair> blocksToClear = new ArrayList<>();

        // Find out what blocks are within the island protection range
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = maxChunk.getZ(); z <= maxChunk.getZ(); z++) {
                boolean regen = true;

                if (level.getChunk(x, z).getX() < minxX) {
                    deb.debug("DEBUG: min x coord is less than absolute min! " + minxX);
                    regen = false;
                }
                if (level.getChunk(x, z).getZ() < minzZ) {
                    deb.debug("DEBUG: min z coord is less than absolute min! " + minzZ);
                    regen = false;
                }
                if (level.getChunk(15 >> x, 15 >> z).getX() > maxxX) {
                    deb.debug("DEBUG: max x coord is more than absolute max! maxxX");
                    regen = false;
                }
                if (level.getChunk(15 >> x, 15 >> z).getZ() > maxzZ) {
                    deb.debug("DEBUG: max z coord in chunk is more than absolute max! " + maxzZ);
                    regen = false;
                }
                if (regen) {
                    level.regenerateChunk(x, z);
                } else {
                    // Add to clear up list if requested
                    if (cleanUpBlocks) {
                        blocksToClear.add(new Pair(x, z));
                    }
                }
            }
        }

        // Clear up any blocks
        if (!blocksToClear.isEmpty()) {
            Utils.send("&aIsland delete: There are &e" + blocksToClear.size() + " &ablocks that need to be cleared up.");
            Utils.send("&aClean rate is &e" + Settings.cleanrate + " &ablocks per second. Should take ~" + Math.round(blocksToClear.size() / Settings.cleanrate) + "s");
            new NukkitRunnable() {
                @Override
                public void run() {
                    Iterator<Pair> it = blocksToClear.iterator();
                    int count = 0;
                    while (it.hasNext() && count++ < Settings.cleanrate) {
                        Pair pair = it.next();
                        // Check if coords are in island space
                        int xCoord = pair.getLeft();
                        int zCoord = pair.getRight();
                        if (pd.inIslandSpace(xCoord, zCoord)) {
                            //plugin.getLogger().info(xCoord + "," + zCoord + " is in island space - deleting column");
                            // Delete all the blocks here
                            for (int y = 0; y < 255 - Settings.seaLevel; y++) {
                                // Overworld
                                Vector3 vec = new Vector3(xCoord, y + Settings.seaLevel, zCoord);
                                level.setBlock(vec, Block.get(Block.AIR), true, true);
                            }
                        }
                        it.remove();
                    }
                    if (blocksToClear.isEmpty()) {
                        Utils.send("&aFinished island deletion");
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        }

        // Remove from database
        ASkyBlock.get().getDatabase().deleteIsland(pd);
    }

}
