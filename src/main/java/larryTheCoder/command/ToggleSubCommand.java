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
import larryTheCoder.database.purger.IslandData;

/**
 * @author larryTheCoder
 */
public class ToggleSubCommand extends SubCommand{

    public ToggleSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.toggle");
    }

    @Override
    public String getUsage() {
        return "<homes>";
    }

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public String getDescription() {
        return "Toggle every actions on your island";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tgg"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if(args.length < 2){
            sender.sendMessage(TextFormat.GRAY + "Too few parameters Usage: /is tgg help");
            return true;
        }
        switch(args[2]){
            case "?":
            case "help":
                sender.sendMessage("§d--- §aToggle SubCommand §d---");
                sender.sendMessage("§5- §a/is tgg tp §7<boolean>§e: §eEnable teleport to your island");  
                break;
            case "tp":
            case "teleport":
                if(args.length != 3){
                    sender.sendMessage(TextFormat.GRAY + "Too few parameters Usage: /is tgg teleport <true | false>");
                    break;
                }
                if(!args[3].equalsIgnoreCase("true") || !args[3].equalsIgnoreCase("false")){
                    sender.sendMessage(TextFormat.GRAY + "Your parameters is not a boolean");
                    break;
                }
                IslandData pd = getPlugin().getDatabase().getIsland(p.getName());
                switch(args[3].toLowerCase()){
                    case "true":
                        pd.locked = 0;
                        break;
                    case "false":
                        pd.locked = 1;
                        break;
                    default:
                        sender.sendMessage(getPrefix() + TextFormat.GRAY + "Usage: /is tgg teleport <true | false>");
                }
                break;
            case "name":
                
        }
        return true;
    }

}
