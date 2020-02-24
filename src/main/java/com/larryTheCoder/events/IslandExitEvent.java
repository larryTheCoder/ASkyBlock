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
import cn.nukkit.level.Location;
import com.larryTheCoder.storage.IslandData;

/**
 * Fired when a player exits an island's protected area
 *
 * @author larryTheCoder
 * @author tastybento
 */
public class IslandExitEvent extends ASkyBlockEvent {
    private final Location location;

    /**
     * @param player
     * @param island   that the player is leaving
     * @param location - Location of where the player exited the island's protected area
     */
    public IslandExitEvent(Player player, IslandData island, Location location) {
        super(player, island);
        this.location = location;
    }

    /**
     * Location of where the player exited the island's protected area
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

}
