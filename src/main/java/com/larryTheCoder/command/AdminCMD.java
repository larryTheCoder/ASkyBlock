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
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.SkyBlockGenerator;
import com.larryTheCoder.storage.IslandData;

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
        super("asadmin", "Island admin command", "\u00a77<parameters>", new String[]{"as", "asc"});
        this.plugin = ev;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;
        if (!sender.hasPermission("is.admin.command")) {
            sender.sendMessage(plugin.getLocale(p).errorNoPermission);
        }
        switch (args.length) {
            case 0:
                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).helpMessage.replace("[com]", commandLabel));
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        sender.sendMessage("§d--- §aAdmin command help page §e1 §aof §e1 §d---");
                        sender.sendMessage("§a/" + commandLabel + " help: " + plugin.getLocale(p).adminHelpCommand);
                        sender.sendMessage("§a/" + commandLabel + " generate <level>: " + plugin.getLocale(p).adminHelpGenerate);
                        sender.sendMessage("§a/" + commandLabel + " kick <player>: " + plugin.getLocale(p).adminHelpKick);
                        sender.sendMessage("§a/" + commandLabel + " rename <player> <name>: " + plugin.getLocale(p).adminHelpRename);
                        sender.sendMessage("§6You also can use: /asc");
                        break;
                    case "generate":
                        sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " generate <level>");
                        break;
                    case "kick":
                        sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " kick <player>");
                        break;
                    case "rename":
                        sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " rename <player> <name>");
                        break;
                    case "setspawn":
                        this.setSpawn(sender);
                        break;
                    case "delete":
                        if (p == null) {
                            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
                            break;
                        }
                        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).adminDeleteIslandError);
                        break;
                    default:
                        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).helpMessage.replace("[com]", commandLabel));
                        return true;
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "generate":
                        if (!sender.hasPermission("is.admin.generate")) {
                            sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                            break;
                        }
                        if (plugin.level.contains(args[1])) {
                            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorLevelGenerated);
                            return true;
                        } else if (!plugin.getServer().isLevelGenerated(args[1])) {
                            plugin.getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
                            plugin.getServer().loadLevel(args[1]);
                            plugin.level.add(args[1]);
                            plugin.getDatabase().saveWorlds(plugin.level);
                            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).generalSuccess);
                            return true;
                        }
                        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorLevelGenerated);
                        break;
                    case "kick":
                        if (!sender.hasPermission("is.admin.kick")) {
                            sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                            break;
                        }
                        plugin.getIsland().kickPlayerByAdmin(sender, args[1]);
                        break;
                    case "rename":
                        sender.sendMessage(plugin.getPrefix() + "§aUsage: /" + commandLabel + " rename <player> <name>");
                    case "setspawn":
                        this.setSpawn(sender);
                        break;
                    case "deleteisland":
                        if (p == null) {
                            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
                            break;
                        }
                        if (!args[1].equalsIgnoreCase("confirm")) {
                            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).adminDeleteIslandError);
                            return true;
                        }
                        // Get the island I am on
                        IslandData island = plugin.getIsland().GetIslandAt(p);
                        if (island == null) {
                            sender.sendMessage(plugin.getLocale(p).adminDeleteIslandnoid);
                            return true;
                        }
                        // Try to get the owner of this island

                        String owner = island.owner;
                        if (owner != null) {
                            sender.sendMessage(plugin.getLocale(p).adminSetSpawnOwnedBy.replace("[name]", owner));
                            sender.sendMessage(plugin.getLocale(p).adminDeleteIslandUse.replace("[name]", owner));
                            return true;
                        } else {
                            sender.sendMessage(plugin.getLocale(p).deleteRemoving.replace("[name]", owner));
                            deleteIslands(island, sender);
                        }
                        break;
                    case "delete":
                        if (p == null) {
                            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
                            break;
                        }
                        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).adminDeleteIslandError);
                        break;
                    default:
                        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).helpMessage.replace("[com]", "/asc"));
                        return true;
                }
                break;
            case 3:
                switch (args[0].toLowerCase()) {
                    case "setspawn":
                        this.setSpawn(sender);
                        break;
                    case "rename":
                        if (!sender.hasPermission("is.admin.rename")) {
                            sender.sendMessage(plugin.getLocale(p).errorNoPermission);
                            break;
                        }
                        IslandData pd = plugin.getIslandInfo(args[1]);
                        if (pd == null) {
                            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorNoIslandOther);
                            break;
                        }
                        pd.name = args[2];
                        boolean secces = plugin.getDatabase().saveIsland(pd);
                        if (secces) {
                            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).renameSeccess);
                            break;
                        } else {
                            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorFailedNormal);
                            break;
                        }
                }
                break;
            default:
                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(p).helpMessage.replace("[com]", commandLabel));
                return true;

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
        if (plugin.getIslandInfo(p) == null) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorNoIsland);
            return;
        } else if (!plugin.inIslandWorld(p)) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorWrongWorld);
            return;
        } else if (!plugin.getIslandInfo(p.getLocation()).owner.equalsIgnoreCase(p.getName())) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorNotOnIsland);
            return;
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

    private void sendHelp(CommandSender sender, String commandLabel) {
        Player p = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;
        if (p == null) {
            sender.sendMessage("§d--- §aAdmin command help page §e1 §aof §e1 §d---");
            sender.sendMessage("§a/" + commandLabel + " help: " + plugin.getLocale(p).adminHelpCommand);
            sender.sendMessage("§a/" + commandLabel + " generate <level>: " + plugin.getLocale(p).adminHelpGenerate);
            sender.sendMessage("§a/" + commandLabel + " kick <player>: " + plugin.getLocale(p).adminHelpKick);
            sender.sendMessage("§a/" + commandLabel + " rename <player> <name>: " + plugin.getLocale(p).adminHelpRename);
            sender.sendMessage("§6You also can use: /asc");
        } else {
            if (sender.hasPermission("is.admin.setspawn")) {
                sender.sendMessage("§a/" + commandLabel + " setSpawn: " + plugin.getLocale(p).adminHelpSpawn);
            }
            if (sender.hasPermission("is.admin.rename")) {
                sender.sendMessage("§a/" + commandLabel + " rename <player> <name>: " + plugin.getLocale(p).adminHelpRename);
            }
            if (sender.hasPermission("is.admin.kick")) {
                sender.sendMessage("§a/" + commandLabel + " kick <player>: " + plugin.getLocale(p).adminHelpKick);
            }
            if (sender.hasPermission("is.admin.generate")) {
                sender.sendMessage("§a/" + commandLabel + " generate <level>: " + plugin.getLocale(p).adminHelpGenerate);
            }
            // todo
            if (sender.hasPermission("is.admin.delete")) {
                sender.sendMessage("§a/" + commandLabel + " delete <level>: " + plugin.getLocale(p).adminHelpDelete);
            }
        }
    }

    /**
     * Deletes the overworld and nether islands together
     *
     * @param island
     * @param sender
     */
    private void deleteIslands(IslandData island, CommandSender sender) {
        // Todo
    }

}
