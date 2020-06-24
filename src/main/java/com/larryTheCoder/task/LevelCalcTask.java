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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.cache.settings.WorldSettings;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.StoreMetadata;
import com.larryTheCoder.utils.Utils;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Calculates the level of the island
 */
public class LevelCalcTask extends Thread {

    private final ASkyBlock plugin;
    private final List<String> reportLines = new ArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private final Queue<StoreMetadata> levelUpdateQueue = new ArrayDeque<>(32);

    public LevelCalcTask(ASkyBlock plugin) {
        this.plugin = plugin;

        setName("SkyBlock Calculation Thread");
        setDaemon(true);
        start();

        Utils.send("&7Started island level calculation thread.");
    }

    public void shutdown() {
        isRunning.compareAndSet(true, false);

        synchronized (this) {
            notify();
        }
    }

    /**
     * Adds a player into the queue to check its
     * level by chunks. The player will be checked after the thread has successfully cleared out
     * their executions.
     *
     * @param islandDb The player island data that will be checked
     * @param sender   The sender who executed this command
     */
    public void addUpdateQueue(IslandData islandDb, CommandSender sender) {
        levelUpdateQueue.add(new StoreMetadata(islandDb, sender));

        synchronized (this) {
            notify();
        }
    }

    /**
     * The heartbeat of the thread execution.
     */
    @SneakyThrows
    public void run() {
        while (isRunning.get()) {
            updateList();

            synchronized (this) {
                wait(150000);
            }
        }
    }

    /**
     * Updates the list of the queued players that need to be calculated. This is a complex
     * algorithm which its need to be executed in a new thread, will also performs better
     * with a wait-for-resources technique.
     */
    private void updateList() {
        int size = Math.min(1048575, levelUpdateQueue.size());

        if (size == 0) {
            return;
        }

        int limit = Math.min(10, size); // Uh, thread
        StoreMetadata[] copy = new StoreMetadata[limit];
        for (int i = 0; i < limit; i++) {
            copy[i] = levelUpdateQueue.poll();
        }

        for (StoreMetadata data : copy) {
            IslandData pd = data.getIslandData();
            Player targetPlayer = Server.getInstance().getPlayer(pd.getPlotOwner());
            CommandSender sender = data.getSender();

            // Get the player multiplier if it available
            int levelMultiplier = 1;
            if (targetPlayer != null) {
                Map<String, Boolean> plPermission = plugin.getPermissionHandler().getPermissions(targetPlayer.getUniqueId());

                for (Map.Entry<String, Boolean> pType : plPermission.entrySet()) {
                    String type = pType.getKey();

                    // Statement: The player has the multiplier, but the player do not have the permission
                    if (!type.startsWith("is.multiplier.") || !pType.getValue()) {
                        continue;
                    }

                    // Then check if the player has a valid value
                    String spl = type.substring(14);
                    if ((!spl.isEmpty()) && Utils.isNumeric(spl)) {
                        // Get the max value should there be more than one
                        levelMultiplier = Math.max(levelMultiplier, Integer.parseInt(spl));

                        // Do some sanity checking
                        if (levelMultiplier < 1) levelMultiplier = 1;
                    } else {
                        Utils.send("&cPlayer " + pd.getPlotOwner() + " has permission: " + type + " <-- the last part MUST be a number! Ignoring...");
                    }
                }
            }

            // Get the handicap
            int levelHandicap = pd.getLevelHandicap();
            // Get the death handicap
            int deathHandicap = 0;

            Level level = Server.getInstance().getLevelByName(pd.getLevelName());

            Set<FullChunk> chunkSnapshot = new HashSet<>();
            for (int x = pd.getMinProtectedX(); x < (pd.getMinProtectedX() + pd.getProtectionSize() + 16); x += 16) {
                for (int z = pd.getMinProtectedZ(); z < (pd.getMinProtectedZ() + pd.getProtectionSize() + 16); z += 16) {
                    if (!level.getChunk(x >> 4, z >> 4).isLoaded()) {
                        level.loadChunk(x >> 4, z >> 4, true);

                        chunkSnapshot.add(level.getChunk(x >> 4, z >> 4));

                        level.unloadChunk(x >> 4, z >> 4);
                    } else {
                        chunkSnapshot.add(level.getChunk(x >> 4, z >> 4));
                    }
                }
            }

            int levels = levelMultiplier;
            int finalLevelMultiplier = levelMultiplier;

            // I wonder why I bother trying to pass these execution into an async worker lmao
            File log;
            PrintWriter out = null;
            List<Integer> mdLog = new ArrayList<>();
            List<Integer> noCountLog = new ArrayList<>();
            List<Integer> overflowLog = new ArrayList<>();

            if (Settings.verboseCode) {
                log = new File(plugin.getDataFolder(), "level.log");
                try {
                    log.createNewFile();
                    if (log.exists()) {
                        out = new PrintWriter(new FileWriter(log, true));
                    } else {
                        out = new PrintWriter(log);
                    }
                } catch (IOException e) {
                    Utils.sendDebug("Level log (level.log) could not be opened...");
                    e.printStackTrace();
                }

                Utils.sendDebug("Debugging chunk info for island " + pd.getIslandUniquePlotId());
            }

            WorldSettings settings = ASkyBlock.get().getSettings(level.getName());

            int blockScore = 0;

            // Check the blocks by chunks, which its faster.
            HashMap<Integer, Integer> limitCount = new HashMap<>(Settings.blockLimits);
            for (FullChunk chunk : chunkSnapshot) {
                for (int x = 0; x < 16; x++) {
                    // Check if the block coordinates is inside the protection zone and if not, don't count it
                    if (chunk.getX() * 16 + x < pd.getMinProtectedX() || chunk.getX() * 16 + x >= pd.getMinProtectedX() + pd.getProtectionSize()) {
                        continue;
                    }

                    for (int z = 0; z < 16; z++) {
                        // Check if the block coordinates is inside the protection zone and if not, don't count it
                        if (chunk.getZ() * 16 + z < pd.getMinProtectedZ() || chunk.getZ() * 16 + z >= pd.getMinProtectedZ() + pd.getProtectionSize()) {
                            continue;
                        }

                        // Do not count below sea level, its toxic
                        for (int y = settings.getSeaLevel(); y < 256; y++) {
                            // int b = chunk.getFullBlock(x, y, z);
                            // Block block = Block.get(b >> 4, b & 0x0f);
                            int fullBlock = chunk.getFullBlock(x, y, z);
                            int idBlock = chunk.getFullBlock(x, y, z) >> 4;

                            if (idBlock == Block.AIR) {
                                continue;
                            }

                            if ((limitCount.containsKey(fullBlock) && Settings.blockValues.containsKey(fullBlock)) || (limitCount.containsKey(idBlock) && Settings.blockValues.containsKey(idBlock))) {
                                int block;
                                if (limitCount.containsKey(fullBlock) && Settings.blockValues.containsKey(fullBlock)) {
                                    block = fullBlock;
                                } else {
                                    block = idBlock;
                                }
                                int count = limitCount.get(block);
                                if (count > 0) {
                                    limitCount.put(block, --count);
                                    blockScore += Settings.blockValues.get(block);

                                    if (Settings.verboseCode) mdLog.add(block);
                                } else if (Settings.verboseCode) {
                                    overflowLog.add(block);
                                }
                            }
                            if (Settings.blockValues.containsKey(fullBlock)) {
                                blockScore += Settings.blockValues.get(fullBlock);

                                if (Settings.verboseCode) mdLog.add(fullBlock);
                            } else if (Settings.blockValues.containsKey(idBlock)) {
                                blockScore += Settings.blockValues.get(idBlock);

                                if (Settings.verboseCode) mdLog.add(idBlock);
                            } else if (Settings.verboseCode) {
                                noCountLog.add(fullBlock);
                            }
                        }
                    }
                }
            }

            final int score = (((blockScore * levels) - (deathHandicap * Settings.deathPenalty)) / Settings.levelCost) - levelHandicap;

            if (Settings.verboseCode) {
                int total = 0;
                // provide counts
                Multiset<Integer> mdCount = HashMultiset.create(mdLog);
                Multiset<Integer> ncCount = HashMultiset.create(noCountLog);
                Multiset<Integer> ofCount = HashMultiset.create(overflowLog);
                reportLines.add("Level Log for island at " + pd.getCenter());
                if (targetPlayer != null) {
                    reportLines.add("Asker is " + sender.getName());
                } else {
                    reportLines.add("Asker is " + pd.getPlotOwner());
                }
                reportLines.add("Total block score count = " + String.format("%,d", blockScore));
                reportLines.add("Level cost = " + Settings.levelCost);
                reportLines.add("Level multiplier = " + levels + " (Player must be online to get a permission multiplier)");
                reportLines.add("Schematic level handicap = " + levelHandicap + " (level is reduced by this amount)");
                reportLines.add("Deaths handicap = " + (deathHandicap * Settings.deathPenalty) + " (" + deathHandicap + " deaths)");
                reportLines.add("Level calculated = " + score);
                reportLines.add("==================================");
                reportLines.add("Regular block count");
                reportLines.add("Total number of blocks = " + String.format("%,d", mdCount.size()));
                Iterable<Multiset.Entry<Integer>> entriesSortedByCount = mdCount.entrySet();
                Iterator<Multiset.Entry<Integer>> it = entriesSortedByCount.iterator();
                while (it.hasNext()) {
                    Multiset.Entry<Integer> type = it.next();
                    int value = 0;
                    if (Settings.blockValues.containsKey(type.getElement())) {
                        // Generic
                        value = Settings.blockValues.get(type.getElement());
                    }
                    Block block = Block.get(type.getElement() >> 4, type.getElement() & 0x0f);
                    reportLines.add(block.toString() + ":" + String.format("%,d", type.getCount()) + " blocks x " + value + " = " + (value * type.getCount()));
                    total += (value * type.getCount());
                }
                reportLines.add("Total = " + total);
                reportLines.add("Total checked blocks = " + String.format("%,d", mdLog.size()));
                reportLines.add("==================================");
                reportLines.add("Blocks not counted because they exceeded limits: " + String.format("%,d", ofCount.size()));
                entriesSortedByCount = ofCount.entrySet();
                it = entriesSortedByCount.iterator();
                while (it.hasNext()) {
                    Multiset.Entry<Integer> type = it.next();
                    Integer limits = Settings.blockLimits.get(type.getElement());
                    String explain = ")";
                    if (limits == null) {
                        limits = Settings.blockLimits.get(type.getElement() >> 4);
                        explain = " - All types)";
                    }
                    Block block = Block.get(type.getElement() >> 4, type.getElement() & 0x0f);
                    reportLines.add(block.toString() + ": " + String.format("%,d", type.getCount()) + " blocks (max " + limits + explain);
                }
                reportLines.add("==================================");
                reportLines.add("Blocks on island that are not in blockvalues.yml");
                reportLines.add("Total number = " + String.format("%,d", ncCount.size()));
                entriesSortedByCount = ncCount.entrySet();
                it = entriesSortedByCount.iterator();
                while (it.hasNext()) {
                    Multiset.Entry<Integer> type = it.next();
                    Block block = Block.get(type.getElement() >> 4, type.getElement() & 0x0f);
                    reportLines.add(block.toString() + ": " + String.format("%,d", type.getCount()) + " blocks");
                }
                reportLines.add("====================================");

                if (out != null) {
                    // Write to file
                    for (String line : reportLines) {
                        out.println(line);
                    }
                    Utils.sendDebug("Finished writing level log.");
                    out.close();
                }
            }

            // Calculate how many points are required to get to the next level
            int calculatePointsToNextLevel = (Settings.levelCost * (score + 1 + levelHandicap)) - ((blockScore * finalLevelMultiplier) - (deathHandicap * Settings.deathPenalty));
            // Sometimes it will return 0, so calculate again to make sure it will display a good value
            if (calculatePointsToNextLevel == 0) {
                calculatePointsToNextLevel = (Settings.levelCost * (score + 2 + levelHandicap)) - ((blockScore * finalLevelMultiplier) - (deathHandicap * Settings.deathPenalty));
            }

            final int pointsToNextLevel = calculatePointsToNextLevel;
        }
    }

}
