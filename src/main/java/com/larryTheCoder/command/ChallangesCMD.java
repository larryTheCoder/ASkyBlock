/*
 * Copyright (C) 2017 larryTheHarry 
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
package com.larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.sound.ExperienceOrbSound;
import cn.nukkit.level.sound.Sound;
import cn.nukkit.potion.Potion;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.economyHandler.Economy;
import com.larryTheCoder.events.ChallengeCompleteEvent;
import com.larryTheCoder.events.ChallengeLevelCompleteEvent;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author larryTheCoder
 */
public final class ChallangesCMD extends Command {

    private final ASkyBlock plugin;
    // Database of challenges
    private final LinkedHashMap<String, List<String>> challengeList = new LinkedHashMap<>();
    private Config challengeFile = null;
    private File challengeConfigFile;

    public ChallangesCMD(ASkyBlock ev) {
        super("challenges", "Challange yourself for some big prize", "\u00a77<parameters>", new String[]{"c", "chall", "ch"});
        this.plugin = ev;
        saveDefaultChallengeConfig();
        reloadChallengeConfig();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (args[0] != null) {
            p.sendMessage(plugin.getPrefix() + "§cfew parameters, /c help for a list of commands");
        }
        switch (args[0]) {
            case "help":
                p.sendMessage("§d--- §aChallanges help page §e1 §aof §e1 §d---");
                p.sendMessage("\u00a7aUse /c <name> to view information about a challenge.");
                p.sendMessage("\u00a7aUse /c complete <name> to attempt to complete that challenge.");
                p.sendMessage("\u00a7aUse /c list to view all information about the challenge.");
            case "list":
                if (args[1] != null) {
                    this.showHelp(p, Integer.valueOf(args[1]));
                } else {
                    this.showHelp(p, 1);
                }
                break;
            case "complete":
                if (checkIfCanCompleteChallenge(p, args[1].toLowerCase())) {
                    int oldLevel = getLevelDone(p);
                    giveReward(p, args[1].toLowerCase());
                    int newLevel = getLevelDone(p);
                    // Fire an event if they are different
                    //Utils.ConsoleMsg("DEBUG: " + oldLevel + " " + newLevel);
                    if (oldLevel < newLevel) {
                        // Update chat
                        plugin.getChatHandlers().setPlayerChallengeLevel(p);
                        // Run commands and give rewards but only if they haven't done it below
                        //Utils.ConsoleMsg("DEBUG: old level = " + oldLevel + " new level = " + newLevel);
                        String level = Settings.challengeLevels.get(newLevel);
                        if (!level.isEmpty() && !checkChallenge(p, level)) {
                            //Utils.ConsoleMsg("DEBUG: level name = " + level);
                            completeChallenge(p.getUniqueId(), level);
                            String message = TextFormat.colorize('&', getChallengeConfig().getString("challenges.levelUnlock." + level + ".message", ""));
                            if (!message.isEmpty()) {
                                p.sendMessage(TextFormat.GREEN + message);
                            }

                            String[] itemReward = getChallengeConfig().getString("challenges.levelUnlock." + level + ".itemReward", "").split(" ");
                            String rewardDesc = getChallengeConfig().getString("challenges.levelUnlock." + level + ".rewardDesc", "");
                            if (!rewardDesc.isEmpty()) {
                                // p.sendMessage(TextFormat.GOLD + ": " + TextFormat.WHITE + rewardDesc);
                            }
                            List<Item> rewardedItems = giveItems(p, itemReward);
                            double moneyReward = getChallengeConfig().getDouble("challenges.levelUnlock." + level + ".moneyReward", 0D);
                            int expReward = getChallengeConfig().getInt("challenges.levelUnlock." + level + ".expReward", 0);
                            if (expReward > 0) {
                                p.sendMessage(TextFormat.GOLD + "You got Exp: " + TextFormat.WHITE + expReward);
                                p.addExperience(expReward);
                            }
                            if (Settings.useEconomy && moneyReward > 0 && (ASkyBlock.econ != null)) {
                                ASkyBlock.econ.addMoney(p, moneyReward);
                                p.sendMessage(TextFormat.GOLD + "You received : " + TextFormat.WHITE + "$" + moneyReward);
                            }
                            String[] permList = getChallengeConfig().getString("challenges.levelUnlock." + level + ".permissionReward", "").split(" ");

                            for (final String s : permList) {
                                if (!s.isEmpty()) {
                                    p.addAttachment(plugin).setPermission(s, true);
                                    Utils.ConsoleMsg("Added permission " + s + " to " + p.getName() + "");
                                }
                            }
                            List<String> commands = getChallengeConfig().getStringList("challenges.levelUnlock." + level + ".commands");
                            runCommands(p, commands);
                            // Fire event
                            ChallengeLevelCompleteEvent event = new ChallengeLevelCompleteEvent(p, oldLevel, newLevel, rewardedItems);
                            plugin.getServer().getPluginManager().callEvent(event);

                        }
                    }
                }
            default:
                p.sendMessage(plugin.getPrefix() + "§cUnknown parameters, /c help for a list of commands");
                break;
        }
        return true;
    }

    private void showHelp(CommandSender sender, int numbers) {
        List<String> names = new ArrayList<>();
        getChallengeConfig().getSection("challenges.challengeList").getKeys(false).stream().forEach((AADES) -> {
            names.add(AADES);
        });
        int pageNumber = numbers;
        int pageHeight = 3;
        int totalPage = names.size() % pageHeight == 0 ? names.size() / pageHeight : names.size() / pageHeight + 1;
        pageNumber = Math.min(pageNumber, totalPage);
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        sender.sendMessage("§d--- §aChallanges list page §e" + pageNumber + " §aof §e" + totalPage + " §d---");
        int i = 1;
        for (String cn : names) {
            if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(names.size(), pageNumber * pageHeight)) {
//                if (plugin.getPlayerInfo(sender).checkChallenge(challenge)
//                        && (!type.equals("inventory") || !getChallengeConfig().getBoolean("challenges.challengeList." + cn + ".repeatable", false))) {
//                    sender.sendMessage(getPrefix() +TextFormat.RED + "This Challenge is not repeatable!");
//                }                
                sender.sendMessage(TextFormat.GREEN + "Challenges Name: " + TextFormat.YELLOW + cn);
                sender.sendMessage(TextFormat.GREEN + "Max Level: " + TextFormat.YELLOW
                        + getChallengeConfig().getString("challenges.challengeList." + cn + ".level", ""));
                String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + cn + ".description", "").replace("[label]", ""));
                List<String> result = new ArrayList<>();
                if (desc.contains("|")) {
                    result.addAll(Arrays.asList(desc.split("\\|")));
                } else {
                    result.add(desc);
                }
                result.stream().forEach((line) -> {
                    sender.sendMessage(TextFormat.YELLOW + line);
                });
                final String type = getChallengeConfig().getString("challenges.challengeList." + cn + ".type", "").toLowerCase();
                if (type.equals("inventory")) {
                    if (getChallengeConfig().getBoolean("challenges.challengeList." + cn + ".takeItems")) {
                        sender.sendMessage(TextFormat.RED + "All required items are taken in your inventory when you complete this challenge!");
                    }
                } else if (type.equals("island")) {
                    sender.sendMessage(TextFormat.RED + "All required items must be close to you on your island!");
                }
                sender.sendMessage(TextFormat.AQUA + "----");
            }
            i++;
        }
        if (!names.get(numbers).isEmpty()) {
            sender.sendMessage(TextFormat.GREEN + "Type " + TextFormat.YELLOW + "/c list " + (numbers + 1) + TextFormat.GREEN + " to see the next page.");
        }
        names.clear();
    }

    /**
     * Saves the challenge.yml file if it does not exist
     */
    public void saveDefaultChallengeConfig() {
        challengeFile = new Config(new File(plugin.getDataFolder(), "challenges.yml"), Config.YAML);
        challengeConfigFile = new File(plugin.getDataFolder(), "challenges.yml");
    }

    /**
     * @return challenges FileConfiguration object
     */
    public Config getChallengeConfig() {
        if (challengeFile == null) {
            reloadChallengeConfig();
        }
        return challengeFile;
    }

    /**
     * Reloads the challenge config file
     */
    public void reloadChallengeConfig() {
        Settings.challengeList = getChallengeConfig().getSection("challenges.challengeList").getKeys(false);
        Settings.challengeLevels = Arrays.asList(getChallengeConfig().getString("challenges.levels", "").split(" "));
        Settings.freeLevels = Arrays.asList(getChallengeConfig().getString("challenges.freelevels", "").split(" "));
        Settings.waiverAmount = getChallengeConfig().getInt("challenges.waiveramount", 1);
        if (Settings.waiverAmount < 0) {
            Settings.waiverAmount = 0;
        }
        populateChallengeList();
    }

    /**
     * Goes through all the challenges in the config.yml file and puts them into
     * the challenges list
     */
    public void populateChallengeList() {
        challengeList.clear();
        for (String s : Settings.challengeList) {
            String level = getChallengeConfig().getString("challenges.challengeList." + s + ".level", "");
            // Verify that this challenge's level is in the list of levels
            if (Settings.challengeLevels.contains(level) || level.isEmpty()) {
                if (challengeList.containsKey(level)) {
                    challengeList.get(level).add(s);
                } else {
                    List<String> t = new ArrayList<>();
                    t.add(s);
                    challengeList.put(level, t);
                }
            } else {
                Utils.ConsoleMsg("&cLevel (" + level + ") for challenge " + s + " does not exist. Check challenges.yml.");
            }
        }
    }

    /**
     * Returns true if the level is unlocked and false if not
     *
     * @param player
     * @param level
     * @return true/false
     */
    public boolean isLevelAvailable(final Player player, final String level) {
        if (challengeList.size() < 2) {
            return true;
        }
        for (int i = 0; i < Settings.challengeLevels.size(); i++) {
            if (Settings.challengeLevels.get(i).equalsIgnoreCase(level)) {
                if (i == 0) {
                    return true;
                }

                if (checkLevelCompletion(player, Settings.challengeLevels.get(i - 1)) <= 0) {
                    return true;
                }
            }

        }

        return false;
    }

    /**
     * Returns the number of challenges that must still be completed to finish a
     * level Based on how many challenges there are in a level, how many have
     * been done and how many are okay to leave undone.
     *
     * @param player
     * @param level
     * @return int of challenges that must still be completed to finish level.
     */
    public int checkLevelCompletion(final Player player, final String level) {
        if (Settings.freeLevels.contains(level)) {
            return 0;
        }
        int challengesCompleted = 0;
        List<String> levelChallengeList = challengeList.get(level);
        int waiver = Settings.waiverAmount;
        if (levelChallengeList != null) {
            for (String challenge : levelChallengeList) {
                if (plugin.getPlayerInfo(player).checkChallenge(challenge)) {
                    challengesCompleted++;
                }
            }
            // If the number of challenges in a level is below the waiver amount, then they all need to be done
            if (levelChallengeList.size() <= Settings.waiverAmount) {
                waiver = 0;
            }
            return levelChallengeList.size() - waiver - challengesCompleted;
        }
        return 0;
    }

    /**
     * Gets the name of the highest challenge level the player has completed
     *
     * @param player
     * @return challenge level
     */
    public String getChallengeLevel(Player player) {
        //Utils.ConsoleMsg("DEBUG: getting challenge level for " + player.getName());
        if (Settings.challengeLevels.isEmpty()) {
            return "";
        }
        return Settings.challengeLevels.get(getLevelDone(player));
    }

    /**
     * Checks the highest level this player has achieved
     *
     * @param player
     * @return level number
     */
    private int getLevelDone(Player player) {
        //Utils.ConsoleMsg("DEBUG: checking level completed");
        //Utils.ConsoleMsg("DEBUG: getting challenge level for " + player.getName());
        for (int result = 0; result < Settings.challengeLevels.size(); result++) {
            if (checkLevelCompletion(player, Settings.challengeLevels.get(result)) > 0) {
                return result;
            }
        }
        return (Math.max(0, Settings.challengeLevels.size() - 1));
    }

    /**
     * Checks if player can complete challenge
     *
     * @param player
     * @param challenge
     * @return true if player can complete otherwise false
     */
    public boolean checkIfCanCompleteChallenge(final Player player, final String challenge) {
        // Utils.ConsoleMsg("DEBUG: " + player.getDisplayName() + " " +
        // challenge);
        // Utils.ConsoleMsg("DEBUG: 1");
        // Check if the challenge exists
        /*
        if (!isLevelAvailable(player, getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".level"))) {
            player.sendMessage(TextFormat.RED + plugin.myLocale(player.getUniqueId()).challengesunknownChallenge + " '" + challenge + "'");
            return false;
        }*/
        // Check if this challenge level is available
        String level = getChallengeConfig().getString("challenges.challengeList." + challenge + ".level");
        if (level == null) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "Unknown challange: '" + challenge + "'");
            return false;
        }
        // Only check if the challenge has a level, otherwise it's a free level
        if (!level.isEmpty()) {
            if (!isLevelAvailable(player, level)) {
                player.sendMessage(plugin.getPrefix() + TextFormat.RED + "You have not unlocked this challenge yet!");
                return false;
            }
        }
        // Check if the player has maxed out the challenge
        if (getChallengeConfig().getBoolean("challenges.challengeList." + challenge + ".repeatable")) {
            int maxTimes = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".maxtimes", 0);
            if (maxTimes > 0) {
                // There is a limit
                if (checkChallengeTimes(player, challenge) >= maxTimes) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This challange is not repeatable!");
                    return false;
                }
            }
        }
        // Utils.ConsoleMsg("DEBUG: 2");
        // Check if it is repeatable
        if (checkChallenge(player, challenge)
                && !getChallengeConfig().getBoolean("challenges.challengeList." + challenge + ".repeatable")) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This challange is not repeatable!");
            return false;
        }
        // Utils.ConsoleMsg("DEBUG: 3");
        // If the challenge is an island type and already done, then this too is
        // not repeatable
        if (checkChallenge(player, challenge)
                && getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("island")) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This challange is not repeatable!");
            return false;
        }
        // Utils.ConsoleMsg("DEBUG: 4");
        // Check if this is an inventory challenge
        if (getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("inventory")) {
            // Check if the player has the required items
            if (!hasRequired(player, challenge, "inventory")) {
                player.sendMessage(TextFormat.RED + "You dont have enough items");
                String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".description", "").replace("[label]", "/is"));
                List<String> result = new ArrayList<String>();
                if (desc.contains("|")) {
                    result.addAll(Arrays.asList(desc.split("\\|")));
                } else {
                    result.add(desc);
                }
                for (String line : result) {
                    player.sendMessage(TextFormat.RED + line);
                }
                return false;
            }
            return true;
        }
        // Utils.ConsoleMsg("DEBUG: 5");
        // Check if this is an island-based challenge
        if (getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("island")) {
            // Utils.ConsoleMsg("DEBUG: 6");
            if (!plugin.getGrid().playerIsOnIsland(player)) {
                player.sendMessage(TextFormat.RED + "You are not in island!");
                return false;
            }
            if (!hasRequired(player, challenge, "island")) {
                int searchRadius = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".searchRadius", 10);
                if (searchRadius < 10) {
                    searchRadius = 10;
                } else if (searchRadius > 50) {
                    searchRadius = 50;
                }
                player.sendMessage(TextFormat.RED + "You must be standing within [number] blocks of all required items.".replace("[number]", String.valueOf(searchRadius)));
                String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".description").replace("[label]", "is"));
                List<String> result = new ArrayList<String>();
                if (desc.contains("|")) {
                    result.addAll(Arrays.asList(desc.split("\\|")));
                } else {
                    result.add(desc);
                }
                for (String line : result) {
                    player.sendMessage(TextFormat.RED + line);
                }
                return false;
            }
            // Utils.ConsoleMsg("DEBUG: 7");
            return true;
        }
        // Island level check
        if (getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("level")) {
            if (plugin.getIslandLevel(player) >= getChallengeConfig().getInt("challenges.challengeList." + challenge + ".requiredItems")) {
                return true;
            }
            player.sendMessage(TextFormat.RED
                    + "Your island must be level [level] to complete this challenge!".replace("[level]",
                            String.valueOf(getChallengeConfig().getInt("challenges.challengeList." + challenge + ".requiredItems"))));
            return false;
        }
        player.sendMessage(TextFormat.RED + "Command not ready yet");
        plugin.getLogger().error(
                "The challenge " + challenge + " is of an unknown type " + getChallengeConfig().getString("challenges.challengeList." + challenge + ".type"));
        plugin.getLogger().error("Types should be 'island', 'inventory' or 'level'");
        return false;
    }

    /**
     * Checks if a player has enough for a challenge. Supports two types of
     * checks, inventory and island. Removes items if required.
     *
     * @param player
     * @param challenge
     * @param type
     * @return true if the player has everything required
     */
    public boolean hasRequired(final Player player, final String challenge, final String type) {
        //This part is todo
        return true;
    }

    private boolean checkChallenge(Player player, String challenge) {
        PlayerData pd = plugin.getPlayerInfo(player);
        return pd.checkChallenge(challenge);
    }

    public int checkChallengeTimes(Player player, String challenge) {
        PlayerData pd = plugin.getPlayerInfo(player);
        return pd.checkChallengeTimes(challenge);
    }

    /**
     * Gives the reward for completing the challenge
     *
     * @param player
     * @param challenge
     * @return ture if reward given successfully
     */
    private boolean giveReward(final Player player, final String challenge) {
        // Grab the rewards from the config.yml file
        String[] permList;
        String[] itemRewards;
        double moneyReward = 0;
        int expReward = 0;
        String rewardText = "";
        // If the friendly name is available use it
        String challengeName = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".friendlyname",
                challenge.substring(0, 1).toUpperCase() + challenge.substring(1)));

        // Gather the rewards due
        // If player has done a challenge already, the rewards are different
        if (!checkChallenge(player, challenge)) {
            // First time
            player.sendMessage(TextFormat.GREEN + "You completed the challange: [challenge]".replace("[challenge]", challengeName));
            if (Settings.broadcastMessages) {
                for (Player p : plugin.getServer().getOnlinePlayers().values()) {
                    p.sendMessage(
                            TextFormat.GOLD + "[name] just completed a challange: [challenge] !".replace("[name]", player.getDisplayName()).replace("[challenge]", challengeName));
                }
            }
            plugin.getMessages().tellOfflineTeam(player.getUniqueId(),
                    TextFormat.GOLD + "[name] just completed a challange: [challenge] !".replace("[name]", player.getName()).replace("[challenge]", challengeName));
            itemRewards = getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".itemReward", "").split(" ");
            moneyReward = getChallengeConfig().getDouble("challenges.challengeList." + challenge.toLowerCase() + ".moneyReward", 0D);
            rewardText = TextFormat.colorize('&',
                    getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".rewardText", "Goodies!"));
            expReward = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".expReward", 0);
        } else {
            // Repeat challenge
            player.sendMessage(TextFormat.GREEN + "You just reapeated challange: [challenge]".replace("[challenge]", challengeName));
            itemRewards = getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".repeatItemReward", "").split(" ");
            moneyReward = getChallengeConfig().getDouble("challenges.challengeList." + challenge.toLowerCase() + ".repeatMoneyReward", 0);
            rewardText = TextFormat.colorize('&',
                    getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".repeatRewardText", "Goodies!"));
            expReward = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".repeatExpReward", 0);
        }
        // Report the rewards and give out exp, money and permissions if
        // appropriate
        player.sendMessage(TextFormat.GOLD + "Rewards: " + TextFormat.WHITE + rewardText);
        if (expReward > 0) {
            player.sendMessage(TextFormat.GOLD + "EXPRewards: " + TextFormat.WHITE + expReward);
            player.addExperience(expReward);
        }
        if (Settings.useEconomy && moneyReward > 0) {
            //EconomyResponse e = VaultHelper.econ.depositPlayer(player, Settings.worldName, moneyReward);
            if (false) {
                //player.sendMessage(TextFormat.GOLD + plugin.myLocale(player.getUniqueId()).challengesmoneyReward + ": " + TextFormat.WHITE + VaultHelper.econ.format(moneyReward));
            } else {
                plugin.getLogger().error("Error giving player " + player.getUniqueId() + " challenge money:");//) e.errorMessage);
                plugin.getLogger().error("Reward was $" + moneyReward);
            }
        }
        // Dole out permissions
        permList = getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".permissionReward", "").split(" ");
        for (final String s : permList) {
            if (!s.isEmpty()) {
                player.addAttachment(plugin).setPermission(s, true);
                Utils.ConsoleMsg("Added permission " + s + " to " + player.getName() + "");
            }
        }
        // Give items
        List<Item> rewardedItems = giveItems(player, itemRewards);
        if (rewardedItems == null) {
            return false;
        }

        // Run reward commands
        if (!checkChallenge(player, challenge)) {
            // First time
            List<String> commands = getChallengeConfig().getStringList("challenges.challengeList." + challenge.toLowerCase() + ".rewardcommands");
            runCommands(player, commands);
        } else {
            // Repeat challenge
            List<String> commands = getChallengeConfig().getStringList("challenges.challengeList." + challenge.toLowerCase() + ".repeatrewardcommands");
            runCommands(player, commands);
        }

        // Mark the challenge as complete
        // if (!plugin.getPlayers().checkChallenge(player.getUniqueId(),challenge)) {
        plugin.getPlayerInfo(player).completeChallenge(challenge);
        // }
        // Call the Challenge Complete Event
        final ChallengeCompleteEvent event = new ChallengeCompleteEvent(player, challenge, permList, itemRewards, moneyReward, expReward, rewardText, rewardedItems);
        plugin.getServer().getPluginManager().callEvent(event);
        return true;
    }

    private List<Item> giveItems(Player player, String[] itemRewards) {
        List<Item> rewardedItems = new ArrayList<>();
        Item rewardItem;
        return rewardedItems;
    }

    private void runCommands(Player player, List<String> commands) {
    }

    private void completeChallenge(UUID uniqueId, String level) {
    }
}
