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
package com.larryTheCoder.command.island;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;

/**
 * @author larryTheCoder
 */
public class TeleportSubCommand extends SubCommand {

    public TeleportSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.teleport") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "<player>";
    }

    @Override
    public String getName() {
        return "teleport";
    }

    @Override
    public String getDescription() {
        return "teleport to others player Island";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tp"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return false;
        }
        Player p = getPlugin().getServer().getPlayer(sender.getName());
        getPlugin().getIsland().teleportPlayer(p, args[1]);
        return true;
    }

}
