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

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import com.larryTheCoder.storage.SettingsFlag;

import java.util.*;

/**
 * @author Adam Matthew
 */
public class Settings {

    public static GameType GAMETYPE = GameType.SKYBLOCK;

    // system config
    public static List<String> challengeLevels = new ArrayList<>();

    // config config
    public static boolean checkUpdate;
    public static int islandHeight = 60;
    public static int islandMaxNameLong;
    public static int cleanRate;
    public static final ArrayList<String> bannedCommands = new ArrayList<>();
    public static int reset = 3;
    public static int gameMode;
    public static int memberTimeOut;
    public static Item[] chestItems = new Item[0];
    public static boolean broadcastMessages;
    public static boolean teamChat;
    public static boolean saveInventory;
    public static int resetTime;
    public static String defaultLanguage;

    // default config
    public static Set<String> challengeList = new HashSet<>();
    public static List<String> freeLevels = new ArrayList<>();
    public static int waiverAmount = 0;

    /**
     * Default world protection settings
     */
    public static final HashMap<SettingsFlag, Boolean> defaultWorldSettings = new HashMap<>();
    /**
     * Default island protection settings
     */
    public static final HashMap<SettingsFlag, Boolean> defaultIslandSettings = new HashMap<>();
    /**
     * Default spawn protection settings
     */
    public static final HashMap<SettingsFlag, Boolean> defaultSpawnSettings = new HashMap<>();

    public static boolean allowTNTDamage;
    public static boolean allowChestDamage;
    public static boolean allowCreeperGriefing;
    public static boolean allowCreeperDamage;

    public static boolean useEconomy = false;
    public static double islandCost = 10D;
    public static boolean firstIslandFree = true;

    // Magic Cobble Generator
    public static boolean useMagicCobbleGen;
    public static TreeMap<Integer, TreeMap<Double, Block>> magicCobbleGenChances;
}
