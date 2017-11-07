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
import cn.nukkit.Server;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Location;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;

/**
 * @author Adam Matthew
 */
public class ASkyBlockEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final IslandData island;

    /**
     * @param player
     * @param island
     */
    public ASkyBlockEvent(Player player, IslandData island) {
        this.player = player;
        this.island = island;
    }

    public ASkyBlockEvent(Player player) {
        this.player = player;
        this.island = null;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the player involved in this event
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The island involved in the event
     *
     * @return the island
     */
    public IslandData getIsland() {
        return island;
    }

    /**
     * Convenience function to obtain the island's protection size
     *
     * @return the protectionSize
     */
    public int getProtectionSize() {
        return Settings.islandDistance / 2;
    }

    /**
     * Convenience function to obtain the island's locked status
     *
     * @return the isLocked
     */
    public boolean isLocked() {
        return island.isLocked();
    }

    /**
     * Convenience function to obtain the island's distance
     *
     * @return the islandDistance
     */
    public int getIslandDistance() {
        return Settings.islandDistance;
    }

    /**
     * @return the teamLeader
     */
    public Player getTeamLeader() {
        return Server.getInstance().getPlayer(island.getOwner());
    }

    /**
     * Convenience function to obtain the island's owner
     *
     * @return UUID of owner
     */
    public Player getIslandOwner() {
        return Server.getInstance().getPlayer(island.getOwner());
    }

    /**
     * Convenience function to obtain the island's center location
     *
     * @return the island location
     */
    public Location getIslandLocation() {
        return new Location(0, 0, 0, 0, 0, Server.getInstance().getLevelByName(island.getLevelName())).add(island.getCenter());
    }
}
