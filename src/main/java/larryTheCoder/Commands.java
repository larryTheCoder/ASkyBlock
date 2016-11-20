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
import larryTheCoder.command.HelpSubCommand;
import larryTheCoder.command.SubCommand;

/**
 * @author larryTheCoder
 */
public class Commands extends PluginCommand<ASkyBlock> {

    private final List<SubCommand> commands = new ArrayList<>();
    private final ConcurrentHashMap<String, Integer> SubCommand = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "OverridableMethodCallInConstructor"})
    public Commands(ASkyBlock plugin) {
        super("is", plugin);
        this.setAliases(new String[]{"sky", "island", "skyblock"});
        this.setPermission("is.command");
        this.setDescription("SkyBlock main command");

        this.loadSubCommand(new HelpSubCommand(getPlugin()));
    }

    private void loadSubCommand(SubCommand cmd) {
        commands.add(cmd);
        int commandId = (commands.size()) - 1;
        SubCommand.put(cmd.getName().toLowerCase(), commandId);
        for (String alias : cmd.getAliases()) {
            SubCommand.put(alias, commandId);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            getPlugin().getLogger().info("CLASS 1 RUN");
            return this.sendHelp(sender);
        }
        String subcommand = args[0].toLowerCase();
        if (SubCommand.containsKey(subcommand)) {
            getPlugin().getLogger().info("CLASS 2 RUN");
            SubCommand command = commands.get(SubCommand.get(subcommand));
            boolean canUse = command.canUse(sender);
            if (canUse) {
                if (!command.execute(sender, args)) {
                    sender.sendMessage(TextFormat.YELLOW + "Usage: /is " + command.getName() + " " + command.getUsage());
                }
            } else if (!(sender instanceof Player)) {
                sender.sendMessage(TextFormat.RED + "Please run this command in-game.");
            } else {
                sender.sendMessage(TextFormat.RED + "You do not have permissions to run this command");
            }
        } else {
            getPlugin().getLogger().info("CLASS 3 RUN");
            return this.sendHelp(sender);
        }
        getPlugin().getLogger().info("CLASS 4 RUN");
        return true;
    }

    private boolean sendHelp(CommandSender sender) {
        sender.sendMessage("===========[SkyBlock commands]===========");
        for (SubCommand command : commands) {
            if (command.canUse(sender)) {
                sender.sendMessage(
                        TextFormat.DARK_GREEN + "/is " + command.getName() + " " + command.getUsage() + ": "
                        + TextFormat.WHITE + command.getDescription()
                );
            }
        }
        return true;
    }

}
