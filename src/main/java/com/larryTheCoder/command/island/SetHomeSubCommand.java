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
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.BlockUtil;

/**
 * Author: Adam Matthew
 * <p>
 * SetHomeSubCommand class
 */
public class SetHomeSubCommand extends SubCommand {

    public SetHomeSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.home");
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    public String getDescription() {
        return "Set your island home spawn position";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"shome"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = Server.getInstance().getPlayer(sender.getName());
        IslandData pd = getPlugin().getIslandInfo(p.getLocation());
        // Check if the ground is an air
        if (!BlockUtil.isBreathable(p.clone().add(p.down()).getLevelBlock())) {
            p.sendMessage(getLocale(p).groundNoAir);
            return true;
        }
        // Check if the player on their own island or not
        if (pd != null && pd.getOwner().equalsIgnoreCase(sender.getName())) {
            pd.setHomeLocation(p.getLocation());
            p.sendMessage(getLocale(p).setHomeSuccess);
        } else {
            p.sendMessage(getLocale(p).errorNotOnIsland);
        }
        return true;
    }

}
