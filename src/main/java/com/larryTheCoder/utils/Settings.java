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

import cn.nukkit.item.Item;
import cn.nukkit.level.generator.biome.Biome;
import com.larryTheCoder.storage.IslandData.SettingsFlag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Adam Matthew
 */
public class Settings {

    public static GameType GAMETYPE = GameType.SKYBLOCK;
    
    // system variables
    public static boolean useSchematicPanel;
    public static boolean chooseIslandRandomly;
    public static List<String> challengeLevels = new ArrayList<>();
    
    // config variables
    public static int maxHome;
    public static boolean updater;
    public static int islandDistance = 200;
    public static int islandHieght = 60;
    public static int protectionrange;
    public static int islandXOffset;
    public static int islandZOffset;
    public static int islandMaxNameLong;
    public static int cleanrate;
    public static int seaLevel = 0;
    public static ArrayList<String> bannedCommands = new ArrayList<>();
    public static int reset = 3;
    public static int gamemode;
    public static int memberTimeOut;
    public static List<String> companionNames = new ArrayList<>();
    public static boolean chestInventoryOverride;
    public static Item[] chestItems = new Item[0];
    public static boolean broadcastMessages;
    public static double biomeCost;
    public static Biome defaultBiome;
    public static boolean teamChat;
    public static boolean saveInventory;

    public static String defaultLanguage;
    public static boolean stopTime;
    
    // default variables
    public static String islandCompanion = "COW";
    public static Set<String> challengeList = new HashSet<>();
    public static List<String> freeLevels = new ArrayList<>();
    public static int waiverAmount = 0;
    
    // GridProtection
    public static boolean shouldTeleportSpawn = false;
    

    /**
     * Default world protection settings
     */
    public static HashMap<SettingsFlag, Boolean> defaultWorldSettings = new HashMap<SettingsFlag, Boolean>();

    /**
     * Default island protection settings
     */
    public static HashMap<SettingsFlag, Boolean> defaultIslandSettings = new HashMap<SettingsFlag, Boolean>();
    /**
     * Default spawn protection settings
     */
    public static HashMap<SettingsFlag, Boolean> defaultSpawnSettings = new HashMap<SettingsFlag, Boolean>();
    /**
     * Visitors settings to show in the GUI
     */
    public static HashMap<SettingsFlag, Boolean> visitorSettings = new HashMap<SettingsFlag, Boolean>();
    
    public static boolean allowTNTDamage;
    public static boolean allowChestDamage;
    public static boolean allowCreeperGriefing;
    public static boolean allowCreeperDamage;
    
    public static boolean useEconomy = false;
    public static double islandCost = 10D;
    public static boolean firstIslandFree = true;
    
}
