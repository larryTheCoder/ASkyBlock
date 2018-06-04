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
import java.util.HashMap;

/**
 * @author Adam Matthew
 */
public class ConfigManager {

    public static final String CONFIG_VERSION = "ad7b3e9c";

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public static void load() {
        Config cfg = new Config(new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);

        // The order in this file should match the order in config.yml so that it is easy to check that everything is covered
        // ********************** Island settings **************************
        Settings.checkUpdate = cfg.getBoolean("allowUpdate");
        scheduleCheck(cfg.getBoolean("economy.enable"), cfg);
        Settings.islandHeight = cfg.getInt("island.islandHeight", 100);

        Settings.teamChat = cfg.getBoolean("teamChat", true);
        Settings.islandMaxNameLong = cfg.getInt("island.nameLimit", 20);
        Settings.cleanRate = cfg.getInt("island.chunkResetPerBlocks", 256);
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
        Settings.gameMode = cfg.getInt("island.gameMode", 0);
        Settings.memberTimeOut = cfg.getInt("island.timeOut", 0);
        //Chest Items
        Settings.resetTime = cfg.getInt("island.deleteTiming", 0);
        String chestItems = cfg.getString("island.items.chestItems", "");
        // Check chest items
        if (!chestItems.isEmpty()) {
            final String[] chestItemString = chestItems.split(" ");
            // getLogger().info("DEBUG: chest items = " + chestItemString);
            final Item[] tempChest = new Item[chestItemString.length];
            for (int i = 0; i < tempChest.length; i++) {
                String[] amountData = chestItemString[i].split(":");
                try {
                    Item mat;
                    if (Utils.isNumeric(amountData[0])) {
                        mat = Item.get(Integer.parseInt(amountData[0]));
                    } else {
                        mat = Item.fromString(amountData[0].toUpperCase());
                    }
                    if (amountData.length == 2) {
                        tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountData[1]));
                    } else if (amountData.length == 3) {
                        tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountData[2]), Integer.parseInt(amountData[1]));
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
                    Item.getCreativeItems().forEach((c) -> Server.getInstance().getLogger().info(c.getName()));
                }
            }
            Settings.chestItems = tempChest;
        } else {
            // Nothing in the chest
            Settings.chestItems = new Item[0];
        }
        Settings.saveInventory = cfg.getBoolean("island.saveInventory");
        // Challenges
        Settings.broadcastMessages = cfg.getBoolean("general.broadcastmessages", true);
        // ******************** Biome Settings *********************

        // System utils eg., Default world protection, cancel teleport distance
        // ************ Protection Settings ****************
        // Default settings hashmaps - make sure this is kept up to date with new settings
        // If a setting is not listed, the world default is used
        Settings.defaultWorldSettings.clear();
        Settings.defaultIslandSettings.clear();
        Settings.defaultSpawnSettings.clear();
        ConfigSection protectionWorld = cfg.getSections("protection.world");
        for (String setting : protectionWorld.getKeys(false)) {
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
        Utils.send(TextFormat.YELLOW + "Successfully checked config.yml");
    }

    private static void scheduleCheck(boolean flag, Config cfg) {
        if (flag) {
            Plugin plugin = ASkyBlock.get().getServer().getPluginManager().getPlugin("EconomyAPI");
            if (plugin != null && !plugin.isEnabled()) {
                Utils.send("&eScheduling Economy instance due to 'plugin not enabled'");
                // schedule another delayed task
                TaskManager.runTaskLater(() -> scheduleCheck(true, cfg), 60); // 3 sec
                return;
            } else if (plugin != null && plugin.isEnabled()) {
                Utils.send("&eSuccessfully created an instance with Economy plugin");
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
