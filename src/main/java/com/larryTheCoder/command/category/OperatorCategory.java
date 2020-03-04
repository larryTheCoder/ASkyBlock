/*
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

package com.larryTheCoder.command.category;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.SkyBlockGenerator;
import com.larryTheCoder.listener.LavaCheck;
import com.larryTheCoder.cache.PlayerData;
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.cache.settings.WorldSettings;
import com.larryTheCoder.task.DeleteIslandTask;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.text.Collator;
import java.util.*;

public class OperatorCategory extends SubCategory {

    public OperatorCategory(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public List<String> baseCommands() {
        return Arrays.asList("isadmin", "isa");
    }

    @Override
    public List<String> getCommands() {
        return Collections.singletonList("help");
    }

    @Override
    public boolean canUse(CommandSender sender, String command) {
        return sender.hasPermission("is.admin.command");
    }

    @Override
    public String getDescription(String commandName) {
        return "Special command for admins to control other islands";
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String[] args) {
        Player pl = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;

        if (args.length == 0) {
            return;
        }

        switch (args[0]) {
            case "generate":
                if (!sender.hasPermission("is.admin.generate")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }

                if (args.length <= 1) {
                    sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " generate <level>");
                    break;
                }

                if (getPlugin().loadedLevel.contains(args[1])) {
                    sender.sendMessage(getPlugin().getPrefix() + getPlugin().getLocale(pl).errorLevelGenerated);
                    break;
                } else if (!getPlugin().getServer().isLevelGenerated(args[1])) {
                    getPlugin().getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
                    getPlugin().getServer().loadLevel(args[1]);
                    WorldSettings world = new WorldSettings(getPlugin().getServer().getLevelByName(args[1]));
                    Config cfg = new Config(new File(getPlugin().getDataFolder(), "worlds.yml"), Config.YAML);
                    cfg.set(args[1] + ".permission", world.getPermission());
                    cfg.set(args[1] + ".maxHome", world.getMaximumIsland());
                    cfg.set(args[1] + ".protectionRange", world.getProtectionRange());
                    cfg.set(args[1] + ".stopTime", world.isStopTime());
                    cfg.set(args[1] + ".seaLevel", world.getSeaLevel());
                    cfg.save();
                    getPlugin().saveLevel(false);
                    getPlugin().getLevel().add(world);
                    sender.sendMessage(getPlugin().getPrefix() + getPlugin().getLocale(pl).generalSuccess);
                    break;
                }
                sender.sendMessage(getPlugin().getPrefix() + getPlugin().getLocale(pl).errorLevelGenerated);
                break;
            case "clear": // TODO: Is it reasonable to use this command anymore?
                if (!sender.hasPermission("is.admin.clear")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }
                getPlugin().getInventory().clearSavedInventory();
                sender.sendMessage(TextFormat.RED + "Cleared memory usage.");
                sender.sendMessage(TextFormat.RED + "Warning: Player data may be lost during this cleanup");
                break;
            case "kick":
                if (args.length <= 1) {
                    sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " kick <player>");
                    break;
                }

                if (!sender.hasPermission("is.admin.kick")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }
                getPlugin().getIslandManager().kickPlayerByAdmin(sender, args[1]);
                break;
            case "rename":
                if (!sender.hasPermission("is.admin.rename")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }

                if (args.length <= 2) {
                    sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " rename <player> <name>");
                    break;
                }

                getPlugin().getFastCache().getIslandData(args[1], pd -> {
                    if (pd == null) {
                        sender.sendMessage(getPlugin().getPrefix() + getPlugin().getLocale(pl).errorNoIslandOther);
                        return;
                    }
                    pd.setIslandName(args[2]);
                    pd.saveIslandData();

                    sender.sendMessage(getPlugin().getPrefix() + getPlugin().getLocale(pl).renameSuccess);
                });
                break;
            case "cobblestats":
                if (!sender.hasPermission("is.admin.cobblestats")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }
                if (LavaCheck.getStats().size() == 0) {
                    sender.sendMessage(TextFormat.OBFUSCATED + "");
                    break;
                }
                // Display by level
                for (Integer level : LavaCheck.getStats().keySet()) {
                    if (level == Integer.MIN_VALUE) {
                        sender.sendMessage(getPlugin().getLocale(pl).challengesLevel + ": Default");
                    } else {
                        sender.sendMessage(getPlugin().getLocale(pl).challengesLevel + ": " + level);
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
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " completechallenge <player>");
                    break;
                }
                IPlayer offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                PlayerData pld = getPlugin().getPlayerInfo(offlinePlayer.getName());
                if (pld == null) {
                    sender.sendMessage(TextFormat.RED + getPlugin().getLocale(pl).errorUnknownPlayer);
                    break;
                }
                if (pld.checkChallenge(args[2].toLowerCase()) || pld.challengeNotExists(args[2].toLowerCase())) {
                    sender.sendMessage(TextFormat.RED + getPlugin().getLocale(pl).errorChallengeDoesNotExist);
                    break;
                }
                pld.completeChallenge(args[2].toLowerCase());
                pld.saveData();

                sender.sendMessage(TextFormat.YELLOW + getPlugin().getLocale(pl).completeChallengeCompleted
                        .replace("[challengename]", args[2].toLowerCase())
                        .replace("[name]", args[1]));
                break;
            case "rc":
            case "resetchallenge":
                if (!sender.hasPermission("is.admin.resetchallenge")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " resetchallenge <player>");
                    break;
                }

                offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                pld = getPlugin().getPlayerInfo(offlinePlayer.getName());
                if (pld == null) {
                    sender.sendMessage(TextFormat.RED + getPlugin().getLocale(pl).errorUnknownPlayer);
                    break;
                }
                if (!pld.checkChallenge(args[2].toLowerCase()) || pld.challengeNotExists(args[2].toLowerCase())) {
                    sender.sendMessage(TextFormat.RED + getPlugin().getLocale(pl).errorChallengeDoesNotExist);
                    break;
                }
                pld.resetChallenge(args[2].toLowerCase());
                pld.saveData();
                sender.sendMessage(TextFormat.YELLOW + getPlugin().getLocale(pl).resetChallengeReset
                        .replace("[challengename]", args[2].toLowerCase())
                        .replace("[name]", args[1]));
                break;
            case "delete":
                if (!sender.hasPermission("is.admin.delete")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }

                IslandData island;
                if (pl == null) {
                    // Well its console, so they could perform it by deleting it with command isn't?
                    if (args.length <= 2) {
                        sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " delete <player> <id>");
                        break;
                    }
                    int id = Integer.getInteger(args[2]);

                    // Show them, lets see if this dude got a data in database
                    offlinePlayer = Server.getInstance().getOfflinePlayer(args[1]);
                    island = getPlugin().getIslandInfo(offlinePlayer.getName(), id);
                    if (island == null) {
                        sender.sendMessage(getPlugin().getLocale("").errorUnknownPlayer);
                        break;
                    }

                    sender.sendMessage(getPlugin().getLocale("").deleteRemoving.replace("[name]", "null"));
                    deleteIslands(island, sender);
                    break;
                }

                // Get the island I am on
                island = getPlugin().getIslandManager().getIslandAt(pl);

                if (island == null) {
                    sender.sendMessage(getPlugin().getLocale(pl).adminDeleteIslandnoid);
                    break;
                }

                // Try to get the owner of this island
                String owner = island.getPlotOwner();
                if (!args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage(getPlugin().getPrefix() + getPlugin().getLocale(pl).adminDeleteIslandError.replace("[player]", owner));
                    break;
                }

                if (owner != null) {
                    sender.sendMessage(getPlugin().getLocale(pl).adminSetSpawnOwnedBy.replace("[name]", owner));
                    sender.sendMessage(getPlugin().getLocale(pl).adminDeleteIslandUse.replace("[name]", owner));
                    break;
                } else {
                    sender.sendMessage(getPlugin().getLocale(pl).deleteRemoving.replace("[name]", "null"));
                    deleteIslands(island, sender);
                }
                break;
            case "info":
            case "challenges":
                if (!sender.hasPermission("is.admin.challenges")) {
                    sender.sendMessage(getPlugin().getLocale(pl).errorNoPermission);
                    break;
                }
                if (args.length <= 1) {
                    sender.sendMessage(getPlugin().getPrefix() + "§aUsage: /" + commandLabel + " info <player>");
                    break;
                }

                // Show them, lets see if this dude got a data in database
                showInfoChallenges(Server.getInstance().getOfflinePlayer(args[1]), sender);
                break;
        }
    }

    /**
     * Shows info on the challenge situation for player,
     *
     * @param player The Offline player to be checked on
     * @param sender Sender who performed the command
     */
    private void showInfoChallenges(IPlayer player, CommandSender sender) {
        PlayerData pd = getPlugin().getPlayerInfo(player.getName());
        // No way
        if (pd == null) {
            sender.sendMessage(TextFormat.RED + getPlugin().getLocale("").errorUnknownPlayer);
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
        TaskManager.runTask(new DeleteIslandTask(getPlugin(), island, sender));
    }
}
