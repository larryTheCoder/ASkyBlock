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
package com.larryTheCoder.command.generic;

import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.SkyBlockGenerator;
import com.larryTheCoder.command.SubCommand;

/**
 * @author larryTheCoder
 */
public class AGenerateSubCommand extends SubCommand {

    public AGenerateSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.admin");
    }

    @Override
    public String getUsage() {
        return "<level>";
    }

    @Override
    public String getName() {
        return "generate";
    }

    @Override
    public String getDescription() {
        return "create a new Island Level";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"gen", "migrate"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return false;
        }
        if (getPlugin().level.contains(args[1])) {
            sender.sendMessage(getPrefix() + getMsg("generate_error"));
            return true;
        } else if (!getPlugin().getServer().isLevelGenerated(args[1])) {
            getPlugin().getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
            getPlugin().getServer().loadLevel(args[1]);
            getPlugin().level.add(args[1]);
            sender.sendMessage(getPrefix() + getMsg("generate").replace("[level]", args[1]));
            return true;
        }
        sender.sendMessage(getPrefix() + getMsg("generate_error"));
        return true;
    }

}
