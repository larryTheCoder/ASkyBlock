/*
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

package com.larryTheCoder.command.category;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.listener.invitation.Invitation;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.player.TeamManager;

import java.util.Arrays;
import java.util.List;

public class AdminCategory extends SubCategory {

    public AdminCategory(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public List<String> getCommands() {
        return Arrays.asList("accept", "deny", "reject", "invite", "kick", "quit");
    }

    @Override
    public boolean canUse(CommandSender sender, String command) {
        switch (command.toLowerCase()) {
            case "accept":
                return sender.hasPermission("is.command.accept") && sender.isPlayer();
            case "deny":
            case "reject":
                return sender.hasPermission("is.command.reject") && sender.isPlayer();
            case "invite":
                return sender.hasPermission("is.command.invite") && sender.isPlayer();
            case "kickmember":
                return sender.hasPermission("is.command.kick") && sender.isPlayer();
            case "quit":
                return sender.hasPermission("is.command.quit") && sender.isPlayer();
        }
        return false;
    }

    @Override
    public String getDescription(String command) {
        switch (command.toLowerCase()) {
            case "accept":
                return "Accept an invitation from a player.";
            case "reject":
                return "Denies an invitation from a player.";
            case "invite":
                return "Invite a player to be as your island member.";
            case "kickmember":
                return "Kick a member from being your island member.";
            case "quit":
                return "Leave from being an island member.";
        }
        return null;
    }

    @Override
    public String getParameters(String commandName) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        InvitationHandler pd = getPlugin().getInvitationHandler();

        switch (args[0].toLowerCase()) {
            case "accept":
                Invitation invite = pd.getInvitation(p);
                if (invite == null) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorNotPending);
                    break;
                }

                if (args.length == 2) {
                    invite = pd.getInvitation(p, args[1]);

                    if (invite == null) {
                        sender.sendMessage(getPrefix() + getLocale(p).errorNotPending2.replace("[player]", args[1]));
                        break;
                    }
                }

                invite.acceptInvitation();
                break;
            case "deny":
            case "reject":
                invite = pd.getInvitation(p);
                if (invite == null) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorNotPending);
                    break;
                }

                if (args.length == 2) {
                    invite = pd.getInvitation(p, args[1]);

                    if (invite == null) {
                        sender.sendMessage(getPrefix() + getLocale(p).errorNotPending2.replace("[player]", args[1]));
                        break;
                    }
                }

                invite.denyInvitation();
                break;
            case "locale":
                if (args.length != 2) {
                    break;
                }

                // Player cannot invite other players when he have no island
                if (!getPlugin().getIslandManager().checkIsland(p)) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
                    break;
                }

                // The invite player.
                Player inviter = sender.getServer().getPlayer(args[1]);
                if (inviter == null) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorOfflinePlayer);
                    break;
                }

                // This checks either the player already set into a team
                // or not.
                TeamManager manager = getPlugin().getTManager();
                if (manager.hasTeam(inviter.getName())) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorInTeam.replace("[player]", args[1]));
                    break;
                }

                // Now we add the invitation into list.
                getPlugin().getInvitationHandler().addInvitation(p, inviter);
        }
    }
}
