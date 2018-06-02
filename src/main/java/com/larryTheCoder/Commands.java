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

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.command.generic.ExpelSubCommand;
import com.larryTheCoder.command.generic.LeaveSubCommand;
import com.larryTheCoder.command.generic.LocaleSubCommand;
import com.larryTheCoder.command.island.*;
import com.larryTheCoder.command.management.SettingsSubCommand;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Commands v2.0 [SkyBlock]
 *
 * @author Adam Matthew
 */
public class Commands extends PluginCommand<ASkyBlock> {

    private final List<SubCommand> commands = new ArrayList<>();
    private final ConcurrentHashMap<String, Integer> SubCommand = new ConcurrentHashMap<>();
    private final ASkyBlock plugin;

    Commands(ASkyBlock plugin) {
        super("is", plugin);
        this.setAliases(new String[]{"sky", "island", "skyblock"});
        this.setPermission("is.command");
        this.setDescription("SkyBlock main command");
        this.plugin = plugin;

        // Todo: add the partner (Team) for players
//        this.loadSubCommand(new AcceptSubCommand(getPlugin()));
//        this.loadSubCommand(new ChatSubCommand(getPlugin()));
//        this.loadSubCommand(new DenySubCommand(getPlugin()));
//        this.loadSubCommand(new InviteSubCommand(getPlugin()));
//        this.loadSubCommand(new MessageSubCommand(getPlugin()));
        this.loadSubCommand(new LocaleSubCommand(getPlugin()));
        this.loadSubCommand(new CreateISubCommand(getPlugin()));
        this.loadSubCommand(new DeleteSubCommand(getPlugin()));
        this.loadSubCommand(new ExpelSubCommand(getPlugin()));
        this.loadSubCommand(new HomeSubCommand(getPlugin()));
        this.loadSubCommand(new InfoSubCommand(getPlugin()));
        this.loadSubCommand(new LeaveSubCommand(getPlugin()));
        this.loadSubCommand(new SetHomeSubCommand(getPlugin()));
        this.loadSubCommand(new SettingsSubCommand(getPlugin()));
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
        if (!sender.hasPermission("is.command")) {
            sender.sendMessage(getLocale(p).errorNoPermission);
            return true;
        }
        if (args.length == 0) {
            if (p != null && sender.hasPermission("is.create")) {
                plugin.getIsland().handleIslandCommand(p, false);
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
                sender.sendMessage(plugin.getLocale(null).errorUseInGame);
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
        if (args.length == 0 || !args[0].equalsIgnoreCase("help")) {
            if (args.length == 0) {
                sender.sendMessage("§cUnknown command use /is help for a list of commands");
                return true;
            }
            switch (args[0]) {
//                case "pos":
//                    sender.sendMessage(sender.toString());
//                    break;
                case "version":
                case "ver":
                    sender.sendMessage("§aASkyBlock Module §7" + ASkyBlock.moduleVersion + " Build 9");
                    sender.sendMessage("§aVendor Type: §7" + System.getProperty("os.name"));
                    sender.sendMessage("§aJava Module Version: §7" + System.getProperty("java.version"));
                    break;
                case "about":
                    sender.sendMessage("§7A Fresh Nukkit SkyBlock module for MCBE " + ProtocolInfo.MINECRAFT_VERSION);
                    sender.sendMessage("§7This game inspired from a plugin called B-SkyBlock. (Better SkyBlock)");
                    sender.sendMessage("§aSame as this plugin but it only in PC. The most powerful Java game in the world");
                    sender.sendMessage("§eHopefully that you can contribute more with us at: ");
                    sender.sendMessage("§eGitHub: §dhttps://github.com/TheSolidCrafter/ASkyBlock-Nukkit");
                    sender.sendMessage("§eDonate: §dhttp://www.paypal.me/DoubleCheese");
                    break;
                default:
                    sender.sendMessage("§cUnknown command use /is help for a list of commands");
            }
            return true;
        }

        // Command #IRC
        if (args.length == 2 && !Utils.isNumeric(args[1])) {
            if (SubCommand.containsKey(args[1].toLowerCase())) {
                // Show help for #IRC
                SubCommand sub = commands.get(SubCommand.get(args[1].toLowerCase()));
                StringBuilder command = new StringBuilder();
                for (String arg : sub.getAliases()) {
                    if (!command.toString().equals("")) {
                        command.append(" ");
                    }
                    command.append(arg);
                }
                if (command.length() == 0) {
                    command = new StringBuilder("none");
                }
                String usage = sub.getUsage();
                if (sub.getUsage().isEmpty()) {
                    usage = "none";
                }
                sender.sendMessage("§aHelp for §e/is " + sub.getName() + "§a:");
                sender.sendMessage(" §d- §aAliases: §e" + command);
                sender.sendMessage(" §d- §aDescription: §e" + sub.getDescription());
                sender.sendMessage(" §d- §aAgreements: §e" + usage);
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
            pageHeight = 9;
        }

        List<String> helpList = new ArrayList<>();

        helpList.add("");

        for (SubCommand cmd : commands) {
            // Console can use this command (NOT PLAYER)
            if (cmd.canUse(sender) || !sender.isPlayer()) {
                helpList.add("§eis " + cmd.getName() + TextFormat.GRAY + " => §a" + cmd.getDescription());
            }
        }
        helpList.add("§eis version" + TextFormat.GRAY + " => §aGets the current module version.");
        helpList.add("§eis about" + TextFormat.GRAY + " => §aListen to what this author say.");

        if (sender.hasPermission("is.admin.command")) {
            helpList.add("§eisa" + TextFormat.GRAY + " => §aThe admin command Module");
        }

        int totalPage = helpList.size() % pageHeight == 0 ? helpList.size() / pageHeight : helpList.size() / pageHeight + 1;
        pageNumber = Math.min(pageNumber, totalPage);
        if (pageNumber < 1) {
            pageNumber = 1;
        }

        sender.sendMessage("§7--- §dSkyBlock §eHelp Page §a" + pageNumber + " §eof §a" + totalPage + " §7---");

        int i = 0;
        for (String list : helpList) {
            if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(helpList.size(), pageNumber * pageHeight)) {
                sender.sendMessage(list.replace("&", "§"));
            }
            i++;
        }
        return true;
    }

    // ----- [0]  SkyBlock Help Page 1 of 8 [0] -----
    // is command => Involved of an description
    // inGame help

}
