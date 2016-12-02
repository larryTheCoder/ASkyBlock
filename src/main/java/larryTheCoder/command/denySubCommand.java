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
import larryTheCoder.invitation.InvitationHandler;

/**
 * @author larryTheCoder
 */
public class denySubCommand extends SubCommand{

    public denySubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.reject") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "deny";
    }

    @Override
    public String getDescription() {
        return "Decline an invitation";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"decline", "unaccept"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        InvitationHandler pd = ASkyBlock.get().getInvitationHandler();
        if(pd.getInvitation(p) == null){
            sender.sendMessage(getMsg("no_pending"));
            return false;
        }
        pd.getInvitation(p).deny();
        return true;
    }

}
