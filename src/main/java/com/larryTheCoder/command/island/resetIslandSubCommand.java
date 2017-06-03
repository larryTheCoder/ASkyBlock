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
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;

/**
 * @author larryTheCoder
 */
public class ResetIslandSubCommand extends SubCommand {

    public ResetIslandSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.reset") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "<homes>";
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Refresh your island where you standing at";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"refresh", "clean"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length != 2){
            return false;
        }
        Player p = getPlugin().getServer().getPlayer(sender.getName());
        if (getPlugin().getIsland().isPlayerIsland(p, p.getLocation())) {
            sender.sendMessage(getPrefix() + getMsg(p).errorNoIsland);
            return true;
        }
        getPlugin().getIsland().reset(p, true, getPlugin().getIslandInfo(p.getLocation()));
        return true;
    }

}
