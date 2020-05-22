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
package com.larryTheCoder.listener.invitation;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.CoopData;
import com.larryTheCoder.utils.Settings;

/**
 * @author larryTheCoder
 */
public class Invitation {

    private final InvitationHandler handler;
    private final CommandSender sender;
    private final Player receiver;
    private final ASkyBlock plugin;
    private final CoopData coopData;

    private int time;

    /**
     * Invitation constructor.
     *
     * @param handler  The classloader for invite handler
     * @param sender   The sender of this invite
     * @param receiver The receiver of this invite
     */
    Invitation(InvitationHandler handler, CommandSender sender, Player receiver, CoopData pd) {
        this.handler = handler;
        this.sender = sender;
        this.receiver = receiver;
        this.coopData = pd;
        this.time = Settings.memberTimeOut;
        this.plugin = ASkyBlock.get();
    }

    /**
     * Return invitation sender
     *
     * @return CommandSender
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Return invitation receiver
     *
     * @return Player
     */
    Player getReceiver() {
        return receiver;
    }

    public void acceptInvitation() {
        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(sender.isPlayer() ? (Player) sender : null).acceptedTo.replace("[player]", receiver.getName()));
        receiver.sendMessage(plugin.getPrefix() + plugin.getLocale(receiver).acceptedFrom.replace("[player]", sender.getName()));

        coopData.addMember(sender.getName());
        handler.removeInvitation(this);
    }

    public void denyInvitation() {
        sender.sendMessage(plugin.getPrefix() + TextFormat.RED + receiver.getName() + " denied your invitation!");
        receiver.sendMessage(plugin.getPrefix() + TextFormat.RED + "You denied " + sender.getName() + "'s invitation!");

        // Otherwise remove this.
        handler.removeInvitation(this);
    }

    private void expire() {
        receiver.sendMessage(plugin.getPrefix() + TextFormat.RED + "The invitation from " + sender.getName() + " expired!");
        sender.sendMessage(plugin.getPrefix() + TextFormat.RED + "The invitation to " + receiver + " expired!");
    }

    boolean tick() {
        // Fix infinite expire time
        time--;
        if (time <= 0) {
            expire();
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int userData = sender.getName().hashCode();
        int receiverData = sender.getName().hashCode();

        return (userData / receiverData) + super.hashCode();
    }
}
