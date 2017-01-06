/*
 * Copyright (C) 2017 larryTheHarry 
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

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class ChallengesCommand extends Command{

    private final ASkyBlock plugin;

    public ChallengesCommand(ASkyBlock ev) {
        super("challenges", "Challange yourself for some big prize", "\u00a77<parameters>", new String[]{"c", "chall", "ch"});
        this.plugin = ev;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(!sender.isPlayer()){
            sender.sendMessage("Run this command in-game");
            return true;
        }
        Player p = plugin.getServer().getPlayer(sender.getName());
        switch(args.length){
            case 0:
                //p.addWindow();
        }
       return true;
    }

}
