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
import java.util.ArrayList;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class ChatHandler {

    private final ASkyBlock plugin;
    private ArrayList<Chat> chats = new ArrayList<>();

    public ChatHandler(ASkyBlock plugin) {
        this.plugin = plugin;
    }
    
    public boolean isInChat(Player player) {
        for(Chat ch : chats){
            chats.toArray().equals(player);
            return true;
        }
        return false;
    }
}
