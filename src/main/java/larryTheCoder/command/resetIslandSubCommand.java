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
public class resetIslandSubCommand extends SubCommand {

    public resetIslandSubCommand(ASkyBlock plugin) {
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
        return "Refresh your island";
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
        if (Island.checkIsland(p)) {
            sender.sendMessage(getPrefix() + getMsg("no_island_error"));
            return true;
        }
        if(Utils.getInt(args[1]) == -1){
            sender.sendMessage(getPrefix() + TextFormat.RED + "You dont have homes that LOT!");
            return true;
        }
        Island.reset(p, true, Utils.getInt(args[1]));
        return true;
    }

}
