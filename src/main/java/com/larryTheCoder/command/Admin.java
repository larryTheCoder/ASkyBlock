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

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.SkyBlockGenerator;
import com.larryTheCoder.listener.LavaCheck;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.task.DeleteIslandTask;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.text.Collator;
import java.util.*;

/**
 * The main administrator command allows operator
 * to change player/island/challenges behaviour.
 *
 * @author Adam Matthew
 */
public class Admin extends Command {

    private final ASkyBlock plugin;

    public Admin(ASkyBlock ev) {
        super("isadmin", "Island admin command", "\u00a77<parameters>", new String[]{"isa"});
        this.plugin = ev;
        // Set this command permission (This should not be known to players)
        this.setPermission("is.admin.command");
        this.setPermissionMessage(TextFormat.RED + "Unknown command. Try /help for a list of commands");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;

        if (args.length == 0) {
            this.sendHelp(sender, commandLabel, args);
            return true;
        }

        // Todo: Remove this switches
        switch (args[0]) {
            case "help":
                this.sendHelp(sender, commandLabel, args);
                break;
            case "generate":
                if (!sender.hasPermission("is.admin.generate")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " generate <level>");
                    break;
                }

                if (plugin.loadedLevel.contains(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorLevelGenerated);
                    break;
                } else if (!plugin.getServer().isLevelGenerated(args[1])) {
                    plugin.getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
                    plugin.getServer().loadLevel(args[1]);
                    WorldSettings world = new WorldSettings(plugin.getServer().getLevelByName(args[1]));
                    Config cfg = new Config(new File(plugin.getDataFolder(), "worlds.yml"), Config.YAML);
                    cfg.set(args[1] + ".permission", world.getPermission());
                    cfg.set(args[1] + ".maxHome", world.getMaximumIsland());
                    cfg.set(args[1] + ".protectionRange", world.getProtectionRange());
                    cfg.set(args[1] + ".stopTime", world.isStopTime());
                    cfg.set(args[1] + ".seaLevel", world.getSeaLevel());
                    cfg.save();
                    plugin.saveLevel(false);
                    plugin.level.add(world);
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).generalSuccess);
                    break;
                }
                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorLevelGenerated);
                break;
            case "clear":
                if (!sender.hasPermission("is.admin.clear")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }
                plugin.getInventory().clearSavedInventory();
                plugin.getDatabase().free();
                sender.sendMessage(TextFormat.RED + "Cleared memory usage.");
                sender.sendMessage(TextFormat.RED + "Warning: Player data may be lost during this cleanup");
                break;
            case "kick":
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " kick <player>");
                    break;
                }

                if (!sender.hasPermission("is.admin.kick")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }
                plugin.getIsland().kickPlayerByAdmin(sender, args[1]);
                break;
            case "rename":
                if (!sender.hasPermission("is.admin.rename")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                if (args.length <= 2) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " rename <player> <name>");
                    break;
                }

                IslandData pd = plugin.getIslandInfo(args[1]);
                if (pd == null) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorNoIslandOther);
                    break;
                }
                pd.setName(args[2]);
                boolean success = plugin.getDatabase().saveIsland(pd);
                if (success) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).renameSuccess);
                } else {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorFailedNormal);
                }
                break;
            case "cobblestats":
                if (!sender.hasPermission("is.admin.cobblestats")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }
                if (LavaCheck.getStats().size() == 0) {
                    sender.sendMessage(TextFormat.OBFUSCATED + "");
                    break;
                }
                // Display by level
                for (Integer level : LavaCheck.getStats().keySet()) {
                    if (level == Integer.MIN_VALUE) {
                        sender.sendMessage(plugin.getLocale(p).challengesLevel + ": Default");
                    } else {
                        sender.sendMessage(plugin.getLocale(p).challengesLevel + ": " + level);
                    }
                    // Collect and sort
                    Collection<String> result = new TreeSet<>(Collator.getInstance());
                    for (Block mat : LavaCheck.getStats().get(level).elementSet()) {
                        result.add("   " + Utils.prettifyText(mat.toString()) + ": " + LavaCheck.getStats().get(level).count(mat) + "/" + LavaCheck.getStats().get(level).size() + " or "
                                + ((int) ((double) LavaCheck.getStats().get(level).count(mat) / LavaCheck.getStats().get(level).size() * 100))
                                + "% (config = " + String.valueOf(LavaCheck.getConfigChances(level, mat)) + "%)");
                    }
                    // Send to player
                    for (String r : result) {
                        sender.sendMessage(r);
                    }
                }
                break;
			case "cc":
            case "completechallenge":
                if (!sender.hasPermission("is.admin.completechallenge")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " completechallenge <player>");
                    break;
                }
                IPlayer offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                PlayerData pld = plugin.getDatabase().getPlayerData(offlinePlayer.getName());
                if (pld == null) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(p).errorUnknownPlayer);
                    break;
                }
                if (pld.checkChallenge(args[2].toLowerCase()) || pld.challengeNotExists(args[2].toLowerCase())) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(p).errorChallengeDoesNotExist);
                    break;
                }
                pld.completeChallenge(args[2].toLowerCase());
                pld.saveData();
                ASkyBlock.get().getDatabase().savePlayerData(pld);
                sender.sendMessage(TextFormat.YELLOW + plugin.getLocale(p).completeChallengeCompleted
                        .replace("[challengename]", args[2].toLowerCase())
                        .replace("[name]", args[1]));
                break;
			case "rc":
            case "resetchallenge":
                if (!sender.hasPermission("is.admin.resetchallenge")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " resetchallenge <player>");
                    break;
                }

                offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                pld = plugin.getDatabase().getPlayerData(offlinePlayer.getName());
                if (pld == null) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(p).errorUnknownPlayer);
                    break;
                }
                if (!pld.checkChallenge(args[2].toLowerCase()) || pld.challengeNotExists(args[2].toLowerCase())) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(p).errorChallengeDoesNotExist);
                    break;
                }
                pld.resetChallenge(args[2].toLowerCase());
                pld.saveData();
                sender.sendMessage(TextFormat.YELLOW + plugin.getLocale(p).resetChallengeReset
                        .replace("[challengename]", args[2].toLowerCase())
                        .replace("[name]", args[1]));
                break;
            case "delete":
                if (!sender.hasPermission("is.admin.delete")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                IslandData island;
                if (p == null) {
                    // Well its console, so they could perform it by deleting it with command isn't?
                    if (args.length <= 2) {
                        sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " delete <player> <id>");
                        break;
                    }
                    int id = Integer.getInteger(args[2]);

                    // Show them, lets see if this dude got a data in database
                    offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                    island = plugin.getDatabase().getIsland(offlinePlayer.getName(), id);
                    if (island == null) {
                        sender.sendMessage(plugin.getLocale(null).errorUnknownPlayer);
                        break;
                    }

                    sender.sendMessage(plugin.getLocale(null).deleteRemoving.replace("[name]", "null"));
                    deleteIslands(island, sender);
                    break;
                }

                // Get the island I am on
                island = plugin.getIsland().GetIslandAt(p);

                if (island == null) {
                    sender.sendMessage(plugin.getLocale(p).adminDeleteIslandnoid);
                    break;
                }

                // Try to get the owner of this island
                String owner = island.getOwner();
                if (!args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).adminDeleteIslandError.replace("[player]", owner));
                    break;
                }

                if (owner != null) {
                    sender.sendMessage(plugin.getLocale(p).adminSetSpawnOwnedBy.replace("[name]", owner));
                    sender.sendMessage(plugin.getLocale(p).adminDeleteIslandUse.replace("[name]", owner));
                    break;
                } else {
                    sender.sendMessage(plugin.getLocale(p).deleteRemoving.replace("[name]", "null"));
                    deleteIslands(island, sender);
                }
                break;
            case "setspawn":
                if (!sender.hasPermission("is.admin.setspawn")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                setSpawn(sender);
                break;
            case "addmessage":
                if (!sender.hasPermission("is.admin.addmessage")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                StringBuilder msg = new StringBuilder();

                for (String arg : args) {
                    msg.append(arg).append(" ");
                }

                if (msg.length() > 0) {
                    msg = new StringBuilder(msg.substring(0, msg.length() - 1));
                }

                List<String> players = plugin.getDatabase().getPlayersData();

                for (String pl : players) {
                    List<String> list = plugin.getMessages().getMessages(pl);
                    list.add(msg.toString());
                    plugin.getMessages().put(pl, list);
                }
            case "info":
            case "challenges":
                if (!sender.hasPermission("is.admin.challenges")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " info <player>");
                    break;
                }

                // Show them, lets see if this dude got a data in database
                showInfoChallenges(Server.getInstance().getOfflinePlayer(args[1]), sender);
                break;
            default:
                this.sendHelp(sender, commandLabel, args);
                break;
        }

        return true;
    }

    private void setSpawn(CommandSender sender) {
        Player p = sender.isPlayer() ? plugin.getServer().getPlayer(sender.getName()) : null;
        if (p == null) {
            sender.sendMessage(plugin.getLocale(null).errorUseInGame);
            return;
        }
        if (plugin.inIslandWorld(p) && plugin.getIslandInfo(p) != null) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).adminSetSpawnOverride);
        }
        // To avoid multiple spawns, try to remove the old spawn
        if (plugin.getDatabase().getSpawn() != null) {
            IslandData pd = plugin.getDatabase().getSpawn();
            pd.setSpawn(false);
            plugin.getDatabase().saveIsland(pd);
        }
        // Save this island
        IslandData pd = plugin.getIslandInfo(p.getLocation());
        pd.setSpawn(true);
        plugin.getDatabase().saveIsland(pd);
        sender.sendMessage(TextFormat.GREEN + plugin.getLocale(p).generalSuccess);
    }

    /**
     * Shows info on the challenge situation for player,
     *
     * @param player The Offline player to be checked on
     * @param sender Sender who performed the command
     */
    private void showInfoChallenges(IPlayer player, CommandSender sender) {
        PlayerData pd = plugin.getDatabase().getPlayerData(player.getName());
        // No way
        if (pd == null) {
            sender.sendMessage(TextFormat.RED + plugin.getLocale(null).errorUnknownPlayer);
            return;
        }
        sender.sendMessage("Name:" + TextFormat.GREEN + player.getName());
        sender.sendMessage("UUID: " + TextFormat.YELLOW + player.getUniqueId());

        // Completed challenges
        sender.sendMessage(TextFormat.WHITE + "Challenges:");
        Map<String, Boolean> challenges = pd.getChallengeStatus();
        Map<String, Integer> challengeTimes = pd.getChallengeTimes();
        if (challenges.isEmpty()) {
            sender.sendMessage(TextFormat.RED + "Empty in here...");
            return;
        }
        for (String c : challenges.keySet()) {
            if (challengeTimes.containsKey(c)) {
                sender.sendMessage(c + ": " + (challenges.get(c) ?
                        TextFormat.GREEN + "Complete" :
                        TextFormat.AQUA + "Incomplete")
                        + "(" + pd.checkChallengeTimes(c) + ")");

            } else {
                sender.sendMessage(c + ": " + (challenges.get(c) ?
                        TextFormat.GREEN + "Complete" :
                        TextFormat.AQUA + "Incomplete"));
            }
        }
    }

    /**
     * Deletes the overworld and nether islands together
     *
     * @param island The player's island
     * @param sender The sender (player)
     */
    private void deleteIslands(IslandData island, CommandSender sender) {
        // Nukkit has a slow progress on Nether gameplay
        TaskManager.runTask(new DeleteIslandTask(plugin, island, sender));
    }

    private void sendHelp(CommandSender sender, String label, String[] args) {
        Player p = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;
        int pageNumber = 1;

        if (args.length == 2 && Utils.isNumeric(args[1])) {
            pageNumber = Integer.parseInt(args[1]);
        }
        int pageHeight;
        if (sender instanceof ConsoleCommandSender) {
            pageHeight = Integer.MAX_VALUE;
        } else {
            pageHeight = 5;
        }

        sender.sendMessage(
                "§eNotes for admins, all the command about the island related usages could be used on the island " +
                        "instead of typing the player, this only applies to the player, not to console"
            );

        List<String> helpList = new ArrayList<>();

        helpList.add(""); // Nope its not just math

        helpList.add("&7" + label + " rename &l&5»&r&f &a" + plugin.getLocale(p).adminHelpRename);
        helpList.add("&7" + label + " kick &l&5»&r&f &a" + plugin.getLocale(p).adminHelpKick);
        helpList.add("&7" + label + " generate &l&5»&r&f &a" + plugin.getLocale(p).adminHelpGenerate);
        helpList.add("&7" + label + " setspawn &l&5»&r&f &a" + plugin.getLocale(p).adminHelpSpawn);
        helpList.add("&7" + label + " delete &l&5»&r&f &a" + plugin.getLocale(p).adminHelpDelete);
        helpList.add("&7" + label + " addmessage &l&5»&r&f &a" + plugin.getLocale(p).adminHelpMessage);
        helpList.add("&7" + label + " clear &l&5»&r&f &a" + plugin.getLocale(p).adminHelpClear);
        helpList.add("&7" + label + " cobblestats &l&5»&r&f &a" + plugin.getLocale(p).adminHelpDelete);
        helpList.add("&7" + label + " completechallenge &l&5»&r&f &a" + plugin.getLocale(p).adminHelpMessage);
        helpList.add("&7" + label + " resetchallenge &l&5»&r&f &a" + plugin.getLocale(p).adminHelpClear);
        helpList.add("&7" + label + " challenges &l&5»&r&f &a" + plugin.getLocale(p).adminHelpInfo);

        if (label.length() > 4) {
            helpList.add("");
            helpList.add("&eYou can use 'isa' for 'isadmin' aliases");
        }

        int totalPage = helpList.size() % pageHeight == 0 ? helpList.size() / pageHeight : helpList.size() / pageHeight + 1;
        pageNumber = Math.min(pageNumber, totalPage);
        if (pageNumber < 1) {
            pageNumber = 1;
        }

        sender.sendMessage("§e--- §eAdmin SkyBlock Help Page §a" + pageNumber + " §eof §a" + totalPage + " §e---");

        int i = 0;
        for (String list : helpList) {
            if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(helpList.size(), pageNumber * pageHeight)) {
                sender.sendMessage(list.replace("&", "§"));
            }
            i++;
        }
    }

}
