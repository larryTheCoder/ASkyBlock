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
package com.larryTheCoder.listener;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.block.BlockID.*;

/**
 * @author tastybento
 * @author larryTheCoder
 */
public class LavaCheck implements Listener {

    private static Map<Integer, Multiset<Block>> stats = new HashMap<>();
    private static Map<Integer, Map<Block, Double>> configChances = new HashMap<>();
    private final ASkyBlock plugin;
    private List<Vector3> isOnTick = new ArrayList<>();

    public LavaCheck(ASkyBlock aSkyBlock) {
        plugin = aSkyBlock;
        stats.clear();
    }

    /**
     * @return the magic cobble stone stats
     */
    public static Map<Integer, Multiset<Block>> getStats() {
        return stats;
    }

    /**
     * Clears the magic cobble gen stats
     */
    public static void clearStats() {
        stats.clear();
    }

    /**
     * Store the configured chances in %
     *
     * @param levelInt The level for the chances
     * @param chances  The block with custom chances
     */
    public static void storeChances(int levelInt, Map<Block, Double> chances) {
        configChances.put(levelInt, chances);
    }

    /**
     * Clear the magic cobble gen chances
     */
    public static void clearChances() {
        configChances.clear();
    }

    /**
     * Return the chances for this level and material
     *
     * @param level    The level for the block
     * @param material The block of the level
     * @return chance, or 0 if the level or material don't exist
     */
    public static double getConfigChances(Integer level, Block material) {
        double result = 0;
        if (configChances.containsKey(level) && configChances.get(level).containsKey(material)) {
            result = configChances.get(level).get(material);
        }
        return result;
    }

    /**
     * Determines if a location is in the island world or not or in the new
     * nether if it is activated
     *
     * @param loc Location of the entity to be checked
     * @return true if in the island world
     */
    private boolean notInWorld(Location loc) {
        return !ASkyBlock.get().getLevels().contains(loc.getLevel().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCleanstoneGen(BlockFromToEvent e) {
        // If magic cobble gen isn't used
        if (!Settings.useMagicCobbleGen) {
            return;
        }
        // Do this only in the SkyBlock world
        if (notInWorld(e.getBlock().getLocation())) {
            return;
        }

        Block block = e.getTo();
        if (block.getId() == WATER || block.getId() == STILL_WATER || block.getId() == LAVA || block.getId() == STILL_LAVA) {
            BlockLiquid blockLiq = (BlockLiquid) block;

            // This is a very weird thing to be reproduce.
            // #BlameNukkit :/
            Block flowedFrom = e.getBlock();

            if (!generatesCobble(block, flowedFrom)) {
                return;
            }

            int level = Integer.MIN_VALUE;
            IslandData pd = plugin.getIslandInfo(e.getBlock());
            if (pd != null && pd.getPlotOwner() != null) {
                PlayerData pd2 = plugin.getPlayerInfo(pd.getPlotOwner());
                if (pd2 != null) {
                    level = pd2.getIslandLevel();
                }
            }

            if (!Settings.magicCobbleGenChances.isEmpty()) {
                Map.Entry<Integer, TreeMap<Double, Block>> entry = Settings.magicCobbleGenChances.floorEntry(level);
                double maxValue = entry.getValue().lastKey();
                double rnd = Utils.randomDouble() * maxValue;
                Map.Entry<Double, Block> en = entry.getValue().ceilingEntry(rnd);

                if (en != null) {
                    e.setCancelled();
                    flowedFrom.getLevel().setBlock(flowedFrom, en.getValue());
                    flowedFrom.getLevel().addSound(flowedFrom.add(0.5, 0.5, 0.5), Sound.RANDOM_FIZZ, 1, 2.6F + (ThreadLocalRandom.current().nextFloat() - ThreadLocalRandom.current().nextFloat()) * 0.8F);

                    for (int i = 0; i < 8; ++i) {
                        flowedFrom.getLevel().addParticle(new SmokeParticle(flowedFrom.add(Math.random(), 1.2, Math.random())));
                    }

                    // Record stats, per level
                    if (stats.containsKey(entry.getKey())) {
                        stats.get(entry.getKey()).add(en.getValue());
                    } else {
                        Multiset<Block> set = HashMultiset.create();
                        set.add(en.getValue());
                        stats.put(entry.getKey(), set);
                    }
                }
            }
        }
    }

    public boolean generatesCobble(Block block, Block toBlock) {
        int mirrorID1 = block.getId() == WATER || block.getId() == STILL_WATER ? LAVA : WATER;
        int mirrorID2 = block.getId() == WATER || block.getId() == STILL_WATER ? STILL_LAVA : STILL_WATER;
        for (BlockFace face : BlockFace.values()) {
            Block r = toBlock.getSide(face);
            if (r.getId() == mirrorID1 || r.getId() == mirrorID2) {
                return true;
            }
        }
        return false;
    }

}
