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

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.PluginCommand;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.category.*;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A better version of Command
 *
 * @author larryTheCoder
 */
public class Commands extends PluginCommand<ASkyBlock> {

    private final List<SubCategory> commandCategory = new ArrayList<>();
    private final List<String> baseCommand = new ArrayList<>();

    public Commands(ASkyBlock plugin) {
        super("is", plugin);

        this.registerCategories();
    }

    private void registerCategories() {
        commandCategory.add(new GenericCategory(getPlugin()));
        commandCategory.add(new IslandCategory(getPlugin()));
        commandCategory.add(new ChatCategory(getPlugin()));
        //commandCategory.add(new CoopCategory(getPlugin()));
        commandCategory.add(new OperatorCategory(getPlugin()));

        // Then we add them into the array.
        commandCategory.forEach(i -> i.baseCommands().stream().filter(o -> !baseCommand.contains(o)).forEach(baseCommand::add));

        setAliases(baseCommand.toArray(new String[0]));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player p = sender.isPlayer() ? getPlugin().getServer().getPlayer(sender.getName()) : null;
        if (!sender.hasPermission("is.command")) {
            sender.sendMessage(getLocale(p).errorNoPermission);
            return true;
        }

        if (args.length == 0) {
            if (p == null) {
                sender.sendMessage(String.format("§cUnknown command. Please use /%s help for a list of commands", label));
            } else {
                getPlugin().getIslandManager().handleIslandCommand(p, false);
            }

            return true;
        }

        // An array always consisting of (n + 1) where n is the value of data in an array.
        if (args[0].equalsIgnoreCase("help")) {
            int pageNumber = 1;
            if (args.length >= 2 && Utils.isNumeric(args[1])) {
                pageNumber = Integer.parseInt(args[1]);
            }

            switch (label.toLowerCase()) {
                case "is":
                case "island":
                    sendHelp(sender, 0, pageNumber);
                    break;
                case "isa":
                case "isadmin":
                    sendHelp(sender, 1, pageNumber);
                    break;
            }

            return true;
        }

        SubCategory cmdCategory = commandCategory.stream().filter(i -> i.getCommands().contains(args[0].toLowerCase()) && i.baseCommands().contains(label.toLowerCase())).findFirst().orElse(null);
        if (cmdCategory == null || !cmdCategory.canUse(sender, args[0])) {
            sender.sendMessage(String.format("§cUnknown command. Please use /%s help for a list of commands", label));

            return true;
        }

        cmdCategory.execute(sender, label, args);

        return true;
    }

    public void sendHelp(CommandSender sender, int helpId, int pageNumber) {
        int pageHeight;
        if (sender instanceof ConsoleCommandSender) {
            pageHeight = Integer.MAX_VALUE;
        } else {
            pageHeight = 9;
        }

        List<String> helpList = new ArrayList<>();

        switch (helpId) {
            case 0:
                commandCategory.forEach(i -> i.getCommands().stream()
                        .filter(h -> i.canUse(sender, h) && i.getDescription(h) != null && i.baseCommands().contains("is"))
                        .forEach(a -> {
                            String param = i.getParameters(a);
                            if (param == null || param.isEmpty()) {
                                helpList.add(String.format("&6/%s %s &l&5»&r&f %s", Utils.compactSmall(i.baseCommands().toArray(new String[0])), a, i.getDescription(a)));
                            } else {
                                helpList.add(String.format("&6/%s %s &a%s &l&5»&r&f %s", Utils.compactSmall(i.baseCommands().toArray(new String[0])), a, param, i.getDescription(a)));
                            }
                        }));

                helpList.add("&6/isa help &l&5»&r&f Special command for admins to control other islands");

                int totalPage = helpList.size() % pageHeight == 0 ? helpList.size() / pageHeight : helpList.size() / pageHeight + 1;
                pageNumber = Math.min(pageNumber, totalPage);
                if (pageNumber < 1) {
                    pageNumber = 1;
                }

                sender.sendMessage("§9--- §cASkyBlock help §7page §e" + pageNumber + " §7of §e" + totalPage + "§9 ---§r§f");
                break;
            case 1:
                helpList.add("&7isa rename &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpRename);
                helpList.add("&7isa kick &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpKick);
                helpList.add("&7isa generate &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpGenerate);
                helpList.add("&7isa delete &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpDelete);
                helpList.add("&7isa addmessage &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpMessage);
                helpList.add("&7isa clear &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpClear);
                helpList.add("&7isa cobblestats &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpDelete);
                helpList.add("&7isa completechallenge &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpMessage);
                helpList.add("&7isa resetchallenge &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpClear);
                helpList.add("&7isa challenges &l&5»&r&f &a" + getPlugin().getLocale(sender).adminHelpInfo);

                totalPage = helpList.size() % pageHeight == 0 ? helpList.size() / pageHeight : helpList.size() / pageHeight + 1;
                pageNumber = Math.min(pageNumber, totalPage);
                if (pageNumber < 1) {
                    pageNumber = 1;
                }

                sender.sendMessage("§e--- §eAdmin SkyBlock Help Page §a" + pageNumber + " §eof §a" + totalPage + " §e---");
                break;
        }

        int i = 0;
        for (String list : helpList) {
            i++; // Very smart

            if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(helpList.size(), pageNumber * pageHeight)) {
                sender.sendMessage(list.replace("&", "§"));
            }
        }
    }

    private ASlocales getLocale(Player key) {
        return getPlugin().getLocale(key);
    }
}
