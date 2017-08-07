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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.larryTheCoder.ASkyBlock;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Matthew
 */
public class Utils {

    public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static String LOCALES_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "locale";
    public static String DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator;
    public static ConcurrentHashMap<String, Long> tooSoon = new ConcurrentHashMap<>();

    public static void ClearPotionEffects(Player p) {
        try {
            p.removeAllEffects();
        } catch (Throwable exc) {
            Utils.ConsoleMsg(TextFormat.RED + "Error removing potion effect from " + TextFormat.YELLOW + p.getName() + TextFormat.RED + ": " + exc.getMessage());
        }

    }

    public static Config loadYamlFile(String file) {
        File dataFolder = ASkyBlock.get().getDataFolder();
        File yamlFile = new File(dataFolder, file);

        Config config = null;
        if (yamlFile.exists()) {
            try {
                config = new Config();
                config.load(file);
            } catch (Exception e) {
                if (ASkyBlock.get().isDebug()) {
                    e.printStackTrace();
                }
            }
        } else {
            // Create the missing file
            config = new Config();
            Utils.ConsoleMsg("&cNo " + file + " found. Creating it...");
            try {
                if (ASkyBlock.get().getResource(file) != null) {
                    ConsoleMsg("&cUsing default found in jar file.");
                    ASkyBlock.get().saveResource(file, false);
                    config = new Config();
                    config.load(file);
                } else {
                    config.save(yamlFile);
                }
            } catch (Exception e) {
                ConsoleMsg("&cCould not create the " + file + " file!");
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
            if (msDelta < msWaitTime) {
                //p.sendMessage(ASkyBlock.get().getPrefix() + TextFormat.RED + "[" + what + "] Too soon, you must wait: " + TextFormat.AQUA + Utils.TimeDeltaString_JustMinutesSecs(msWaitTime - msDelta));
                return false;
            }
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
        String e = Utils.TimeDeltaString_JustMinutesSecs(msWaitTime - msDelta);
        return e;
    }

    public static String ConcatArgs(String[] args, int startIdx) {
        StringBuilder sb = new StringBuilder();
        int i = startIdx;
        while (i < args.length) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(args[i]);
            ++i;
        }
        return sb.toString();
    }

    public static String CamelCase(String str) {
        if (str == null) {
            return "";
        }
        if (str.length() <= 0) {
            return "";
        }
        return String.valueOf(str.toUpperCase().charAt(0)) + str.substring(1).toLowerCase();
    }

    public static String GetDateStringFromLong(long dt) {
        return shortDateFormat.format(dt);
    }

    public static String LocStringShort(Location loc) {
        if (loc == null) {
            return "NULL";
        }
        return String.valueOf(loc.getLevel().getName()) + "(" + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ() + ")";
    }

    public static void loadChunkAt(Location loc) {
        if (loc != null && !loc.getLevel().isChunkLoaded(loc.getFloorX() >> 4, loc.getFloorZ() >> 4)) {
            loc.getLevel().loadChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
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
        if (s == null || s.trim() == "") {
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
     * null, returns empty string
     *
     * @param l
     * @return String of location
     */
    static public String getStringLocation(final Location location) {
        if (location == null || location.getLevel() == null) {
            return "";
        }
        return location.getFloorX() + ":" + location.getFloorY() + ":" + location.getFloorZ() + ":" + Float.floatToRawIntBits((float) location.getYaw()) + ":" + Float.floatToIntBits((float) location.getPitch()) + ":" + location.getLevel().getName();
    }

    public static String LocStringShortNoWorld(Location loc) {
        if (loc == null) {
            return "NULL";
        }
        return "(" + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ() + ")";
    }

    public static Map stringToMap(String append) {
        if (append.isEmpty()) {
            return new HashMap<>();
        }
        HashMap errs = new HashMap<>();
        String[] at = append.split(", ");
        for (String string : at) {
            String[] at2 = string.split(":");
            ArrayList<String> atd = new ArrayList<>();
            atd.addAll(Arrays.asList(at2));
            errs.put(atd.get(0), atd.get(1));
        }
        return errs;
    }

    public static String hashToString(Map err) {
        StringBuilder buf = new StringBuilder();

        HashMap<Object, Object> errs = (HashMap<Object, Object>) err;
        errs.entrySet().stream().forEach((fer) -> {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(fer.getKey()).append(":").append(String.valueOf(fer.getValue()));
        });
        return buf.toString();
    }

    public static String arrayToString(List arr) {
        if (arr == null || arr.isEmpty()) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        arr.stream().forEach((str) -> {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(str);
        });
        return buf.toString();
    }

    public static ArrayList<String> stringToArray(String charc, String commas) {
        if (charc.isEmpty()) {
            return new ArrayList<>();
        }
        String[] at = charc.split(commas);
        ArrayList<String> atd = new ArrayList<>();
        atd.addAll(Arrays.asList(at));
        return atd;
    }

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

    public static boolean EnsureDirectory(String dirName) {
        File pDir = new File(dirName);
        if (pDir.isDirectory()) {
            return false;
        }
        try {
            Server.getInstance().getLogger().info("Creating directory: " + dirName);
            pDir.mkdir();
        } catch (Throwable exc) {
            Server.getInstance().getLogger().error("EnsureDirectory " + dirName + ": " + exc.toString());
        }
        return true;
    }

    public static void ConsoleMsg(String msg) {
        try {
            Server.getInstance().getLogger().info(ASkyBlock.get().getPrefix() + TextFormat.WHITE + msg.replace("&", "§"));
        } catch (Throwable exc) {
            System.out.println("SkyBlock: Failed to Write ConsoleMsg: " + msg);
        }
    }

    static void DisableFlyForUser(Player p) {
        //TO-DO
    }

    public static String RainbowString(String str) {
        return Utils.RainbowString(str, "");
    }

    public static boolean IsPotionHarmful(Potion pe) {
        if (pe == null) {
            return false;
        }
        Effect pet = pe.getEffect();
        if (pet.getId() == 18) {
            return true;
        }
        if (pet.getId() == 19) {
            return true;
        }
        if (pet.getId() == 2) {
            return true;
        }
        return pet.getId() == 7;
    }

    public static String RainbowString(String str, String ctl) {
        if (ctl.equalsIgnoreCase("x")) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        boolean useBold = ctl.indexOf(98) >= 0;
        boolean useItalics = ctl.indexOf(105) >= 0;
        boolean useUnderline = ctl.indexOf(117) >= 0;
        int i = 0;
        while (i < str.length()) {
            switch (idx % 6) {
                case 0:
                    sb.append(TextFormat.RED);
                    break;
                case 1:
                    sb.append(TextFormat.GOLD);
                    break;
                case 2:
                    sb.append(TextFormat.YELLOW);
                    break;
                case 3:
                    sb.append(TextFormat.GREEN);
                    break;
                case 4:
                    sb.append(TextFormat.AQUA);
                    break;
                case 5:
                    sb.append(TextFormat.LIGHT_PURPLE);
                    break;
                default:
                    break;
            }
            if (useBold) {
                sb.append(TextFormat.BOLD);
            }
            if (useItalics) {
                sb.append(TextFormat.ITALIC);
            }
            if (useUnderline) {
                sb.append(TextFormat.UNDERLINE);
            }
            sb.append(str.charAt(i));
            if (str.charAt(i) != ' ') {
                ++idx;
            }
            ++i;
        }
        return sb.toString();
    }

    public static String TimeDeltaString_JustMinutesSecs(long ms) {
        int secs = (int) (ms / 1000 % 60);
        int mins = (int) (ms / 1000 / 60 % 60);
        return String.format("%02dm %02ds", mins, secs);
    }

    public static long secondsAsMillis(long sec) {
        long ms = (sec * 60);
        return ms;
    }

    /**
     * Saves a YAML file
     *
     * @param yamlFile
     * @param fileLocation
     */
    public static void saveYamlFile(Config yamlFile, String fileLocation) {
        File dataFolder = ASkyBlock.get().getDataFolder();
        File file = new File(dataFolder, fileLocation);

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

    public static String prettifyText(String toString) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static String secureCheck(int i) {
        int returner;
        returner = Integer.rotateLeft(i, 3);
        returner += Integer.reverse(returner);
        return Integer.toHexString(returner);
    }
}
