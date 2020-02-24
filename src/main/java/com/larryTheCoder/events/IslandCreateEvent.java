/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.larryTheCoder.events;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Location;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;

/**
 * @author larryTheCoder
 * @author tastybento
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
        return ASkyBlock.get().getSettings(island.getLevelName()).getIslandDistance();
    }
}
