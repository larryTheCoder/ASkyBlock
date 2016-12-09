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

package larryTheCoder.chat;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;
import larryTheCoder.ASkyBlock;

/** 
 * @author larryTheCoder
 */
public class ChatFormatListener implements Listener {

    public final ASkyBlock plugin;

    public ChatFormatListener(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent e){
        Player p = e.getPlayer();
        if(!p.hasPermission("is.format") && e.getMessage().contains("&")){
            e.setCancelled(true);
            p.sendMessage(TextFormat.RED + "Insuffiction permission");
        } else {
            e.setMessage(e.getMessage().replaceAll("&", "§"));
        }
    }
}
