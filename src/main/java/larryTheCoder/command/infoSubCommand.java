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
import java.util.ArrayList;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class infoSubCommand extends SubCommand{

    public infoSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.info") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "get the Island information";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"inf", "get"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p =  sender.getServer().getPlayer(sender.getName());
        for(String level : getPlugin().level){
            if(!p.getLevel().getName().equalsIgnoreCase(level)){
                sender.sendMessage(getMsg("level_error"));
                return true;
            }
        }
        getPlugin().getIsland().islandInfo(p, p.getLocation());
        return true;
    }

}
