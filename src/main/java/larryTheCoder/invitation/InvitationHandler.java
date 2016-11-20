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

package larryTheCoder.invitation;

import cn.nukkit.Player;
import cn.nukkit.Server;
import java.util.Map;
import larryTheCoder.ASkyBlock;
import larryTheCoder.island.Island;

/**
 * @author larryTheCoder
 * 
 * WARNING: NOT SURE THIS WILL WORKS! DO NOT USE IT
 */
public class InvitationHandler {

    private Map<Invitation, Player> invitation;
    private final ASkyBlock plugin;
    
    public InvitationHandler(ASkyBlock main){
        plugin = main;
    }
    
    public void removeInvitation(Invitation player) {
        Player p = Server.getInstance().getPlayer(player.getReceiver().getName());
        invitation.remove(player, p);
    }
    
    public ASkyBlock getPlugin(){
        return plugin;
    }
    
    /**
     * Return all invitations
     *
     * @return Invitation[]
     */
    public Invitation getInvitations() {
        return (Invitation) invitation;
    }

    public String getInvitation(Player player) {
        for(Player p : invitation.values()){
            if(p.getName().equalsIgnoreCase(player.getName())){
                return player.getName();
            }
        }
        return null;
    }
    
    /**
     * Create a new invitation
     * 
     * @param sender
     * @param receiver
     * @param island
     */
    public void addInvitation(Player sender, Player receiver, Island island) {
        invitation.put(new Invitation(this, sender, receiver, island), sender);
    }
    
    public void tick(){
        invitation.keySet().stream().forEach((tick) -> {
            tick.tick();
        });
    }
}
