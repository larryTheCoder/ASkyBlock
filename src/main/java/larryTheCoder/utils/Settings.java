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
package larryTheCoder.utils;

import larryTheCoder.utils.GameType;
import cn.nukkit.item.Item;
import cn.nukkit.level.generator.biome.Biome;
import java.util.ArrayList;
import java.util.List;

/**
 * @author larryTheCoder
 */
public class Settings {

    public static GameType GAMETYPE = GameType.SKYBLOCK;
    public static Item[] chestItems = new Item[0];
    public static int islandSize = 200;
    public static int islandHieght = 60;
    public static ArrayList<String> bannedCommands = new ArrayList<>();
    public static int islandLevel = 0;
    public static int seaLevel = 0;
    public static int reset = 3;
    public static int memberTimeOut;
    public static List<String> challengeLevels = new ArrayList<>();

    public static Biome defaultBiome = Biome.getBiome(Biome.PLAINS);
    public static boolean usePhysics = false;
    public static String islandCompanion = "COW";
    public static List<String> companionNames = new ArrayList<>();
    public static int gamemode;

    public static class enabled {

        public static boolean DATABASE = true;
        public static boolean EVENTS = false;
        public static boolean TEAM = false;
        public static boolean UPDATER = true;
        public static boolean ANTIHACK = false; // A day will come for a long time ago
    }
}
