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
import java.util.ArrayList;
import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;

/**
 * @author larryTheCoder
 */
public class CreateISubCommand extends SubCommand{

    public CreateISubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.create") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        String concat = "";
        if(getPlugin().cfg.getInt("maxHomes") != 0){
            concat = "[1 - " + getPlugin().cfg.getInt("maxHomes") + "]";
        }
        return concat;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new Fresh island!";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p =  sender.getServer().getPlayer(sender.getName());
        if(args.length > 2){
            return false;
        }
        int maxIslands = getPlugin().cfg.getInt("maxhome");
        ArrayList<IslandData> maxPlotsOfPlayers = getPlugin().getDatabase().getIslands(p.getName());
        if (maxIslands >= 0 && maxPlotsOfPlayers.size() >= maxIslands) {
            sender.sendMessage(getPlugin().getMsg("max_islands"));
            return true;
        }
        return true;
    }

}
