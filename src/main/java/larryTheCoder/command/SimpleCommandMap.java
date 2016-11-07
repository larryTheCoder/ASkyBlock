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

import cn.nukkit.command.PluginCommand;
import java.util.ArrayList;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class SimpleCommandMap extends PluginCommand {

    private final ASkyBlock plugin;
   
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SimpleCommandMap(ASkyBlock plugin) {
        super("island", plugin);
        setAliases(new String[]{
            "is",
            "skyblock",
            "sky"
        });
        setPermission("is.command");
        setDescription("Claim and manage your islands");
        
         
        this.plugin = plugin;
    }

    public void loadSubCommand(SubCommand cmd){
        
    }

}
