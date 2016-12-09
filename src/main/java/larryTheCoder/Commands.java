/*
 * Copyright (C) 2016 larryTheHarry 
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
package larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import larryTheCoder.command.AKickSubCommand;
import larryTheCoder.command.CreateISubCommand;
import larryTheCoder.command.SubCommand;
import larryTheCoder.command.kickSubCommand;
import larryTheCoder.command.VGamemodeSubCommand;
import larryTheCoder.command.leaveSubCommand;
import larryTheCoder.command.deleteSubCommand;
import larryTheCoder.command.ASetLobbySubCommand;
import larryTheCoder.command.ToggleSubCommand;
import larryTheCoder.command.acceptSubCommand;
import larryTheCoder.command.denySubCommand;
import larryTheCoder.command.homeSubCommand;
import larryTheCoder.command.infoSubCommand;
import larryTheCoder.command.inviteSubCommand;
import larryTheCoder.command.teleportSubCommand;

/**
 * @author larryTheCoder
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

        //this.loadSubCommand(new AGenerateSubCommand(getPlugin()));
        this.loadSubCommand(new acceptSubCommand(getPlugin()));
        this.loadSubCommand(new AKickSubCommand(getPlugin()));
        this.loadSubCommand(new ASetLobbySubCommand(getPlugin()));
        this.loadSubCommand(new CreateISubCommand(getPlugin()));
        this.loadSubCommand(new denySubCommand(getPlugin()));
        this.loadSubCommand(new deleteSubCommand(getPlugin()));
        this.loadSubCommand(new infoSubCommand(getPlugin()));
        this.loadSubCommand(new inviteSubCommand(getPlugin()));
        this.loadSubCommand(new kickSubCommand(getPlugin()));
        this.loadSubCommand(new leaveSubCommand(getPlugin()));
        this.loadSubCommand(new teleportSubCommand(getPlugin()));
        this.loadSubCommand(new ToggleSubCommand(getPlugin()));
        this.loadSubCommand(new VGamemodeSubCommand(getPlugin()));
        this.loadSubCommand(new homeSubCommand(getPlugin()));
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
        if (args.length == 0) {
            if (sender.isPlayer() && sender.hasPermission("is.create")) {
                Player p = getPlugin().getServer().getPlayer(sender.getName());
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
                    sender.sendMessage(ASkyBlock.get().getPrefix() + TextFormat.RED + "Usage:" + TextFormat.GRAY + " /is " + command.getName() + " " + command.getUsage().replace("&", "§"));
                }
            } else if (!(sender instanceof Player)) {
                sender.sendMessage(TextFormat.RED + "Please run this command in-game.");
            } else {
                sender.sendMessage(TextFormat.RED + "You do not have permissions to run this command");
            }
        } else {
            return this.sendHelp(sender, args);
        }
        return true;
    }

    public String getMsg(String key) {
        return getPlugin().getMsg(key);
    }

    private boolean sendHelp(CommandSender sender, String[] args) {
        int pageNumber = 1;
        int pageHeight = 5;
        if (args.length == 2 && Utils.isNumeric(args[1])) {
            pageNumber = Integer.parseInt(args[1]);
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
                sender.sendMessage(TextFormat.DARK_GREEN + "/is " + cmd.getName() + ": " + TextFormat.WHITE + cmd.getDescription());
            }
            i++;
        }
        if (pageNumber != totalPage) {
            sender.sendMessage("§aType §e/is help " + (pageNumber + 1) + "§a to see the next page.");
        }
        return true;
    }

}
