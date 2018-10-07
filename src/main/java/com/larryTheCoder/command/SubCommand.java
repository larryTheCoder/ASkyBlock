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
package com.larryTheCoder.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.MainLogger;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.locales.ASlocales;

/**
 * A class that define a command parameters.
 * Requires an ASkyBlock instance.
 *
 * @author larryTheCoder
 */
public abstract class SubCommand {

    public final MainLogger deb = Server.getInstance().getLogger();
    private final ASkyBlock plugin;

    protected SubCommand(ASkyBlock plugin) {
        if (plugin == null) {
            Server.getInstance().getLogger().error("plugin cant be null");
        }
        this.plugin = plugin;
    }

    /**
     * @return larryTheCoder\ASkyBlock
     */
    protected ASkyBlock getPlugin() {
        return plugin;
    }

    protected ASlocales getLocale(Player key) {
        return plugin.getLocale(key);
    }

    protected String getPrefix() {
        return plugin.getPrefix();
    }

    /**
     * @param sender CommandSender
     * @return boolean
     */
    public abstract boolean canUse(CommandSender sender);

    /**
     * @return string
     */
    public abstract String getUsage();

    /**
     * @return string
     */
    public abstract String getName();

    /**
     * @return string
     */
    public abstract String getDescription();

    /**
     * @return string[]
     */
    public abstract String[] getAliases();

    /**
     * @param sender the sender      - CommandSender
     * @param args   The arrugements      - String[]
     * @return true if true
     */

    public abstract boolean execute(CommandSender sender, String[] args);
}
