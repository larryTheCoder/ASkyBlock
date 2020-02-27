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
import cn.nukkit.event.Cancellable;
import com.larryTheCoder.storage.IslandData;

/**
 * This event is fired after ASkyBlock calculates an island level but before it is communicated
 * to the player.
 * Use getLevel() to see the level calculated and setLevel() to change it.
 * Canceling this event will result in no change in level.
 * See IslandPostLevelEvent to cancel notifications to the player.
 *
 * @author tastybento
 */
@SuppressWarnings("ALL")
public class IslandPreLevelEvent extends ASkyBlockEvent implements Cancellable {

    private int level;
    private int points;

    /**
     * The main constructor for IslandPreLevel.
     *
     * @param player The player
     * @param island The player's island data
     * @param level  The level that been calculated
     * @param points The points that the player received
     */
    public IslandPreLevelEvent(Player player, IslandData island, int level, int points) {
        super(player, island);
        this.level = level;
        this.points = points;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the number of points
     */
    public int getPointsToNextLevel() {
        return points;
    }

    /**
     * Set the number of blocks the player requires to reach the next level.
     * If this is set to a negative number, the player will not be informed of
     * how many points they need to reach the next level.
     *
     * @param points The point to be set
     */
    public void setPointsToNextLevel(int points) {
        this.points = points;

    }
}

