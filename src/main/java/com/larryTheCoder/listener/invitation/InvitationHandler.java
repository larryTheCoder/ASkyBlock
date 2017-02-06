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
import cn.nukkit.Server;
import java.util.HashMap;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;

/**
 * This class handle all Invitations and addmember function It will tick every
 * seconds as in config.yml
 *
 * @api
 * @author larryTheCoder
 */
public class InvitationHandler {

    private HashMap<Player, Invitation> invitation = new HashMap<>();
    private final ASkyBlock plugin;

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
    public void addInvitation(Player sender, Player receiver, PlayerData island) {
        invitation.put(sender, new Invitation(this, sender, receiver, island));
    }

    public void tick() {
        if (plugin.cfg.getInt("island.timeOut") != -1) {
            invitation.values().stream().forEach((inv) -> {
                inv.tick();
            });
        }

    }
}
