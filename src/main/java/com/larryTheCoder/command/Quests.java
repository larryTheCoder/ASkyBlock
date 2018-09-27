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

package com.larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.ChallengeLevelCompleteEvent;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.util.*;

@SuppressWarnings("ALL")
public class Quests extends Command {

    private final ASkyBlock plugin;
    private final Config challengeFile;
    private final LinkedHashMap<String, List<String>> challengeList = new LinkedHashMap<>();

    public Quests(ASkyBlock plugin) {
        super("quest");
        setAliases(new String[]{"q", "c", "challenges", "ch"});
        setDescription("Challenge yourself with the big quests");
        setPermission("island.challenges");
        setUsage("");

        // Configure the config file and then the
        // Settings itself.
        challengeFile = new Config(new File(plugin.getDataFolder(), "quests.yml"), Config.YAML);

        Settings.challengeList = challengeFile.getSection("challengeList").getKeys(false);
        Settings.challengeLevels = Arrays.asList(challengeFile.getString("levels", "").split(" "));
        Settings.freeLevels = Arrays.asList(challengeFile.getString("freelevels", "").split(" "));
        Settings.waiverAmount = challengeFile.getInt("waiveramount", 1);
        if (Settings.waiverAmount < 0) {
            Settings.waiverAmount = 0;
        }

        // Populate the list
        challengeList.clear();
        Settings.challengeList.forEach((s) -> {
            String level = challengeFile.getString("challengeList." + s + ".level", "");
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
                Utils.send("&cLevel (" + level + ") for quests " + s + " does not exist. Check quests.yml.");
            }
        });

        this.plugin = plugin;
        // TODO: Prefixs, improve interfaces, colorize, simplify
    }

    public LinkedHashMap<String, List<String>> getChallengeList() {
        return challengeList;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (!sender.isPlayer()) {
            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
            return true;
        }
        // Check island
        if (plugin.getIslandInfo(p) == null) {
            p.sendMessage(plugin.getLocale(p).errorNoIsland);
            return true;
        }
        switch (args.length) {
            case 0:
                // TODO: Refactor back the forms
                plugin.getPanel().addChallengesForm(p);
                break;
            case 1:
                if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("complete") || args[0].equalsIgnoreCase("c")) {
                    p.sendMessage(plugin.getLocale(p).challengesHelp1);
                    p.sendMessage(plugin.getLocale(p).challengesHelp2);
                } else if (isLevelAvailable(p, challengeFile.getString("challengeList." + args[0].toLowerCase() + ".level"))) {
                    // Provide info on the challenge
                    // - Challenge Name
                    // - Description
                    // - Type
                    // - Items taken or not
                    // - island or not
                    final String challenge = args[0].toLowerCase();
                    p.sendMessage(plugin.getLocale(p).challengesName + ": " + TextFormat.WHITE + challenge);
                    p.sendMessage(plugin.getLocale(p).challengesLevel + ": " + TextFormat.GOLD + getChallengeConfig().getString("challengeList." + challenge + ".level", ""));
                    List<String> result = new ArrayList<>();
                    for (String desc : challengeFile.getStringList("challengeList." + challenge + ".description")) {
                        result.add(TextFormat.colorize('&', desc.replace("[label]", "is")));

                    }
                    result.forEach((line) -> p.sendMessage(TextFormat.RED + line));

                    final String type = getChallengeConfig().getString("challengeList." + challenge + ".type", "").toLowerCase();
                    if (type.equals("inventory")) {
                        if (getChallengeConfig().getBoolean("challengeList." + args[0].toLowerCase() + ".takeItems")) {
                            p.sendMessage(plugin.getLocale(p).challengesItemTakeWarning);
                        }
                    } else if (type.equals("island")) {
                        p.sendMessage(plugin.getLocale(p).challengesErrorItemsNotThere);
                    }
                    if (checkChallenge(p, challenge) && (!type.equals("inventory") || !getChallengeConfig().getBoolean("challengeList." + challenge + ".repeatable", false))) {
                        p.sendMessage(plugin.getLocale(p).challengesNotRepeatable);
                        return true;
                    }
                    double moneyReward;
                    int expReward;
                    List<String> rewardText = new ArrayList<>();

                    if (!checkChallenge(p, challenge)) {
                        // First time
                        moneyReward = getChallengeConfig().getDouble("challengeList." + challenge.toLowerCase() + ".moneyReward", 0D);
                        // Reduce, Reuse, and fucking recycle
                        rewardText.addAll(challengeFile.getStringList("challengeList." + challenge.toLowerCase() + ".rewardText"));
                        expReward = getChallengeConfig().getInt("challengeList." + challenge + ".expReward", 0);
                        p.sendMessage(plugin.getLocale(p).challengesFirstTimeRewards);
                    } else {
                        // Repeat challenge
                        moneyReward = getChallengeConfig().getDouble("challengeList." + challenge.toLowerCase() + ".repeatMoneyReward", 0D);
                        // Reduce, Reuse, and fucking recycle
                        rewardText.addAll(challengeFile.getStringList("challengeList." + challenge.toLowerCase() + ".repeatRewardText"));
                        expReward = getChallengeConfig().getInt("challengeList." + challenge + ".repeatExpReward", 0);
                        p.sendMessage(plugin.getLocale(p).challengesRepeatRewards);
                    }

                    rewardText.forEach((line) -> p.sendMessage(TextFormat.RED + line));
                    if (expReward > 0) {
                        p.sendMessage(plugin.getLocale(p).challengesExpReward + ": " + TextFormat.WHITE + expReward);
                    }
                    if (Settings.useEconomy && moneyReward > 0) {
                        p.sendMessage(plugin.getLocale(p).challengesMoneyReward + ": " + TextFormat.WHITE + "$" + moneyReward);
                    }
                    p.sendMessage(plugin.getLocale(p).challengesToCompleteUse + TextFormat.WHITE + " /" + label + " c " + challenge);
                } else {
                    p.sendMessage(plugin.getLocale(p).challengesInvalidChallengeName);
                }
                break;
            case 2:
                if (checkIfCanCompleteChallenge(p, args[1].toLowerCase())) {
                    int oldLevel = getLevelDone(p);
                    giveReward(p, args[1].toLowerCase());
                    int newLevel = getLevelDone(p);
                    // Fire an event if they are different
                    //Utils.send("DEBUG: " + oldLevel + " " + newLevel);
                    if (oldLevel < newLevel) {
                        // Update chat
                        plugin.getChatHandlers().setPlayerChallengeLevel(p);
                        // Run commands and give rewards but only if they haven't done it below
                        //Utils.send("DEBUG: old level = " + oldLevel + " new level = " + newLevel);
                        String level = Settings.challengeLevels.get(newLevel);
                        if (!level.isEmpty() && !checkChallenge(p, level)) {
                            //Utils.send("DEBUG: level name = " + level);
                            completeChallenge(p, level);
                            String message = TextFormat.colorize('&', getChallengeConfig().getString("challenges.levelUnlock." + level + ".message", ""));
                            if (!message.isEmpty()) {
                                p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + message);
                            }
                            String[] itemReward = getChallengeConfig().getString("challenges.levelUnlock." + level + ".itemReward", "").split(" ");
                            String rewardDesc = getChallengeConfig().getString("challenges.levelUnlock." + level + ".rewardDesc", "");
                            if (!rewardDesc.isEmpty()) {
                                p.sendMessage(plugin.getPrefix() + TextFormat.GOLD + ": " + TextFormat.WHITE + rewardDesc);
                            }
                            List<Item> rewardedItems = giveItems(p, itemReward);
                            double moneyReward = getChallengeConfig().getDouble("challenges.levelUnlock." + level + ".moneyReward", 0D);
                            int expReward = getChallengeConfig().getInt("challenges.levelUnlock." + level + ".expReward", 0);
                            if (expReward > 0) {
                                p.sendMessage(plugin.getPrefix() + TextFormat.GOLD + "You got Exp: " + TextFormat.WHITE + expReward);
                                p.addExperience(expReward);
                            }
                            if (Settings.useEconomy && moneyReward > 0 && (ASkyBlock.econ != null)) {
                                ASkyBlock.econ.addMoney(p, moneyReward);
                                p.sendMessage(plugin.getPrefix() + TextFormat.GOLD + "You received : " + TextFormat.WHITE + "$" + moneyReward);
                            }
                            String[] permList = getChallengeConfig().getString("challenges.levelUnlock." + level + ".permissionReward", "").split(" ");

                            for (final String s : permList) {
                                if (!s.isEmpty()) {
                                    p.addAttachment(plugin).setPermission(s, true);
                                    Utils.send("Added permission " + s + " to " + p.getName() + "");
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
                break;
        }

        return true;
    }

    private void completeChallenge(Player player, String level) {
        PlayerData pd = plugin.getPlayerInfo(player);
        pd.completeChallenge(level);
        pd.saveData();
    }

    /**
     * Checks the highest level this player has achieved
     *
     * @param player The player that need to be retrieved
     * @return level number
     */
    public int getLevelDone(Player player) {
        //Utils.send("DEBUG: checking level completed");
        //Utils.send("DEBUG: getting challenge level for " + player.getName());
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
     * @param player    The player that need to complete this challenge
     * @param challenge The challenge that need to be checked
     * @return true if player can complete otherwise false
     */
    public boolean checkIfCanCompleteChallenge(Player player, String challenge) {
        // Check if this challenge level is available
        String level = getChallengeConfig().getString("challengeList." + challenge + ".level");
        if (level == null) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "Unknown quests: '" + challenge + "'");
            return false;
        }
        // Only check if the challenge has a level, otherwise it's a free level
        if (!level.isEmpty() && !isLevelAvailable(player, level)) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "You have not unlocked this challenge yet!");
            return false;
        }
        // Check if the player has maxed out the challenge
        if (getChallengeConfig().getBoolean("challengeList." + challenge + ".repeatable")) {
            int maxTimes = getChallengeConfig().getInt("challengeList." + challenge + ".maxtimes", 0);
            if (maxTimes > 0) {
                // There is a limit
                if (checkChallengeTimes(player, challenge) >= maxTimes) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This quests is not repeatable!");
                    return false;
                }
            }
        }
        // Check if it is repeatable
        if (checkChallenge(player, challenge) && !getChallengeConfig().getBoolean("challengeList." + challenge + ".repeatable")) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This quests is not repeatable!");
            return false;
        }
        // If the challenge is an island type and already done, then this too is
        // not repeatable
        if (checkChallenge(player, challenge) && getChallengeConfig().getString("challengeList." + challenge + ".type").equalsIgnoreCase("island")) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This quests is not repeatable!");
            return false;
        }
        // Check if this is an inventory challenge
        if (getChallengeConfig().getString("challengeList." + challenge + ".type").equalsIgnoreCase("inventory")) {
            // Check if the player has the required items
            if (!hasRequired(player, challenge, "inventory")) {
                player.sendMessage(TextFormat.RED + "You don't have enough items");
                List<String> result = new ArrayList<>();
                for (String desc : challengeFile.getStringList("challengeList." + challenge + ".description")) {
                    result.add(TextFormat.colorize('&', desc.replace("[label]", "is")));

                }
                result.forEach((line) -> player.sendMessage(TextFormat.RED + line));
                return false;
            }
            return true;
        }
        // Utils.send("DEBUG: 5");
        // Check if this is an island-based challenge
        if (getChallengeConfig().getString("challengeList." + challenge + ".type").equalsIgnoreCase("island")) {
            // Utils.send("DEBUG: 6");
            if (!plugin.getGrid().playerIsOnIsland(player)) {
                player.sendMessage(TextFormat.RED + "You are not in island!");
                return false;
            }
            if (!hasRequired(player, challenge, "island")) {
                int searchRadius = getChallengeConfig().getInt("challengeList." + challenge + ".searchRadius", 10);
                if (searchRadius < 10) {
                    searchRadius = 10;
                } else if (searchRadius > 50) {
                    searchRadius = 50;
                }
                player.sendMessage(TextFormat.RED + "You must be standing within [number] blocks of all required items.".replace("[number]", String.valueOf(searchRadius)));
                List<String> result = new ArrayList<>();
                for (String desc : challengeFile.getStringList("challengeList." + challenge + ".description")) {
                    result.add(TextFormat.colorize('&', desc.replace("[label]", "is")));

                }
                result.forEach((line) -> player.sendMessage(TextFormat.RED + line));
                return false;
            }
            // Utils.send("DEBUG: 7");
            return true;
        }
        // Island level check
        if (getChallengeConfig().getString("challengeList." + challenge + ".type").equalsIgnoreCase("level")) {
            if (plugin.getIslandLevel(player) >= getChallengeConfig().getInt("challengeList." + challenge + ".requiredItems")) {
                return true;
            }
            player.sendMessage(TextFormat.RED
                    + "Your island must be level [level] to complete this challenge!".replace("[level]",
                    String.valueOf(getChallengeConfig().getInt("challengeList." + challenge + ".requiredItems"))));
            return false;
        }
        player.sendMessage(TextFormat.RED + "Command not ready yet");
        Utils.send(TextFormat.RED
                + "The challenge " + challenge + " is of an unknown type " + getChallengeConfig().getString("challengeList." + challenge + ".type"));
        Utils.send(TextFormat.RED + "Types should be 'island', 'inventory' or 'level'");
        return false;
    }

    /**
     * Gets the name of the highest challenge level the player has completed
     *
     * @param player The player or the target
     * @return challenge level
     */
    public String getChallengeLevel(Player player) {
        //Utils.send("DEBUG: getting challenge level for " + player.getName());
        if (Settings.challengeLevels.isEmpty()) {
            return "";
        }
        return Settings.challengeLevels.get(getLevelDone(player));
    }

    /**
     * Returns true if the level is unlocked and false if not
     *
     * @param player The player
     * @param level  level
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
     * @param player The player itself
     * @param level  The level that need to be checked
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
            challengesCompleted = levelChallengeList.stream().filter((challenge) -> (plugin.getPlayerInfo(player).checkChallenge(challenge))).map((_item) -> 1).reduce(challengesCompleted, Integer::sum);
            // If the number of challenges in a level is below the waiver amount, then they all need to be done
            if (levelChallengeList.size() <= Settings.waiverAmount) {
                waiver = 0;
            }
            return levelChallengeList.size() - waiver - challengesCompleted;
        }
        return 0;
    }

    /**
     * Checks if a player has enough for a challenge. Supports two types of
     * checks, inventory and island. Removes items if required.
     *
     * @param player    The player that need to be checked
     * @param challenge The type of the challenge
     * @param type      The type of the checks
     * @return true if the player has everything required
     */
    public boolean hasRequired(Player player, String challenge, String type) {
        return hasRequired(player, challenge, type, false);
    }

    /**
     * Checks if a player has enough for a challenge. Supports two types of
     * checks, inventory and island. Removes items if required.
     *
     * @param player    The player that need to be checked
     * @param challenge The type of the challenge
     * @param type      The type of the checks
     * @param silence   Should the check be silenced or not, this will not send the error
     *                  message to the player
     * @return true if the player has everything required
     */
    public boolean hasRequired(Player player, String challenge, String type, boolean silence) {
        //Utils.sendDebug("DEBUG: Checking " + challenge + " for " + player.getName() + " in " + type);
        // Check money
        double moneyReq = 0;
        if (Settings.useEconomy) {
            moneyReq = challengeFile.getDouble("challengeList." + challenge + ".requiredMoney", 0D);
            if (moneyReq > 0 && !ASkyBlock.econ.reduceMoney(player, moneyReq)) {
                List<String> result = new ArrayList<>();
                for (String desc : challengeFile.getStringList("challengeList." + challenge + ".description")) {
                    result.add(TextFormat.colorize('&', desc.replace("[label]", "is")));
                }

                if (!silence) {
                    player.sendMessage(TextFormat.RED + "You don't have enough money!");
                    result.forEach((line) -> player.sendMessage(TextFormat.RED + line));
                }
                return false;
            }
        }

        String[] reqList = challengeFile.getString("challengeList." + challenge + ".requiredItems").split(" ");

        // The format of the requiredItems is as follows:
        // Item:Qty or Item:damage:Qty

        // This second one is so that items such as potions or variations on
        // standard items can be collected
        switch (type.toLowerCase()) {
            case "inventory":
                List<Item> toBeRemoved = new ArrayList<>();
                Item reqItem;
                for (final String s : reqList) {
                    String[] part = s.split(":");
                    // Item:Qty
                    if (part.length == 2) {
                        try {
                            if (Utils.isNumeric(part[0])) {
                                reqItem = Item.get(Integer.parseInt(part[0]));
                            } else {
                                reqItem = Item.fromString(part[0].toUpperCase());
                            }
                            int reqAmount = Integer.parseInt(part[1]);
                            reqItem.setCount(reqAmount);
                            Item item = reqItem;

                            //Utils.sendDebug("DEBUG: required item = " + reqItem.toString());
                            //Utils.sendDebug("DEBUG: item amount = " + reqAmount);
                            if (!player.getInventory().contains(reqItem)) {
                                //Utils.sendDebug("DEBUG: item not in inventory");
                                return false;
                            } else {
                                // check amount
                                int amount = 0;
                                //Utils.sendDebug("DEBUG: Amount in inventory = " + player.getInventory().all(reqItem).size());

                                // Go through all the inventory and try to find
                                // enough required items
                                for (Map.Entry<Integer, Item> en : player.getInventory().all(reqItem).entrySet()) {
                                    // Get the item
                                    Item i = en.getValue();
                                    // If the item is enchanted, skip - it doesn't count
                                    // Map needs special handling because the
                                    // durability increments every time a new one is
                                    // made by the player
                                    if (!i.hasEnchantments() || (reqItem.getId() == Item.MAP && i.getId() == Item.MAP)) {
                                        if ((amount + i.getCount()) < reqAmount) {
                                            // Remove all of this item stack - clone
                                            // otherwise it will keep a reference to
                                            // the original
                                            toBeRemoved.add(i.clone());
                                            amount += i.getCount();
                                            //Utils.sendDebug("DEBUG: amount is <= req Remove " + i.toString() + ":" + i.getDamage() + " x " + i.getCount());
                                        } else if ((amount + i.getCount()) == reqAmount) {
                                            //Utils.sendDebug("DEBUG: amount is = req Remove " + i.toString() + ":" + i.getDamage() + " x " + i.getCount());
                                            toBeRemoved.add(i.clone());
                                            amount += i.getCount();
                                            break;
                                        } else {
                                            //Utils.sendDebug("DEBUG: amount is > req Remove " + i.toString() + ":" + i.getDamage() + " x " + i.getCount());
                                            // Remove a portion of this item
                                            item.setCount(reqAmount - amount);
                                            toBeRemoved.add(item);
                                            amount += i.getCount();
                                            break;
                                        }
                                    }
                                }
                                //Utils.sendDebug("DEBUG: amount " + amount);

                                // Check if the amount is low
                                if (amount < reqAmount) {
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            Utils.send(TextFormat.RED + "Problem with " + s + " in quests.yml!");
                            Utils.send(TextFormat.RED + "Correct quests.yml with the correct material.");
                            if (!silence)
                                player.sendMessage(TextFormat.RED + "Error: Quest error contact server admin");
                        }
                    } else if (part.length == 3) {
                        //Utils.sendDebug("DEBUG: Item with durability");
                        if (Utils.isNumeric(part[0])) {
                            reqItem = Item.get(Integer.parseInt(part[0]));
                        } else {
                            reqItem = Item.fromString(part[0].toUpperCase());
                        }
                        int reqAmount = Integer.parseInt(part[2]);
                        int reqDurability = Integer.parseInt(part[1]);
                        reqItem.setCount(reqAmount);
                        reqItem.setDamage(reqDurability);

                        Item item = reqItem;

                        // check amount
                        int amount = 0;
                        // Go through all the inventory and try to find
                        // enough required items
                        for (Map.Entry<Integer, ? extends Item> en : player.getInventory().all(reqItem).entrySet()) {
                            // Get the item
                            Item i = en.getValue();
                            if (i.getDamage() == reqDurability) {
                                // Clear any naming, or lore etc.
                                if ((amount + i.getCount()) < reqAmount) {
                                    // Remove all of this item stack - clone
                                    // otherwise it will keep a reference to
                                    // the original
                                    toBeRemoved.add(i.clone());
                                    amount += i.getCount();
                                    //Utils.sendDebug("DEBUG: amount is <= req Remove " + i.toString() + ":" + i.getDamage() + " x " + i.getCount());
                                } else if ((amount + i.getCount()) == reqAmount) {
                                    toBeRemoved.add(i.clone());
                                    amount += i.getCount();
                                    //Utils.sendDebug("DEBUG: amount is = req Remove " + i.toString() + ":" + i.getDamage() + " x " + i.getCount());
                                    break;
                                } else {
                                    //Utils.sendDebug("DEBUG: amount is > req Remove " + i.toString() + ":" + i.getDamage() + " x " + i.getCount());

                                    // Remove a portion of this item
                                    item.setCount(reqAmount - amount);
                                    item.setDamage(i.getDamage());
                                    toBeRemoved.add(item);
                                    amount += i.getCount();
                                    break;
                                }
                            }
                        }
                        //Utils.sendDebug("DEBUG: amount is " + amount);
                        //Utils.sendDebug("DEBUG: req amount is " + reqAmount);

                        if (amount < reqAmount) {
                            //Utils.sendDebug("DEBUG: Failure! Insufficient amount of " + item.toString() + " required = " + reqAmount + " actual = " + amount);
                            //Utils.sendDebug("DEBUG: Insufficient items around");
                            return false;
                        }
                        //Utils.sendDebug("DEBUG: before set amount " + item.toString() + ":" + item.getDamage() + " x " + item.getCount());
                    } else {
                        Utils.send("&cProblem with " + s + " in challenges.yml!");
                        return false;
                    }
                }
                // Build up the items in the inventory and remove them if they are
                // all there.
                if (challengeFile.getBoolean("challengeList." + challenge + ".takeItems") && !silence) {
                    int qty = 0;
                    //Utils.sendDebug("DEBUG: Removing items");
                    for (Item i : toBeRemoved) {
                        qty++;
                        //Utils.sendDebug("DEBUG: Remove " + i.toString() + "::" + i.getDamage() + " x " + i.getCount());
                        Item[] leftOver = player.getInventory().removeItem(i);
                        if (leftOver.length != 0) {
                            Utils.send("&cExploit? Could not remove the following in quest " + challenge + " for player " + player.getName() + ":");
                            for (Item left : leftOver) {
                                Utils.send(TextFormat.GREEN + left.toString());
                            }
                            return false;
                        }
                    }
                    // Remove money
                    if (moneyReq > 0) {
                        if (!ASkyBlock.econ.reduceMoney(player, moneyReq)) {
                            Utils.send("&cExploit? Could not remove $" + moneyReq + " from " + player.getName() + " in quest " + challenge);
                        }
                    }
                    //Utils.sendDebug("DEBUG: total = " + qty);
                }
                break;
            case "island":
                HashMap<Block, Integer> neededItem = new HashMap<>();
                Block reqBlock;
                for (String aReqList : reqList) {
                    String[] sPart = aReqList.split(":");
                    // Parse the qty required first
                    try {
                        final int qty = Integer.parseInt(sPart[1]);

                        if (Utils.isNumeric(sPart[0])) {
                            reqBlock = Block.get(Integer.parseInt(sPart[0]));
                        } else {
                            reqBlock = Block.get(Item.fromString(sPart[0].toUpperCase()).getId());
                        }
                        if (reqBlock != null) {
                            neededItem.put(reqBlock, qty);
                        } else {
                            plugin.getLogger().warning("Problem parsing required item for quest " + challenge + " in quests.yml!");
                            return false;
                        }
                    } catch (Exception e) {
                        Utils.send(TextFormat.RED + "Problem with " + aReqList + " in quests.yml!");
                        Utils.send(TextFormat.RED + "Correct quests.yml with the correct material.");
                        if (!silence) player.sendMessage(TextFormat.RED + "Error: Quest error contact server admin");
                    }
                }
                // We now have two sets of required items or entities
                // Check the items first
                Location l = player.getLocation();
                int px = l.getFloorX();
                int py = l.getFloorY();
                int pz = l.getFloorZ();
                // Get search radius - min is 10, max is 50
                int searchRadius = challengeFile.getInt("quests.challengeList." + challenge + ".searchRadius", 10);
                if (searchRadius < 10) {
                    searchRadius = 10;
                } else if (searchRadius > 50) {
                    searchRadius = 50;
                }
                for (int x = -searchRadius; x <= searchRadius; x++) {
                    for (int y = -searchRadius; y <= searchRadius; y++) {
                        for (int z = -searchRadius; z <= searchRadius; z++) {
                            Block b = new Location(px + x, py + y, pz + z, l.getLevel()).getLevelBlock();
                            if (neededItem.containsKey(b)) {
                                if (neededItem.get(b) == 1) {
                                    neededItem.remove(b);
                                } else {
                                    // Reduce the require amount by 1
                                    neededItem.put(b, neededItem.get(b) - 1);
                                }
                            }
                        }
                    }
                }
                // Check if all the needed items have been amassed
                if (!neededItem.isEmpty()) {
                    //Utils.sendDebug("DEBUG: Insufficient items around");
                    for (Block missing : neededItem.keySet()) {
                        if (!silence)
                            player.sendMessage(TextFormat.RED + "You are missing " + neededItem.get(missing) + " x " + Utils.prettifyText(missing.toString()));
                    }
                    return false;
                }
                break;
            case "level":
                if (plugin.getIslandLevel(player) < getChallengeConfig().getInt("challengeList." + challenge + ".requiredItems")) {
                    return false;
                }
                break;
            default:
                Utils.send("&cUnknown type of checks: " + type);
                return false;
        }
        //Utils.sendDebug("DEBUG: Has required all the items for:" + type);
        return true;
    }

    /**
     * Gives the reward for completing the challenge
     *
     * @param player    The player that executes the command
     * @param challenge The challenge of the player
     */
    public void giveReward(Player player, String challenge) {
        // Grab the rewards from the config.yml file
        String[] permList;
        String[] itemRewards;
        List<String> rewardText = new ArrayList<>();
        double moneyReward;
        int expReward = 0;
        // If the friendly name is available use it
        String challengeName = TextFormat.colorize('&', challengeFile.getString("challengeList." + challenge + ".friendlyname", challenge.substring(0, 1).toUpperCase() + challenge.substring(1)));

        // Gather the rewards due
        // If player has done a challenge already, the rewards are different
        if (!checkChallenge(player, challenge)) {
            // First time
            player.sendMessage(TextFormat.GREEN + "You completed the challenge: [challenge]".replace("[challenge]", challengeName));
            if (Settings.broadcastMessages) {
                plugin.getServer().getOnlinePlayers().values().forEach((p) -> p.sendMessage(TextFormat.GOLD + "[name] just completed a Quest: [challenge] !".replace("[name]", player.getDisplayName()).replace("[challenge]", challengeName)));
            }
            // Reduce, Reuse, and fucking recycle
            for (String reward : challengeFile.getStringList("challengeList." + challenge.toLowerCase() + ".rewardText")) {
                rewardText.add(reward);
            }
            plugin.getMessages().tellOfflineTeam(player.getName(), TextFormat.GOLD + "[name] just completed a Quest: [challenge] !".replace("[name]", player.getName()).replace("[challenge]", challengeName));
            itemRewards = challengeFile.getString("challengeList." + challenge.toLowerCase() + ".itemReward", "").split(" ");
            moneyReward = challengeFile.getDouble("challengeList." + challenge.toLowerCase() + ".moneyReward", 0D);
            expReward = challengeFile.getInt("challengeList." + challenge + ".expReward", 0);
        } else {
            player.sendMessage(TextFormat.GREEN + "You just repeated challenge: [challenge]".replace("[challenge]", challengeName));
            // Second time
            if (Settings.broadcastMessages) {
                plugin.getServer().getOnlinePlayers().values().forEach((p) -> p.sendMessage(TextFormat.GOLD + "[name] just completed a Quest: [challenge] !".replace("[name]", player.getDisplayName()).replace("[challenge]", challengeName)));
            }
            // Reduce, Reuse, and fucking recycle
            for (String reward : challengeFile.getStringList("challengeList." + challenge.toLowerCase() + ".repeatRewardText")) {
                rewardText.add(reward);
            }
            itemRewards = challengeFile.getString("challengeList." + challenge.toLowerCase() + ".repeatItemReward", "").split(" ");
            moneyReward = challengeFile.getDouble("challengeList." + challenge.toLowerCase() + ".repeatMoneyReward", 0);
            expReward = challengeFile.getInt("challengeList." + challenge + ".repeatExpReward", 0);
        }

        rewardText.iterator().forEachRemaining((i) ->  player.sendMessage(TextFormat.WHITE + i));

        if (expReward > 0) {
            player.sendMessage(TextFormat.GOLD + "EXPRewards: " + TextFormat.WHITE + expReward);
            player.addExperience(expReward);
        }

        // Use economy huh?
        if (Settings.useEconomy && moneyReward > 0) {
            if (ASkyBlock.econ.addMoney(player, moneyReward)) {
                player.sendMessage(TextFormat.GOLD + "You retrieved" + TextFormat.WHITE + " $" + moneyReward);
            } else {
                Utils.send(TextFormat.RED + "Error giving player " + player + " challenge money:");
                Utils.send(TextFormat.RED + "Reward was $" + moneyReward);
            }
        }

        // Dole out stupid permissions
        permList = challengeFile.getString("challengeList." + challenge.toLowerCase() + ".permissionReward", "").split(" ");
        for (final String s : permList) {
            if (!s.isEmpty()) {
                player.addAttachment(plugin).setPermission(s, true);
                Utils.send("Added permission " + s + " to " + player.getName() + "");
            }
        }

        // Give items
        List<Item> rewardedItems = giveItems(player, itemRewards);
        if (rewardedItems == null) {
            return;
        }

        // Run reward commands
        if (!checkChallenge(player, challenge)) {
            // First time
            List<String> commands = challengeFile.getStringList("challengeList." + challenge.toLowerCase() + ".rewardcommands");
            runCommands(player, commands);
        } else {
            // Repeat challenge
            List<String> commands = challengeFile.getStringList("challengeList." + challenge.toLowerCase() + ".repeatrewardcommands");
            runCommands(player, commands);
        }

        completeChallenge(player, challenge);
    }

    /**
     * Execute a command to the player
     *
     * @param player   The player itself that need to execute it
     * @param commands The command line that need to be executed
     */
    public void runCommands(Player player, List<String> commands) {
        for (String cmd : commands) {
            if (cmd.startsWith("[SELF]")) {
                plugin.getLogger().info("Running command '" + cmd + "' as " + player.getName());
                cmd = cmd.substring(6, cmd.length()).replace("[player]", player.getName()).trim();
                try {
                    plugin.getServer().dispatchCommand(player, cmd);
                } catch (Exception e) {
                    Utils.send(TextFormat.RED + "Problem executing island command executed by player - skipping!");
                    Utils.send(TextFormat.RED + "Command was : " + cmd);
                    Utils.send(TextFormat.RED + "Error was: " + e.getMessage());
                    e.printStackTrace();
                }

                continue;
            }
            // Substitute in any references to player
            try {
                if (!plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("[player]", player.getName()))) {
                    Utils.send(TextFormat.RED + "Problem executing Quest reward commands - skipping!");
                    Utils.send(TextFormat.RED + "Command was : " + cmd);
                }
            } catch (Exception e) {
                Utils.send(TextFormat.RED + "Problem executing Quest reward commands - skipping!");
                Utils.send(TextFormat.RED + "Command was : " + cmd);
                Utils.send(TextFormat.RED + "Error was: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Give the player the items for the rewards
     * list. Also its converts the string typed items
     * to Item class itself
     *
     * @param player      The player who executed this command
     * @param itemRewards The item that need to be converted
     * @return The list of the items.
     */
    public List<Item> giveItems(Player player, String[] itemRewards) {
        List<Item> rewardedItems = new ArrayList<>();
        Item[] leftOvers;
        Item rewardItem;
        Item tempItemHolder;

        // Build the item stack of rewards to give the player
        for (String s : itemRewards) {
            String[] element = s.split(":");
            if (element.length == 2) {
                // Item:Qty
                try {
                    if (Utils.isNumeric(element[0])) {
                        rewardItem = Item.get(Integer.parseInt(element[0]));
                    } else {
                        rewardItem = Item.fromString(element[0].toUpperCase());
                    }
                    // Here is the item that will be parsed and placed
                    // Into the array list
                    tempItemHolder = new Item(rewardItem.getId(), 0, Integer.parseInt(element[1]));
                    rewardedItems.add(tempItemHolder);
                    leftOvers = player.getInventory().addItem(tempItemHolder);
                    if (leftOvers.length != 0) {
                        player.getLevel().dropItem(player.getLocation(), leftOvers[0]);
                    }
                } catch (Exception e) {
                    Utils.send(TextFormat.RED + "Could not give " + element[0] + ":" + element[1] + " to " + player.getName() + " for Quest reward!");
                    player.sendMessage(TextFormat.RED + "There a problem while executing your command");
                    return null;
                }
            } else if (element.length == 3) {
                // Item:damage:Qty
                try {
                    if (Utils.isNumeric(element[0])) {
                        rewardItem = Item.get(Integer.parseInt(element[0]));
                    } else {
                        rewardItem = Item.fromString(element[0].toUpperCase());
                    }

                    // TODO: Checks potions
                    int rewMod = Integer.parseInt(element[1]);
                    tempItemHolder = new Item(rewardItem.getId(), Integer.parseInt(element[2]), rewMod);

                    rewardedItems.add(tempItemHolder);
                    leftOvers = player.getInventory().addItem(tempItemHolder);
                    if (leftOvers.length != 0) {
                        player.getLevel().dropItem(player.getLocation(), leftOvers[0]);
                    }

                } catch (Exception e) {
                    player.sendMessage(TextFormat.RED + "There was a problem giving your reward. Ask Admin to check log!");
                    Utils.send(TextFormat.RED + "Could not give " + element[0] + ":" + element[1] + ":" + element[2] + " to " + player.getName() + " for Quest reward!");
                    return null;
                }
            }
            // TODO: Potions
        }

        return rewardedItems;
    }

    private boolean checkChallenge(Player player, String challenge) {
        return plugin.getPlayerInfo(player).checkChallenge(challenge);
    }

    private int checkChallengeTimes(Player player, String challenge) {
        return plugin.getPlayerInfo(player).checkChallengeTimes(challenge);
    }

    public Config getChallengeConfig() {
        return challengeFile;
    }
}
