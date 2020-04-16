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

package com.larryTheCoder.command.category;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.TeamManager;

import java.util.Arrays;
import java.util.List;

/**
 * ChatCategory, command that handles everything that is related
 * to chat-interaction.
 * <p>
 * The commands are as follows:
 * - /is chat       => Allows members to chat within their member teams.
 * - /is messages   => Get the news from a server/leader.
 * <p>
 * This category is TBD.
 */
public class ChatCategory extends SubCategory {

    public ChatCategory(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public List<String> getCommands() {
        return Arrays.asList("chat", "messages");
    }

    @Override
    public boolean canUse(CommandSender sender, String command) {
        switch (command.toLowerCase()) {
            case "chat":
                return sender.hasPermission("is.command.teamChat") && sender.isPlayer();
            case "messages":
                return sender.hasPermission("is.command.messages") && sender.isPlayer();
            default:
                return false;
        }
    }

    @Override
    public String getDescription(String commandName) {
        switch (commandName.toLowerCase()) {
            case "chat":
                return "Chat with your island members.";
            case "messages":
                return "Read a new messages from island leader.";
            default:
                return null;
        }
    }

    @Override
    public String getParameters(String commandName) {
        return "";
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = Server.getInstance().getPlayer(sender.getName());

        switch (args[0].toLowerCase()) {
            case "chat":
                if (getPlugin().getIslandManager().checkIsland(p)) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
                    break;
                }

                TeamManager manager = getPlugin().getTManager();
                if (manager.hasTeam(p.getName())) {
                    // Check if team members are online
                    boolean online = false;
                    for (String teamMember : manager.getPlayerCoop(p.getName()).getMembers()) {
                        if (!teamMember.equals(p.getName()) && getPlugin().getServer().getPlayer(teamMember) != null) {
                            online = true;
                        }
                    }
                    if (!online) {
                        p.sendMessage(getPrefix() + getLocale(p).teamChatNoTeamAround);
                        p.sendMessage(getPrefix() + getLocale(p).teamChatStatusOff);
                        getPlugin().getChatHandler().unSetPlayer(p);
                        break;
                    }
                    if (getPlugin().getChatHandler().isTeamChat(p)) {
                        // Toggle
                        p.sendMessage(getPrefix() + getLocale(p).teamChatStatusOff);
                        getPlugin().getChatHandler().unSetPlayer(p);
                    } else {
                        p.sendMessage(getPrefix() + getLocale(p).teamChatStatusOn);
                        getPlugin().getChatHandler().setPlayer(p);
                    }
                }
                break;
            case "messages":
                List<String> list = getPlugin().getMessages().getMessages(p.getName());
                if (!list.isEmpty()) {
                    p.sendMessage(getPlugin().getLocale(p).newsHeadline);
                    list.forEach((i) -> p.sendMessage("- §e" + i));
                    getPlugin().getMessages().clearMessages(p.getName());
                } else {
                    p.sendMessage(getPrefix() + getPlugin().getLocale(p).newsEmpty);
                }
                break;
        }

    }
}
