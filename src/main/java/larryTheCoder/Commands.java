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
import larryTheCoder.command.SubCommand;
import larryTheCoder.command.kickSubCommand;
import larryTheCoder.island.Island;

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

        this.loadSubCommand(new kickSubCommand(getPlugin()));
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
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if(sender.isPlayer() && sender.hasPermission("is.create")){
                Player p = getPlugin().getServer().getPlayer(sender.getName());
                Island.handleIslandCommand(p);
            } else if (!(sender instanceof Player)){
                sender.sendMessage(getMsg("help"));
            }
            return true;
        }
        String subcommand = args[0].toLowerCase();
        if (SubCommand.containsKey(subcommand)) {
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
            return this.sendHelp(sender);
        }
        getPlugin().getLogger().info("CLASS 4 RUN");
        return true;
    }

    public String getMsg(String key){
        return getPlugin().getMsg(key);
    }
    
    private boolean sendHelp(CommandSender sender) {
        String label = "is";     
        // Header
        sender.sendMessage(Utils.RainbowString("SkyBlock", "b") + " " + TextFormat.RESET + TextFormat.GREEN + getPlugin().getDescription().getVersion() + " help:");
        // This is for a Player command only
        if (sender.isPlayer()) {
            Player p = getPlugin().getServer().getPlayer(sender.getName());
            // Create island functions
            if (sender.hasPermission("is.create")) {
                // Check if player has an island or not 
                if (Island.checkIsland(p)) {
                    // If the player does have an island, the help message will show teleport
                    sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getPlugin().getMsg("help_teleport"));
                } else {
                    // if not help message will show how to create an island
                    sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getPlugin().getMsg("help_island"));
                }
            }
            // Kick / expel functions...
            if (sender.hasPermission("is.command.kick") && Island.checkIsland(p)) {
                sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getPlugin().getMsg("help_kick"));
            }

        }
        // generate function
        if (sender.hasPermission("is.command.generate")) {
            sender.sendMessage(TextFormat.GREEN + "/" + label + " generate: " + getPlugin().getMsg("help_generate"));
        }
        // This will not using any permission :D
        sender.sendMessage(TextFormat.GREEN + "/" + label + " about: " + getPlugin().getMsg("help_about"));
        return true;
    }

}
