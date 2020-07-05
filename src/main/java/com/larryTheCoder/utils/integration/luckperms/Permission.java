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

package com.larryTheCoder.utils.integration.luckperms;

import cn.nukkit.command.CommandSender;

import java.util.Map;

public abstract class Permission {

    /**
     * Check either the sender has the permission for the given string
     *
     * @param sender     The command sender class itself.
     * @param permission The permission that need to be checked.
     * @return {@code true} if the sender has the permission to execute this.
     */
    public abstract boolean hasPermission(CommandSender sender, String permission);

    /**
     * Gets all permissions nodes from the player name given. This might
     * also return {@code null} if the player were not found. This function is a
     * blocking-thread operation and which should NEVER be called other
     * than an async tasks or threads.
     *
     * @param playerName The player unique name that needs to be checked.
     * @return {@code true} the list of permissions that the player had.
     */
    public abstract Map<String, Boolean> getPermissions(String playerName);
}
