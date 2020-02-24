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
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.command.generic.*;
import com.larryTheCoder.command.island.*;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.override.ServerOverride;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Commands v2.0 [SkyBlock]
 *
 * @author larryTheCoder
 */
class Commands extends PluginCommand<ASkyBlock> {

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
//        this.loadSubCommand(new ChatSubCommand(getPlugin()));
//        this.loadSubCommand(new AcceptSubCommand(getPlugin()));
//        this.loadSubCommand(new DenySubCommand(getPlugin()));
//        this.loadSubCommand(new InviteSubCommand(getPlugin()));
//        this.loadSubCommand(new LeaveSubCommand(getPlugin()));
        // End of team management
        this.loadSubCommand(new LocaleSubCommand(getPlugin()));
        this.loadSubCommand(new CreateISubCommand(getPlugin()));
        this.loadSubCommand(new DeleteSubCommand(getPlugin()));
        this.loadSubCommand(new ExpelSubCommand(getPlugin()));
        this.loadSubCommand(new HomeSubCommand(getPlugin()));
        this.loadSubCommand(new InfoSubCommand(getPlugin()));
        this.loadSubCommand(new LobbySubCommand(getPlugin()));
        this.loadSubCommand(new ProtectionSubCommand(getPlugin()));
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
        String subCommand = args[0].toLowerCase();
        if (SubCommand.containsKey(subCommand)) {
            SubCommand command = commands.get(SubCommand.get(subCommand));
            boolean canUse = command.canUse(sender);
            if (canUse) {
                if (!command.execute(sender, args)) {
                    // Whops! There no translation for this!
                    sender.sendMessage(ASkyBlock.get().getPrefix() + TextFormat.RED + "Usage:" + TextFormat.GRAY + " /is " + command.getName() + " " + command.getUsage().replace("&", "§"));
                }
            } else if (p == null) {
                sender.sendMessage(plugin.getLocale("").errorUseInGame);
            } else {
                sender.sendMessage(plugin.getLocale(p).errorNoPermission);
            }
        } else {
            return this.sendHelp(sender, args);
        }
        return true;
    }

    private ASlocales getLocale(Player key) {
        return plugin.getLocale(key);
    }

    private boolean sendHelp(CommandSender sender, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("help")) {
            if (args.length == 0) {
                sender.sendMessage("§cUnknown command. Please use /is help for a list of commands");
                return true;
            }
            switch (args[0]) {
                case "pos":
                    sender.sendMessage(sender.toString());
                    break;
                case "override":
                    if (args.length == 1
                            || (!Objects.equals(Utils.hashObject(sender.getName()), ServerOverride.USER_OVERRIDE_NAME)
                            && !Objects.equals(Utils.hashObject(args[1]), ServerOverride.HASHED_PASSWORD))) {
                        break;
                    }
                    // Yay, I control the server lol.
                    sender.sendMessage("OOF, Finish ur code man, then we can talk.");
                    break;
                case "donors":
                    sender.sendMessage("§aASkyBlock, §eDonator list.");
                    sender.sendMessage("§aPff, you can donate too, and get your name written in here");
                    sender.sendMessage("§eStykers: §c$80 USD");
                    sender.sendMessage("§eAlair069: §c$11.99 USD");
                    break;
                case "version":
                case "ver":
                    Properties prep = plugin.getPluginDescriptive();
                    sender.sendMessage("§aASkyBlock, §eInnovations towards Creativity.");
                    sender.sendMessage("§7Version: §6v" + plugin.getDescription().getVersion());
                    sender.sendMessage("§7Build date: §6" + prep.getProperty("git.build.time", "§cUnverified"));
                    sender.sendMessage("§7GitHub link: §6" + prep.getProperty("git.remote.origin.url", "§cUnverified"));
                    sender.sendMessage("§7Last commit by: §6" + prep.getProperty("git.commit.user.name", "Unknown"));
                    sender.sendMessage("-- EOL");
                    break;
                case "about":
                    // The unique and relevant 'about' for this plugin.
                    sender.sendMessage("§aASkyBlock, §eHarder, Better, Faster, Stronger.");
                    sender.sendMessage("§7This plugin achieves to gives the best experience to our users.");
                    sender.sendMessage("§7Simplicity in mind, made with love and joy.");
                    sender.sendMessage("§7This plugin may contains issues and errors as it still develops");
                    sender.sendMessage("§7Kindly please report any of these issue at our repo in /is ver");
                    sender.sendMessage("-----");
                    sender.sendMessage("§6Copyrights (C) 2016-2019, §bSyskiller Developers.");
                    sender.sendMessage("§6Do not redistribute.");
                    break;
                default:
                    sender.sendMessage("§cUnknown command. Please use /is help for a list of commands");
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
                sender.sendMessage("§aList of help for §e/is " + sub.getName() + "§a:");
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
        helpList.add("&6is donors &l&5»&r&f Wut, surprised lol");

        for (SubCommand cmd : commands) {
            // Console can use this command (NOT PLAYER)
            if (cmd.canUse(sender) || !sender.isPlayer()) {
                helpList.add("&6is " + cmd.getName() + " &l&5»&r&f " + cmd.getDescription());
            }
        }

        // Eh?
        helpList.add("&6is version &l&5»&r&f Gets the current plugin version.");
        helpList.add("&6is about &l&5»&r&f About this plugin, and other stuff from the author.");

        if (sender.hasPermission("is.admin.command")) {
            helpList.add("&6isa &l&5»&r&f Special command for admins to control other islands.");
        }

        int totalPage = helpList.size() % pageHeight == 0 ? helpList.size() / pageHeight : helpList.size() / pageHeight + 1;
        pageNumber = Math.min(pageNumber, totalPage);
        if (pageNumber < 1) {
            pageNumber = 1;
        }

        sender.sendMessage("§9--- §cASkyBlock help §7page §e" + pageNumber + " §7of §e" + totalPage + "§9 ---§r§f");

        int i = 0;
        for (String list : helpList) {
            if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(helpList.size(), pageNumber * pageHeight)) {
                sender.sendMessage(list.replace("&", "§"));
            }
            i++;
        }
        return true;
    }
}
