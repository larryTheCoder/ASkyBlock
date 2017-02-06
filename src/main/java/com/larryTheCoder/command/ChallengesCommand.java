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
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author larryTheCoder
 */
public class ChallengesCommand extends Command {

    private final ASkyBlock plugin;
    // Database of challenges
    private final LinkedHashMap<String, List<String>> challengeList = new LinkedHashMap<>();
    private Config challengeFile = null;
    private File challengeConfigFile;

    public ChallengesCommand(ASkyBlock ev) {
        super("challenges", "Challange yourself for some big prize", "\u00a77<parameters>", new String[]{"c", "chall", "ch"});
        this.reloadChallengeConfig();
        this.plugin = ev;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isPlayer()) {
            sender.sendMessage("Run this command in-game");
            return true;
        }
        Player p = plugin.getServer().getPlayer(sender.getName());
        switch (args.length) {
            case 0:
                if (isLevelAvailable(p, getChallengeConfig().getString("challenges.challengeList." + args[1].toLowerCase() + ".level"))) {
                    // Provide info on the challenge
                    // Challenge Name
                    // Description
                    // Type
                    // Items taken or not
                    // island or not
                    final String challenge = args[1].toLowerCase();
                    sender.sendMessage(TextFormat.GOLD + "Challenges Name: " + TextFormat.WHITE + challenge);
                    sender.sendMessage(TextFormat.WHITE + "Max Level: " + TextFormat.GOLD
                            + getChallengeConfig().getString("challenges.challengeList." + challenge + ".level", ""));
                    String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".description", "").replace("[label]", ""));
                    List<String> result = new ArrayList<>();
                    if (desc.contains("|")) {
                        result.addAll(Arrays.asList(desc.split("\\|")));
                    } else {
                        result.add(desc);
                    }
                    for (String line : result) {
                        sender.sendMessage(TextFormat.GOLD + line);
                    }
                    final String type = getChallengeConfig().getString("challenges.challengeList." + challenge + ".type", "").toLowerCase();
                    if (type.equals("inventory")) {
                        if (getChallengeConfig().getBoolean("challenges.challengeList." + args[1].toLowerCase() + ".takeItems")) {
                            sender.sendMessage(TextFormat.RED + "All required items are taken when you complete this challenge!");
                        }
                    } else if (type.equals("island")) {
                        sender.sendMessage(TextFormat.RED + "All required items must be close to you on your island!");
                    }
                    if (plugin.getPlayerInfo(p).checkChallenge(challenge)
                            && (!type.equals("inventory") || !getChallengeConfig().getBoolean("challenges.challengeList." + challenge + ".repeatable", false))) {
                        sender.sendMessage(TextFormat.RED + "This Challenge is not repeatable!");
                        return true;
                    }
                    double moneyReward;
                    int expReward;
                    String rewardText;

                    if (!plugin.getPlayerInfo(p).checkChallenge(challenge)) {
                        // First time
                        moneyReward = getChallengeConfig().getDouble("challenges.challengeList." + challenge.toLowerCase() + ".moneyReward", 0D);
                        rewardText = TextFormat.colorize('&',
                                getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".rewardText", "Goodies!"));
                        expReward = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".expReward", 0);
                        sender.sendMessage(TextFormat.GOLD + "First time reward(s)");
                    } else {
                        // Repeat challenge
                        moneyReward = getChallengeConfig().getDouble("challenges.challengeList." + challenge.toLowerCase() + ".repeatMoneyReward", 0D);
                        rewardText = TextFormat.colorize('&',
                                getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".repeatRewardText", "Goodies!"));
                        expReward = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".repeatExpReward", 0);
                        sender.sendMessage(TextFormat.GOLD + "Repeat reward(s)");

                    }
                    sender.sendMessage(TextFormat.WHITE + rewardText);
                    if (Settings.useEconomy && moneyReward > 0) {
                        //sender.sendMessage(TextFormat.GOLD + plugin.myLocale(player.getUniqueId()).challengesmoneyReward + ": " + TextFormat.WHITE + VaultHelper.econ.format(moneyReward));
                    }
                    //sender.sendMessage(TextFormat.GOLD + plugin.myLocale(player.getUniqueId()).challengestoCompleteUse + TextFormat.WHITE + " /" + label + " c " + challenge);
                } else {
                    //sender.sendMessage(TextFormat.RED + plugin.myLocale(player.getUniqueId()).challengesinvalidChallengeName);
                }
                break;
            case 1:
                if (args[1].equalsIgnoreCase("help") || args[1].equalsIgnoreCase("complete") || args[1].equalsIgnoreCase("c")) {
                    p.sendMessage("\u00a7aUse /c <name> to view information about a challenge.");
                    p.sendMessage("\u00a7aUse /c complete <name> to attempt to complete that challenge.");
                }
        }
        return true;
    }

    /**
     * Saves the challenge.yml file if it does not exist
     */
    public void saveDefaultChallengeConfig() {
        if (challengeConfigFile == null) {
            challengeConfigFile = new File(plugin.getDataFolder(), "challenges.yml");
        }
        if (!challengeConfigFile.exists()) {
            plugin.saveResource("challenges.yml", false);
        }
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
    public final void reloadChallengeConfig() {
        this.saveDefaultChallengeConfig();
        if (challengeConfigFile == null) {
            challengeConfigFile = new File(plugin.getDataFolder(), "challenges.yml");
        }
        challengeFile = new Config(challengeConfigFile);

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
}
