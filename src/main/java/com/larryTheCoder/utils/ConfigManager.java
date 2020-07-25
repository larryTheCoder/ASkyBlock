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
package com.larryTheCoder.utils;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.listener.LavaCheck;
import com.larryTheCoder.utils.integration.economy.EconomyAPI;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author larryTheCoder
 */
public class ConfigManager {

    /**
     * Loads the various settings from the config.yml file into the plugin
     */
    public static void load() {
        ASkyBlock.recheck();
        Config cfg = new Config(new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);

        // The order in this file should match the order in config.yml so that it is easy to check that everything is covered
        // ********************** Island settings **************************
        Settings.checkUpdate = cfg.getBoolean("allowUpdate");
        Settings.verboseCode = cfg.getBoolean("debug", false);

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
        Settings.gameMode = cfg.getInt("island.gameMode", 0);
        Settings.memberTimeOut = cfg.getInt("island.timeOut", 0);
        //Chest Items
        Settings.resetTime = cfg.getInt("island.island-delay-timeout", 0);
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
                        tempChest[i] = new Item(mat.getId(), 0, Integer.parseInt(amountData[1]));
                    } else if (amountData.length == 3) {
                        tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountData[2]), Integer.parseInt(amountData[1]));
                    }

                } catch (IllegalArgumentException ex) {
                    if (Settings.verboseCode) {
                        ex.printStackTrace();
                    }
                    Server.getInstance().getLogger().error("Problem loading chest item from config.yml so skipping it: " + chestItemString[i]);
                    Server.getInstance().getLogger().error("Error is : " + ex.getMessage());
                } catch (Exception e) {
                    if (Settings.verboseCode) {
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
        Settings.loadCacheBefore = cfg.getInt("island.cache-load-before", 30);
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
        ConfigSection protectionWorld = cfg.getSection("protection.world");
        for (String setting : protectionWorld.getKeys(false)) {
            try {
                SettingsFlag flag = SettingsFlag.valueOf(setting.toUpperCase());
                boolean value = cfg.getBoolean("protection.world." + setting);
                Settings.defaultWorldSettings.put(flag, value);
                Settings.defaultSpawnSettings.put(flag, value);
                Settings.defaultIslandSettings.put(flag, value);

            } catch (Exception e) {
                Utils.send("&cUnknown setting in config.yml:protection.world " + setting.toUpperCase() + " skipping...");
            }
        }
        // Get the default language
        Settings.defaultLanguage = cfg.getString("defaultlanguage", "en_US");

        // Magic Cobble Generator Settings
        Settings.useMagicCobbleGen = cfg.getBoolean("general.usemagiccobblegen", false);
        if (Settings.useMagicCobbleGen) {
            ConfigSection section = cfg.getSection("general.magiccobblegenchances");
            if (!section.isEmpty()) {
                // Clear the cobble gen chances so they can be reloaded
                LavaCheck.clearChances();
                Settings.magicCobbleGenChances = new TreeMap<>();
                for (String level : section.getKeys(false)) {
                    int levelInt;
                    try {
                        if (level.equals("default")) {
                            levelInt = Integer.MIN_VALUE;
                        } else {
                            levelInt = Integer.parseInt(level);
                        }
                        TreeMap<Double, Block> blockMapTree = new TreeMap<>();
                        double chanceTotal = 0;
                        for (String block : cfg.getSection("general.magiccobblegenchances." + level).getKeys(false)) {
                            double chance = cfg.getDouble("general.magiccobblegenchances." + level + "." + block, 0);
                            Item item = Item.fromString(block);
                            if (chance > 0 && item.canBePlaced()) {
                                // Store the cumulative chance in the treemap. It does not need to add up to 100%
                                chanceTotal += chance;
                                blockMapTree.put(chanceTotal, Block.get(item.getId(), item.getDamage()));
                            }
                        }
                        if (!blockMapTree.isEmpty()) {
                            Settings.magicCobbleGenChances.put(levelInt, blockMapTree);
                        }
                        // Store the requested values as a % chance
                        Map<Block, Double> chances = new HashMap<>();
                        for (Map.Entry<Double, Block> en : blockMapTree.entrySet()) {
                            double chance = cfg.getDouble("general.magiccobblegenchances." + level + "." + en.getValue(), 0D);
                            chances.put(en.getValue(), (chance / chanceTotal) * 100);
                        }
                        LavaCheck.storeChances(levelInt, chances);
                    } catch (NumberFormatException e) {
                        // Putting the catch here means that an invalid level is skipped completely
                        Utils.send("&cUnknown level '" + level + "' listed in magiccobblegenchances section! Must be an integer or 'default'. Skipping...");
                    }
                }
            }
        }

        // ****************** Levels blocks.yml ****************
        // Get the blocks.yml file
        Config levelCfg = new Config(new File(ASkyBlock.get().getDataFolder(), "blocks.yml"), Config.YAML);

        // Get the under water multiplier
        Settings.deathPenalty = levelCfg.getInt("deathpenalty", 0);
        Settings.sumTeamDeaths = levelCfg.getBoolean("sumteamdeaths");
        Settings.maxDeaths = levelCfg.getInt("maxdeaths", 10);
        Settings.islandResetDeathReset = levelCfg.getBoolean("islandresetdeathreset", true);
        Settings.teamJoinDeathReset = levelCfg.getBoolean("teamjoindeathreset", true);
        Settings.levelCost = levelCfg.getInt("levelcost", 100);
        if (Settings.levelCost < 1) {
            Settings.levelCost = 1;
            Utils.send("&clevelcost in blocks.yml cannot be less than 1. Setting to 1.");
        }

        ConfigSection section = levelCfg.getSection("limits");
        if (!section.isEmpty()) {
            for (String material : section.getKeys(false)) {
                try {
                    String[] split = material.split(":");
                    int data = 0;
                    if (split.length > 1) {
                        data = Integer.valueOf(split[1]);
                    }
                    Block item;
                    if (Utils.isNumeric(split[0])) {
                        item = Block.get(Integer.parseInt(split[0]));
                    } else {
                        item = Item.fromString(split[0]).getBlock();
                    }
                    item.setDamage(data);
                    Settings.blockLimits.put(item.getFullId(), levelCfg.getInt("limits." + material, 0));
                } catch (Exception e) {
                    Utils.sendDebug("&eUnknown material (" + material + ") in blocks.yml Limits section. Skipping...");
                }
            }
        }

        section = levelCfg.getSection("blocks");
        if (!section.isEmpty()) {
            for (String material : section.getKeys(false)) {
                try {
                    int value = levelCfg.getInt("blocks." + material, 0);

                    String[] split = material.split(":");
                    int data = 0;
                    if (split.length > 1) {
                        data = Integer.valueOf(split[1]);
                    }

                    Block block;
                    if (Utils.isNumeric(split[0])) {
                        block = Block.get(Integer.parseInt(split[0]));
                    } else {
                        block = Item.fromString(split[0]).getBlock();
                    }
                    block.setDamage(data);

                    Settings.blockValues.put(block.getFullId(), value);
                } catch (Exception e) {
                    Utils.send("&cUnknown material (" + material + ") in blocks.yml blocks section. Skipping...");
                }
            }
        } else {
            Utils.send("&cNo block values in blocks.yml! All island levels will be zero!");
        }

        Utils.send(TextFormat.YELLOW + "Successfully checked config.yml");
    }

    private static void scheduleCheck(boolean flag, Config cfg) {
        if (flag) {
            Plugin plugin = ASkyBlock.get().getServer().getPluginManager().getPlugin("EconomyAPI");
            // Note to self: Do not attempt to create task when the server is on load
            if (plugin != null) {
                Utils.send("&aHooked with EconomyAPI plugin");
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
