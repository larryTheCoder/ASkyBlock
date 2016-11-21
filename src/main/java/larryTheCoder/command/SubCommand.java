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

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public abstract class SubCommand {

    private final ASkyBlock plugin;

    public SubCommand(ASkyBlock plugin){
        if(plugin == null){
            Server.getInstance().getLogger().error("plugin cant be null");
        }
        this.plugin = plugin;
    }

    /**
     * @return larryTheCoder\ASkyBlock
     */
    public ASkyBlock getPlugin(){
        return plugin;
    }

    public String getMsg(String key){
        return plugin.getMsg(key);
    }
    
    public String getPrefix(){
        return plugin.getPrefix();
    }
    /**
     * @param sender CommandSender
     * @return boolean
     */
    public abstract boolean canUse(CommandSender sender);

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
     * @param sender the sender      - CommandSender
     * @param args The arrugements      - String[]
     * @return true if true
     */
    
    public abstract boolean execute(CommandSender sender ,String[] args);
}
