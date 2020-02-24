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
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.scheduler.Task;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class UpdateBiomeTask extends Task {

    private final IslandData pd;
    private final CommandSender player;
    private final ASkyBlock plugin;

    public UpdateBiomeTask(ASkyBlock plugin, IslandData pd, CommandSender player) {
        this.plugin = plugin;
        this.pd = pd;
        this.player = player;
    }

    @Override
    public void onRun(int currentTick) {
        Player p = player.isPlayer() ? (Player) player : null;
        Level level = plugin.getServer().getLevelByName(pd.getLevelName());
        WorldSettings settings = plugin.getSettings(pd.getLevelName());

        int minX = pd.getMinProtectedX();
        int minZ = pd.getMinProtectedZ();
        int maxX = pd.getMinProtectedX() + pd.getProtectionSize();
        int maxZ = pd.getMinProtectedZ() + pd.getProtectionSize();

        // get the chunks for these locations
        final BaseFullChunk minChunk = level.getChunk(minX >> 4, minZ >> 4, true);
        final BaseFullChunk maxChunk = level.getChunk(maxX >> 4, maxZ >> 4, true);

        if (!minChunk.isGenerated() || !maxChunk.isGenerated()) {
            level.regenerateChunk(minChunk.getX(), minChunk.getZ());
            level.regenerateChunk(maxChunk.getX(), maxChunk.getZ());
        }

        List<BaseFullChunk> biomeToChanged = new ArrayList<>();

        // Find out what chunks are within the island protection range
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                Utils.loadChunkAt(new Position(x, 0, z, level));
                // Loop in loop are not recommended.
                // So we separate some chunks and let the task do it works
                biomeToChanged.add(level.getChunk(x, z));
            }
        }

        // Clear up any chunks in list
        if (!biomeToChanged.isEmpty()) {
            new NukkitRunnable() {

                @Override
                public void run() {
                    Iterator<BaseFullChunk> iChunk = biomeToChanged.iterator();
                    int count = 0;
                    while (iChunk.hasNext() && count++ < Settings.cleanRate) {
                        BaseFullChunk chunk = iChunk.next();
                        for (int y = settings.getSeaLevel(); y < 255 - settings.getSeaLevel(); y++) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    chunk.setBiomeId(x, z, getBiome(pd.getPlotBiome()).getId());
                                }
                            }
                        }
                        level.generateChunkCallback(chunk.getX(), chunk.getZ(), chunk);
                        iChunk.remove();
                    }
                    if (biomeToChanged.isEmpty()) {
                        player.sendMessage(plugin.getPrefix() + plugin.getLocale(p).biomeChangeComplete.replace("[biome]", pd.getPlotBiome()));
                        this.cancel();
                    }
                }

            }.runTaskTimerAsynchronously(plugin, 0, 20);
        }
    }

    /**
     * Get Biome by name.
     *
     * @param name Name of biome. Name could contain symbol "_" instead of space
     * @return Biome. Null - when biome was not found
     */
    private Biome getBiome(String name) {
        for (Biome biome : Biome.biomes) {
            if (biome != null) {
                if (biome.getName().equalsIgnoreCase(name.replace("_", " "))) return biome;
            }
        }
        return EnumBiome.PLAINS.biome;
    }
}
