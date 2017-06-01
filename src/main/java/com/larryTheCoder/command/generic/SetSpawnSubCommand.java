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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;

/**
 * @author larryTheCoder
 */
public class SetSpawnSubCommand extends SubCommand {

    public SetSpawnSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.admin.setspawn") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getName() {
        return "setspawn";
    }

    @Override
    public String getDescription() {
        return "Set the island world spawn";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = getPlugin().getServer().getPlayer(sender.getName());
        if (getPlugin().getIslandInfo(p) == null) {
            p.sendMessage(getPrefix() + TextFormat.RED + "You need to create an island first!");
            return true;
        } else if (!p.getLevel().getName().equalsIgnoreCase("SkyBlock")) {
            p.sendMessage(getPrefix() + TextFormat.RED + "You must be inside the island world.");
            return true;
        } else if(!getPlugin().getIslandInfo(p.getLocation()).owner.equalsIgnoreCase(p.getName())){
            p.sendMessage(getPrefix() + TextFormat.RED + "You must standing on your OWN island");
            return true;            
        }
        // To avoid multiple spawns, try to remove the old spawn
        if(getPlugin().getDatabase().getSpawn() != null){
            IslandData pd = getPlugin().getDatabase().getSpawn();
            pd.setSpawn(false);
            getPlugin().getDatabase().saveIsland(pd);
        }
        // Save this island
        IslandData pd = getPlugin().getIslandInfo(p.getLocation());
        pd.setSpawn(true);
        getPlugin().getDatabase().saveIsland(pd);
        sender.sendMessage(TextFormat.GREEN + "Seccess! ");
        return true;
    }

}
