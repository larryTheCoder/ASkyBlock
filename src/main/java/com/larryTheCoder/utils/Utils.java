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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.item.*;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.google.common.base.Preconditions;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.SkyBlockGenerator;
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.cache.settings.IslandSettings;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utils functions
 *
 * @author larryTheCoder
 */
public class Utils {

    public static final String UPDATES_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "updates" + File.separator;
    public static final String SCHEMATIC_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "schematics" + File.separator;
    public static final String LOCALES_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "locale" + File.separator;
    public static final String DIRECTORY = ASkyBlock.get().getDataFolder() + File.separator;
    public static final Map<String, Runnable> TASK_SCHEDULED = new HashMap<>();
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

    public static String hashObject(String hashedObject) {
        // Hashing works well, SHA-256 Hashing instance
        // PS: You are not encrypting, you are hashing.
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        md.update(hashedObject.getBytes());

        byte[] byteData = md.digest();

        // Convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteData.length / 2; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static boolean canBypassTimer(Player p, String what, int seconds) {
        if (p.hasPermission("is.bypass.wait")) {
            return true;
        }
        String key = what + "." + p.getName();
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
        String key = what + "." + p.getName();
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
        if (loc != null && !loc.getLevel().isChunkLoaded(loc.getChunkX(), loc.getChunkZ())) {
            loc.getLevel().loadChunk(loc.getChunkZ(), loc.getChunkZ());
        }
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

    public static boolean isNumeric(final Object str) {
        if (!(str instanceof String)) {
            return str instanceof Integer;
        }
        String intnum = (String) str;
        for (int sz = intnum.length(), i = 0; i < sz; ++i) {
            if (!Character.isDigit(intnum.charAt(i))) {
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
        int min = (int) (ms / 1000 / 60 % 60);
        return String.format("%02dm %02ds", min, secs);
    }

    public static int secondsAsMillis(int sec) {
        return (sec * 60);
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
        IslandData pd = ASkyBlock.get().getFastCache().getIslandData(p.getLocation());
        IslandSettings data = pd == null ? new IslandSettings("") : pd.getIgsSettings();

        // Checked nukkit source code, the only things that triggers PHYSICAL
        // Is these dudes.
        if (type instanceof BlockFarmland) {
            return data.getIgsFlag(SettingsFlag.BREAK_BLOCKS);
        } else {
            return data.getIgsFlag(SettingsFlag.PRESSURE_PLATE);
        }
    }

    /**
     * Unpair a string into a valid vector3 coordinates.
     *
     * @param pos A compressed integer by {@link Utils#getVector3Pair}
     * @return a valid {@link Vector3} class
     */
    public static Vector3 unpairVector3(String pos) {
        String[] list = pos.split(":");
        return new Vector3(Integer.parseInt(list[0]), Integer.parseInt(list[1]), Integer.parseInt(list[2]));
    }

    /**
     * Unpair a string into a valid vector2 coordinates.
     *
     * @param pos A compressed integer by {@link Utils#getVector3Pair}
     * @return a valid {@link Vector3} class
     */
    public static Vector2 unpairVector2(String pos) {
        String[] list = pos.split(":");
        return new Vector2(Integer.parseInt(list[0]), Integer.parseInt(list[1]));
    }

    /**
     * Compress a vector3 coordinates into one long
     * integer.
     *
     * @param vec The coordinates of the position
     * @return the compressed integer
     */
    public static String getVector3Pair(Vector3 vec) {
        if (vec == null) return "0:0:0";

        return vec.getFloorX() + ":" + vec.getFloorY() + ":" + vec.getFloorZ();
    }

    /**
     * Compress a vector2 coordinates into one long
     * integer.
     *
     * @param vec The coordinates of the position
     * @return the compressed integer
     */
    public static String getVector2Pair(Vector2 vec) {
        if (vec == null) return "0:0";

        return vec.getFloorX() + ":" + vec.getFloorY();
    }

    /**
     * @return random double using XORShift random number generator
     */
    public static double randomDouble() {
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return (double) Math.abs(x) / Long.MAX_VALUE;
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
        IslandData pd = ASkyBlock.get().getFastCache().getIslandData(p.getLocation());
        IslandSettings data = pd == null ? new IslandSettings("") : pd.getIgsSettings();

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
        IslandData pd = ASkyBlock.get().getFastCache().getIslandData(p.getLocation());
        IslandSettings data = pd == null ? new IslandSettings("") : pd.getIgsSettings();

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
        } else if (type instanceof BlockFurnace) {
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

    /**
     * Sorts map in descending order
     *
     * @param map The map of the
     * @return The serialized map in descending order
     */
    public static <Key, Value extends Comparable<? super Value>> LinkedHashMap<Key, Value> sortByValue(Map<Key, Value> map) {
        List<Map.Entry<Key, Value>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> {
            // Switch these two if you want ascending
            return (o2.getValue()).compareTo(o1.getValue());
        });

        LinkedHashMap<Key, Value> result = new LinkedHashMap<>();
        for (Map.Entry<Key, Value> entry : list) {
            result.put(entry.getKey(), entry.getValue());
            if (result.size() > 20)
                break;
        }
        return result;
    }

    public static String checkChallenge(String challengeType, String type) {
        Map<String, Boolean> challengeList = new HashMap<>();
        Map<String, Integer> challengeListTimes = new HashMap<>();

        for (String challenges : Settings.challengeList) {
            challengeList.put(challenges, false);
            challengeListTimes.put(challenges, 0);
        }

        StringBuilder buf = new StringBuilder();
        if (type.equals("cl")) {
            // Challenges encode for PlayerData.challengeList
            if (!challengeType.isEmpty()) {
                String[] at = challengeType.split(", ");
                for (String string : at) {
                    try {
                        String[] at2 = string.split(":");
                        ArrayList<String> list = new ArrayList<>(Arrays.asList(at2));

                        boolean value = list.get(1).equalsIgnoreCase("1");
                        challengeList.put(list.get(0).toLowerCase(), value);
                    } catch (Exception ignored) {
                    }
                }
            }

            challengeList.forEach((key, value) -> {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(key).append(":").append(value ? "1" : "0");
            });
        } else if (type.equals("clt")) {
            if (!challengeType.isEmpty()) {
                // Challenges encode for PlayerData.challengeListTimes
                String[] at = challengeType.split(", ");
                for (String string : at) {
                    try {
                        String[] at2 = string.split(":");
                        ArrayList<String> list = new ArrayList<>(Arrays.asList(at2));

                        challengeListTimes.put(list.get(0).toLowerCase(), Integer.parseInt(list.get(1)));
                    } catch (Exception ignored) {
                    }
                }
            }

            challengeListTimes.forEach((key, value) -> {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(key).append(":").append(value);
            });
        } else {
            Utils.send("&cUnknown challenge list: " + type + ", returning null...");
            buf.append("null");
        }
        return buf.toString();
    }

    public static void loadLevelSeed(String levelName) {
        if (!Server.getInstance().isLevelGenerated(levelName)) {
            Server.getInstance().generateLevel(levelName, 0, SkyBlockGenerator.class);
        }
        if (!Server.getInstance().isLevelLoaded(levelName)) {
            Server.getInstance().loadLevel(levelName);
        }
    }

    public static String compactSmall(String[] list) {
        Preconditions.checkArgument(list.length != 0, "The list cannot be empty or null.");

        String currentString = list[0];
        for (String str : list) if (currentString.length() > str.length()) currentString = str;

        return currentString;
    }
}
