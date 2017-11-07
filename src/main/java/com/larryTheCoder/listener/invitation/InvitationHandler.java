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
 * @author Adam Matthew
 * @api
 */
public class InvitationHandler {

    private final ASkyBlock plugin;
    private HashMap<Player, Invitation> invitation = new HashMap<>();

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
     * @param island
     */
    public void addInvitation(Player sender, Player receiver) {
        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(sender).generalSuccess);
        receiver.sendMessage(plugin.getPrefix() + plugin.getLocale(receiver).newInvitation.replace("[player]", sender.getName()));
        invitation.put(sender, new Invitation(this, sender, receiver));
    }

    public void tick() {
        if (Settings.memberTimeOut != -1) {
            invitation.values().stream().forEach((inv) -> {
                inv.tick();
            });
        }

    }
}
