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

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;

import java.util.*;

/**
 * @author larryTheCoder
 */
public class Settings {

    // system config
    public static List<String> challengeLevels = new ArrayList<>();

    // config config
    public static boolean verboseCode = false;
    public static boolean autoUpdate = false;
    public static boolean checkUpdate;
    public static int islandHeight = 60;
    public static int islandMaxNameLong;
    public static int cleanRate;
    public static boolean respawnOnIsland;
    public static int deathPenalty;
    public static boolean sumTeamDeaths;
    public static final ArrayList<String> bannedCommands = new ArrayList<>();
    public static int gameMode;
    public static int memberTimeOut;
    public static Item[] chestItems = new Item[0];
    public static boolean broadcastMessages;
    public static boolean teamChat;
    public static int resetTime;
    public static String defaultLanguage;
    public static int loadCacheBefore = 30;

    // Levels
    // The format are as 'FullID => Level'
    public final static Map<Integer, Integer> blockLimits = new HashMap<>();
    public final static Map<Integer, Integer> blockValues = new HashMap<>();

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

    public static boolean useEconomy = false;
    public static double islandCost = 10D;
    public static boolean firstIslandFree = true;

    // Magic Cobble Generator
    public static boolean useMagicCobbleGen;
    public static TreeMap<Integer, TreeMap<Double, Block>> magicCobbleGenChances;

    // Level configuration settings.
    public static int levelCost;
    public static int maxDeaths;
    public static boolean islandResetDeathReset;
    public static boolean teamJoinDeathReset;
}
