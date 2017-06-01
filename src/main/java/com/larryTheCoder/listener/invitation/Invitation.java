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
package com.larryTheCoder.listener.invitation;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Settings;

/**
 * @author larryTheCoder
 */
public class Invitation {

    private final InvitationHandler handler;

    private final Player sender;

    private final Player receiver;

    private final PlayerData island;

    private final int time;

    /**
     * Invitation constructor.
     *
     * @param member InvitationHandler
     * @param sender Player
     * @param receiver Player
     * @param island Island
     */
    public Invitation(InvitationHandler member, Player sender, Player receiver, PlayerData island) {
        this.handler = member;
        this.sender = sender;
        this.receiver = receiver;
        this.island = island;
        this.time = Settings.memberTimeOut;
    }

    /**
     * Return invitation sender
     *
     * @return Player
     */
    public Player getSender() {
        return sender;
    }

    /**
     * Return invitation receiver
     *
     * @return Player
     */
    public Player getReceiver() {
        return receiver;
    }

    public void accept() {
        handler.getPlugin().getTManager().addTeam(sender, receiver);
    }

    public void deny() {
        sender.sendMessage(this.handler.getPlugin().getPrefix() + TextFormat.YELLOW + this.receiver.getName() + " denied your invitation!");
        receiver.sendMessage(this.handler.getPlugin().getPrefix() + TextFormat.YELLOW + "You denied " + this.sender.getName() + "'s invitation!");
    }

    public void expire() {
        sender.sendMessage(this.handler.getPlugin().getPrefix() + TextFormat.YELLOW + "The invitation to " + sender.getName() + " expired!");
        handler.removeInvitation(this);
    }

    public void tick() {
        if (time <= 0) {
            expire();
        }
    }
}
