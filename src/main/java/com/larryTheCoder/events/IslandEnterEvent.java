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
import cn.nukkit.level.Location;
import com.larryTheCoder.storage.IslandData;

/**
 * Fired when a player enters an island's area
 *
 * @author Adam Matthew
 */
public class IslandEnterEvent extends ASkyBlockEvent {

    private final Location location;

    /**
     * Called to create the event
     *
     * @param player
     * @param island - island the player is entering
     * @param loc    - Location of where the player entered the island or tried to enter
     */
    public IslandEnterEvent(Player player, IslandData island, Location loc) {
        super(player, island);
        this.location = loc;
    }

    /**
     * Location of where the player entered the island or tried to enter
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }
}
