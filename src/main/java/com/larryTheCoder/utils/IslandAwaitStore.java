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

package com.larryTheCoder.utils;

import cn.nukkit.Player;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.QueryDb;
import com.larryTheCoder.database.QueryInfo;
import org.sql2o.data.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Awaits class, this class stores a temporary player island reset data. This class
 * may benefit the owner by restricting the amount of island created by a certain amount of time.
 */
public class IslandAwaitStore {

    public static final Map<String, Long> playerLimit = new HashMap<>();

    /**
     * Stores a temporary await data key into a hash mapping data.
     * This data will be saved later by an async database.
     *
     * @param player The player unique id.
     */
    public static void storeAwaitData(String playerName) {
        if (playerLimit.containsKey(playerName)) return;

        long currentTime;
        playerLimit.put(playerName, currentTime = System.currentTimeMillis());

        ASkyBlock.get().getDatabase().executeUpdate(new QueryInfo(QueryDb.getInstance().awaitStore)
                .addParameter("plUniqueId", playerName)
                .addParameter("timestamp", currentTime));
    }

    /**
     * Checks if the player can bypass the await timer.
     *
     * @param player The player class itself.
     * @return The time that player needs in order to bypass
     */
    public static long canBypassAwait(Player player) {
        String plName = player.getName();
        if (ASkyBlock.get().getPermissionHandler().hasPermission(player, "is.timer.bypass")) return -1;
        if (!playerLimit.containsKey(plName)) return -1;

        long totalTime = TimeUnit.MINUTES.toMillis(Settings.resetTime) - (System.currentTimeMillis() - playerLimit.get(plName));
        if (totalTime <= 0) {
            ASkyBlock.get().getDatabase().executeUpdate(new QueryInfo("DELETE FROM lastExecution WHERE playerUniqueId = :plName").addParameter("plName", plName));
            return -1;
        }

        return totalTime;
    }

    /**
     * Initializes the IslandAwaitStore class.
     */
    public static void init() {
        Table table = ASkyBlock.get().getDatabase().fetchData(new QueryInfo("SELECT * FROM lastExecution")).join();

        table.rows().forEach(row -> playerLimit.put(row.getString("playerUniqueId"), row.getLong("lastQueried")));
    }
}
