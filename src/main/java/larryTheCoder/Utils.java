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
package larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author larryTheCoder
 */
public class Utils {

    public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static String LOCALES_DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator + "locales";
    public static String DIRECTORY = "plugins" + File.separator + "ASkyBlock" + File.separator;
    public static ConcurrentHashMap<String, Long> tooSoon = new ConcurrentHashMap<>();
    

    public static void ClearPotionEffects(Player p) {
        try {
            p.removeAllEffects();
        } catch (Throwable exc) {
            Utils.ConsoleMsg(TextFormat.RED + "Error removing potion effect from " + TextFormat.YELLOW + p.getName() + TextFormat.RED + ": " + exc.getMessage());
        }

    }

    public static boolean TooSoon(Player p, String what, int seconds) {
        if (p.hasPermission("is.bypass.wait")) {
            return false;
        }
        String key = String.valueOf(what) + "." + p.getName();
        Long msBefore = tooSoon.get(key);
        Long curMS = System.currentTimeMillis();
        if (msBefore != null) {
            Long msDelta = curMS - msBefore;
            Long msWaitTime = 1000 * (long) seconds;
            if (msDelta < msWaitTime) {
                p.sendMessage(TextFormat.RED + "[" + what + "] Too soon, you must wait: " + TextFormat.AQUA + Utils.TimeDeltaString_JustMinutesSecs(msWaitTime - msDelta));
                return true;
            }
        }
        tooSoon.put(key, curMS);
        return false;
    }
    
    public static String getPlayerResetTime(Player p, String what, int seconds){
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

    public static String LocStringShortNoWorld(Location loc) {
        if (loc == null) {
            return "NULL";
        }
        return "(" + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ() + ")";
    }

    public static String GetCommaList(ArrayList<String> arr) {
        StringBuilder buf = new StringBuilder();
        arr.stream().forEach((str) -> {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(str);
        });
        return buf.toString();
    }

    public static void EnsureDirectory(String dirName) {
        File pDir = new File(dirName);
        if (pDir.isDirectory()) {
            return;
        }
        try {
            Server.getInstance().getLogger().info("Creating directory: " + dirName);
            pDir.mkdir();
        } catch (Throwable exc) {
            Server.getInstance().getLogger().error("EnsureDirectory " + dirName + ": " + exc.toString());
        }
    }

    public static void ConsoleMsg(String msg) {
        try {
            Server.getInstance().getLogger().info(ASkyBlock.object.getPrefix() + TextFormat.WHITE + msg);
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
}
