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
package com.larryTheCoder.command.generic;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Position;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;

/**
 * @author larryTheCoder
 */
public class leaveSubCommand extends SubCommand {

    public leaveSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.leave") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "return to main world";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"lobby", "exit"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player pt = getPlugin().getServer().getPlayer(sender.getName());
        for (String level : getPlugin().level) {
            if (!pt.getLevel().getName().equalsIgnoreCase(level)) {
                sender.sendMessage(getMsg("leave_error"));
                return true;
            }
        }
        // Check if sender is in gamemode 1
        if(!pt.isOp()){
            if(pt.getGamemode() == 1){
                pt.setGamemode(0);
            }
        }
        getPlugin().getInventory().loadPlayerInventory(pt);
        int x = getPlugin().cfg.getInt("lobby.lobbyX");
        int y = getPlugin().cfg.getInt("lobby.lobbyY");
        int z = getPlugin().cfg.getInt("lobby.lobbyZ");
        String world = getPlugin().cfg.getString("lobby.world");
        pt.teleport(new Position(x, y, z, getPlugin().getServer().getLevelByName(world)));
        return true;
    }

}
