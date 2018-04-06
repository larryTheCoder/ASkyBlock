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

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.MainLogger;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.utils.Pair;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: larryTheCoder
 * <p>
 * The best solution for reset provider in island removal,
 * Using chunk instead of using set-block
 */
public class DeleteIslandTask implements Runnable {

    public final MainLogger deb = Server.getInstance().getLogger();
    private final IslandData pd;
    private final CommandSender player;
    private final ASkyBlock plugin;

    public DeleteIslandTask(ASkyBlock plugin, IslandData pd, CommandSender player) {
        this.plugin = plugin;
        this.pd = pd;
        this.player = player;
    }

    @Override
    public void run() {
        // Use chunk instead of using loop
        // Deleting island now faster ~99%
        if (pd.onIsland(player.getServer().getPlayer(player.getName()))) {
            Server.getInstance().dispatchCommand(player, "is leave"); // Easy
        }
        Level level = plugin.getServer().getLevelByName(pd.getLevelName());
        WorldSettings settings = plugin.getSettings(level.getName());
        if (level == null) {
            Utils.send("ERROR: Cannot find the level " + pd.getLevelName());
            Utils.send("The sender who execute this: " + pd.getOwner());
            return;
        }

        // Determine if chunks need to be cleaned up or not
        boolean cleanUpBlocks = false;
        if (settings.getIslandDistance() - pd.getProtectionSize() < 16) {
            cleanUpBlocks = true;
        }

        int range = pd.getProtectionSize() / 2;
        int minX = pd.getMinProtectedX();
        int minZ = pd.getMinProtectedZ();
        int maxX = pd.getMinProtectedX() + pd.getProtectionSize();
        int maxZ = pd.getMinProtectedZ() + pd.getProtectionSize();

        int islandSpacing = plugin.getSettings(level.getName()).getIslandDistance() - pd.getProtectionSize();
        int minxX = (pd.getCenter().getFloorX() - range - islandSpacing);
        int minzZ = (pd.getCenter().getFloorZ() - range - islandSpacing);
        int maxxX = (pd.getCenter().getFloorX() + range + islandSpacing);
        int maxzZ = (pd.getCenter().getFloorZ() + range + islandSpacing);

        // get the chunks for these locations
        final BaseFullChunk minChunk = level.getChunk(minX >> 4, minZ >> 4, true);
        final BaseFullChunk maxChunk = level.getChunk(maxX >> 4, maxZ >> 4, true);

        if (!minChunk.isGenerated() || !maxChunk.isGenerated()) {
            level.regenerateChunk(minChunk.getX(), minChunk.getZ());
            level.regenerateChunk(maxChunk.getX(), maxChunk.getZ());
        }

        List<Pair> chunksToClear = new ArrayList<>();
        List<BaseFullChunk> chunksToRemoved = new ArrayList<>();

        // Find out what chunks are within the island protection range
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                boolean regen = true;

                if ((level.getChunk(x, z, true).getX() << 4) < minxX) {
                    deb.debug("DEBUG: min x coord is less than absolute min! " + minxX);
                    regen = false;
                }
                if ((level.getChunk(x, z, true).getZ() << 4) < minzZ) {
                    deb.debug("DEBUG: min z coord is less than absolute min! " + minzZ);
                    regen = false;
                }
                if ((level.getChunk(x, z, true).getX() << 4) > maxxX) {
                    deb.debug("DEBUG: max x coord is more than absolute max! " + maxxX);
                    regen = false;
                }
                if ((level.getChunk(x, z, true).getZ() << 4) > maxzZ) {
                    deb.debug("DEBUG: max z coord in chunk is more than absolute max! " + maxzZ);
                    regen = false;
                }

                Utils.loadChunkAt(new Position(x, 0, z, level));

                if (regen) {
                    // Loop in loop are not recommended.
                    // So we seperate some chunks and let the task do it works
                    chunksToRemoved.add(level.getChunk(x, z));
                } else {
                    // Add to clear up list if requested
                    if (cleanUpBlocks) {
                        chunksToClear.add(new Pair(x, z));
                    }
                }
            }
        }

        // Clear up any chunks in list
        if (!chunksToRemoved.isEmpty()) {
            new NukkitRunnable() {
                public boolean state = false;

                @Override
                public void run() {
                    Iterator<BaseFullChunk> iChunk = chunksToRemoved.iterator();
                    int count = 0;
                    while (iChunk.hasNext() && count++ < Settings.cleanRate) {
                        BaseFullChunk chunk = iChunk.next();
                        for (int y = settings.getSeaLevel(); y < 255 - settings.getSeaLevel(); y++) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    chunk.setBlock(x, y, z, 0); // AIR! SOLUTION! TREATING!
                                    BlockEntity entity = chunk.getTile(x, y, z);
                                    // Sometimes chest will be removed, but the data wont
                                    // This can corrupt the world data so be prepare
                                    if (entity != null) {
                                        chunk.removeBlockEntity(entity);
                                    }
                                }
                            }
                        }
                        level.generateChunkCallback(chunk.getX(), chunk.getZ(), chunk);
                        iChunk.remove();
                    }
                    if (chunksToRemoved.isEmpty()) {
                        this.cancel();
                        return;
                    }
                }

            }.runTaskTimer(plugin, 0, 20);
        }

        // Clear up any chunks
        if (!chunksToClear.isEmpty()) {
            Utils.send("&eIsland delete Task-2: There are &a" + chunksToClear.size() + " &echunks that need to be cleared up.");
            Utils.send("&eClean rate is &a" + Settings.cleanRate + " &echunks per second. Should take ~" + Math.round(chunksToClear.size() / Settings.cleanRate) + "s");
            new NukkitRunnable() {
                @Override
                public void run() {
                    Iterator<Pair> it = chunksToClear.iterator();
                    int count = 0;
                    while (it.hasNext() && count++ < Settings.cleanRate) {
                        Pair pair = it.next();
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                // Check if coords are in island space
                                int xCoord = pair.getLeft() * 16 + x; // Its chunks u need to understand
                                int zCoord = pair.getRight() * 16 + z;
                                if (pd.inIslandSpace(xCoord, zCoord)) {
                                    // Delete all the chunks here
                                    for (int y = 0; y < 255 - settings.getSeaLevel(); y++) {
                                        // Overworld
                                        Vector3 vec = new Vector3(xCoord, y + settings.getSeaLevel(), zCoord);
                                        level.setBlock(vec, Block.get(Block.AIR), true, true);
                                    }
                                }
                            }
                        }

                        it.remove();
                    }
                    if (chunksToClear.isEmpty()) {
                        Utils.send("&aFinished island deletion Task-2");
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        }

        // Remove from database
        boolean result = ASkyBlock.get().getDatabase().deleteIsland(pd);
        if (!result) {
            Utils.sendDebug("Unable to delete player island data from database");
        }
    }

}
