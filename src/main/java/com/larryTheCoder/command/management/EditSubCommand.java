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
package com.larryTheCoder.command.management;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;

/**
 * @author Adam Matthew
 */
public class EditSubCommand extends SubCommand {

    public EditSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.edit");
    }

    @Override
    public String getUsage() {
        return "<args>";
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit your island information";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"edit"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Too few parameters Usage: /is edit help");
            return true;
        }
        if (!getPlugin().getAPI(getPlugin()).getIsland().isPlayerIsland(p, p.getLocation())) {
            sender.sendMessage(TextFormat.RED + "You are not in your island");
            return true;
        }
        IslandData pd = getPlugin().getAPI(ASkyBlock.get()).getIslandInfo(p.getLocation());
        switch (args[1]) {
            case "?":
            case "help":
                sender.sendMessage("§d--- §aToggle SubCommand §d---");
                sender.sendMessage("§5- §a/is edit tp §7<true|false>§e: §eEnable teleport to your island");
                sender.sendMessage("§5- §a/is edit name §7<string>§e: §eRename your island");
                break;
            case "tp":
            case "teleport":
                if (args.length != 3) {
                    sender.sendMessage(getPrefix() + TextFormat.RED + "Too few parameters Usage: /is edit teleport <true | false>");
                    break;
                }
                if (!args[2].equalsIgnoreCase("true") || !args[3].equalsIgnoreCase("false")) {
                    sender.sendMessage(getPrefix() + TextFormat.RED + "Your parameters is not a boolean");
                    break;
                }
                switch (args[2].toLowerCase()) {
                    case "true":
                        pd.locked = true;
                        sender.sendMessage(getPrefix() + TextFormat.GREEN + "Players will be able to teleport to your island.");
                        break;
                    case "false":
                        pd.locked = false;
                        sender.sendMessage(getPrefix() + TextFormat.GREEN + "Players will not be able to teleport to your island.");
                        break;
                    default:
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Usage: /is edit teleport <true | false>");
                }
                break;
            case "name":
                if (args.length != 3) {
                    sender.sendMessage(getPrefix() + TextFormat.RED + "Too few parameters Usage: /is edit teleport <true | false>");
                    break;
                }
                if (args[2].length() > Settings.islandMaxNameLong) {
                    if (Settings.islandMaxNameLong != -1) {
                        sender.sendMessage(getPrefix() + TextFormat.RED + "Whoah! What a long name is that! Maximum length is " + Settings.islandMaxNameLong);
                        break;
                    }
                }
                pd.name = args[2];
                sender.sendMessage(getPrefix() + TextFormat.GREEN + "Renamed your island name into " + args[2]);
                break;
        }
        getPlugin().getAPI(ASkyBlock.get()).getDatabase().saveIsland(pd);
        return true;
    }
}
