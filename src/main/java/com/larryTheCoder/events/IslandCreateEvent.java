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
package com.larryTheCoder.events;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Location;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.schematic.Schematic;

/**
 * @author larryTheCoder
 */
public class IslandCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Schematic schematic;
    private final IslandData island;

    /**
     * @param player
     * @param schematic
     * @param island
     */
    public IslandCreateEvent(Player player, Schematic schematic, IslandData island) {
        this.player = player;
        this.schematic = schematic;
        this.island = island;
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
    public Schematic getSchematicName() {
        return schematic;
    }

    /**
     * @return the island
     */
    public Location getIslandLocation() {
        return new Location(island.X, island.floor_y, island.Z, 0, 0, Server.getInstance().getLevelByName(island.levelName));
    }

    /**
     * @return the protectionSize
     */
    public int getProtectionSize() {
        return Settings.islandSize / 2;
    }

    /**
     * @return the isLocked
     */
    public boolean isLocked() {
        return island.locked;
    }

    /**
     * @return the islandDistance
     */
    public int getIslandDistance() {
        return Settings.islandSize;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
