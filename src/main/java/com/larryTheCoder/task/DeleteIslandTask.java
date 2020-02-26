/*
 * Adapted from the Wizardry License
 *
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
 * The best solution for reset provider in island removal,
 * Using chunk instead of using set-block
 *
 * @author larryTheCoder
 * @author tastybento
 */
public class DeleteIslandTask implements Runnable {

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
            Utils.send("The sender who execute this: " + pd.getPlotOwner());
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
        int minzZ = (pd.getCenter().getFloorY() - range - islandSpacing);
        int maxxX = (pd.getCenter().getFloorX() + range + islandSpacing);
        int maxzZ = (pd.getCenter().getFloorY() + range + islandSpacing);

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
                    //Utils.sendDebug"DEBUG: min x coord is less than absolute min! " + minxX);
                    regen = false;
                }
                if ((level.getChunk(x, z, true).getZ() << 4) < minzZ) {
                    //Utils.sendDebug"DEBUG: min z coord is less than absolute min! " + minzZ);
                    regen = false;
                }
                if ((level.getChunk(x, z, true).getX() << 4) > maxxX) {
                    //Utils.sendDebug"DEBUG: max x coord is more than absolute max! " + maxxX);
                    regen = false;
                }
                if ((level.getChunk(x, z, true).getZ() << 4) > maxzZ) {
                    //Utils.sendDebug"DEBUG: max z coord in chunk is more than absolute max! " + maxzZ);
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
        //boolean result = ASkyBlock.get().getDatabase().deleteIsland(pd);
//        if (!result) {
//            //Utils.sendDebug"Unable to delete player island data from database");
//        }
    }

}
