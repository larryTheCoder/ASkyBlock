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
import com.larryTheCoder.task.TaskManager;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;
import org.sql2o.data.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Awaits class, this class stores a temporary player island reset data. This class
 * may benefit the owner by restricting the amount of island created by a certain amount of time.
 */
public class IslandAwaitStore {

    public static final Map<UUID, Long> playerLimit = new HashMap<>();

    public static final Map<UUID, Long> saveQueue = new ConcurrentHashMap<>();
    public static final Queue<UUID> deleteQueue = new ConcurrentLinkedQueue<>();

    /**
     * Stores a temporary await data key into a hash mapping data.
     * This data will be saved later by an async database.
     *
     * @param player The player unique id.
     */
    public static void storeAwaitData(UUID player) {
        if (playerLimit.containsKey(player)) return;

        long currentTime = System.currentTimeMillis();
        playerLimit.put(player, currentTime);
        saveQueue.put(player, currentTime);
    }

    /**
     * Checks if the player can bypass the await timer.
     *
     * @param player The player class itself.
     * @return {@code true} if the player can bypass the await limit.
     */
    public static boolean canBypassAwait(Player player) {
        UUID uuid = player.getUniqueId();
        if (ASkyBlock.get().getPermissionHandler().hasPermission(player, "is.timer.bypass")) return true;
        if (!playerLimit.containsKey(uuid)) return true;

        if ((System.currentTimeMillis() - playerLimit.get(uuid)) >= TimeUnit.MINUTES.toMillis(Settings.resetTime)) {
            deleteQueue.add(uuid);
            return true;
        }

        return false;
    }

    /**
     * Initializes the IslandAwaitStore class.
     *
     * @param connection The connection to the sql2o database.
     */
    public static void init(Connection connection) {
        Table table = connection.createQuery("SELECT * FROM lastExecution").executeAndFetchTable();

        table.rows().forEach(row -> playerLimit.put(UUID.fromString(row.getString("playerUniqueId")), row.getLong("lastQueried")));
    }

    /**
     * Stores IslandAwaitStore data into the database.
     *
     * @param connection The connection to the sql2o database.
     */
    public static void databaseTick(Connection connection) {
        try {
            if (!saveQueue.isEmpty()) {
                saveQueue.forEach((uuid, timestamp) -> connection.createQuery("INSERT INTO lastExecution(playerUniqueId, lastQueried) VALUES (:plUniqueId, :timestamp)")
                        .addParameter("plUniqueId", uuid.toString())
                        .addParameter("timestamp", timestamp)
                        .executeUpdate());

                saveQueue.clear();
            }

            if (!deleteQueue.isEmpty()) {
                deleteQueue.forEach(uuid -> connection.createQuery("DELETE FROM lastExecution WHERE playerUniqueId = :plUniqueId")
                        .addParameter("plUniqueId", uuid.toString())
                        .executeUpdate());

                deleteQueue.clear();
            }
        } catch (Sql2oException err) {
            TaskManager.runTask(err::printStackTrace);
        }
    }
}
