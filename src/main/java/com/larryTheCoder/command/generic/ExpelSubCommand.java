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

package com.larryTheCoder.command.generic;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;

/**
 * @author Adam Matthew
 */
public class ExpelSubCommand extends SubCommand {

    public ExpelSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.expel") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

    @Override
    public String getName() {
        return "expel";
    }

    @Override
    public String getDescription() {
        return "Kick the intruders from your island!";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = getPlugin().getServer().getPlayer(sender.getName());
        if (getPlugin().getIsland().checkIsland(p)) {
            sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
            return true;
        } else if (args.length != 2) {
            return false;
        }
        if (getPlugin().getServer().getPlayer(args[1]) == null) {
            sender.sendMessage(getPrefix() + getLocale(p).errorOfflinePlayer);
            return true;
        }
        getPlugin().getIsland().kickPlayerByName(p, args[1]);
        return true;
    }

}
