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

package larryTheCoder.events;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Location;
import java.util.UUID;
import larryTheCoder.IslandData;
import larryTheCoder.Settings;

/**
 * @author larryTheCoder
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

    /**
     * Gets the player involved in this event
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The island involved in the event
     * @return the island
     */
    public IslandData getIsland() {
        return island;
    }

    /**
     * Convenience function to obtain the island's protection size
     * @return the protectionSize
     */
    public int getProtectionSize() {
        return Settings.islandSize / 2;
    }

    /**
     * Convenience function to obtain the island's locked status
     * @return the isLocked
     */
    public boolean isLocked() {
        return island.locked.equalsIgnoreCase("true");
    }

    /**
     * Convenience function to obtain the island's distance
     * @return the islandDistance
     */
    public int getIslandDistance() {
        return Settings.islandSize;
    }

    /**
     * @return the teamLeader
     */
    public Player getTeamLeader() {
        return Server.getInstance().getPlayer(island.owner);
    }

    /**
     * Convenience function to obtain the island's owner
     * @return UUID of owner
     */
    public Player getIslandOwner() {
        return Server.getInstance().getPlayer(island.owner);
    }

    /**
     * Convenience function to obtain the island's center location
     * @return the island location
     */
    public Location getIslandLocation() {
        return new Location(island.X, island.floor_y, island.Z, 0, 0, Server.getInstance().getLevelByName(island.levelName));
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
