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
package com.larryTheCoder.command.management;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.player.PlayerData;

/**
 * @author Adam Matthew
 */
public class InviteSubCommand extends SubCommand {

    public InviteSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.invite");
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
        if (args.length != 2) {
            return false;
        }
        Player p = sender.getServer().getPlayer(sender.getName());
        if (!getPlugin().getIsland().checkIsland(p)) {
            sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
            return true;
        }
        Player invite = sender.getServer().getPlayer(args[1]);
        if (invite == null) {
            sender.sendMessage(getPrefix() + getLocale(p).errorOfflinePlayer);
            return true;
        }
        PlayerData pdinv = ASkyBlock.get().getPlayerInfo(invite);
        PlayerData pd = ASkyBlock.get().getPlayerInfo(p);
        if (pdinv.inTeam) {
            sender.sendMessage(getPrefix() + getLocale(p).errorInTeam.replace("[player]", args[1]));
            return false;
        }
        if (pd.members.contains(invite.getName())) {
            sender.sendMessage(getPrefix() + getLocale(p).errorInTeam.replace("[player]", args[1]));
            return false;
        }
        getPlugin().getInvitationHandler().addInvitation(p, invite);
        return true;
    }

}
