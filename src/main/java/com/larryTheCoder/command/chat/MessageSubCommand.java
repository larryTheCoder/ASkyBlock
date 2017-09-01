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
package com.larryTheCoder.command.chat;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;

import java.util.List;

/**
 * The default command of messages
 *
 * @author Adam Matthew
 */
public class MessageSubCommand extends SubCommand {

    public MessageSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.message") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "messages";
    }

    @Override
    public String getDescription() {
        return "Get new messages while you offline";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"msg"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = Server.getInstance().getPlayer(sender.getName());
        List<String> list = getPlugin().getMessages().getMessages(p.getName());
        if (!list.isEmpty()) {
            p.sendMessage(getPlugin().getLocale(p).newsHeadline);
            list.forEach((alist) -> {
                p.sendMessage("- §e" + alist);

            });
            getPlugin().getMessages().clearMessages(p.getName());
        } else {
            p.sendMessage(getPrefix() + getPlugin().getLocale(p).newsEmpty);
        }
        return true;
    }

}
