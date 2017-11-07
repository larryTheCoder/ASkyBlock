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
package com.larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.SkyBlockGenerator;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.task.DeleteIslandTask;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin command.
 * <p>
 * Full access to all islands (depends on player permissions)
 *
 * @author Adam Matthew
 */
public class AdminCMD extends Command {

    private final ASkyBlock plugin;

    public AdminCMD(ASkyBlock ev) {
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

                if (plugin.level.contains(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorLevelGenerated);
                    return true;
                } else if (!plugin.getServer().isLevelGenerated(args[1])) {
                    plugin.getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
                    plugin.getServer().loadLevel(args[1]);
                    plugin.level.add(new WorldSettings(plugin.getServer().getLevelByName(args[1])));
                    plugin.saveLevel(false);
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).generalSuccess);
                    return true;
                }
                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorLevelGenerated);
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
                    break;
                } else {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorFailedNormal);
                    break;
                }
            case "delete":
                if (p == null) {
                    sender.sendMessage(plugin.getLocale(p).errorUseInGame);
                    break;
                }

                if (!sender.hasPermission("is.admin.delete")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                // Get the island I am on
                IslandData island = plugin.getIsland().GetIslandAt(p);

                // Try to get the owner of this island
                String owner = island.getOwner();
                if (!args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).adminDeleteIslandError.replace("[player]", owner));
                    return true;
                }

                if (island == null) {
                    sender.sendMessage(plugin.getLocale(p).adminDeleteIslandnoid);
                    return true;
                }

                if (owner != null) {
                    sender.sendMessage(plugin.getLocale(p).adminSetSpawnOwnedBy.replace("[name]", owner));
                    sender.sendMessage(plugin.getLocale(p).adminDeleteIslandUse.replace("[name]", owner));
                    return true;
                } else {
                    sender.sendMessage(plugin.getLocale(p).deleteRemoving.replace("[name]", owner));
                    deleteIslands(island, sender);
                }
                break;
            case "setspawn":
                this.setSpawn(sender);
                break;
            case "addmessage":
                if (!sender.hasPermission("is.admin.delete")) {
                    sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                    break;
                }

                String msg = "";
                String[] var6 = args;
                int var7 = args.length;

                for (int var8 = 0; var8 < var7; ++var8) {
                    String arg = var6[var8];
                    msg = msg + arg + " ";
                }

                if (msg.length() > 0) {
                    msg = msg.substring(0, msg.length() - 1);
                }

                List<String> players = plugin.getDatabase().getPlayersData();

                for (String pl : players) {
                    List<String> list = plugin.getMessages().getMessages(pl);
                    list.add(msg);
                    plugin.getMessages().put(pl, list);
                }
            default:
                this.sendHelp(sender, commandLabel, args);
                break;
        }

        return true;
    }

    private void setSpawn(CommandSender sender) {
        Player p = sender.isPlayer() ? plugin.getServer().getPlayer(sender.getName()) : null;
        if (p == null) {
            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
            return;
        }
        if (!sender.hasPermission("is.admin.setspawn")) {
            sender.sendMessage(plugin.getLocale(p).errorNoPermission);
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
            sender.sendMessage(TextFormat.GREEN + plugin.getLocale(p).generalSuccess);
            return;
        }
        // Save this island
        IslandData pd = plugin.getIslandInfo(p.getLocation());
        pd.setSpawn(true);
        plugin.getDatabase().saveIsland(pd);
        sender.sendMessage(TextFormat.GREEN + plugin.getLocale(p).generalSuccess);
    }


    /**
     * Deletes the overworld and nether islands together
     *
     * @param island
     * @param sender
     */
    private void deleteIslands(IslandData island, CommandSender sender) {
        // Nukkit has a slow progress on Nether gameplay
        TaskManager.runTask(new DeleteIslandTask(plugin, island, sender));
    }

    public void sendHelp(CommandSender sender, String label, String[] args) {
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

        List<String> helpList = new ArrayList<>();

        helpList.add(""); // Really weird Java Machine bug (Usually this will be stored in List but not)

        if (sender.hasPermission("is.admin.rename")) {
            helpList.add("&e" + label + " rename &7=> &a" + plugin.getLocale(p).adminHelpRename);
        }

        if (sender.hasPermission("is.admin.kick")) {
            helpList.add("&e" + label + " kick &7=> &a" + plugin.getLocale(p).adminHelpKick);
        }

        if (sender.hasPermission("is.admin.generate")) {
            helpList.add("&e" + label + " generate &7=> &a" + plugin.getLocale(p).adminHelpGenerate);
        }

        if (sender.hasPermission("is.admin.setspawn")) {
            helpList.add("&e" + label + " setspawn &7=> &a" + plugin.getLocale(p).adminHelpSpawn);
        }

        if (sender.hasPermission("is.admin.delete")) {
            helpList.add("&e" + label + " delete &7=> &a" + plugin.getLocale(p).adminHelpDelete);
        }

        if (label.length() > 4) {
            helpList.add("");
            helpList.add("&eTired to use looooong commands of isadmin? You can use 'isa' for aliases!");
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
