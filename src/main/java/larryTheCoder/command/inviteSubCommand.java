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
import larryTheCoder.IslandData;
import larryTheCoder.island.Island;

/**
 * @author larryTheCoder
 */
public class inviteSubCommand extends SubCommand {

    public inviteSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.addteam");
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player to be member of your island";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"inv"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length != 2){
            return false;
        }
        Player p = sender.getServer().getPlayer(sender.getName());    
        if(!Island.checkIsland(p)){
            sender.sendMessage(getMsg("no_island_error"));
            return true;
        }
        Player invite = sender.getServer().getPlayer(args[1]);
        if(invite == null){
            sender.sendMessage(getMsg("player_error2"));
            return true;
        }
        IslandData pdinv = ASkyBlock.get().getDatabase().getIsland(invite.getName());
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(sender.getName());
        if(!pdinv.team.isEmpty()){
            sender.sendMessage(getMsg("player_has_teamed").replace("[player]", args[1]));
            return false;
        }
        if(pd.isMember(p.getName())){
            sender.sendMessage(getMsg("player_in_team").replace("[player]", args[1]));
            return false;
        }
        getPlugin().getInvitationHandler().addInvitation(p, invite, pd);
        return true;
    }

}
