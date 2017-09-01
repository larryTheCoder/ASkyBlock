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
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.item.Item;

import java.util.List;

/**
 * This event is fired when a player completes a challenge level
 *
 * @author Adam Matthew
 */
public class ChallengeLevelCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final int oldLevel;
    private final int newLevel;
    private final List<Item> rewardedItems;

    /**
     * @param player
     * @param oldLevel
     * @param newLevel
     * @param rewardedItems
     */
    public ChallengeLevelCompleteEvent(Player player, int oldLevel, int newLevel, List<Item> rewardedItems) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.rewardedItems = rewardedItems;
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
     * @return the oldLevel
     */
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * @return the newLevel
     */
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * @return the rewardedItems
     */
    public List<Item> getRewardedItems() {
        return rewardedItems;
    }
}

