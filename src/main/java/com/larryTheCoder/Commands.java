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
package com.larryTheCoder;

import com.larryTheCoder.utils.Utils;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.larryTheCoder.command.generic.*;
import com.larryTheCoder.command.island.*;
import com.larryTheCoder.command.management.*;
import com.larryTheCoder.command.chat.*;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.locales.ASlocales;

/**
 * Commands v2.0 [SkyBlock]
 *
 * @author Adam Matthew
 */
public class Commands extends PluginCommand<ASkyBlock> {

    private final List<SubCommand> commands = new ArrayList<>();
    private final ConcurrentHashMap<String, Integer> SubCommand = new ConcurrentHashMap<>();
    private final ASkyBlock plugin;

    @SuppressWarnings({"unchecked", "OverridableMethodCallInConstructor"})
    public Commands(ASkyBlock plugin) {
        super("is", plugin);
        this.setAliases(new String[]{"sky", "island", "skyblock"});
        this.setPermission("is.command");
        this.setDescription("SkyBlock main command");
        this.plugin = plugin;

        // A-Z
        this.loadSubCommand(new AcceptSubCommand(getPlugin()));
        this.loadSubCommand(new ChatSubCommand(getPlugin()));
        this.loadSubCommand(new CreateISubCommand(getPlugin()));
        this.loadSubCommand(new DeleteSubCommand(getPlugin()));
        this.loadSubCommand(new DenySubCommand(getPlugin()));
        this.loadSubCommand(new EditSubCommand(getPlugin()));
        this.loadSubCommand(new ExpelSubCommand(getPlugin()));
        this.loadSubCommand(new HomeSubCommand(getPlugin()));
        this.loadSubCommand(new InfoSubCommand(getPlugin()));
        this.loadSubCommand(new InviteSubCommand(getPlugin()));
        this.loadSubCommand(new LeaveSubCommand(getPlugin()));
        this.loadSubCommand(new MessageSubCommand(getPlugin()));
        this.loadSubCommand(new SetHomeSubCommand(getPlugin()));
        this.loadSubCommand(new SetSpawnSubCommand(getPlugin()));
        this.loadSubCommand(new TeleportSubCommand(getPlugin()));
    }

    private void loadSubCommand(SubCommand cmd) {
        commands.add(cmd);
        int commandId = (commands.size()) - 1;
        SubCommand.put(cmd.getName().toLowerCase(), commandId);
        for (String alias : cmd.getAliases()) {
            SubCommand.put(alias.toLowerCase(), commandId);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player p = sender.isPlayer() ? getPlugin().getServer().getPlayer(sender.getName()) : null;
        if(!sender.hasPermission("is.command")){
            sender.sendMessage(getLocale(p).errorNoPermission);
            return true;
        }
        if (args.length == 0) {
            if (p != null && sender.hasPermission("is.create")) {
                plugin.getIsland().handleIslandCommand(p);
            } else if (!(sender instanceof Player)) {
                return this.sendHelp(sender, args);
            } else {
                return this.sendHelp(sender, args);
            }
            return true;
        }
        String subcommand = args[0].toLowerCase();
        if (SubCommand.containsKey(subcommand)) {
            SubCommand command = commands.get(SubCommand.get(subcommand));
            boolean canUse = command.canUse(sender);
            if (canUse) {
                if (!command.execute(sender, args)) {
                    // Whops! There no translation for this!
                    sender.sendMessage(ASkyBlock.get().getPrefix() + TextFormat.RED + "Usage:" + TextFormat.GRAY + " /is " + command.getName() + " " + command.getUsage().replace("&", "§"));
                }
            } else if (p == null) {
                sender.sendMessage(plugin.getLocale(p).errorUseInGame);
            } else {
                sender.sendMessage(plugin.getLocale(p).errorNoPermission);
            }
        } else {
            return this.sendHelp(sender, args);
        }
        return true;
    }

    public ASlocales getLocale(Player key) {
        return plugin.getLocale(key);
    }

    private boolean sendHelp(CommandSender sender, String[] args) {
        if (args.length != 0) {
            if (!args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(TextFormat.RED + "Unknown command use /is help for a list of commands");
                return true;
            }
            if (args.length == 2 && !Utils.isNumeric(args[1])) {
                if (SubCommand.containsKey(args[1].toLowerCase())) {
                    // Show help for #IRC
                    SubCommand sub = commands.get(SubCommand.get(args[1].toLowerCase()));
                    String command = "";
                    for (String arg : sub.getAliases()) {
                        if (!command.equals("")) {
                            command += " ";
                        }
                        command += arg;
                    }
                    if (command.isEmpty()) {
                        command = "none";
                    }
                    String usage = sub.getUsage();
                    if (sub.getUsage().isEmpty()) {
                        usage = "none";
                    }
                    sender.sendMessage("§aHelp for §e/is " + sub.getName() + "§a:");
                    sender.sendMessage(" §d- §aAliases: §e" + command);
                    sender.sendMessage(" §d- §aDescription: §e" + sub.getDescription());
                    sender.sendMessage(" §d- §aArrugements: §e" + usage);
                    return true;
                } else {
                    sender.sendMessage("§cNo help for §e" + args[1] + "");
                    return true;
                }
            }
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
            int totalPage = commands.size() % pageHeight == 0 ? commands.size() / pageHeight : commands.size() / pageHeight + 1;
            pageNumber = Math.min(pageNumber, totalPage);
            if (pageNumber < 1) {
                pageNumber = 1;
            }
            sender.sendMessage("§d--- §aASkyBlock help page §e" + pageNumber + " §aof §e" + totalPage + " §d---");
            int i = 1;
            for (SubCommand cmd : commands) {
                if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(commands.size(), pageNumber * pageHeight)) {
                    sender.sendMessage(TextFormat.DARK_GREEN + "/is " + cmd.getName() + ": §e" + TextFormat.WHITE + cmd.getDescription());
                }
                i++;
            }
            if (pageNumber != totalPage) {
                sender.sendMessage("§aType §e/is help " + (pageNumber + 1) + "§a to see the next page.");
            } else {
                sender.sendMessage("§aUse /is help §e<#IRC> §ato see command parameters");
            }
        } else {
            sender.sendMessage("§cUnknown command use /is help for a list of commands");
        }
        return true;
    }

}
