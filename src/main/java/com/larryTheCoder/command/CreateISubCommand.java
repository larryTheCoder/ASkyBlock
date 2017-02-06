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
import cn.nukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.schematic.Schematic;
import com.larryTheCoder.utils.Utils;

/**
 * @author larryTheCoder
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
            case 1:
                name = args[1];
                break;
            case 2:
                name = args[1];
                if (getPlugin().getSchematic(args[2]) != null) {
                    List<String> ft = new ArrayList<>();
                    ASkyBlock.schematics.keySet().stream().forEach((de) -> {
                        ft.add(de);
                    });
                    p.sendMessage(getMsg("schematics_list").replace("[SCEM]", Utils.arrayToString(ft)));
                    return true;
                }
                break;
        }
        int maxIslands = getPlugin().cfg.getInt("maxhome");
        List<IslandData> maxPlotsOfPlayers = getPlugin().getDatabase().getIslands(p.getName());
        if (maxIslands >= 0 && maxPlotsOfPlayers.size() >= maxIslands) {
            sender.sendMessage(getPlugin().getMsg("max_islands").replace("[maxplot]", "" + maxIslands));
            return true;
        }
        getPlugin().getIsland().createIsland(p, smt, name);
        return true;
    }

}
