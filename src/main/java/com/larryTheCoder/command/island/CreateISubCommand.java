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
package com.larryTheCoder.command.island;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.schematic.Schematic;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Matthew
 */
public class CreateISubCommand extends SubCommand {

    public CreateISubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.create") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "<Island name> <Schematic>";
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new island!";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"c", "crte"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        Schematic smt = null;
        String name = "My Island";
        switch (args.length) {
            case 2:
                name = args[1];
                break;
            case 3:
                name = args[1];
                if (getPlugin().getAPI(ASkyBlock.get()).getSchematic(args[2]) == null) {
                    p.sendMessage(getPrefix() + "Unknown schematic. Use /is listsc to list all of available schematics");
                    return true;
                } else {
                    smt = getPlugin().getAPI(ASkyBlock.get()).getSchematic(args[2]);
                }
                break;
        }
        List<IslandData> maxPlotsOfPlayers = getPlugin().getAPI(ASkyBlock.get()).getDatabase().getIslands(sender.getName(), getPlugin().getAPI(getPlugin()).getDefaultWorld(p));
        if (Settings.maxHome >= 0 && maxPlotsOfPlayers.size() >= Settings.maxHome) {
            sender.sendMessage(getPrefix() + getLocale(p).errorMaxIsland.replace("[maxplot]", "" + Settings.maxHome));
            return true;
        }
        getPlugin().getAPI(getPlugin()).getIsland().createIsland(p, smt, name);
        return true;
    }

}
