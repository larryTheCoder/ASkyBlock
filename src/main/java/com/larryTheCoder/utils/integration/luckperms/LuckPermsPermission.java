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
import cn.nukkit.command.CommandSender;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation to LuckPermsPermission
 * aka, PermissionEx
 */
public class LuckPermsPermission extends Permission {

    private LuckPerms luckPerms;

    public LuckPermsPermission() {
        getLuckPerms();
    }

    private void getLuckPerms() {
        try {
            luckPerms = LuckPermsProvider.get();

            Utils.send("&7Successfully integrated with LuckPerms &7plugin.");
        } catch (IllegalStateException ignored) {
            TaskManager.runTaskLater(this::getLuckPerms, 60);
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player)) return true;

        User user = luckPerms.getUserManager().getUser(((Player) sender).getUniqueId());
        if (user == null) {
            Utils.sendDebug("The user " + sender.getName() + " were not found in LuckPermsAPI");
            return false;
        }

        ContextManager cm = luckPerms.getContextManager();

        QueryOptions queryOptions = cm.getQueryOptions(user).orElse(cm.getStaticQueryOptions());
        CachedPermissionData permissionData = user.getCachedData().getPermissionData(queryOptions);

        return permissionData.checkPermission(permission).asBoolean();
    }

    public Map<String, Boolean> getPermissions(String playerName) {
        UserManager userManager = luckPerms.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(userManager.lookupUniqueId(playerName).join());

        User user = userFuture.join();
        if (user == null) {
            Utils.sendDebug("The player " + playerName + " were not found in LuckPermsAPI");
            return null;
        }

        ContextManager cm = luckPerms.getContextManager();

        QueryOptions queryOptions = cm.getQueryOptions(user).orElse(cm.getStaticQueryOptions());
        CachedPermissionData permissionData = user.getCachedData().getPermissionData(queryOptions);

        return permissionData.getPermissionMap();

    }
}
