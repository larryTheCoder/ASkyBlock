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
package com.larryTheCoder.command.island;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Utils;
import java.util.List;

/**
 * @author larryTheCoder
 */
public class HomeSubCommand extends SubCommand {

    public HomeSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.home");
    }

    @Override
    public String getUsage() {
        return "<island number>";
    }

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getDescription() {
        return "Teleport to your island";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"h"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int islandNumber;
        if (args.length != 2) {
            islandNumber = 1;
        } else if (Utils.isNumeric(args[1]) && Integer.getInteger(args[1]) == 1) {
            islandNumber = Integer.getInteger(args[1]);
        } else {
            return false;
        }
        Player p = sender.getServer().getPlayer(sender.getName());
        List<IslandData> island = getPlugin().getDatabase().getIslands(sender.getName());
        if (island == null) {
            sender.sendMessage(getPrefix() + getMsg(p).errorNoIsland);
            return true;
        }
        if (island.size() != islandNumber) {
            sender.sendMessage(getPrefix() +TextFormat.RED + "You don't have an island with home number " + islandNumber);
            return true;
        }
        getPlugin().getGrid().homeTeleport(p, islandNumber);
        return true;
    }

}
