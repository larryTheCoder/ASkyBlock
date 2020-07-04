/*
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
import cn.nukkit.math.Vector2;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.IslandData;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class SkyBlockEvent extends Event {

    @Getter
    private static final HandlerList handlers = new HandlerList();

    /**
     * The island involved in the event.
     */
    @Getter
    private final IslandData island;

    public SkyBlockEvent(IslandData island) {
        this.island = island;
    }


    /**
     * Convenience function to obtain the island's protection size
     *
     * @return the protectionSize
     */
    public int getProtectionSize() {
        return ASkyBlock.get().getSettings(island.getLevelName()).getIslandDistance() / 2;
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
        return ASkyBlock.get().getSettings(island.getLevelName()).getIslandDistance();
    }

    /**
     * Convenience function to obtain the island's owner
     *
     * @return UUID of owner
     */
    public Player getIslandOwner() {
        return Server.getInstance().getPlayer(island.getPlotOwner());
    }

    /**
     * Convenience function to obtain the island's center location
     *
     * @return the island location
     */
    public Location getIslandLocation() {
        Vector2 cartesianPlane = island.getCenter();

        return new Location(cartesianPlane.getFloorX(), 0, cartesianPlane.getFloorY(), Server.getInstance().getLevelByName(island.getLevelName()));
    }

    /**
     * Calls an event statically. This is function is to cut off the amount
     * of code required just to call this event.
     *
     * @param event The SkyBlock event.
     */
    public static void eventCall(SkyBlockEvent event) {
        log.debug("Calling an event...");

        Server.getInstance().getPluginManager().callEvent(event);
    }

    /**
     * Calls an event in which the event is an instanceof {@linkplain cn.nukkit.event.Cancellable} class.
     * As the above, it is just a convenience tool to cut off the amount of code required to call
     * this event.
     *
     * @return {@code true} if the event is cancelled.
     */
    public static boolean eventCancellableCall(SkyBlockEvent event) {
        eventCall(event);

        return event instanceof Cancellable && event.isCancelled();
    }
}
