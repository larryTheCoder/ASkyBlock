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
import larryTheCoder.ASkyBlock;
import larryTheCoder.island.Island;

/**
 * @author larryTheCoder
 */
public class teleportSubCommand extends SubCommand{

    public teleportSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.teleport") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

    @Override
    public String getName() {
        return "teleport";
    }

    @Override
    public String getDescription() {
        return "teleport to others player Island";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tp"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length != 2){
            return false;
        }
        Player p = getPlugin().getServer().getPlayer(sender.getName());
        Island.teleportPlayer(p, args[1]);
        return true;
    }

}
