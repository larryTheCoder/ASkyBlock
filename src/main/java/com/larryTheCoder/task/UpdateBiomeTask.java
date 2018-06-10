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
 *
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
                                    chunk.setBiomeId(x, z, getBiome(pd.getBiome()).getId());
                                }
                            }
                        }
                        level.generateChunkCallback(chunk.getX(), chunk.getZ(), chunk);
                        iChunk.remove();
                    }
                    if (biomeToChanged.isEmpty()) {
                        player.sendMessage(plugin.getPrefix() + plugin.getLocale(p).biomeChangeComplete.replace("[biome]", pd.getBiome()));
                        this.cancel();
                    }
                }

            }.runTaskTimer(plugin, 0, 20);
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
