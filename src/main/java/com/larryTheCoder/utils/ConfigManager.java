/*
 * Copyright (C) 2016 larryTheHarry 
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
package com.larryTheCoder.utils;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.util.ArrayList;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData.SettingsFlag;
import java.util.Iterator;
import java.util.List;

/**
 * @author larryTheCoder
 */
public class ConfigManager {

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public static void load() {
        Config cfg = new Config(new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);

        // The order in this file should match the order in config.yml so that it is easy to check that everything is covered
        // ********************** Island settings **************************
        Settings.maxHome = cfg.getInt("maxhome", 10);
        Settings.updater = cfg.getBoolean("updater");
        Settings.islandDistance = cfg.getInt("island.islandSize", 200);
        Settings.islandHieght = cfg.getInt("island.islandHieght", 100);
        Settings.protectionrange = cfg.getInt("island.protectionRange", 100);
        if (Settings.protectionrange % 2 != 0) {
            Settings.protectionrange--;
            Utils.ConsoleMsg("Protection range must be even, using " + Settings.protectionrange);
        }
        if (Settings.protectionrange > Settings.islandDistance) {
            Utils.ConsoleMsg("Protection range cannot be > island distance. Setting them to be equal.");
            Settings.protectionrange = Settings.islandDistance;
        }
        if (Settings.protectionrange < 0) {
            Settings.protectionrange = 0;
        }
        // xoffset and zoffset are not public and only used for IslandWorld compatibility
        Settings.islandXOffset = cfg.getInt("island.xoffset", 0);
        if (Settings.islandXOffset < 0) {
            Settings.islandXOffset = 0;
            Utils.ConsoleMsg("Setting minimum island X Offset to 0");
        } else if (Settings.islandXOffset > Settings.islandDistance) {
            Settings.islandXOffset = Settings.islandDistance;
            Utils.ConsoleMsg("Setting maximum island X Offset to " + Settings.islandDistance);
        }
        Settings.islandZOffset = cfg.getInt("island.zoffset", 0);
        if (Settings.islandZOffset < 0) {
            Settings.islandZOffset = 0;
            Utils.ConsoleMsg("Setting minimum island Z Offset to 0");
        } else if (Settings.islandZOffset > Settings.islandDistance) {
            Settings.islandZOffset = Settings.islandDistance;
            Utils.ConsoleMsg("Setting maximum island Z Offset to " + Settings.islandDistance);
        }
        Settings.facebook = cfg.getBoolean("chat.teamChat", true);
        Settings.islandMaxNameLong = cfg.getInt("island.nameLimit", 20);
        Settings.cleanrate = cfg.getInt("island.chunkResetPerBlocks", 256);
        Settings.seaLevel = cfg.getInt("island.seaLevel", 3);
        String cmd = cfg.getString("island.restrictedCommands", "");
        final String[] pieces = cmd.substring(cmd.length()).trim().split(",");
        String[] array;
        for (int length = (array = pieces).length, i = 0; i < length; ++i) {
            final String piece = array[i];
            if (piece != null) {
                if (piece.length() > 0) {
                    Settings.bannedCommands.add(piece);
                }
            }
        }
        Settings.reset = cfg.getInt("island.reset", 0);
        Settings.gamemode = cfg.getInt("island.gamemode", 0);
        Settings.memberTimeOut = cfg.getInt("island.timeOut", 0);
        // Companion names
        List<String> companionNames = cfg.getStringList("island.companionnames");
        Settings.companionNames = new ArrayList<>();
        companionNames.forEach((name) -> {
            Settings.companionNames.add(TextFormat.colorize('&', name));
        });
        //Chest Items
        String chestItems = cfg.getString("island.chestItems", "");
        // Check chest items
        if (!chestItems.isEmpty()) {
            final String[] chestItemString = chestItems.split(" ");
            // getLogger().info("DEBUG: chest items = " + chestItemString);
            final Item[] tempChest = new Item[chestItemString.length];
            for (int i = 0; i < tempChest.length; i++) {
                String[] amountdata = chestItemString[i].split(":");
                try {
                    Item mat;
                    if (Utils.isNumeric(amountdata[0])) {
                        mat = Item.get(Integer.parseInt(amountdata[0]));
                    } else {
                        mat = Item.fromString(amountdata[0].toUpperCase());
                    }
                    if (amountdata.length == 2) {
                        tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountdata[1]));
                    } else if (amountdata.length == 3) {
                        tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountdata[2]), Integer.parseInt(amountdata[1]));
                    }

                } catch (java.lang.IllegalArgumentException ex) {
                    if (ASkyBlock.get().isDebug()) {
                        ex.printStackTrace();
                    }
                    Server.getInstance().getLogger().error("Problem loading chest item from config.yml so skipping it: " + chestItemString[i]);
                    Server.getInstance().getLogger().error("Error is : " + ex.getMessage());
                } catch (Exception e) {
                    if (ASkyBlock.get().isDebug()) {
                        e.printStackTrace();
                    }
                    Server.getInstance().getLogger().error("Problem loading chest item from config.yml so skipping it: " + chestItemString[i]);
                    Server.getInstance().getLogger().info("Potential material types are: ");
                    Item.getCreativeItems().stream().forEach((c) -> {
                        Server.getInstance().getLogger().info(c.getName());
                    });
                }
            }
            Settings.chestItems = tempChest;
        } else {
            // Nothing in the chest
            Settings.chestItems = new Item[0];
        }
        Settings.saveInventory = cfg.getBoolean("island.saveInventory");
        Settings.stclock = new Location(cfg.getInt("lobby.lobbyX"), cfg.getInt("lobby.lobbyY"), cfg.getInt("lobby.lobbyZ"), Server.getInstance().getLevelByName(cfg.getString("lobby.world")));
        // Challenges
        Settings.broadcastMessages = cfg.getBoolean("general.broadcastmessages", true);
        // ******************** Biome Settings *********************
        Settings.biomeCost = cfg.getDouble("biomesettings.defaultcost", 100D);
        if (Settings.biomeCost < 0D) {
            Settings.biomeCost = 0D;
            Utils.ConsoleMsg("Biome default cost is < $0, so set to zero.");
        }
        String defaultBiome = cfg.getString("biomesettings.defaultbiome", "PLAINS");
        try {
            // re-check if the biome exsits
            Settings.defaultBiome = Biome.getBiome(defaultBiome);
        } catch (Exception e) {
            Utils.ConsoleMsg("Could not parse biome " + defaultBiome + " using PLAINS instead.");
            Settings.defaultBiome = Biome.getBiome(Biome.PLAINS);
        }

        // System utils eg., Default world protection, cancel teleport distance
        // ************ Protection Settings ****************
        // Default settings hashmaps - make sure this is kept up to date with new settings
        // If a setting is not listed, the world default is used
        Settings.defaultWorldSettings.clear();
        Settings.defaultIslandSettings.clear();
        Settings.defaultSpawnSettings.clear();
        Settings.visitorSettings.clear();
        ConfigSection protectionWorld = cfg.getSections("protection.world");
        for (Iterator<String> it = protectionWorld.getKeys(false).iterator(); it.hasNext();) {
            String setting = it.next();
            try {
                SettingsFlag flag = SettingsFlag.valueOf(setting.toUpperCase());
                boolean value = cfg.getBoolean("protection.world." + flag.name());
                Settings.defaultWorldSettings.put(flag, value);
                Settings.defaultSpawnSettings.put(flag, value);
                Settings.defaultIslandSettings.put(flag, value);
            } catch (Exception e) {
                Utils.ConsoleMsg("Unknown setting in config.yml:protection.world " + setting.toUpperCase() + " skipping...");
            }
        }
        Utils.ConsoleMsg(TextFormat.YELLOW + "Seccessfully checked config.yml");
    }
}
