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
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class ChatSubCommand extends SubCommand {

    public ChatSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.teamChat") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "teamchat";
    }

    @Override
    public String getDescription() {
        return "Chat with you team";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tc", "teamc"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = Server.getInstance().getPlayer(sender.getName());
        if (getPlugin().getIsland().checkIsland(p)) {
            sender.sendMessage(getMsg("no_island_error"));
            return true;
        }
//        if (plugin.ggetDa.inTeam(playerUUID)) {
//            // Check if team members are online
//            boolean online = false;
//            for (UUID teamMember : plugin.getPlayers().getMembers(playerUUID)) {
//                if (!teamMember.equals(playerUUID) && plugin.getServer().getPlayer(teamMember) != null) {
//                    online = true;
//                }
//            }
//            if (!online) {
//                player.sendMessage(ChatColor.RED + plugin.myLocale(playerUUID).teamChatNoTeamAround);
//                player.sendMessage(ChatColor.GREEN + plugin.myLocale(playerUUID).teamChatStatusOff);
//                plugin.getChatListener().unSetPlayer(playerUUID);
//                return true;
//            }
//            if (plugin.getChatListener().isTeamChat(playerUUID)) {
//                // Toggle
//                player.sendMessage(ChatColor.GREEN + plugin.myLocale(playerUUID).teamChatStatusOff);
//                plugin.getChatListener().unSetPlayer(playerUUID);
//            } else {
//                player.sendMessage(ChatColor.GREEN + plugin.myLocale(playerUUID).teamChatStatusOn);
//                plugin.getChatListener().setPlayer(playerUUID);
//            }
//        } else {
//            player.sendMessage(ChatColor.RED + plugin.myLocale(playerUUID).teamChatNoTeam);
//        }
        return false;
    }

}
