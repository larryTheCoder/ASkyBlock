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
 * @author larryTheCoder
 * @author tastybento
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
        Player pl = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;

        if (args.length == 0) {
            this.sendHelp(sender, commandLabel, args);
            return true;
        }

        // Todo: Remove this switches
        switch (args[0]) {
            case "generate":
                if (!sender.hasPermission("is.admin.generate")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }

                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " generate <level>");
                    break;
                }

                if (plugin.loadedLevel.contains(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(pl).errorLevelGenerated);
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
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(pl).generalSuccess);
                    break;
                }
                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(pl).errorLevelGenerated);
                break;
            case "clear": // TODO: Is it reasonable to use this command anymore?
                if (!sender.hasPermission("is.admin.clear")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }
                plugin.getInventory().clearSavedInventory();
                sender.sendMessage(TextFormat.RED + "Cleared memory usage.");
                sender.sendMessage(TextFormat.RED + "Warning: Player data may be lost during this cleanup");
                break;
            case "kick":
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " kick <player>");
                    break;
                }

                if (!sender.hasPermission("is.admin.kick")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }
                plugin.getIslandManager().kickPlayerByAdmin(sender, args[1]);
                break;
            case "rename":
                if (!sender.hasPermission("is.admin.rename")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }

                if (args.length <= 2) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " rename <player> <name>");
                    break;
                }

                IslandData pd = plugin.getIslandInfo(args[1]);
                if (pd == null) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(pl).errorNoIslandOther);
                    break;
                }
                pd.setIslandName(args[2]);
                pd.saveIslandData();

                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(pl).renameSuccess);
                break;
            case "cobblestats":
                if (!sender.hasPermission("is.admin.cobblestats")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }
                if (LavaCheck.getStats().size() == 0) {
                    sender.sendMessage(TextFormat.OBFUSCATED + "");
                    break;
                }
                // Display by level
                for (Integer level : LavaCheck.getStats().keySet()) {
                    if (level == Integer.MIN_VALUE) {
                        sender.sendMessage(plugin.getLocale(pl).challengesLevel + ": Default");
                    } else {
                        sender.sendMessage(plugin.getLocale(pl).challengesLevel + ": " + level);
                    }
                    // Collect and sort
                    Collection<String> result = new TreeSet<>(Collator.getInstance());
                    for (Block mat : LavaCheck.getStats().get(level).elementSet()) {
                        result.add("   " + Utils.prettifyText(mat.toString()) + ": " + LavaCheck.getStats().get(level).count(mat) + "/" + LavaCheck.getStats().get(level).size() + " or "
                                + ((int) ((double) LavaCheck.getStats().get(level).count(mat) / LavaCheck.getStats().get(level).size() * 100))
                                + "% (config = " + LavaCheck.getConfigChances(level, mat) + "%)");
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
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " completechallenge <player>");
                    break;
                }
                IPlayer offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                PlayerData pld = plugin.getPlayerInfo(offlinePlayer.getName());
                if (pld == null) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(pl).errorUnknownPlayer);
                    break;
                }
                if (pld.checkChallenge(args[2].toLowerCase()) || pld.challengeNotExists(args[2].toLowerCase())) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(pl).errorChallengeDoesNotExist);
                    break;
                }
                pld.completeChallenge(args[2].toLowerCase());
                pld.saveData();

                sender.sendMessage(TextFormat.YELLOW + plugin.getLocale(pl).completeChallengeCompleted
                        .replace("[challengename]", args[2].toLowerCase())
                        .replace("[name]", args[1]));
                break;
            case "rc":
            case "resetchallenge":
                if (!sender.hasPermission("is.admin.resetchallenge")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " resetchallenge <player>");
                    break;
                }

                offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                pld = plugin.getPlayerInfo(offlinePlayer.getName());
                if (pld == null) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(pl).errorUnknownPlayer);
                    break;
                }
                if (!pld.checkChallenge(args[2].toLowerCase()) || pld.challengeNotExists(args[2].toLowerCase())) {
                    sender.sendMessage(TextFormat.RED + plugin.getLocale(pl).errorChallengeDoesNotExist);
                    break;
                }
                pld.resetChallenge(args[2].toLowerCase());
                pld.saveData();
                sender.sendMessage(TextFormat.YELLOW + plugin.getLocale(pl).resetChallengeReset
                        .replace("[challengename]", args[2].toLowerCase())
                        .replace("[name]", args[1]));
                break;
            case "delete":
                if (!sender.hasPermission("is.admin.delete")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
                    break;
                }

                IslandData island;
                if (pl == null) {
                    // Well its console, so they could perform it by deleting it with command isn't?
                    if (args.length <= 2) {
                        sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " delete <player> <id>");
                        break;
                    }
                    int id = Integer.getInteger(args[2]);

                    // Show them, lets see if this dude got a data in database
                    offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                    island = plugin.getIslandInfo(offlinePlayer.getName(), id);
                    if (island == null) {
                        sender.sendMessage(plugin.getLocale("").errorUnknownPlayer);
                        break;
                    }

                    sender.sendMessage(plugin.getLocale("").deleteRemoving.replace("[name]", "null"));
                    deleteIslands(island, sender);
                    break;
                }

                // Get the island I am on
                island = plugin.getIslandManager().getIslandAt(pl);

                if (island == null) {
                    sender.sendMessage(plugin.getLocale(pl).adminDeleteIslandnoid);
                    break;
                }

                // Try to get the owner of this island
                String owner = island.getPlotOwner();
                if (!args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(pl).adminDeleteIslandError.replace("[player]", owner));
                    break;
                }

                if (owner != null) {
                    sender.sendMessage(plugin.getLocale(pl).adminSetSpawnOwnedBy.replace("[name]", owner));
                    sender.sendMessage(plugin.getLocale(pl).adminDeleteIslandUse.replace("[name]", owner));
                    break;
                } else {
                    sender.sendMessage(plugin.getLocale(pl).deleteRemoving.replace("[name]", "null"));
                    deleteIslands(island, sender);
                }
                break;
            case "info":
            case "challenges":
                if (!sender.hasPermission("is.admin.challenges")) {
                    sender.sendMessage(plugin.getLocale(pl).errorNoPermission);
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

    /**
     * Shows info on the challenge situation for player,
     *
     * @param player The Offline player to be checked on
     * @param sender Sender who performed the command
     */
    private void showInfoChallenges(IPlayer player, CommandSender sender) {
        PlayerData pd = plugin.getPlayerInfo(player.getName());
        // No way
        if (pd == null) {
            sender.sendMessage(TextFormat.RED + plugin.getLocale("").errorUnknownPlayer);
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
