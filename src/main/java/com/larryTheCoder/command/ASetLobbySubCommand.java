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

package com.larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import com.larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class ASetLobbySubCommand extends SubCommand{

    public ASetLobbySubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.admin") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "setlobby";
    }

    @Override
    public String getDescription() {
        return "set the ASkyBlock main lobby";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = getPlugin().getServer().getPlayer(sender.getName());
        Location loc = p.getLocation();
        getPlugin().cfg.set("lobby.lobbyX", loc.getFloorX());
        getPlugin().cfg.set("lobby.lobbyY", loc.getFloorY());
        getPlugin().cfg.set("lobby.lobbyZ", loc.getFloorZ());
        getPlugin().cfg.set("lobby.world", p.getLevel().getName());
        getPlugin().cfg.save();
        return true;
    }

}
