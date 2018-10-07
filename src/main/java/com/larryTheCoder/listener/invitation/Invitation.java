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
package com.larryTheCoder.listener.invitation;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;

/**
 * @author larryTheCoder
 */
public class Invitation {

    private final InvitationHandler handler;
    private final Player sender;
    private final Player receiver;
    private final ASkyBlock plugin;

    private final int time;

    /**
     * Invitation constructor.
     *
     * @param member   InvitationHandler
     * @param sender   Player
     * @param receiver Player
     */
    public Invitation(InvitationHandler member, Player sender, Player receiver) {
        this.handler = member;
        this.sender = sender;
        this.receiver = receiver;
        this.time = Settings.memberTimeOut;
        this.plugin = handler.getPlugin();
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
        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(sender).acceptedTo.replace("[player]", receiver.getName()));
        receiver.sendMessage(plugin.getPrefix() + plugin.getLocale(receiver).acceptedFrom.replace("[player]", sender.getName()));
        plugin.getTManager().addTeam(sender, receiver);
    }

    public void deny() {
        sender.sendMessage(plugin.getPrefix() + TextFormat.YELLOW + receiver.getName() + " denied your invitation!");
        receiver.sendMessage(plugin.getPrefix() + TextFormat.YELLOW + "You denied " + sender.getName() + "'s invitation!");
    }

    private void expire() {
        sender.sendMessage(plugin.getPrefix() + TextFormat.YELLOW + "The invitation to " + sender.getName() + " expired!");
        handler.removeInvitation(this);
    }

    public void tick() {
        if (time <= 0) {
            expire();
        }
    }
}
