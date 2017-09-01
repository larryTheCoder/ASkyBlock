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
 * This event is fired when a player completes a challenge
 */
public class ChallengeCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String challengeName;
    private final String[] permList;
    private final String[] itemRewards;
    private final double moneyReward;
    private final int expReward;
    private final String rewardText;
    private final List<Item> rewardedItems;

    /**
     * @param player
     * @param challengeName
     * @param permList
     * @param itemRewards
     * @param moneyReward
     * @param expReward
     * @param rewardText
     * @param rewardedItems
     */
    public ChallengeCompleteEvent(Player player, String challengeName, String[] permList, String[] itemRewards, double moneyReward, int expReward,
                                  String rewardText, List<Item> rewardedItems) {
        this.player = player;
        this.challengeName = challengeName;
        this.permList = permList;
        this.itemRewards = itemRewards;
        this.moneyReward = moneyReward;
        this.expReward = expReward;
        this.rewardText = rewardText;
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
     * @return the challengeName
     */
    public String getChallengeName() {
        return challengeName;
    }

    /**
     * @return the permList
     */
    public String[] getPermList() {
        return permList;
    }

    /**
     * @return the itemRewards
     */
    public String[] getItemRewards() {
        return itemRewards;
    }

    /**
     * @return the moneyReward
     */
    public double getMoneyReward() {
        return moneyReward;
    }

    /**
     * @return the expReward
     */
    public int getExpReward() {
        return expReward;
    }

    /**
     * @return the rewardText
     */
    public String getRewardText() {
        return rewardText;
    }

    /**
     * @return the rewardedItems
     */
    public List<Item> getRewardedItems() {
        return rewardedItems;
    }
}
