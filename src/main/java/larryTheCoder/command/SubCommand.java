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

package larryTheCoder.command;

import cn.nukkit.command.CommandSender;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
abstract class SubCommand {

    private ASkyBlock plugin;

    public SubCommand(ASkyBlock plugin){
        this.plugin = plugin;
    }

    /**
     * @return thebigsmileXD\SkyBlock
     */
    public final ASkyBlock getPlugin(){
        return plugin;
    }

    /**
     * @param CommandSender $sender
     * @return boolean
     */
    public abstract boolean canUse(CommandSender $sender);

    /**
     * @return string
     */
    public abstract String getUsage();

    /**
     * @return string
     */
    public abstract String getName();

    /**
     * @return string
     */
    public abstract String getDescription();

    /**
     * @return string[]
     */
    public abstract String[] getAliases();

    /**
     * @param CommandSender $sender
     * @param string[] $args
     * @return bool
     */
    public abstract boolean execute(CommandSender $sender, String[] $args);
}
