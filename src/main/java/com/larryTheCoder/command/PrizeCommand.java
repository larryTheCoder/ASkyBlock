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
 *
 */
package com.larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;

public class PrizeCommand extends Command {

    private ASkyBlock plugin;
    private boolean usePanelBETA = false;

    public PrizeCommand(ASkyBlock plugin) {
        super("challenges", "Challange yourself for some big prize", "<parameters>", new String[]{"c", "ch"});
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        Player p = sender.isPlayer() ? sender.getServer().getPlayer(sender.getName()) : null;
        if (p == null) {
            sender.sendMessage(plugin.getLocale(p).errorUseInGame);
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(plugin.getPrefix() + "§cToo few parameters, /c help for a list of commands");
            return true;
        }

        if (usePanelBETA) {
            // Todo
        }
        // TODO: Use Panel FOR 1.2 MCPE
        return true;
    }
}
