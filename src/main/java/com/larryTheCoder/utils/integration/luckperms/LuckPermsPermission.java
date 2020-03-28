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

import cn.nukkit.Player;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;

/**
 * Implementation to LuckPermsPermission
 * aka, PermissionEx
 */
public class LuckPermsPermission {

    private final ASkyBlock plugin;
    private LuckPermsApi pubApi;

    public LuckPermsPermission(ASkyBlock instance) {
        this.plugin = instance;
        this.getLuckPerms();
    }

    private void getLuckPerms() {
        try {
            pubApi = LuckPerms.getApi();
        } catch (IllegalStateException ignored) {
            TaskManager.runTaskLater(this::getLuckPerms, 60);
            return;
        }
        Utils.send("&aSuccessfully integrated with LuckPerms plugin.");
    }

    public boolean hasPermission(Player p, String permission) {
        User user = pubApi.getUser(p.getName());
        for (Node perm : user.getPermissions()) {
            if (!perm.getPermission().equalsIgnoreCase(permission) && !perm.getValue()) {
                continue;
            }
            return true;
        }
        // Permission either not found or doesn't applied to
        // This user.
        return false;
    }

}
