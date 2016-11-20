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

import cn.nukkit.utils.Config;
import java.io.File;
import java.util.ArrayList;

/**
 * @author larryTheCoder
 */
public class ConfigManager {

    public static int islandSize;
    public static ArrayList<String> whitelistedCommands;
    public static int islandDeleteSeconds;
    public static int islandHeight;

    static {
        ConfigManager.islandSize = 50;
        ConfigManager.whitelistedCommands = new ArrayList<>();
        ConfigManager.islandDeleteSeconds = 300;
        ConfigManager.islandHeight = 50;
    }

    @SuppressWarnings("deprecation")
    public static void load() {
        final Config cfg = new Config(new File(ASkyBlock.getInstance().getDataFolder(), "config.yml"), Config.YAML);
        // Island Size
        if (cfg.get("island.islandSize") != null) {
            try {
                ConfigManager.islandSize = cfg.getInt("island.islandSize");
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Invalid IslandSize setting");
            }
            if (ConfigManager.islandSize <= 10) {
                return;
            }
            Utils.ConsoleMsg("IslandSize too small. Using IslandSize=10 instead.");
            ConfigManager.islandSize = 10;
        }
        // Island Hieght
        if (cfg.get("island.islandHieght") != null) {
            try {
                ConfigManager.islandHeight = cfg.getInt("island.islandHieght");
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Invalid IslandStartY setting");
            }
            if (ConfigManager.islandHeight <= 8) {
                return;
            }
            Utils.ConsoleMsg("IslandStartY too small. Using IslandStartY=8 instead.");
            ConfigManager.islandHeight = 8;
        }
        // Resetricted commands
        if (cfg.get("island.restrictedCommands") != null) {
            ConfigManager.whitelistedCommands = new ArrayList<>();
            try {
                String string = cfg.getString("island.restrictedCommands");
                final String[] pieces = string.substring(string.length()).trim().split(",");
                String[] array;
                for (int length = (array = pieces).length, i = 0; i < length; ++i) {
                    final String piece = array[i];
                    if (piece != null) {
                        if (piece.length() > 0) {
                            ConfigManager.whitelistedCommands.add(piece);
                        }
                    }
                }
            } catch (Throwable exc2) {
                Utils.ConsoleMsg("Invalid RestrictedCommands setting");
            }
        }
    }
}
