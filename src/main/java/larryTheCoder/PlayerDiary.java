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

import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author larryTheCoder
 */
public class PlayerDiary {
    public static ConcurrentHashMap<String, String> playerExists = new ConcurrentHashMap();
    public static ConcurrentHashMap<String, Long> playerLastSeen = new ConcurrentHashMap();

    public static String GetPlayerExactName(String name) {
        return playerExists.get(name.toLowerCase());
    }

    public static boolean AddExactPlayerName(String name) {
        playerLastSeen.put(name, System.currentTimeMillis());
        if (name == null) {
            return false;
        }
        if (PlayerDiary.GetPlayerExactName(name) != null) {
            return false;
        }
        playerExists.put(name.toLowerCase(), name);
        return true;
    }

    public static void InitializeDiarySystem() {
        long ms1 = System.currentTimeMillis();
        File path = new File("players" + File.separator);
        File[] files = path.listFiles();
        int i = 0;
        while (i < files.length) {
            String fname;
            if (files[i].isFile() && (fname = files[i].getName()).endsWith(".dat")) {
                String pname = fname.substring(0, fname.length() - 4);
                playerExists.put(pname.toLowerCase(), pname);
                playerLastSeen.put(pname.toLowerCase(), files[i].lastModified());
            }
            ++i;
        }
        long ms2 = System.currentTimeMillis();
        Utils.ConsoleMsg("Loaded " + playerExists.size() + " player names from " + path + TextFormat.WHITE + " -- Took " + (ms2 - ms1) + " ms.");
    }
    
}
