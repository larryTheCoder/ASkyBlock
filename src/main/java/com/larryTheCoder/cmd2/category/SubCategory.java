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

package com.larryTheCoder.cmd2.category;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.google.common.base.Preconditions;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.locales.ASlocales;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * A better way to express a SubCommand class.
 * <p>
 * SubCommand was a class where commands are being separated from the base command.
 * However, if a new command need to be added, a new class extending SubCommand class is needed
 * in order to have full functionality of the command.
 * <p>
 * This class adds all commands into few different categories where the commands can be added
 * or removed within category class at the same time without having to create another SubCommand class.
 */
public abstract class SubCategory {

    @Getter
    private final ASkyBlock plugin;

    protected SubCategory(ASkyBlock plugin) {
        Preconditions.checkNotNull(plugin, "ASkyBlock cannot be nullified");

        this.plugin = plugin;
    }

    protected ASlocales getLocale(Player key) {
        return plugin.getLocale(key);
    }

    protected String getPrefix() {
        return plugin.getPrefix();
    }

    public List<String> baseCommands() {
        return Arrays.asList("is", "island");
    }

    /**
     * Return a list of command provided by this category.
     *
     * @return a set of string provided by this category.
     */
    public abstract List<String> getCommands();

    /**
     * Execute a command provided by this category.
     *
     * @param sender       The sender requests to execute this category.
     * @param commandLabel The label of the command, usually it will returns 'is'
     * @param args         The command arguments
     */
    public abstract void execute(CommandSender sender, String commandLabel, String[] args);

    /**
     * Checks either these commands can be used by this sender or not.
     *
     * @param sender CommandSender
     * @return boolean
     */
    public abstract boolean canUse(CommandSender sender, String command);

    /**
     * Get a description of a command. This is useful when it comes to know
     * what command does this do.
     *
     * @param commandName The command name
     * @return The description of the command.
     */
    public abstract String getDescription(String commandName);
}
