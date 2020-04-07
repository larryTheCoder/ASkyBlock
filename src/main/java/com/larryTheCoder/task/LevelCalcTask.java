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
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.utils.TextFormat;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.IslandPostLevelEvent;
import com.larryTheCoder.events.IslandPreLevelEvent;
import com.larryTheCoder.island.TopTen;
import com.larryTheCoder.cache.PlayerData;
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.cache.settings.WorldSettings;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.StoreMetadata;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Calculates the level of the island
 */
public class LevelCalcTask {

    private final Queue<StoreMetadata> levelUpdateQueue = new ArrayDeque<>(32);
    private ASkyBlock plugin;
    private boolean storeLogs = true;
    private List<String> reportLines = new ArrayList<>();

    public LevelCalcTask(ASkyBlock plugin) {
        this.plugin = plugin;
        TaskManager.runTaskRepeatAsync(() -> {
            if (updateList()) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 20);
        Utils.send("&7Started level calculation thread.");
    }

    /**
     * Adds a player into the queue to check its
     * level by chunks. The player will be checked after
     * the next tick.
     *
     * @param pd     The player island data that will be checked
     * @param sender The sender who executed this command
     */
    public void addUpdateQueue(IslandData pd, CommandSender sender) {
        levelUpdateQueue.add(new StoreMetadata(pd, sender));
    }

    /**
     * Updates the list of the queued players that need to be calculated. This is a complex
     * algorithm which its need to be executed in async in async task. Performs better and calculate
     * more than 1 players in this function.
     *
     * @return true if there is an update in list, false will result the thread to be idle
     */
    private boolean updateList() {
        int size = Math.min(0xfffff, levelUpdateQueue.size());

        if (size == 0) {
            // Idle
            return false;
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

            // Sometimes the island could be null
            if (pd == null) {
                continue;
            }

            // Get the player multiplier if it available
            int levelMultiplier = 1;
            if (targetPlayer != null) {
                for (Map.Entry<String, PermissionAttachmentInfo> pType : targetPlayer.getEffectivePermissions().entrySet()) {
                    String type = pType.getKey();
                    if (!type.equalsIgnoreCase("is.multiplier.")) {
                        continue;
                    }
                    // Then check if the player has a valid value
                    String spl = type.substring(14);
                    if (!spl.isEmpty() && Utils.isNumeric(spl)) {
                        // Get the max value should there be more than one
                        levelMultiplier = Math.max(levelMultiplier, Integer.valueOf(spl));
                        // Do some sanity checking
                        if (levelMultiplier < 1) {
                            levelMultiplier = 1;
                        }
                    } else {
                        Utils.send("&cPlayer " + pd.getPlotOwner() + " has permission: " + type + " <-- the last part MUST be a number! Ignoring...");
                    }
                }
            }

            // Get the handicap
            final int levelHandicap = pd.getLevelHandicap();
            // Get the death handicap
            int deathHandicap = 0;
            // TODO: Get the team deaths.

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
            TaskManager.runTaskAsync(() -> {
                // Logging
                File log;
                PrintWriter out = null;
                List<Integer> mdLog = null;
                List<Integer> noCountLog = null;
                List<Integer> overflowLog = null;
                if (storeLogs) {
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
                }
                if (storeLogs) {
                    mdLog = new ArrayList<>();
                    noCountLog = new ArrayList<>();
                    overflowLog = new ArrayList<>();
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
                                // GET FULL BLOCK USAGE: (THIS SHIT)
                                // int b = chunk.getFullBlock(x, y, z);
                                // Block block = Block.get(b >> 4, b & 0x0f);
                                int fullBlock = chunk.getFullBlock(x, y, z);
                                int idBlock = chunk.getFullBlock(x, y, z) >> 4;

                                if (idBlock == Block.AIR) {
                                    continue;
                                }

                                if (limitCount.containsKey(fullBlock) && Settings.blockValues.containsKey(fullBlock)) {
                                    int count = limitCount.get(fullBlock);
                                    if (count > 0) {
                                        limitCount.put(fullBlock, --count);
                                        blockScore += Settings.blockValues.get(fullBlock);
                                        if (storeLogs) {
                                            mdLog.add(fullBlock);
                                        }
                                    } else if (storeLogs) {
                                        overflowLog.add(fullBlock);
                                    }
                                } else if (limitCount.containsKey(idBlock) && Settings.blockValues.containsKey(idBlock)) {
                                    int count = limitCount.get(idBlock);
                                    if (count > 0) {
                                        limitCount.put(idBlock, --count);
                                        blockScore += Settings.blockValues.get(idBlock);
                                        if (storeLogs) {
                                            mdLog.add(idBlock);
                                        }
                                    } else if (storeLogs) {
                                        overflowLog.add(idBlock);
                                    }
                                } else if (Settings.blockValues.containsKey(fullBlock)) {
                                    blockScore += Settings.blockValues.get(fullBlock);
                                    if (storeLogs) {
                                        mdLog.add(fullBlock);
                                    }
                                } else if (Settings.blockValues.containsKey(idBlock)) {
                                    blockScore += Settings.blockValues.get(idBlock);
                                    if (storeLogs) {
                                        mdLog.add(idBlock);

                                    }
                                } else if (storeLogs) {
                                    noCountLog.add(fullBlock);
                                }
                            }
                        }
                    }
                }

                final int score = (((blockScore * levels) - (deathHandicap * Settings.deathPenalty)) / Settings.levelCost) - levelHandicap;

                if (storeLogs) {
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
                }
                if (out != null) {
                    // Write to file
                    for (String line : reportLines) {
                        out.println(line);
                    }
                    Utils.sendDebug("Finished writing level log.");
                    out.close();
                }

                // Calculate how many points are required to get to the next level
                int calculatePointsToNextLevel = (Settings.levelCost * (score + 1 + levelHandicap)) - ((blockScore * finalLevelMultiplier) - (deathHandicap * Settings.deathPenalty));
                // Sometimes it will return 0, so calculate again to make sure it will display a good value
                if (calculatePointsToNextLevel == 0) {
                    calculatePointsToNextLevel = (Settings.levelCost * (score + 2 + levelHandicap)) - ((blockScore * finalLevelMultiplier) - (deathHandicap * Settings.deathPenalty));
                }

                final int pointsToNextLevel = calculatePointsToNextLevel;

                // Wtf is this shit.
//                plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
//                    PlayerData playerInfo = plugin.getPlayerInfo(pd.getPlotOwner());
//                    // Fire the pre-level event
//                    IslandPreLevelEvent event = new IslandPreLevelEvent(targetPlayer, pd, score, pointsToNextLevel);
//                    plugin.getServer().getPluginManager().callEvent(event);
//                    int oldLevel = playerInfo.getIslandLevel();
//                    if (!event.isCancelled()) {
//                        if (oldLevel != event.getLevel()) {
//                            // Update player and team mates
//                            playerInfo.setIslandLevel(event.getLevel());
//                            playerInfo.saveData();
//                        }
//
//                        // TODO: Update player team members too
//                        TopTen.topTenAddEntry(playerInfo.getPlayerName(), event.getLevel());
//                    }
//
//                    // Fire the island post level calculation event
//                    final IslandPostLevelEvent event3 = new IslandPostLevelEvent(targetPlayer, pd, event.getLevel(), event.getPointsToNextLevel());
//                    plugin.getServer().getPluginManager().callEvent(event3);
//
//                    if (!event3.isCancelled()) {
//                        if (sender == null) {
//                            return;
//                        }
//                        if (sender.isPlayer()) {
//                            // Player
//                            if (!storeLogs) {
//                                // Tell offline team members the island level changed
//                                if (playerInfo.getIslandLevel() != oldLevel) {
//                                    //plugin.getLogger().info("DEBUG: telling offline players");
//                                    plugin.getMessages().tellOfflineTeam(pd.getPlotOwner(), TextFormat.GREEN + "Island level is " + TextFormat.WHITE + playerInfo.getIslandLevel());
//                                }
//                                if (sender instanceof Player && ((Player) sender).isOnline()) {
//                                    String message = TextFormat.GREEN + "Island level is " + TextFormat.WHITE + playerInfo.getIslandLevel();
//                                    if (Settings.deathPenalty != 0) {
//                                        message += TextFormat.RED + " [[number] deaths]".replace("[number]", String.valueOf(deathHandicap));
//                                    }
//                                    sender.sendMessage(TextFormat.GREEN + "Island level is " + TextFormat.WHITE + playerInfo.getIslandLevel());
//                                    if (event.getPointsToNextLevel() >= 0) {
//                                        String toNextLevel = TextFormat.GREEN + "You need [points] more points to reach level [next]!".replace("[points]", String.valueOf(event.getPointsToNextLevel()));
//                                        toNextLevel = toNextLevel.replace("[next]", String.valueOf(playerInfo.getIslandLevel() + 1));
//                                        sender.sendMessage(toNextLevel);
//                                    }
//                                }
//                            } else {
//                                if (((Player) sender).isOnline()) {
//                                    for (String line : reportLines) {
//                                        sender.sendMessage(line);
//                                    }
//                                }
//                                sender.sendMessage(TextFormat.GREEN + "Island level is " + TextFormat.WHITE + playerInfo.getIslandLevel());
//                                if (event.getPointsToNextLevel() >= 0) {
//                                    String toNextLevel = TextFormat.GREEN + "You need [points] more points to reach level [next]!".replace("[points]", String.valueOf(event.getPointsToNextLevel()));
//                                    toNextLevel = toNextLevel.replace("[next]", String.valueOf(playerInfo.getIslandLevel() + 1));
//                                    sender.sendMessage(toNextLevel);
//                                }
//                            }
//                        } else {
//                            if (!storeLogs) {
//                                sender.sendMessage(TextFormat.GREEN + "Island level is " + TextFormat.WHITE + playerInfo.getIslandLevel());
//                            } else {
//                                for (String line : reportLines) {
//                                    sender.sendMessage(line);
//                                }
//                                sender.sendMessage(TextFormat.GREEN + "Island level is " + TextFormat.WHITE + playerInfo.getIslandLevel());
//                                if (event.getPointsToNextLevel() >= 0) {
//                                    String toNextLevel = TextFormat.GREEN + "You need [points] more points to reach level [next]!".replace("[points]", String.valueOf(event.getPointsToNextLevel()));
//                                    toNextLevel = toNextLevel.replace("[next]", String.valueOf(playerInfo.getIslandLevel() + 1));
//                                    sender.sendMessage(toNextLevel);
//                                }
//                            }
//                        }
//                    }
//                });
            });
        }
        return true;
    }

}
