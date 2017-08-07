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
package com.larryTheCoder.locales;

import cn.nukkit.utils.Config;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import com.larryTheCoder.ASkyBlock;

/**
 * All the text strings in the game sent to players This version enables
 * different players to have different locales.
 * <p>
 * If the player are null such as Console. The 'default' will be used as main
 * language.
 *
 * @author Adam Matthew
 */
public final class ASlocales {

    private final static Set<String> TITLE_COLORS = new HashSet<>(Arrays.asList(
            "black",
            "dark_blue",
            "dark_green",
            "dark_aqua",
            "dark_red",
            "dark_purple",
            "gold",
            "gray",
            "dark_gray",
            "blue",
            "green",
            "aqua",
            "red",
            "light_purple",
            "yellow",
            "white"
    ));
    // Localization Strings
    private Config locale = null;
    private File localeFile = null;
    private ASkyBlock plugin;
    private Locale localeObject;

    public String changingObsidiantoLava;
    public String acidLore;
    public String acidBucket;
    public String acidBottle;
    public String drankAcidAndDied;
    public String drankAcid;
    public String firstIslandFree = "§aYour first island are free!";
    public String nextIslandPrice = "§aNext time you may need $§e[price] to buy other island";
    // Help defaults
    public String adminHelpCommand = "§eShow a list of admin commands.";
    public String adminHelpGenerate = "§eAttempt to create a new level.";
    public String adminHelpKick = "§eTo kick player from island worlds.";
    public String adminHelpRename = "§eAttempt to rename other player's island.";
    public String adminHelpSpawn = "§eSet the island's spawn point!";
    public String adminHelpDelete = "§eDelete other player island";
    // Errors defaults
    public String errorUnknownPlayer = "§cThat player is unknown.";
    public String errorNoPermission = "§cYou don't have permission to use that command!";
    public String errorNoIsland = "§cYou do not have an island!";
    public String errorNoIslandExsits = "§cYou don't have an island with home number ";
    public String errorNoIslandOther = "§cThat player does not have an island!";
    public String errorCommandNotReady = "§cYou can't use that command right now.";
    public String errorOfflinePlayer = "§cThat player is offline or doesn't exist.";
    public String errorUnknownCommand = "§cUnknown command.";
    public String errorNoTeam = "§cPlayer is not in a team.";
    public String errorMaxIslands = "§cThe world is full of islands! Try again later!";
    public String errorNotABlock = "§cThat is not a block";
    public String errorNotOnIsland = "§cYou are not in your/other's island space!";
    public String errorTooLong = "§cToo long. Maximum size is [length].";
    public String errorTooShort = "§cToo short. Minimum size is [length].";
    public String errorTooSoon = "§cYou need to wait [secs] to [cmd] your island";
    public String errorUseInGame = "§cThis command must be used in-game.";
    public String errorWrongWorld = "§cYou cannot do that in this world.";
    public String errorUnknownWorld = "§cUnknown world. Possible worlds are:";
    public String errorMaxIsland = "§cYou reached the limit of [maxplot] Island per player";
    public String errorNotPending = "§cNo invintation pending! Try again later";
    public String errorInTeam = "§cThe player [player] are already in team!";
    public String errorBlockedByAPI = "§cA plugin using the API blocked this action.";
    public String errorKickOwner = "§cYou cant kick yourself out your own island!";
    public String errorAdminOnly = "§cYou cant kick admins from your island!";
    public String errorFailedNormal = "§cInvilad or wrong parameters";
    public String errorFailedCritical = "§cFailed to attempt this command. Contact admin!";
    public String errorLevelGenerated = "§cThe level has already generated";
    public String errorNotEnoughMoney = "§cYou don't have enough money! Default price: $[price]";
    // Commands messages
    public String kickSeccess = "§aKicked [player] from SkyBlock world!";
    public String createSeccess = "§aSuccesfully created you an island!";
    public String resetSeccess = "§aSuccesfully cleared your island!";
    public String renameSeccess = "§aSuccesfully renamed island!";
    public String setworldSeccess = "§aSuccesfully changed island location!";
    public String generalSuccess = "§aSucces!";
    public String helpMessage = "§aNeed help? Use: /[com] help.";
    // Teleport messages
    public String teleportDelay = "§aYou will be teleported in {0} seconds.";
    public String teleportCancelled = "§cTeleport cancelled";
    // Admin commands message
    public String adminOverride = "§aYou override this command";
    public String adminDeleteIslandError = "§cUse §ldeleteisland confirm §r§cto delete the island you are on.";
    public String adminDeleteIslandnoid  = "§cCannot identify island.";
    public String adminDeleteIslandUse = "§rUse §ldelete [name] §r§cto delete the player instead.";
    public String adminSetSpawnOwnedBy = "§cThis island space is owned by [name]";
    // Team messages
    public String teamChatStatusOff = "§aTeam chat is off";
    public String teamChatStatusOn = "§aTeam chat is on";
    public String teamChatNoTeamAround = "§cNone of your team are online!";
    // Others
    public String deleteRemoving = "§cRemoving [name]'s island.";
    public String kickedFromOwner = "§eYou were kicked from island owned by [name].";
    public String kickedFromAdmin = "§eYou were kicked by Admin on duty.";
    public String kickedFromTeam = "§eYou were kicked from [name]'s team";
    public String newsHeadline = "§aWhile you were offline:";
    public String newsEmpty = "§aThere no messages for you today. Check back later!";
    // Titles
    public String islandSubTitle = "code by larryTheCoder";
    public String islandDonate = "§aSource code made by §e@larryTheCoder";
    public String islandURL = "§aLink: http://github.com/larryTheCoder/ASkyBlock-Nukkit";
    public String islandSupport = "§aLove it? Give us a star on GitHub!";
    public String islandSubTitleColor = "blue";
    public String islandDonateColor = "aqua";
    public String islandTitleColor = "gold";
    public String islandTitle = "[player]'s island";
    
    
    private String localeName;
    private int index;
    

    /**
     * Creates a locale object full of localized strings for a language
     *
     * @param plugin
     * @param localeName - name of the yml file that will be used
     * @param index
     */
    public ASlocales(ASkyBlock plugin, String localeName, int index) {
        this.plugin = plugin;
        this.index = index;
        this.localeName = localeName;
        getLocale(localeName);
        loadLocale();
        if (!localeName.equalsIgnoreCase("locale")) {
            localeObject = new Locale(localeName.substring(0, 2), localeName.substring(3, 5));
        }
    }

    /**
     * @param localeName
     * @return locale Config object
     */
    public Config getLocale(String localeName) {
        if (this.locale == null) {
            reloadLocale(localeName);
        }
        return locale;
    }

    /**
     * Reloads the locale file
     *
     * @param localeName
     */
    public void reloadLocale(String localeName) {
        // Make directory if it doesn't exist
        File localeDir = new File(plugin.getDataFolder() + File.separator + "locale");
        if (!localeDir.exists()) {
            localeDir.mkdir();
        }
        if (localeFile == null) {
            localeFile = new File(localeDir.getPath(), localeName + ".yml");
        }
        if (localeFile.exists()) {
            //plugin.getLogger().info("DEBUG: File exists!");
            locale = new Config(localeFile, Config.YAML);
        } else // Look for defaults in the jar
        if (plugin.getResource("locale/" + localeName + ".yml") != null) {
            plugin.saveResource("locale/" + localeName + ".yml", true);
            localeFile = new File(plugin.getDataFolder() + File.separator + "locale", localeName + ".yml");
            locale = new Config(localeFile, Config.YAML);
            //locale.setDefaults(defLocale);
        } else {
            // Use the default file
            localeFile = new File(plugin.getDataFolder() + File.separator + "locale", "locale.yml");
            if (localeFile.exists()) {
                locale = new Config(localeFile, Config.YAML);
            } else // Look for defaults in the jar                    
            if (plugin.getResource("locale/locale.yml") != null) {
                plugin.saveResource("locale/locale.yml", true);
                localeFile = new File(plugin.getDataFolder() + File.separator + "locale", "locale.yml");
                locale = new Config(localeFile, Config.YAML);
            } else {
                plugin.getLogger().emergency("Could not find any locale file!");
            }
        }
    }

    /**
     * Reloads the locale file
     */
    private void loadLocale() {

    }

    /**
     * Checks that the color supplied is a valid color
     *
     * @param string
     * @return color
     */
    private String colorCheck(String string) {
        string = string.toLowerCase();
        if (TITLE_COLORS.contains(string)) {
            return string;
        }
        plugin.getLogger().warning("Title color " + string + " is unknown. Use one from this list:");
        TITLE_COLORS.stream().forEach((color) -> {
            plugin.getLogger().warning(color);
        });
        return "white";
    }

    /**
     * @return the languageName
     */
    public String getLanguageName() {
        if (localeObject == null) {
            return "unknown";
        }
        return localeObject.getDisplayLanguage(localeObject);
    }

    public String getCountryName() {
        if (localeObject == null) {
            return "unknown";
        }
        return localeObject.getDisplayCountry(localeObject);
    }

    public String getLocaleName() {
        return this.localeName;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
}
