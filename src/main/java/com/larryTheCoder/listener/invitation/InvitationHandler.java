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
import cn.nukkit.Server;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;

import java.util.HashMap;

/**
 * This class handle all Invitations and addmember function It will tick every
 * seconds as in config.yml
 *
 * @author larryTheCoder
 */
public class InvitationHandler {

    private final ASkyBlock plugin;
    private final HashMap<Player, Invitation> invitation = new HashMap<>();

    public InvitationHandler(ASkyBlock main) {
        plugin = main;
    }

    public void removeInvitation(Invitation player) {
        Player p = Server.getInstance().getPlayer(player.getReceiver().getName());
        invitation.remove(player, p);
    }

    public ASkyBlock getPlugin() {
        return plugin;
    }

    /**
     * Return all invitations
     *
     * @return HashMap<>
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<Player, Invitation> getInvitations() {
        return invitation;
    }

    public Invitation getInvitation(Player player) {
        Invitation inv = null;
        for (Invitation p : invitation.values()) {
            if (p.getSender() == player) {
                inv = p;
            }
        }
        return inv;
    }

    /**
     * Create a new invitation
     *
     * @param sender
     * @param receiver
     */
    public void addInvitation(Player sender, Player receiver) {
        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(sender).generalSuccess);
        receiver.sendMessage(plugin.getPrefix() + plugin.getLocale(receiver).newInvitation.replace("[player]", sender.getName()));
        invitation.put(sender, new Invitation(this, sender, receiver));
    }

    public void tick() {
        if (Settings.memberTimeOut != -1) {
            invitation.values().forEach(Invitation::tick);
        }

    }
}
