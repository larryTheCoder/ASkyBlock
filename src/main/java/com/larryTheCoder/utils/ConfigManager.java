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
package com.larryTheCoder.utils;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.economy.EconomyAPI;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.locales.FileLister;
import com.larryTheCoder.storage.IslandData.SettingsFlag;
import com.larryTheCoder.task.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Adam Matthew
 */
public class ConfigManager {

    public static final String CONFIG_VERSION = "ChangingTheWorld";

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public static void load() {
        Config cfg = new Config(new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);

        // The order in this file should match the order in config.yml so that it is easy to check that everything is covered
        // ********************** Island settings **************************
        Settings.maxHome = cfg.getInt("maxhome", 10);
        Settings.updater = cfg.getBoolean("updater");
        scheduleCheck(cfg.getBoolean("economy.enable"), cfg);
        Settings.islandDistance = cfg.getInt("island.islandSize", 200);
        Settings.islandHieght = cfg.getInt("island.islandHieght", 100);
        Settings.protectionrange = cfg.getInt("island.protectionRange", 100);
        if (Settings.protectionrange % 2 != 0) {
            Settings.protectionrange--;
            Utils.send("Protection range must be even, using " + Settings.protectionrange);
        }
        if (Settings.protectionrange > Settings.islandDistance) {
            Utils.send("Protection range cannot be > island distance. Setting them to be equal.");
            Settings.protectionrange = Settings.islandDistance;
        }
        if (Settings.protectionrange < 0) {
            Settings.protectionrange = 0;
        }
        // xoffset and zoffset are not public and only used for IslandWorld compatibility
        Settings.islandXOffset = cfg.getInt("island.xoffset", 0);
        if (Settings.islandXOffset < 0) {
            Settings.islandXOffset = 0;
            Utils.send("Setting minimum island X Offset to 0");
        } else if (Settings.islandXOffset > Settings.islandDistance) {
            Settings.islandXOffset = Settings.islandDistance;
            Utils.send("Setting maximum island X Offset to " + Settings.islandDistance);
        }
        Settings.islandZOffset = cfg.getInt("island.zoffset", 0);
        if (Settings.islandZOffset < 0) {
            Settings.islandZOffset = 0;
            Utils.send("Setting minimum island Z Offset to 0");
        } else if (Settings.islandZOffset > Settings.islandDistance) {
            Settings.islandZOffset = Settings.islandDistance;
            Utils.send("Setting maximum island Z Offset to " + Settings.islandDistance);
        }
        Settings.teamChat = cfg.getBoolean("teamChannels", true);
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
        Settings.chestInventoryOverride = cfg.getBoolean("island.items.shouldOverride", false);
        Settings.resetTime = cfg.getInt("island.deleteTiming", 0);
        String chestItems = cfg.getString("island.items.chestItems", "");
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
        Settings.stopTime = cfg.getBoolean("island.stopTime");
        Settings.saveInventory = cfg.getBoolean("island.saveInventory");
        // Challenges
        Settings.broadcastMessages = cfg.getBoolean("general.broadcastmessages", true);
        // ******************** Biome Settings *********************
        Settings.biomeCost = cfg.getDouble("biomesettings.defaultcost", 100D);
        if (Settings.biomeCost < 0D) {
            Settings.biomeCost = 0D;
            Utils.send("Biome default cost is < $0, so set to zero.");
        }
        String defaultBiome = cfg.getString("biomesettings.defaultbiome", "PLAINS");
        try {
            // re-check if the biome exsits
            Settings.defaultBiome = Biome.getBiome(defaultBiome);
        } catch (Exception e) {
            Utils.send("Could not parse biome " + defaultBiome + " using PLAINS instead.");
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
        for (Iterator<String> it = protectionWorld.getKeys(false).iterator(); it.hasNext(); ) {
            String setting = it.next();
            try {
                SettingsFlag flag = SettingsFlag.valueOf(setting.toUpperCase());
                boolean value = cfg.getBoolean("protection.world." + flag.name());
                Settings.defaultWorldSettings.put(flag, value);
                Settings.defaultSpawnSettings.put(flag, value);
                Settings.defaultIslandSettings.put(flag, value);
            } catch (Exception e) {
                Utils.send("Unknown setting in config.yml:protection.world " + setting.toUpperCase() + " skipping...");
            }
        }
        // Get the default language
        Settings.defaultLanguage = cfg.getString("general.defaultlanguage", "en-US");

        // Load languages
        HashMap<String, ASlocales> availableLocales = new HashMap<>();
        FileLister fl = new FileLister(ASkyBlock.get());
        try {
            int index = 1;
            for (String code : fl.list()) {
                //plugin.getLogger().info("DEBUG: lang file = " + code);
                availableLocales.put(code, new ASlocales(ASkyBlock.get(), code, index++));
            }
        } catch (IOException e1) {
            Utils.send("&cCould not add locales!");
        }
        if (!availableLocales.containsKey(Settings.defaultLanguage)) {
            Utils.send("&c'" + Settings.defaultLanguage + ".yml' not found in /locale folder. Using /locale/en-US.yml");
            Settings.defaultLanguage = "en-US";
            availableLocales.put(Settings.defaultLanguage, new ASlocales(ASkyBlock.get(), Settings.defaultLanguage, 0));
        }
        ASkyBlock.get().setAvailableLocales(availableLocales);
        // GridProtection
        Settings.shouldTeleportSpawn = cfg.getBoolean("grid.teleportSpawn", false);
        Utils.send(TextFormat.YELLOW + "Seccessfully checked config.yml");
    }

    private static void scheduleCheck(boolean flag, Config cfg) {
        if (flag) {
            Plugin plugin = ASkyBlock.get().getServer().getPluginManager()
                .getPlugin("EconomyAPI");
            if (plugin != null && !plugin.isEnabled()) {
                Utils.send("&eScheduling Economy instance due to 'plugin not enabled'");
                // schedule another delayed task
                TaskManager.runTaskLater(() -> {
                    scheduleCheck(true, cfg);
                }, 3); // 3 sec
            } else if (plugin != null && plugin.isEnabled()) {
                Utils.send("&eSeccessfully created an instance with Economy plugin");
                ASkyBlock.econ = new EconomyAPI();
                Settings.useEconomy = true;
            } else {
                Utils.send("&eError: No economy plugin were found!");
                Settings.useEconomy = false;
            }
            if (Settings.useEconomy) {
                Settings.islandCost = cfg.getDouble("economy.islandCost", 5);
                Settings.firstIslandFree = !cfg.getBoolean("economy.payNewIsland", false);
            }
        }
    }
}
