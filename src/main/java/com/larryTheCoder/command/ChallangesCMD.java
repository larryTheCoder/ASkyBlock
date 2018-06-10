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
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPotion;
import cn.nukkit.item.ItemPotionSplash;
import cn.nukkit.potion.Potion;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.ChallengeCompleteEvent;
import com.larryTheCoder.events.ChallengeLevelCompleteEvent;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.util.*;

/**
 * @author Adam Matthew
 */
public final class ChallangesCMD extends Command {

    private final ASkyBlock plugin;
    // Database of challenges
    private final LinkedHashMap<String, List<String>> challengeList = new LinkedHashMap<>();
    private Config challengeFile = null;

    public ChallangesCMD(ASkyBlock ev) {
        super("challenges", "Challange yourself for some big prize", "\u00a77<parameters>", new String[]{"c", "chall", "ch"});
        this.plugin = ev;
        saveDefaultChallengeConfig();
        reloadChallengeConfig();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (!sender.isPlayer()) {
            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
            return true;
        }
        if (args.length == 0) {
            plugin.getPanel().addChallengesFormOverlay(p); // Easy breezy
            return true;
        }
        switch (args[0]) {
            case "help":
                p.sendMessage("§d--- §aChallanges help page §e1 §aof §e1 §d---");
                p.sendMessage("§aUse /c <name> to view information about a challenge.");
                p.sendMessage("§aUse /c complete <name> to attempt to complete that challenge.");
                p.sendMessage("§aUse /c list <page> to view all information about the challenge.");
                break;
            case "list":
                if (args.length == 2) {
                    this.showHelp(p, Integer.valueOf(args[1]));
                } else {
                    this.showHelp(p, 1);
                }
                break;
            case "complete":
                if (args.length != 2) {
                    p.sendMessage(plugin.getPrefix() + "§eUsage: /c complete [challenge name]");
                    break;
                }
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
            default:
                plugin.getPanel().addChallengesFormOverlay(p);
                break;
        }
        return true;
    }

    private void showHelp(CommandSender sender, int numbers) {
        List<String> names = new ArrayList<>(getChallengeConfig().getSection("challenges.challengeList").getKeys(false));
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
                sender.sendMessage(TextFormat.GREEN + "Challenges Name: " + TextFormat.YELLOW + cn);
                sender.sendMessage(TextFormat.GREEN + "Max Level: " + TextFormat.YELLOW
                        + getChallengeConfig().getString("challenges.challengeList." + cn + ".level", ""));
                String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + cn + ".description", "").replace("[label]", "is"));
                List<String> result = new ArrayList<>();
                if (desc.contains("|")) {
                    result.addAll(Arrays.asList(desc.split("\\|")));
                } else {
                    result.add(desc);
                }
                result.forEach((line) -> sender.sendMessage(TextFormat.YELLOW + line));
                final String type = getChallengeConfig().getString("challenges.challengeList." + cn + ".type", "").toLowerCase();
                if (type.equals("inventory")) {
                    if (getChallengeConfig().getBoolean("challenges.challengeList." + cn + ".takeItems")) {
                        sender.sendMessage(TextFormat.RED + "All required items are taken in your inventory when you complete this challenge!");
                    }
                } else if (type.equals("island")) {
                    sender.sendMessage(TextFormat.RED + "All required items must be close to you on your island!");
                }
                sender.sendMessage(TextFormat.YELLOW + "------------------------------------");
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
    private void saveDefaultChallengeConfig() {
        challengeFile = new Config(new File(plugin.getDataFolder(), "challenges.yml"), Config.YAML);
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
    private void reloadChallengeConfig() {
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
    private void populateChallengeList() {
        challengeList.clear();
        Settings.challengeList.forEach((s) -> {
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
                Utils.send("&cLevel (" + level + ") for challenge " + s + " does not exist. Check challenges.yml.");
            }
        });
    }

    /**
     * Returns true if the level is unlocked and false if not
     *
     * @param player The player
     * @param level  level
     * @return true/false
     */
    private boolean isLevelAvailable(final Player player, final String level) {
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
    private int checkLevelCompletion(final Player player, final String level) {
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
     * Gets the name of the highest challenge level the player has completed
     *
     * @param player
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
     * Checks the highest level this player has achieved
     *
     * @param player
     * @return level number
     */
    private int getLevelDone(Player player) {
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
     * @param player
     * @param challenge
     * @return true if player can complete otherwise false
     */
    private boolean checkIfCanCompleteChallenge(final Player player, final String challenge) {
        // Utils.send("DEBUG: " + player.getDisplayName() + " " +
        // challenge);
        // Utils.send("DEBUG: 1");
        // Check if the challenge exists
        /*
        if (!isLevelAvailable(player, getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".level"))) {
            player.sendMessage(TextFormat.RED + plugin.myLocale(player).challengesunknownChallenge + " '" + challenge + "'");
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
        // Utils.send("DEBUG: 2");
        // Check if it is repeatable
        if (checkChallenge(player, challenge)
                && !getChallengeConfig().getBoolean("challenges.challengeList." + challenge + ".repeatable")) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This challange is not repeatable!");
            return false;
        }
        // Utils.send("DEBUG: 3");
        // If the challenge is an island type and already done, then this too is
        // not repeatable
        if (checkChallenge(player, challenge)
                && getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("island")) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This challange is not repeatable!");
            return false;
        }
        // Utils.send("DEBUG: 4");
        // Check if this is an inventory challenge
        if (getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("inventory")) {
            // Check if the player has the required items
            if (hasRequired(player, challenge, "inventory")) {
                player.sendMessage(TextFormat.RED + "You dont have enough items");
                String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".description", "").replace("[label]", "is"));
                List<String> result = new ArrayList<>();
                if (desc.contains("|")) {
                    result.addAll(Arrays.asList(desc.split("\\|")));
                } else {
                    result.add(desc);
                }
                result.forEach((line) -> player.sendMessage(TextFormat.RED + line));
                return false;
            }
            return true;
        }
        // Utils.send("DEBUG: 5");
        // Check if this is an island-based challenge
        if (getChallengeConfig().getString("challenges.challengeList." + challenge + ".type").equalsIgnoreCase("island")) {
            // Utils.send("DEBUG: 6");
            if (!plugin.getGrid().playerIsOnIsland(player)) {
                player.sendMessage(TextFormat.RED + "You are not in island!");
                return false;
            }
            if (hasRequired(player, challenge, "island")) {
                int searchRadius = getChallengeConfig().getInt("challenges.challengeList." + challenge + ".searchRadius", 10);
                if (searchRadius < 10) {
                    searchRadius = 10;
                } else if (searchRadius > 50) {
                    searchRadius = 50;
                }
                player.sendMessage(TextFormat.RED + "You must be standing within [number] blocks of all required items.".replace("[number]", String.valueOf(searchRadius)));
                String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".description").replace("[label]", "is"));
                List<String> result = new ArrayList<>();
                if (desc.contains("|")) {
                    result.addAll(Arrays.asList(desc.split("\\|")));
                } else {
                    result.add(desc);
                }
                result.forEach((line) -> player.sendMessage(TextFormat.RED + line));
                return false;
            }
            // Utils.send("DEBUG: 7");
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
        Utils.send(TextFormat.RED
                + "The challenge " + challenge + " is of an unknown type " + getChallengeConfig().getString("challenges.challengeList." + challenge + ".type"));
        Utils.send(TextFormat.RED + "Types should be 'island', 'inventory' or 'level'");
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
    private boolean hasRequired(final Player player, final String challenge, final String type) {
        // Check money
        double moneyReq = 0D;
        if (Settings.useEconomy) {
            moneyReq = getChallengeConfig().getDouble("challenges.challengeList." + challenge + ".requiredMoney", 0D);
            if (moneyReq > 0D) {
                if (!ASkyBlock.econ.reduceMoney(player, moneyReq)) {
                    player.sendMessage(TextFormat.RED + "You dont have enough money!");
                    String desc = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".description").replace("[label]", "is"));
                    List<String> result = new ArrayList<>();
                    if (desc.contains("|")) {
                        result.addAll(Arrays.asList(desc.split("\\|")));
                    } else {
                        result.add(desc);
                    }
                    result.forEach((line) -> player.sendMessage(TextFormat.RED + line));
                    return true;
                }
            }
        }
        final String[] reqList = getChallengeConfig().getString("challenges.challengeList." + challenge + ".requiredItems").split(" ");
        // The format of the requiredItems is as follows:
        // Item:Qty
        // or
        // Item:damage:Qty
        // This second one is so that items such as potions or variations on
        // standard items can be collected
        if (type.equalsIgnoreCase("inventory")) {
            List<Item> toBeRemoved = new ArrayList<>();
            Item reqItem;
            int reqAmount;
            for (final String s : reqList) {
                final String[] part = s.split(":");
                // Item:Qty
                if (part.length == 2) {
                    try {
                        // Correct some common mistakes
                        if (part[0].equalsIgnoreCase("potato")) {
                            part[0] = "POTATO_ITEM";
                        } else if (part[0].equalsIgnoreCase("brewing_stand")) {
                            part[0] = "BREWING_STAND_ITEM";
                        } else if (part[0].equalsIgnoreCase("carrot")) {
                            part[0] = "CARROT_ITEM";
                        } else if (part[0].equalsIgnoreCase("cauldron")) {
                            part[0] = "CAULDRON_ITEM";
                        } else if (part[0].equalsIgnoreCase("skull")) {
                            part[0] = "SKULL_ITEM";
                        }
                        // TODO: add netherwart vs. netherstalk?
                        if (Utils.isNumeric(part[0])) {
                            reqItem = Item.get(Integer.parseInt(part[0]));
                        } else {
                            reqItem = Item.fromString(part[0].toUpperCase());
                        }
                        reqAmount = Integer.parseInt(part[1]);
                        Item item = reqItem;
                        // Utils.send(TextFormat.GREEN +"DEBUG: required item = " +
                        // reqItem.toString());
                        // Utils.send(TextFormat.GREEN +"DEBUG: item amount = " +
                        // reqAmount);

                        if (!player.getInventory().contains(reqItem)) {
                            return true;
                        } else {
                            // check amount
                            int amount = 0;
                            // Utils.send(TextFormat.GREEN +"DEBUG: Amount in inventory = "
                            // + player.getInventory().all(reqItem).size());
                            // Go through all the inventory and try to find
                            // enough required items
                            for (Map.Entry<Integer, Item> en : player.getInventory().all(reqItem).entrySet()) {
                                // Get the item
                                Item i = en.getValue();
                                // If the item is enchanted, skip - it doesn't count
                                // Map needs special handling because the
                                // durability increments every time a new one is
                                // made by the player
                                // TODO: if there are any other items that act
                                // in the same way, they need adding too...
                                if (!i.hasEnchantments() || (reqItem.getId() == Item.MAP && i.getId() == Item.MAP)) {
                                    // Clear any naming, or lore etc.
                                    //i.setItemMeta(null);
                                    //player.getInventory().setItem(en.getKey(), i);
                                    // #1 item stack qty + amount is less than
                                    // required items - take all i
                                    // #2 item stack qty + amount = required
                                    // item -
                                    // take all
                                    // #3 item stack qty + amount > req items -
                                    // take
                                    // portion of i
                                    // amount += i.getCount();
                                    if ((amount + i.getCount()) < reqAmount) {
                                        // Remove all of this item stack - clone
                                        // otherwise it will keep a reference to
                                        // the
                                        // original
                                        toBeRemoved.add(i.clone());
                                        amount += i.getCount();
                                        // Utils.send(TextFormat.GREEN +"DEBUG: amount is <= req Remove "
                                        // + i.toString() + ":" +
                                        // i.getDurability() + " x " +
                                        // i.getCount());
                                    } else if ((amount + i.getCount()) == reqAmount) {
                                        // Utils.send(TextFormat.GREEN +"DEBUG: amount is = req Remove "
                                        // + i.toString() + ":" +
                                        // i.getDurability() + " x " +
                                        // i.getCount());
                                        toBeRemoved.add(i.clone());
                                        amount += i.getCount();
                                        break;
                                    } else {
                                        // Remove a portion of this item
                                        // Utils.send(TextFormat.GREEN +"DEBUG: amount is > req Remove "
                                        // + i.toString() + ":" +
                                        // i.getDurability() + " x " +
                                        // i.getCount());

                                        item.setCount(reqAmount - amount);
                                        toBeRemoved.add(item);
                                        amount += i.getCount();
                                        break;
                                    }
                                }
                            }
                            // Utils.send(TextFormat.GREEN +"DEBUG: amount "+
                            // amount);
                            if (amount < reqAmount) {
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        Utils.send(TextFormat.RED + "Problem with " + s + " in challenges.yml!");
                        player.sendMessage(TextFormat.RED + "Error: challange error contact server admin");
                        StringBuilder materialList = new StringBuilder();
                        boolean hint = false;
                        for (Item m : Item.getCreativeItems()) {
                            materialList.append(m.toString()).append(",");
                            if (m.toString().contains(s.substring(0, 3).toUpperCase())) {
                                Utils.send(TextFormat.RED + "Did you mean " + m.toString() + "?");
                                hint = true;
                            }
                        }
                        if (!hint) {
                            Utils.send(TextFormat.RED + "Sorry, I have no idea what " + s + " is. Pick from one of these:");
                            Utils.send(TextFormat.RED + materialList.substring(0, materialList.length() - 1));
                        } else {
                            Utils.send(TextFormat.RED + "Correct challenges.yml with the correct material.");
                        }
                        return true;
                    }
                } else if (part.length == 3) {
                    // This handles items with durability
                    // Correct some common mistakes
                    if (part[0].equalsIgnoreCase("potato")) {
                        part[0] = "POTATO_ITEM";
                    } else if (part[0].equalsIgnoreCase("brewing_stand")) {
                        part[0] = "BREWING_STAND_ITEM";
                    } else if (part[0].equalsIgnoreCase("carrot")) {
                        part[0] = "CARROT_ITEM";
                    } else if (part[0].equalsIgnoreCase("cauldron")) {
                        part[0] = "CAULDRON_ITEM";
                    } else if (part[0].equalsIgnoreCase("skull")) {
                        part[0] = "SKULL_ITEM";
                    }
                    if (Utils.isNumeric(part[0])) {
                        reqItem = Item.get(Integer.parseInt(part[0]));
                    } else {
                        reqItem = Item.fromString(part[0].toUpperCase());
                    }
                    reqAmount = Integer.parseInt(part[2]);
                    int reqDurability = Integer.parseInt(part[1]);
                    Item item = reqItem;

                    // Item
                    item.setDamage(reqDurability);
                    // check amount
                    int amount = 0;
                    // Go through all the inventory and try to find
                    // enough required items
                    for (Map.Entry<Integer, ? extends Item> en : player.getInventory().all(reqItem).entrySet()) {
                        // Get the item
                        Item i = en.getValue();
                        if (i.getDamage() == reqDurability) {
                            // Clear any naming, or lore etc.
                            //i.setItemMeta(null);
                            // player.getInventory().setItem(en.getKey(), i);
                            // #1 item stack qty + amount is less than
                            // required items - take all i
                            // #2 item stack qty + amount = required
                            // item -
                            // take all
                            // #3 item stack qty + amount > req items -
                            // take
                            // portion of i
                            // amount += i.getCount();
                            if ((amount + i.getCount()) < reqAmount) {
                                // Remove all of this item stack - clone
                                // otherwise it will keep a reference to
                                // the
                                // original
                                toBeRemoved.add(i.clone());
                                amount += i.getCount();
                                // Utils.send(TextFormat.GREEN +"DEBUG: amount is <= req Remove "
                                // + i.toString() + ":" +
                                // i.getDurability()
                                // + " x " + i.getCount());
                            } else if ((amount + i.getCount()) == reqAmount) {
                                toBeRemoved.add(i.clone());
                                amount += i.getCount();
                                break;
                            } else {
                                // Remove a portion of this item
                                // Utils.send(TextFormat.GREEN +"DEBUG: amount is > req Remove "
                                // + i.toString() + ":" +
                                // i.getDurability()
                                // + " x " + i.getCount());

                                item.setCount(reqAmount - amount);
                                item.setDamage(i.getDamage());
                                toBeRemoved.add(item);
                                amount += i.getCount();
                                break;
                            }
                        }
                    }
                    // Utils.send(TextFormat.GREEN +"DEBUG: amount is " +
                    // amount);
                    // Utils.send(TextFormat.GREEN +"DEBUG: req amount is " +
                    // reqAmount);
                    if (amount < reqAmount) {
                        return true;
                    }

                    // Utils.send(TextFormat.GREEN +"DEBUG: before set amount " +
                    // item.toString() + ":" + item.getDurability() + " x "
                    // + item.getCount());
                    // item.setAmount(reqAmount);
                    // Utils.send(TextFormat.GREEN +"DEBUG: after set amount " +
                    // item.toString() + ":" + item.getDurability() + " x "
                    // + item.getCount());
                    // toBeRemoved.add(item);
                } else if (part.length == 6 && part[0].contains("POTION")) {
                    // BUKKIT v1.0
                    // Run through player's inventory for the item
                    Map<Integer, Item> playerInv = player.getInventory().getContents();
                    try {
                        reqAmount = Integer.parseInt(part[5]);
                        //Utils.send(TextFormat.GREEN +"DEBUG: required amount is " + reqAmount);
                    } catch (Exception e) {
                        Utils.send(TextFormat.RED + "Could not parse the quantity of the potion item " + s);
                        return true;
                    }
                    int count = reqAmount;
                    for (Item i : playerInv.values()) {
                        // Catches all POTION, LINGERING_POTION and SPLASH_POTION
                        if (i instanceof ItemPotion || i instanceof ItemPotionSplash) {
                            //Utils.send(TextFormat.GREEN +"DEBUG:6 part potion check!");
                            // POTION:NAME:<LEVEL>:<EXTENDED>:<SPLASH/LINGER>:QTY
                            // Test potion
                            Potion potionType = Potion.getPotion(i.getDamage());
                            boolean match = true;
                            // plugin.getLogger().info("DEBUG: name check " + part[1]);
                            // Name check
                            if (!part[1].isEmpty()) {
                                // There is a name
                                if (Potion.getPotionByName(part[1]) != null) {
                                    if (!potionType.getEffect().getName().equalsIgnoreCase(part[1])) {
                                        match = false;
                                        // plugin.getLogger().info("DEBUG: name does not match");
                                    } else {
                                        // plugin.getLogger().info("DEBUG: name matches");
                                    }
                                } else {
                                    Utils.send(TextFormat.RED + "Potion type is unknown");
                                    match = false;
                                }
                            }
                            // Level check (upgraded)
                            // plugin.getLogger().info("DEBUG: level check " + part[2]);
                            if (!part[2].isEmpty()) {
                                // There is a level declared - check it
                                if (Utils.isNumeric(part[2])) {
                                    int level = Integer.valueOf(part[2]);
                                    if (level != potionType.getLevel()) {
                                        // plugin.getLogger().info("DEBUG: level does not match");
                                        match = false;
                                    }
                                }
                            }
                            // Extended check
                            // plugin.getLogger().info("DEBUG: extended check " + part[3]);
                            if (!part[3].isEmpty()) {
                                // Post a pull request for new Potion API later
//                                if (part[3].equalsIgnoreCase("EXTENDED") && !potionType.hasExtendedDuration()) {
//                                    match = false;
//                                    // plugin.getLogger().info("DEBUG: extended does not match");
//                                }
//                                if (part[3].equalsIgnoreCase("NOTEXTENDED") && potionType.hasExtendedDuration()) {
//                                    match = false;
//                                    // plugin.getLogger().info("DEBUG: extended does not match");
//                                }
                            }
                            // Splash or Linger check
                            // plugin.getLogger().info("DEBUG: splash/linger check " + part[4]);
                            if (!part[4].isEmpty()) {
                                if (part[4].equalsIgnoreCase("SPLASH") && i.getId() != Item.SPLASH_POTION) {
                                    match = false;
                                    // plugin.getLogger().info("DEBUG: not splash");
                                }
                                if (part[4].equalsIgnoreCase("NOSPLASH") && i.getId() == Item.SPLASH_POTION) {
                                    match = false;
                                    // plugin.getLogger().info("DEBUG: not no splash");
                                }
                                if (part[4].equalsIgnoreCase("LINGER") && i.getId() != Item.LINGERING_POTION) {
                                    match = false;
                                    // plugin.getLogger().info("DEBUG: not linger");
                                }
                                if (part[4].equalsIgnoreCase("NOLINGER") && i.getId() == Item.LINGERING_POTION) {
                                    match = false;
                                    // plugin.getLogger().info("DEBUG: not no linger");
                                }
                            }
                            // Quantity check
                            if (match) {
                                // plugin.getLogger().info("DEBUG: potion matches!");
                                Item removeItem = i.clone();
                                if (removeItem.getCount() > reqAmount) {
                                    // plugin.getLogger().info("DEBUG: found " + removeItem.getAmount() + " qty in inv");
                                    removeItem.setCount(reqAmount);
                                }
                                count = count - removeItem.getCount();
                                // plugin.getLogger().info("DEBUG: " + count + " left");
                                toBeRemoved.add(removeItem);
                            }
                        }
                        if (count <= 0) {
                            // Utils.send(TextFormat.GREEN +"DEBUG: Player has enough");
                            break;
                        }
                        // Utils.send(TextFormat.GREEN +"DEBUG: still need " + count + " to complete");
                    }
                    if (count > 0) {
                        // Utils.send(TextFormat.GREEN +"DEBUG: Player does not have enough");
                        return true;
                    }

                }
            }
            // Build up the items in the inventory and remove them if they are
            // all there.
            if (getChallengeConfig().getBoolean("challenges.challengeList." + challenge + ".takeItems")) {
                for (Item i : toBeRemoved) {
                    Item[] leftOver = player.getInventory().removeItem(i);
                    if (leftOver.length != 0) {
                        Utils.send(TextFormat.RED
                                + "Exploit? Could not remove the following in challenge " + challenge + " for player " + player.getName() + ":");
                        for (Item left : leftOver) {
                            Utils.send(TextFormat.GREEN + left.toString());
                        }
                        return true;
                    }
                }
                // Remove money
                if (moneyReq > 0D) {
                    boolean er = ASkyBlock.econ.reduceMoney(player, moneyReq);
                    if (!er) {

                        Utils.send(TextFormat.RED
                                + "Exploit? Could not remove $" + moneyReq + " from " + player.getName()
                                + " in challenge " + challenge);
                    }
                }
            }
        }
        return false;
    }

    private boolean checkChallenge(Player player, String challenge) {
        PlayerData pd = plugin.getPlayerInfo(player);
        return pd.checkChallenge(challenge);
    }

    private int checkChallengeTimes(Player player, String challenge) {
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
    private void giveReward(final Player player, final String challenge) {
        // Grab the rewards from the config.yml file
        String[] permList;
        String[] itemRewards;
        double moneyReward;
        int expReward;
        String rewardText;
        // If the friendly name is available use it
        String challengeName = TextFormat.colorize('&', getChallengeConfig().getString("challenges.challengeList." + challenge + ".friendlyname",
                challenge.substring(0, 1).toUpperCase() + challenge.substring(1)));

        // Gather the rewards due
        // If player has done a challenge already, the rewards are different
        if (!checkChallenge(player, challenge)) {
            // First time
            player.sendMessage(TextFormat.GREEN + "You completed the challange: [challenge]".replace("[challenge]", challengeName));
            if (Settings.broadcastMessages) {
                plugin.getServer().getOnlinePlayers().values().forEach((p) -> p.sendMessage(
                        TextFormat.GOLD + "[name] just completed a challange: [challenge] !".replace("[name]", player.getDisplayName()).replace("[challenge]", challengeName)));
            }
            plugin.getMessages().tellOfflineTeam(player.getName(),
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
            if (ASkyBlock.econ.addMoney(player, moneyReward)) {
                player.sendMessage(TextFormat.GOLD + "You retrived" + TextFormat.WHITE + " $" + moneyReward);
            } else {
                Utils.send(TextFormat.RED + "Error giving player " + player + " challenge money:");//) e.errorMessage);
                Utils.send(TextFormat.RED + "Reward was $" + moneyReward);
            }
        }
        // Dole out permissions
        permList = getChallengeConfig().getString("challenges.challengeList." + challenge.toLowerCase() + ".permissionReward", "").split(" ");
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
            List<String> commands = getChallengeConfig().getStringList("challenges.challengeList." + challenge.toLowerCase() + ".rewardcommands");
            runCommands(player, commands);
        } else {
            // Repeat challenge
            List<String> commands = getChallengeConfig().getStringList("challenges.challengeList." + challenge.toLowerCase() + ".repeatrewardcommands");
            runCommands(player, commands);
        }

        // Mark the challenge as complete
        // if (!plugin.getPlayers().checkChallenge(player,challenge)) {
        plugin.getPlayerInfo(player).completeChallenge(challenge);
        // }
        // Call the Challenge Complete Event
        final ChallengeCompleteEvent event = new ChallengeCompleteEvent(player, challenge, permList, itemRewards, moneyReward, expReward, rewardText, rewardedItems);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    private List<Item> giveItems(Player player, String[] itemRewards) {
        List<Item> rewardedItems = new ArrayList<>();
        Item rewardItem;
        int rewardQty;
        // Build the item stack of rewards to give the player
        for (final String s : itemRewards) {
            final String[] element = s.split(":");
            if (element.length == 2) {
                try {
                    if (Utils.isNumeric(element[0])) {
                        rewardItem = Item.get(Integer.parseInt(element[0]));
                    } else {
                        rewardItem = Item.fromString(element[0].toUpperCase());
                    }
                    rewardQty = Integer.parseInt(element[1]);
                    Item item = new Item(rewardItem.getId(), rewardQty);
                    rewardedItems.add(item);
                    Item[] leftOvers = player.getInventory().addItem(item);
                    if (leftOvers.length != 0) {
                        player.getLevel().dropItem(player.getLocation(), leftOvers[0]);
                    }
                } catch (Exception e) {
                    player.sendMessage(TextFormat.RED + "There a problem while executing your command");
                    Utils.send(TextFormat.RED + "Could not give " + element[0] + ":" + element[1] + " to " + player.getName() + " for challenge reward!");
                    StringBuilder materialList = new StringBuilder();
                    boolean hint = false;
                    for (Item m : Item.getCreativeItems()) {
                        materialList.append(m.toString()).append(",");
                        if (element[0].length() > 3) {
                            if (m.toString().startsWith(element[0].substring(0, 3))) {
                                Utils.send(TextFormat.RED + "Did you mean " + m.toString() + "? If so, put that in challenges.yml.");
                                hint = true;
                            }
                        }
                    }
                    if (!hint) {
                        Utils.send(TextFormat.RED + "Sorry, I have no idea what " + element[0] + " is. Pick from one of these:");
                        Utils.send(TextFormat.RED + materialList.substring(0, materialList.length() - 1));
                    }
                }
            } else if (element.length == 3) {
                try {
                    if (Utils.isNumeric(element[0])) {
                        rewardItem = Item.get(Integer.parseInt(element[0]));
                    } else {
                        rewardItem = Item.fromString(element[0].toUpperCase());
                    }
                    rewardQty = Integer.parseInt(element[2]);
                    // Check for POTION
                    if (rewardItem.equals(Item.POTION)) {
                        givePotion(player, rewardedItems, element, rewardQty);
                    } else {
                        Item item = null;
                        // Normal item, not a potion, check if it is a Monster Egg
                        if (rewardItem.equals(Item.MONSTER_EGG)) {

                        }
                    }
                } catch (Exception e) {
                    player.sendMessage(TextFormat.RED + "There was a problem giving your reward. Ask Admin to check log!");
                    Utils.send(TextFormat.RED + "Could not give " + element[0] + ":" + element[1] + " to " + player.getName() + " for challenge reward!");
                    /*
                    if (element[0].equalsIgnoreCase("POTION")) {
                        String potionList = "";
                        boolean hint = false;
                        for (PotionEffectType m : PotionEffectType.values()) {
                            potionList += m.toString() + ",";
                            if (element[1].length() > 3) {
                                if (m.toString().startsWith(element[1].substring(0, 3))) {
                                    Utils.send(TextFormat.RED + "Did you mean " + m.toString() + "?");
                                    hint = true;
                                }
                            }
                        }
                        if (!hint) {
                            Utils.send(TextFormat.RED + "Sorry, I have no idea what potion type " + element[1] + " is. Pick from one of these:");
                            Utils.send(TextFormat.RED + potionList.substring(0, potionList.length() - 1));
                        }

                    } else {*/
                    StringBuilder materialList = new StringBuilder();
                    boolean hint = false;
                    for (Item m : Item.getCreativeItems()) {
                        materialList.append(m.toString()).append(",");
                        if (m.toString().startsWith(element[0].substring(0, 3))) {
                            Utils.send(TextFormat.RED + "Did you mean " + m.toString() + "? If so, put that in challenges.yml.");
                            hint = true;
                        }
                    }
                    if (!hint) {
                        Utils.send(TextFormat.RED + "Sorry, I have no idea what " + element[0] + " is. Pick from one of these:");
                        Utils.send(TextFormat.RED + materialList.substring(0, materialList.length() - 1));
                    }
                    //}
                    return null;
                }
            }
        }
        //todo
        return rewardedItems;
    }

    private void runCommands(Player player, List<String> commands) {
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
                    Utils.send(TextFormat.RED + "Problem executing challenge reward commands - skipping!");
                    Utils.send(TextFormat.RED + "Command was : " + cmd);
                }
            } catch (Exception e) {
                Utils.send(TextFormat.RED + "Problem executing challenge reward commands - skipping!");
                Utils.send(TextFormat.RED + "Command was : " + cmd);
                Utils.send(TextFormat.RED + "Error was: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void completeChallenge(Player uniqueId, String level) {
        PlayerData pd = plugin.getPlayerInfo(uniqueId);
        pd.completeChallenge(level);
    }

    private void givePotion(Player player, List<Item> rewardedItems, String[] element, int rewardQty) {
        Item item = getPotion(element, rewardQty);
        rewardedItems.add(item);
        Item[] leftOvers = player.getInventory().addItem(item);
        if (leftOvers.length != 0) {
            player.getLevel().dropItem(player.getLocation(), leftOvers[0]);
        }
    }

    /**
     * Converts a serialized potion to a ItemStack of that potion
     *
     * @param element
     * @param rewardQty
     * @return ItemStack of the potion
     */
    private Item getPotion(String[] element, int rewardQty) {
        // Check for potion aspects
        boolean splash = false;
        boolean extended = false;
        boolean linger = false;
        int level = 1;
        if (element.length > 2) {
            // Add level etc.
            if (!element[2].isEmpty()) {
                try {
                    level = Integer.valueOf(element[2]);
                } catch (Exception e) {
                    level = 1;
                }
            }
        }
        if (element.length > 3) {
            //plugin.getLogger().info("DEBUG: level = " + Integer.valueOf(element[2]));
            if (element[3].equalsIgnoreCase("EXTENDED")) {
                //plugin.getLogger().info("DEBUG: Extended");
                extended = true;
            }
        }
        if (element.length > 4) {
            if (element[4].equalsIgnoreCase("SPLASH")) {
                //plugin.getLogger().info("DEBUG: splash");
                splash = true;
            }
            if (element[4].equalsIgnoreCase("LINGER")) {
                //plugin.getLogger().info("DEBUG: linger");
                linger = true;
            }
        }
        // Add the effect of the potion
        Item result = new Item(Item.POTION, rewardQty);
        if (splash) {
            result = new Item(Item.SPLASH_POTION, rewardQty);
        }
        if (linger) {
            result = new Item(Item.LINGERING_POTION, rewardQty);
        }

        return result;
    }
}
