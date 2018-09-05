/*
 * Copyright (C) 2016-2018 Adam Matthew
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

package com.larryTheCoder.listener;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.level.Location;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static cn.nukkit.block.BlockID.*;

/**
 * Rewrites from tastybento
 */
public class LavaCheck implements Listener {

    private static Map<Integer, Multiset<Block>> stats = new HashMap<>();
    private static Map<Integer, Map<Block, Double>> configChances = new HashMap<>();
    private final ASkyBlock plugin;

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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCleanstoneGen(BlockFromToEvent e) {
        //Utils.sendDebug("DEBUG: " + e.getEventName());
        //Utils.sendDebug("From material is " + e.getBlock().toString());
        //Utils.sendDebug("To material is " + e.getTo().toString());
        //Utils.sendDebug("Magic cobble generator event");
        // If magic cobble gen isn't used
        if (!Settings.useMagicCobbleGen) {
            //Utils.sendDebug("Not enabled");
            return;
        }
        // Do this only in the SkyBlock world
        if (notInWorld(e.getBlock().getLocation())) {
            //Utils.sendDebug("Not in the world");
            return;
        }
        Block block = e.getBlock();
        if (block.getId() == WATER || block.getId() == STILL_WATER || block.getId() == LAVA || block.getId() == STILL_LAVA) {
            Block toBlock = e.getTo();
            // The block that flowed into will always became a
            // cobblestone & stone, which is weird always
            if (toBlock.getId() == COBBLESTONE || toBlock.getId() == STONE) {

                int l = Integer.MIN_VALUE;
                IslandData pd = plugin.getIslandInfo(block.getLocation());
                if (pd != null && pd.getOwner() != null) {
                    PlayerData pd2 = plugin.getDatabase().getPlayerData(pd.getOwner());
                    if (pd2 != null) {
                        l = pd2.getIslandLevel();
                    }
                }
                //Utils.sendDebug("Island level: " + l);

                final int level = l;

                //Utils.sendDebug("DEBUG: Block: " + block.getId());
                //Utils.sendDebug("DEBUG: Cobble generated. Island level = " + level);
                if (!Settings.magicCobbleGenChances.isEmpty()) {
                    Map.Entry<Integer, TreeMap<Double, Block>> entry = Settings.magicCobbleGenChances.floorEntry(level);
                    double maxValue = entry.getValue().lastKey();
                    double rnd = Utils.randomDouble() * maxValue;
                    Map.Entry<Double, Block> en = entry.getValue().ceilingEntry(rnd);
                    //Utils.sendDebug("DEBUG: " + entry.getValue().toString());
                    //Utils.sendDebug("DEBUG: Cobble generated. Island level = " + level);
                    //Utils.sendDebug("DEBUG: rnd = " + rnd + "/" + maxValue);
                    //Utils.sendDebug("DEBUG: material = " + en.getValue());
                    if (en != null) {
                        e.setCancelled(); // Cancel the event so they won't generate shits™
                        block.getLevel().setBlock(block, en.getValue());
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
    }

}
