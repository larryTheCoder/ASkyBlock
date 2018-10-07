/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
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
 * This event is fired when a player completes a challenge
 *
 * @author larryTheCoder
 * @author tastybento
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
