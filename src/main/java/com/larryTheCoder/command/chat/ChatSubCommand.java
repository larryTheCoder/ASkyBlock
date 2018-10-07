/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.larryTheCoder.command.chat;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;

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
        return "chat";
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
            sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
            return true;
        }
        if (getPlugin().getTManager().hasTeam(p)) {
            // Check if team members are online
            boolean online = false;
            for (String teamMember : getPlugin().getPlayerInfo(p).members) {
                if (!teamMember.equals(p.getName()) && getPlugin().getServer().getPlayer(teamMember) != null) {
                    online = true;
                }
            }
            if (!online) {
                p.sendMessage(getPrefix() + getLocale(p).teamChatNoTeamAround);
                p.sendMessage(getPrefix() + getLocale(p).teamChatStatusOff);
                getPlugin().getChatHandlers().unSetPlayer(p);
                return true;
            }
            if (getPlugin().getChatHandlers().isTeamChat(p)) {
                // Toggle
                p.sendMessage(getPrefix() + getLocale(p).teamChatStatusOff);
                getPlugin().getChatHandlers().unSetPlayer(p);
            } else {
                p.sendMessage(getPrefix() + getLocale(p).teamChatStatusOn);
                getPlugin().getChatHandlers().setPlayer(p);
            }
        }
        return false;
    }

}
