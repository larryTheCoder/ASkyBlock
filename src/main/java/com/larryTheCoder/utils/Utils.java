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
package com.larryTheCoder.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.item.*;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandSettings;
import com.larryTheCoder.storage.SettingsFlag;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utils functions
 *
 * @author Adam Matthew
 */
public class Utils {

    public static final String SCHEMATIC_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "schematics";
    public static final String LOCALES_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "locale";
    public static final String DIRECTORY = ASkyBlock.get().getDataFolder() + File.separator;
    private static final ConcurrentHashMap<String, Long> tooSoon = new ConcurrentHashMap<>();
    private static Long x = System.nanoTime();

    public static Config loadYamlFile(String file) {
        File yamlFile = new File(DIRECTORY + file);

        Config config = null;
        if (yamlFile.exists()) {
            try {
                config = new Config();
                config.load(DIRECTORY + file, Config.YAML);
            } catch (Exception e) {
                if (ASkyBlock.get().isDebug()) {
                    e.printStackTrace();
                }
            }
        } else {
            // Create the missing file
            config = new Config();
            Utils.send("&cNo " + file + " found. Creating it...");
            try {
                if (ASkyBlock.get().getResource(file) != null) {
                    send("&cUsing default found in jar file.");
                    ASkyBlock.get().saveResource(file, false);
                    config = new Config();
                    config.load(DIRECTORY + file, Config.YAML);
                } else {
                    config.save(yamlFile);
                }
            } catch (Exception e) {
                send("&cCould not create the " + file + " file!");
            }
        }
        return config;
    }

    public static boolean canBypassTimer(Player p, String what, int seconds) {
        if (p.hasPermission("is.bypass.wait")) {
            return true;
        }
        String key = String.valueOf(what) + "." + p.getName();
        Long msBefore = tooSoon.get(key);
        Long curMS = System.currentTimeMillis();
        if (msBefore != null) {
            Long msDelta = curMS - msBefore;
            Long msWaitTime = 1000 * (long) seconds;
            return msDelta < msWaitTime;
        }
        tooSoon.put(key, curMS);
        return true;
    }

    public static String getPlayerRTime(Player p, String what, int seconds) {
        String key = String.valueOf(what) + "." + p.getName();
        Long msBefore = tooSoon.get(key);
        Long curMS = System.currentTimeMillis();
        Long msDelta = curMS - msBefore;
        Long msWaitTime = 1000 * (long) seconds;
        return Utils.convertTimer(msWaitTime - msDelta);
    }

    /**
     * Loads the chunk in the certain area
     *
     * @param loc The location of chunks target
     */
    public static void loadChunkAt(Position loc) {
        if (loc != null && !loc.getLevel().isChunkLoaded((int) loc.getX() >> 4, (int) loc.getZ() >> 4)) {
            loc.getLevel().loadChunk((int) loc.getX() >> 4, (int) loc.getZ() >> 4);
        }
    }

    /**
     * Converts a serialized location to a Location. Returns null if string is
     * empty
     *
     * @param s - serialized location in format "world:x:y:z"
     * @return Location
     */
    public static Location getLocationString(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            final Level w = Server.getInstance().getLevelByName(parts[0]);
            if (w == null) {
                return null;
            }
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            return new Location(x, y, z, 0, 0, w);
        } else if (parts.length == 6) {
            final Level w = Server.getInstance().getLevelByName(parts[0]);
            if (w == null) {
                return null;
            }
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            final float yaw = Float.intBitsToFloat(Integer.parseInt(parts[4]));
            final float pitch = Float.intBitsToFloat(Integer.parseInt(parts[5]));
            return new Location(x, y, z, yaw, pitch, w);
        }
        return null;
    }

    /**
     * Converts a location to a simple string representation If location is
     * null, returns empty string.
     * <p>
     * Format: x:y:z:yaw:pitch:level
     *
     * @param location
     * @return String of location
     */
    static public String getStringLocation(final Location location) {
        if (location == null || location.getLevel() == null) {
            return "";
        }
        return location.getFloorX() + ":" + location.getFloorY() + ":" + location.getFloorZ() + ":" + Float.floatToRawIntBits((float) location.getYaw()) + ":" + Float.floatToIntBits((float) location.getPitch()) + ":" + location.getLevel().getName();
    }

    /**
     * Returns of the shortened location
     *
     * @param loc The position of the location
     * @return String parameters
     */
    public static String locationShorted(Position loc) {
        if (loc == null) {
            return "Unknown";
        }
        return "(" + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ() + ")";
    }

    // MAPPING STRINGS ---- Start ----

    /**
     * This function changes string to map
     * Warning: This function has been detected to be an error and false casting
     * please do not use this
     *
     * @param append String to be serialized
     * @return HashMap
     */
    @Deprecated
    public static HashMap stringToMap(String append) {
        if (append.isEmpty()) {
            return new HashMap<>();
        }
        HashMap<Object, Object> errs = new HashMap<>();
        String[] at = append.split(", ");
        for (String string : at) {
            String[] at2 = string.split(":");
            ArrayList<String> atd = new ArrayList<>(Arrays.asList(at2));
            errs.put(atd.get(0), atd.get(1));
        }
        return errs;
    }

    /**
     * This method changes an array to be string
     * This function only applicable for string
     *
     * @param arr ArrayList or List class
     * @return String, empty if the array is null or empty
     */
    public static String arrayToString(List<String> arr) {
        if (arr == null || arr.isEmpty()) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        arr.forEach((str) -> {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(str);
        });
        return buf.toString();
    }

    /**
     * Serialize the string to an array
     *
     * @param string String to be decoded
     * @param commas Prefix or sub-prefix
     * @return A clean list of array
     */
    public static ArrayList<String> stringToArray(String string, String commas) {
        if (string.isEmpty()) {
            return new ArrayList<>();
        }
        String[] at = string.split(commas);
        return new ArrayList<>(Arrays.asList(at));
    }

    // MAPPING STRINGS ---- End ----

    public static boolean isNumeric(final String str) {
        if (str == null) {
            return false;
        }
        for (int sz = str.length(), i = 0; i < sz; ++i) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void EnsureDirectory(String dirName) {
        File pDir = new File(dirName);
        if (pDir.isDirectory()) {
            return;
        }
        try {
            Server.getInstance().getLogger().info("§aCreating directory: " + dirName);
            pDir.mkdir();
        } catch (Throwable exc) {
            Server.getInstance().getLogger().info("§eEnsureDirectory " + dirName + ": " + exc.toString());
        }
    }

    public static void send(String msg) {
        try {
            Server.getInstance().getLogger().info(ASkyBlock.get().getPrefix() + TextFormat.GREEN + msg.replace("&", "§"));
        } catch (Throwable exc) {
            System.out.println("ASkyBlock failed to send: " + msg);
        }
    }

    private static String convertTimer(long ms) {
        int secs = (int) (ms / 1000 % 60);
        int mins = (int) (ms / 1000 / 60 % 60);
        return String.format("%02dm %02ds", mins, secs);
    }

    public static int secondsAsMillis(int sec) {
        int ms = (sec * 60);
        return ms;
    }

    /**
     * Saves a YAML file
     *
     * @param yamlFile
     * @param fileLocation
     */
    public static void saveYamlFile(Config yamlFile, String fileLocation) {
        File file = new File(DIRECTORY + fileLocation);

        try {
            yamlFile.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<String> chop(TextFormat color, String longLine, int length) {
        List<String> result = new ArrayList<>();
        int i = 0;
        for (i = 0; i < longLine.length(); i += length) {

            int endIndex = Math.min(i + length, longLine.length());
            String line = longLine.substring(i, endIndex);
            // Do the following only if i+length is not the end of the string
            if (endIndex < longLine.length()) {
                // Check if last character in this string is not a space
                if (!line.substring(line.length() - 1).equals(" ")) {
                    // If it is not a space, check to see if the next character
                    // in long line is a space.
                    if (!longLine.substring(endIndex, endIndex + 1).equals(" ")) {
                        // If it is not, then we are cutting a word in two and
                        // need to backtrack to the last space if possible
                        int lastSpace = line.lastIndexOf(" ");
                        // Only do this if there is a space in the line to
                        // backtrack to...
                        if (lastSpace != -1 && lastSpace < line.length()) {
                            line = line.substring(0, lastSpace);
                            i -= (length - lastSpace - 1);
                        }
                    }
                }
            }
            result.add(color + line);
        }
        return result;
    }


    /**
     * Check physical activity of the player
     *
     * @param p    Player
     * @param type Block
     * @return true if the action is allowed
     */
    public static boolean actionPhysical(Player p, Block type) {
        // Settings priority
        IslandSettings data;
        if (ASkyBlock.get().getIslandInfo(p.getLocation()) != null) {
            data = ASkyBlock.get().getIslandInfo(p.getLocation()).getIgsSettings();
        } else {
            data = new IslandSettings(null);
        }

        // Checked nukkit source code, the only things that triggers PHYSICAL
        // Is these dudes.
        if (type instanceof BlockFarmland) {
            return data.getIgsFlag(SettingsFlag.BREAK_BLOCKS);
        } else {
            return data.getIgsFlag(SettingsFlag.PRESSURE_PLATE);
        }
    }

    /**
     * @return random long number using XORShift random number generator
     */
    public static long randomLong() {
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return Math.abs(x);
    }

    /**
     * @return random double using XORShift random number generator
     */
    public static double randomDouble() {
        return (double) randomLong() / Long.MAX_VALUE;
    }

    /**
     * Check either interacting with an item is allowed
     * Sometimes this item could be air
     *
     * @param p    The Player
     * @param type The player's item that uses to interact
     * @return true if the interacting is allowed
     */
    public static boolean isItemAllowed(Player p, Item type) {
        // User is placing a block, let the other event
        // Do its task, avoiding too much data gathering
        if (type.canBePlaced()) {
            return true;
        }

        // Check if the island have the settings
        IslandSettings data;
        if (ASkyBlock.get().getIslandInfo(p.getLocation()) != null) {
            data = ASkyBlock.get().getIslandInfo(p.getLocation()).getIgsSettings();
        } else {
            data = new IslandSettings(null);
        }

        if (type instanceof ItemEgg) {
            //Utils.sendDebug"User is interacting with chicken egg");
            return data.getIgsFlag(SettingsFlag.EGGS);
        } else if (type instanceof ItemSpawnEgg) {
            //Utils.sendDebug"User is interacting with spawn egg");
            return data.getIgsFlag(SettingsFlag.SPAWN_EGGS);
        } else if (type instanceof ItemShears) {
            // FIXME: This should check if the player is shearing the sheep, not shearing the wood
            //return data.getIgsFlag(SettingsFlag.SHEARING);
            //Utils.sendDebug"Using shears, not yet implemented for interaction");
        } else if (type instanceof ItemFlintSteel) {
            //Utils.sendDebug"User is interacting with flint and steel");
            return data.getIgsFlag(SettingsFlag.FIRE);
        }

        return false;
    }

    public static boolean isInventoryAllowed(Player p, Block type) {
        // Classic
        if (type.getId() == 0) {
            return true;
        }

        // Check if the island have the settings
        IslandSettings data;
        if (ASkyBlock.get().getIslandInfo(p.getLocation()) != null) {
            sendDebug("DEBUG: Settings available");
            data = ASkyBlock.get().getIslandInfo(p.getLocation()).getIgsSettings();
        } else {
            sendDebug("DEBUG: Settings unavailable");
            data = new IslandSettings(null);
        }

        if (type instanceof BlockAnvil) {
            sendDebug("DEBUG: Type of check is anvil");
            return data.getIgsFlag(SettingsFlag.ANVIL);
        } else if (type instanceof BlockChest || type instanceof BlockHopper || type instanceof BlockDispenser) {
            sendDebug("DEBUG: Type of check is chest");
            return data.getIgsFlag(SettingsFlag.CHEST);
        } else if (type instanceof BlockCraftingTable) {
            sendDebug("DEBUG: Type of check is workbench");
            return data.getIgsFlag(SettingsFlag.CRAFTING);
        } else if (type instanceof BlockBrewingStand) {
            sendDebug("DEBUG: Type of check is brewing stand");
            return data.getIgsFlag(SettingsFlag.BREWING);
        } else if (type instanceof BlockFurnace || type instanceof BlockFurnaceBurning) {
            sendDebug("DEBUG: Type of check is furnace");
            return data.getIgsFlag(SettingsFlag.FURNACE);
        } else if (type instanceof BlockEnchantingTable) {
            sendDebug("DEBUG: Type of check is enchantment table");
            return data.getIgsFlag(SettingsFlag.ENCHANTING);
        } else if (type instanceof BlockBed) {
            sendDebug("DEBUG: Type of check is bed");
            return data.getIgsFlag(SettingsFlag.BED);
        } else if (type instanceof BlockFire) {
            sendDebug("DEBUG: Type of check is fire");
            return data.getIgsFlag(SettingsFlag.FIRE_EXTINGUISH);
        } else if (type instanceof BlockFenceGate) {
            sendDebug("DEBUG: Type of check is gate");
            return data.getIgsFlag(SettingsFlag.GATE);
        } else if (type instanceof BlockButton || type instanceof BlockLever) {
            sendDebug("DEBUG: Type of check is lever");
            return data.getIgsFlag(SettingsFlag.LEVER_BUTTON);
        } else if (type instanceof BlockJukebox || type instanceof BlockNoteblock) {
            sendDebug("DEBUG: Type of check is noteblock / jukeblock");
            return data.getIgsFlag(SettingsFlag.MUSIC);
        } else if (type instanceof BlockDoor || type instanceof BlockTrapdoor) {
            sendDebug("DEBUG: Type of check is noteblock / jukeblock");
            return data.getIgsFlag(SettingsFlag.DOOR);
        }

        return false;
    }

    public static void sendDebug(String message) {
        Server.getInstance().getLogger().debug(message);
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     * <p>
     * Credits to mikenon on GitHub!
     * Don't forgot about tastybento.
     */
    public static String prettifyText(String ugly) {
        if (!ugly.contains("_") && (!ugly.equals(ugly.toUpperCase())))
            return ugly;
        StringBuilder fin = new StringBuilder();
        ugly = ugly.toLowerCase();
        if (ugly.contains("_")) {
            String[] split = ugly.split("_");
            int i = 0;
            for (String s : split) {
                i += 1;
                fin.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
                if (i < split.length)
                    fin.append(" ");
            }
        } else {
            fin.append(Character.toUpperCase(ugly.charAt(0))).append(ugly.substring(1));
        }
        return fin.toString();
    }
}
