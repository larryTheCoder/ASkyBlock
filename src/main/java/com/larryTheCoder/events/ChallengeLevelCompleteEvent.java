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
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import cn.nukkit.item.Item;

import java.util.List;

/**
 * This event is fired when a player completes a challenge level
 *
 * @author larryTheCoder
 * @author tastybento
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

