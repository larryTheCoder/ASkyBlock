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

package com.larryTheCoder.events;

import cn.nukkit.Player;
import com.larryTheCoder.storage.IslandData;

/**
 * @author Adam Matthew
 */
public class IslandJoinEvent extends ASkyBlockEvent {
    /**
     * @param player
     * @param island
     */
    public IslandJoinEvent(Player player, IslandData island) {
        super(player, island);
    }
}
