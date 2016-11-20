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
package larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import larryTheCoder.ASkyBlock;
import larryTheCoder.Utils;
import larryTheCoder.island.Island;

/**
 * @author larryTheCoder
 */
public class HelpSubCommand extends SubCommand {

    public HelpSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.help");
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "list all of commands";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
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
