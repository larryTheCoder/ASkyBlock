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
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.CoopData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Handles all the invites for the server.
 *
 * @author larryTheCoder
 */
public class InvitationHandler {

    private final ASkyBlock plugin;
    private final List<Invitation> invitation = new ArrayList<>();

    public InvitationHandler(ASkyBlock plugin) {
        this.plugin = plugin;
        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, () -> {
            if (invitation.isEmpty()) {
                return;
            }
            Queue<Invitation> removeQuery = new ArrayDeque<>(invitation.size());
            // We can't remove the invite while in forEach
            // We store it in Queue and then stores them in it
            // After we done dealing with it.
            invitation.forEach(invite -> {
                if (!invite.tick()) {
                    removeQuery.add(invite);
                }
            });

            // Deal with these things.
            while (!removeQuery.isEmpty()) {
                removeInvitation(removeQuery.poll());
            }
        }, 20);
    }

    /**
     * Removes an invite from a invite.
     *
     * @param invite The invitation class.
     */
    void removeInvitation(Invitation invite) {
        invitation.remove(invite);
    }

    /**
     * Gets an invite by the receiver.
     *
     * @param player The receiver of the invite
     * @return The invite class itself, otherwise null.
     */
    public Invitation getInvitation(Player player) {
        return getInvitation(player, "");
    }

    /**
     * Get an invites given by a specific player towards
     * the target player.
     *
     * @param player The receiver of the invite
     * @return The invite class itself, otherwise null.
     */
    public Invitation getInvitation(Player player, String playerName) {
        return invitation.stream()
                .filter(invite -> invite.getReceiver().getName().equalsIgnoreCase(playerName))
                .filter(invite -> invite.getSender().equals(player))
                .findFirst()
                .orElse(null);
    }

    /**
     * Create a new invitation
     *
     * @param sender   The sender itself
     * @param receiver The receiver of the invitation
     */
    public void addInvitation(CommandSender sender, Player receiver, CoopData pd) {
        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(sender.isPlayer() ? (Player) sender : null).inviteSuccess);
        receiver.sendMessage(plugin.getPrefix() + plugin.getLocale(receiver).newInvitation.replace("[player]", sender.getName()));

        // Add into the list.
        invitation.add(new Invitation(this, sender, receiver, pd));
    }
}