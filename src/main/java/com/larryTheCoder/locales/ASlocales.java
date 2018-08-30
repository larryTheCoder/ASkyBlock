/*
 * Copyright (C) 2016-2018 Adam Matthew
 *
 * This program is free software", you can redistribute it and/or modify
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
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

    public String firstIslandFree = "§aYour first island are free!";
    public String nextIslandPrice = "§aNext time you may need $§e[price] to buy other island";
    // Help defaults
    public String adminHelpGenerate = "§aAttempt to create a new level.";
    public String adminHelpKick = "§aTo kick player from island worlds.";
    public String adminHelpRename = "§aAttempt to rename other player's island.";
    public String adminHelpSpawn = "§aSet the island's spawn point!";
    public String adminHelpDelete = "§aDelete other player island";
    public String adminHelpMessage = "§aAdd a message for all skyblock users";
    public String adminHelpClear = "§aFlushes all he memory in the plugin";
    // Errors defaults
    public String errorMaxReset = "§cYou had reached the limit for island delectation.";
    private String errorUnknownPlayer = "§cThat player is unknown.";
    public String errorNoPermission = "§cYou don't have permission to use that command!";
    public String errorNoIsland = "§cYou do not have an island!";
    public String errorNoIslandOther = "§cThat player does not have an island!";
    private String errorCommandNotReady = "§cYou can't use that command right now.";
    public String errorCommandBlocked = "§cYou can't use that command here.";
    public String errorOfflinePlayer = "§cThat player is offline or doesn't exist.";
    public String errorNotOnIsland = "§cYou are not in your/other's island space!";
    private String errorTooLong = "§cToo long. Maximum size is [length].";
    private String errorTooShort = "§cToo short. Minimum size is [length].";
    public String adminSetSpawnOverride = "§cThere a player owned this plot. Admin override this command";
    public String errorTooSoon = "§cYou need to wait [secs] to [cmd] your island";
    public String errorUseInGame = "§cThis command must be used in-game.";
    public String errorWrongWorld = "§cYou cannot do that in this world.";
    private String errorUnknownWorld = "§cUnknown world. Possible worlds are:";
    public String errorMaxIsland = "§cSorry you cant create island at this world any more";
    public String errorNotPending = "§cNo invitation pending! Try again later";
    public String errorInTeam = "§cThe player [player] are already in team!";
    public String errorBlockedByAPI = "§cA plugin using the API blocked this action.";
    public String errorKickOwner = "§cYou cant kick yourself out your own island!";
    public String errorAdminOnly = "§cYou cant kick admins from your island!";
    public String errorFailedNormal = "§cInvalid or wrong parameters";
    public String errorFailedCritical = "§cFailed to attempt this command. Contact admin!";
    public String errorLevelGenerated = "§cThe level has already generated";
    public String errorNotEnoughMoney = "§cYou don't have enough money! Default price: $[price]";
    public String errorIslandPC = "§cCannot attempt to generate level. PC Type of schematic";
    // Commands messages
    public String kickSuccess = "§aKicked [player] from SkyBlock world!";
    public String createSuccess = "§aSuccessfully created you an island!";
    public String resetSuccess = "§aSuccessfully cleared your island!";
    public String renameSuccess = "§aSuccessfully renamed island!";
    private String setWorldSuccess = "§aSuccessfully changed island world location!";
    public String setHomeSuccess = "§aSuccessfully changed island home location!";
    public String generalSuccess = "§aSuccess!";
    public String biomeChangeComplete = "§aChanged your island biome to: [biome]";
    // Teleport messages
    public String teleportDelay = "§aYou will be teleported in {0} seconds.";
    public String teleportCancelled = "§cTeleport cancelled";
    // Admin commands message
    public String adminOverride = "§aYou override this command";
    public String adminDeleteIslandError = "§cUse §ldelete confirm §r§cto delete the [player]'s island.";
    public String adminDeleteIslandnoid = "§cCannot identify island.";
    public String adminDeleteIslandUse = "§rUse §ldelete [name] §r§cto delete the player instead.";
    public String adminSetSpawnOwnedBy = "§cThis island space is owned by [name]";
    // Team messages
    public String teamChatStatusOff = "§aTeam chat is off";
    public String teamChatStatusOn = "§aTeam chat is on";
    public String teamChatNoTeamAround = "§cNone of your team are online!";
    // Others
    public String hangInThere = "§eHang in there. Finding your best safe teleport position...";
    public String deleteRemoving = "§cRemoving [name]'s island.";
    public String kickedFromOwner = "§eYou were kicked from island owned by [name].";
    public String kickedFromAdmin = "§eYou were kicked by Admin on duty.";
    public String kickedFromTeam = "§eYou were kicked from [name]'s team";
    public String newsHeadline = "§aWhile you were §eOffline§a:";
    public String newsEmpty = "§aThere no messages for you today. Check back later!";
    public String newNews = "§eWelcome back! There's [count] news today! Use /is messages to see all messages";
    public String newInvitation = "§aNew invitation from [player]'s island";
    public String acceptedFrom = "§eYou accepted [player]'s invitation.";
    public String acceptedTo = "§e[player] has accepted your invitation!";
    public String panelCancelled = "§cYou cancelled your island panel!";
    // Titles
    public String islandSubTitle = "&eNice and cosy";
    public String islandDonate = "§aSource code made by §e@larryTheCoder";
    public String islandURL = "§aLink: http://github.com/larryTheCoder/ASkyBlock-Nukkit";
    private String islandSupport = "§aLove it? Give us a star on GitHub!";
    public String islandTitle = "[player]'s island";
    public String groundNoAir = "§eWhat do you think you are? You can't set home on air!";
    // Guard island
    public String islandProtected = "§cThat island is protected";
    // Panels
    public String panelIslandHeader = "§eWelcome to the Island Panel. Please fill in these forms.";
    public String panelIslandHome = "§dYour Home Name.";
    public String panelIslandTemplate = "§dIsland Templates";
    public String panelIslandDefault = "§eThese are your island Settings.";
    public String panelIslandWorld = "§dChoose your world";
    public String panelChallengesHeader = "§aChoose your toppings! All of these are your challenges to complete! You will be awarded with an amazing prize!";
    public String panelHomeHeader = "§dHere are all of the list of your islands. Choose one of these to [function]";
    public String panelSettingHeader = "§eYou can make an simple changes for your island. You can set your island any time.";
    public String panelProtectionHeader = "§eSet you island protection settings, this applies to all visitors to your island. Some features may not available";
    public String deleteIslandSure = "§aAre you sure to delete your island? This is an irreversible!";
    public String deleteIslandCancelled = "§cYou just cancelled your delete island confirmation";
    // Public error
    public String errorResponseUnknown = "§eAn error just occured. Try again later";

    // Localization Strings
    private Config locale = null;
    private File localeFile = null;
    private final ASkyBlock plugin;
    private Locale localeObject;
    private final String localeName;
    private final int index;

    /**
     * Creates a locale object full of localized strings for a language
     *
     * @param plugin     ASkyBlock class
     * @param localeName - name of the yml file that will be used
     * @param index      The count of the file processed
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
     * @param localeName Locale name
     * @return locale Config object
     */
    private void getLocale(String localeName) {
        if (this.locale == null) {
            reloadLocale(localeName);
        }
    }

    /**
     * Reloads the locale file
     *
     * @param localeName Locale name
     */
    private void reloadLocale(String localeName) {
        // Make directory if it doesn't exist
        File localeDir = new File(plugin.getDataFolder() + File.separator + "locale");
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
        // Island economy command
        firstIslandFree = TextFormat.colorize('&', locale.getString("firstIslandFree", "§aYour first island are free!"));
        nextIslandPrice = TextFormat.colorize('&', locale.getString("nextIslandPrice", "§aNext time you may need $§e[price] to buy other island"));
        // Help defaults
        adminHelpGenerate = TextFormat.colorize('&', locale.getString("adminHelpGenerate", "§aAttempt to create a new level."));
        adminHelpKick = TextFormat.colorize('&', locale.getString("adminHelpKick", "§aTo kick player from island worlds."));
        adminHelpRename = TextFormat.colorize('&', locale.getString("adminHelpRename", "§aAttempt to rename other player's island."));
        adminHelpSpawn = TextFormat.colorize('&', locale.getString("adminHelpSpawn", "§aSet the island's spawn point!"));
        adminHelpDelete = TextFormat.colorize('&', locale.getString("adminHelpDelete", "§aDelete other player island"));
        // Errors defaults
        errorMaxReset = TextFormat.colorize('&', locale.getString("errorMaxReset", "§cYou had reached the limit for island delectation."));
        errorUnknownPlayer = TextFormat.colorize('&', locale.getString("errorUnknownPlayer", "§cThat player is unknown."));
        errorNoPermission = TextFormat.colorize('&', locale.getString("errorNoPermission", "§cYou don't have permission to use that command!"));
        errorNoIsland = TextFormat.colorize('&', locale.getString("errorNoIsland", "§cYou do not have an island!"));
        errorNoIslandOther = TextFormat.colorize('&', locale.getString("errorNoIslandOther", "§cThat player does not have an island!"));
        errorCommandNotReady = TextFormat.colorize('&', locale.getString("errorCommandNotReady", "§cYou can't use that command right now."));
        errorCommandBlocked = TextFormat.colorize('&', locale.getString("errorCommandBlocked", "§cYou can't use that command here."));
        errorOfflinePlayer = TextFormat.colorize('&', locale.getString("errorOfflinePlayer", "§cThat player is offline or doesn't exist."));
        errorNotOnIsland = TextFormat.colorize('&', locale.getString("errorNotOnIsland", "§cYou are not in your/other's island space!"));
        errorTooLong = TextFormat.colorize('&', locale.getString("errorTooLong", "§cToo long. Maximum size is [length]."));
        errorTooShort = TextFormat.colorize('&', locale.getString("errorTooShort", "§cToo short. Minimum size is [length]."));
        adminSetSpawnOverride = TextFormat.colorize('&', locale.getString("adminSetSpawnOverride", "§cThere a player owned this plot. Admin override this command"));
        errorTooSoon = TextFormat.colorize('&', locale.getString("errorTooSoon", "§cYou need to wait [secs] to [cmd] your island"));
        errorUseInGame = TextFormat.colorize('&', locale.getString("errorUseInGame", "§cThis command must be used in-game."));
        errorWrongWorld = TextFormat.colorize('&', locale.getString("errorWrongWorld", "§cYou cannot do that in this world."));
        errorUnknownWorld = TextFormat.colorize('&', locale.getString("errorUnknownWorld", "§cUnknown world. Possible worlds are:"));
        errorMaxIsland = TextFormat.colorize('&', locale.getString("errorMaxIsland", "§cSorry you cant create island at this world any more"));
        errorNotPending = TextFormat.colorize('&', locale.getString("errorNotPending", "§cNo invitation pending! Try again later"));
        errorInTeam = TextFormat.colorize('&', locale.getString("errorInTeam", "§cThe player [player] are already in team!"));
        errorBlockedByAPI = TextFormat.colorize('&', locale.getString("errorBlockedByAPI", "§cA plugin using the API blocked this action."));
        errorKickOwner = TextFormat.colorize('&', locale.getString("errorKickOwner", "§cYou cant kick yourself out your own island!"));
        errorAdminOnly = TextFormat.colorize('&', locale.getString("errorAdminOnly", "§cYou cant kick admins from your island!"));
        errorFailedNormal = TextFormat.colorize('&', locale.getString("errorFailedNormal", "§cInvalid or wrong parameters"));
        errorFailedCritical = TextFormat.colorize('&', locale.getString("errorFailedCritical", "§cFailed to attempt this command. Contact admin!"));
        errorLevelGenerated = TextFormat.colorize('&', locale.getString("errorLevelGenerated", "§cThe level has already generated"));
        errorNotEnoughMoney = TextFormat.colorize('&', locale.getString("errorNotEnoughMoney", "§cYou don't have enough money! Default price: $[price]"));
        errorIslandPC = TextFormat.colorize('&', locale.getString("errorIslandPC", "§cCannot attempt to generate level. PC Type of schematic"));
        // Commands messages
        kickSuccess = TextFormat.colorize('&', locale.getString("kickSuccess", "§aKicked [player] from SkyBlock world!"));
        createSuccess = TextFormat.colorize('&', locale.getString("createSuccess", "§aSuccessfully created you an island!"));
        resetSuccess = TextFormat.colorize('&', locale.getString("resetSuccess", "§aSuccessfully cleared your island!"));
        renameSuccess = TextFormat.colorize('&', locale.getString("renameSuccess", "§aSuccessfully renamed island!"));
        setWorldSuccess = TextFormat.colorize('&', locale.getString("setWorldSuccess", "§aSuccessfully changed island world location!"));
        setHomeSuccess = TextFormat.colorize('&', locale.getString("setHomeSuccess", "§aSuccessfully changed island home location!"));
        generalSuccess = TextFormat.colorize('&', locale.getString("generalSuccess", "§aSuccess!"));
        biomeChangeComplete = TextFormat.colorize('&', locale.getString("biomeChangeComplete", "§aChanged your island biome to: [biome]"));
        // Teleport messages
        teleportDelay = TextFormat.colorize('&', locale.getString("teleportDelay", "§aYou will be teleported in {0} seconds."));
        teleportCancelled = TextFormat.colorize('&', locale.getString("teleportCancelled", "§cTeleport cancelled"));
        // Admin commands message
        adminOverride = TextFormat.colorize('&', locale.getString("adminOverride", "§aYou override this command"));
        adminDeleteIslandError = TextFormat.colorize('&', locale.getString("adminDeleteIslandError", "§cUse §ldelete confirm §r§cto delete the [player]'s island."));
        adminDeleteIslandnoid = TextFormat.colorize('&', locale.getString("adminDeleteIslandnoid", "§cCannot identify island."));
        adminDeleteIslandUse = TextFormat.colorize('&', locale.getString("adminDeleteIslandUse", "§rUse §ldelete [name] §r§cto delete the player instead."));
        adminSetSpawnOwnedBy = TextFormat.colorize('&', locale.getString("adminSetSpawnOwnedBy", "§cThis island space is owned by [name]"));
        // Team messages
        teamChatStatusOff = TextFormat.colorize('&', locale.getString("teamChatStatusOff", "§aTeam chat is off"));
        teamChatStatusOn = TextFormat.colorize('&', locale.getString("teamChatStatusOn", "§aTeam chat is on"));
        teamChatNoTeamAround = TextFormat.colorize('&', locale.getString("teamChatNoTeamAround", "§cNone of your team are online!"));
        // Others
        hangInThere = TextFormat.colorize('&', locale.getString("hangInThere", "§eHang in there. Finding your best safe teleport position..."));
        deleteRemoving = TextFormat.colorize('&', locale.getString("deleteRemoving", "§cRemoving [name]'s island."));
        kickedFromOwner = TextFormat.colorize('&', locale.getString("kickedFromOwner", "§eYou were kicked from island owned by [name]."));
        kickedFromAdmin = TextFormat.colorize('&', locale.getString("kickedFromAdmin", "§eYou were kicked by Admin on duty."));
        kickedFromTeam = TextFormat.colorize('&', locale.getString("kickedFromTeam", "§eYou were kicked from [name]'s team"));
        newsHeadline = TextFormat.colorize('&', locale.getString("newsHeadline", "§aWhile you were §eOffline§a:"));
        newsEmpty = TextFormat.colorize('&', locale.getString("newsEmpty", "§aThere no messages for you today. Check back later!"));
        newNews = TextFormat.colorize('&', locale.getString("newNews", "§eWelcome back! There's [count] news today! Use /is messages to see all messages"));
        newInvitation = TextFormat.colorize('&', locale.getString("newInvitation", "§aNew invitation from [player]'s island"));
        acceptedFrom = TextFormat.colorize('&', locale.getString("acceptedFrom", "§eYou accepted [player]'s invitation."));
        acceptedTo = TextFormat.colorize('&', locale.getString("acceptedTo", "§e[player] has accepted your invitation!"));
        panelCancelled = TextFormat.colorize('&', locale.getString("panelCancelled", "§cYou cancelled your island panel!"));
        // Titles
        islandSubTitle = TextFormat.colorize('&', locale.getString("islandSubTitle", "&eNice and cosy"));
        islandDonate = TextFormat.colorize('&', locale.getString("islandDonate", "§aSource code made by §e@larryTheCoder"));
        islandURL = TextFormat.colorize('&', locale.getString("islandURL", "§aLink: http://github.com/larryTheCoder/ASkyBlock-Nukkit"));
        islandSupport = TextFormat.colorize('&', locale.getString("islandSupport", "§aLove it? Give us a star on GitHub!"));
        islandTitle = TextFormat.colorize('&', locale.getString("islandTitle", "[player]'s island"));
        groundNoAir = TextFormat.colorize('&', locale.getString("groundNoAir", "§eWhat do you think you are? You can't set home on air!"));
        // Guard island
        islandProtected = TextFormat.colorize('&', locale.getString("islandProtected", "§cThat island is protected"));
        // Panels
        panelIslandHeader = TextFormat.colorize('&', locale.getString("panelIslandHeader", "§eWelcome to the Island Panel. Please fill in these forms."));
        panelIslandHome = TextFormat.colorize('&', locale.getString("panelIslandHome", "§dYour Home Name."));
        panelIslandTemplate = TextFormat.colorize('&', locale.getString("panelIslandTemplate", "§dIsland Templates"));
        panelIslandDefault = TextFormat.colorize('&', locale.getString("panelIslandDefault", "§eThese are your island Settings."));
        panelIslandWorld = TextFormat.colorize('&', locale.getString("panelIslandWorld", "§dChoose your world"));
        panelChallengesHeader = TextFormat.colorize('&', locale.getString("panelChallengesHeader", "§aChoose your toppings! All of these are your challenges to complete! You will be awarded with an amazing prize!"));
        panelHomeHeader = TextFormat.colorize('&', locale.getString("panelHomeHeader", "§dHere are all of the list of your islands. Choose one of these to [function]"));
        panelSettingHeader = TextFormat.colorize('&', locale.getString("panelSettingHeader", "§eYou can make an simple changes for your island. You can set your island any time."));
        deleteIslandSure = TextFormat.colorize('&', locale.getString("deleteIslandSure", "§aAre you sure to delete your island? This is an irreversible!"));
        deleteIslandCancelled = TextFormat.colorize('&', locale.getString("deleteIslandCancelled", "§cYou just cancelled your delete island confirmation"));
        // Public error
        errorResponseUnknown = TextFormat.colorize('&', locale.getString("errorResponseUnknown", "§eAn error just occured. Try again later"));
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
