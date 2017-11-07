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
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Location;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;

/**
 * @author Adam Matthew
 */
public class IslandCreateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int schematicId;
    private final IslandData island;

    /**
     * @param player
     * @param schematicId
     * @param island
     */
    public IslandCreateEvent(Player player, int schematicId, IslandData island) {
        this.player = player;
        this.schematicId = schematicId;
        this.island = island;
    }

    public static HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the schematicName
     */
    public int getSchematicId() {
        return schematicId;
    }

    /**
     * @return the island
     */
    public Location getIslandLocation() {
        return new Location(0, 0, 0, 0, 0, Server.getInstance().getLevelByName(island.getLevelName())).add(island.getCenter());
    }

    /**
     * @return the protectionSize
     */
    public int getProtectionSize() {
        return island.getProtectionSize();
    }

    /**
     * @return the isLocked
     */
    public boolean isLocked() {
        return island.isLocked();
    }

    /**
     * @return the islandDistance
     */
    public int getIslandDistance() {
        return Settings.islandDistance;
    }
}
