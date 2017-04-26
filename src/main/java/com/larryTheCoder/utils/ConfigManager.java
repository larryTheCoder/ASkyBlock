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
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.util.ArrayList;
import com.larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class ConfigManager {

    public static void load() {
        int error = 0;
        Config cfg = new Config(new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);

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
                    error += 1;
                } catch (Exception e) {
                    if (ASkyBlock.get().isDebug()) {
                        e.printStackTrace();
                    }
                    Server.getInstance().getLogger().error("Problem loading chest item from config.yml so skipping it: " + chestItemString[i]);
                    Server.getInstance().getLogger().info("Potential material types are: ");
                    Item.getCreativeItems().stream().forEach((c) -> {
                        Server.getInstance().getLogger().info(c.getName());
                    });
                    error += 1;
                }
            }
            Settings.chestItems = tempChest;
        } else {
            // Nothing in the chest
            Settings.chestItems = new Item[0];
        }

        // Island Size
        int islandDistance = cfg.getInt("island.islandSize", 200);
        if (cfg.get("island.islandSize") != null) {
            try {
                Settings.islandSize = islandDistance;
                Settings.protectionrange = (islandDistance / 2);
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Invalid IslandSize setting");
                error += 1;
            }
            if (Settings.islandSize < 10) {
                Utils.ConsoleMsg("IslandSize too small. Using islandSize: 100 instead.");
                Settings.islandSize = 100;
                error += 1;
            }

        }

        // island Hieght
        int islandHieght = cfg.getInt("island.islandHieght", 60);
        if (cfg.get("island.islandHieght") != null) {
            try {
                Settings.islandHieght = islandHieght;
            } catch (Throwable ignore) {
                Utils.ConsoleMsg("Invalid islandHieght setting");
                error += 1;
            }
            if (Settings.islandHieght < 10 || Settings.islandHieght > 257) {
                Utils.ConsoleMsg("IslandHieght too BIG!. Using islandHieght: 60 instead.");
                Settings.islandHieght = 100;
                error += 1;
            }

        }

        //restriced commands
        String cmd = cfg.getString("island.restrictedCommands", "");
        if (cfg.get("island.restrictedCommands") != null) {
            Settings.bannedCommands = new ArrayList<>();
            try {
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
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Check your config! [Restricted Commands]");
                error += 1;
            }
        }

        // Island Sea level
        int sealevel = cfg.getInt("island.seaLevel", 5);
        if (cfg.get("island.seaLevel") != null) {
            try {
                Settings.seaLevel = sealevel;
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Check your config! [SeaLevel]");
                error += 1;
            }
        }
        // Reset for players
        int islandTimer = cfg.getInt("island.resetPerPlayer", 5);
        if (cfg.get("island.resetPerPlayer") != null) {
            try {
                Settings.reset = islandTimer;
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Check your config! [Reset PerPlayer!]");
                error += 1;
            }
        }

        // Island timeout
        int members = cfg.getInt("island.timeOut", 10);
        if (cfg.get("island.timeOut") != null) {
            try {
                Settings.memberTimeOut = members;
            } catch (Throwable exc) {
                Utils.ConsoleMsg("Check your config! [IslandTimeOut]");
                error += 1;
            }
        }

        // Companion Names
        String names = cfg.getString("island.companionNames", "&aFood?");
        if (cfg.get("island.companionNames") != null) {
            try {
                final String[] name = names.split(", ");
                for (String name1 : name) {
                    Settings.companionNames.add(name1.replace("&", "§"));
                }

            } catch (Throwable exc) {
                Utils.ConsoleMsg("Check your config! [companionNames]");
                error += 1;
            }
        }
        if (cfg.get("island.gamemode") != null) {
            try {
                boolean enabled = cfg.getBoolean("island.gamemode.enable");
                if (enabled) {
                    int mode = cfg.getInt("island.gamemode.mode");
                    Settings.gamemode = mode;
                } else {
                    Settings.gamemode = -1;
                }
            } catch (Throwable ex) {
                Utils.ConsoleMsg("Check your config! [gamemode]");
                error += 1;
            }
        }
        if (cfg.get("island.chunkResetPerBlocks") != null) {
            try {
                int mode = cfg.getInt("island.chunkResetPerBlocks");
                Settings.maxBlocks = mode;
            } catch (Throwable ex) {
                Utils.ConsoleMsg("Check your config! [gamemode]");
                error += 1;
            }
        }
        if (error > 5) {
            Utils.ConsoleMsg(TextFormat.RED + "You might check your config.yml!");
            Utils.ConsoleMsg(TextFormat.RED + "Make sure it is in the right format");
        } else {
            Utils.ConsoleMsg(TextFormat.YELLOW + "Seccessfully checked config.yml with " + TextFormat.RED + error + TextFormat.YELLOW + " Errors");
        }
    }
}
