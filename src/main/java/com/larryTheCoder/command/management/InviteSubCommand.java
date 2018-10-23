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
package com.larryTheCoder.command.management;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.player.PlayerData;

/**
 * @author larryTheCoder
 */
public class InviteSubCommand extends SubCommand {

    public InviteSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isPlayer() && sender.hasPermission("is.command.invite");
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player to be member of your island";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"inv"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return false;
        }
        Player p = sender.getServer().getPlayer(sender.getName());
        if (!getPlugin().getIsland().checkIsland(p)) {
            sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
            return true;
        }
        Player invite = sender.getServer().getPlayer(args[1]);
        if (invite == null) {
            sender.sendMessage(getPrefix() + getLocale(p).errorOfflinePlayer);
            return true;
        }
        PlayerData pdinv = ASkyBlock.get().getPlayerInfo(invite);
        PlayerData pd = ASkyBlock.get().getPlayerInfo(p);
        if (pdinv.hasTeam()) {
            sender.sendMessage(getPrefix() + getLocale(p).errorInTeam.replace("[player]", args[1]));
            return false;
        }
        if (pd.members.contains(invite.getName())) {
            sender.sendMessage(getPrefix() + getLocale(p).errorInTeam.replace("[player]", args[1]));
            return false;
        }
        getPlugin().getInvitationHandler().addInvitation(p, invite);
        return true;
    }

}
