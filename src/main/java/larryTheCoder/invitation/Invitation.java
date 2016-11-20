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
import cn.nukkit.utils.TextFormat;
import larryTheCoder.island.Island;

/**
 * @author larryTheCoder
 */
public class Invitation {
    
    private final InvitationHandler handler;

    private final Player sender;

    private final Player receiver;

    private final Island island;

    private final int time = 30;
    
    /**
     * Invitation constructor.
     *
     * @param member InvitationHandler
     * @param sender Player
     * @param receiver Player
     * @param island Island
     */
    public Invitation(InvitationHandler member,Player sender, Player receiver, Island island){
        this.handler = member;
        this.sender = sender;
        this.receiver = receiver;
        this.island = island;
    }
    
    /**
     * Return invitation sender
     *
     * @return Player
     */
    public Player getSender(){
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
    
    public void accept(){
        Island.addMember(sender, sender.getLocation(), receiver.getName());
    }
    
    public void deny() {
        sender.sendMessage(TextFormat.RED + "* " + TextFormat.YELLOW + "{$this->receiver->getName()} denied your invitation!");
        receiver.sendMessage(TextFormat.RED + "* " + TextFormat.YELLOW + "You denied {$this->sender->getName()}'s invitation!");
    }

    public void expire() {
        sender.sendMessage(TextFormat.RED + " * " + TextFormat.YELLOW + "The invitation to "+ sender.getName() +" expired!");
        handler.removeInvitation(this);
    }
    
    public void tick() {
        if(time <= 0){
            expire();
        }
    }
}
