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

import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author larryTheCoder
 */
public class Utils {
    public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static String Directory = "plugins" + File.separator + "ASkyBlock" + File.separator;
    public static ConcurrentHashMap<String, Long> tooSoon = new ConcurrentHashMap();
    
    public static void EnsureDirectory(String dirName) {
        File pDir = new File(dirName);
        if (pDir.isDirectory()) {
            return;
        }
        try {
            Server.getInstance().getLogger().info("Creating directory: " + dirName);
            pDir.mkdir();
        }
        catch (Throwable exc) {
            Server.getInstance().getLogger().error("EnsureDirectory " + dirName + ": " + exc.toString());
        }
    }

    public static void ConsoleMsg(String msg){
        try {
            Server.getInstance().getLogger().info(TextFormat.LIGHT_PURPLE + "[SkyBlock] " + TextFormat.WHITE + msg);
        }
        catch (Throwable exc) {
            System.out.println("SkyBlock: Failed to Write ConsoleMsg: " + msg);
        }
    }
    
}
