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

package com.larryTheCoder.task;

import cn.nukkit.Player;
import cn.nukkit.permission.PermissionAttachmentInfo;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * Calculates the level of the island
 */
public class LevelCalcTask {

    private final Queue<Player> levelUpdateQueue = new ArrayDeque<>(32);
    private ASkyBlock plugin;

    public LevelCalcTask(ASkyBlock plugin) {
        this.plugin = plugin;
        TaskManager.runTaskRepeatAsync(() -> {
            if (!updateList()) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 20);
    }

    /**
     * Adds a player into the queue to check its
     * level by chunks. The player will be checked after
     * the next tick.
     *
     * @param player The player itself
     */
    public void addUpdateQueue(Player player) {
        levelUpdateQueue.add(player);
    }

    private boolean updateList() {
        int size = Math.min(0xfffff, levelUpdateQueue.size());

        if (size == 0) {
            // Idle
            return false;
        }

        int limit = Math.min(20, size);
        Player[] copy = new Player[limit];
        for (int i = 0; i < limit; i++) {
            copy[i] = levelUpdateQueue.poll();
        }

        for (Player targetPlayer : copy) {
            PlayerData pda = plugin.getPlayerInfo(targetPlayer);
            IslandData pd = plugin.getIslandInfo(targetPlayer);

            // Get the player multiplier if it available
            int multiplier = 1;
            for (Map.Entry<String, PermissionAttachmentInfo> pType : targetPlayer.getEffectivePermissions().entrySet()) {
                String type = pType.getKey();
                if (!type.equalsIgnoreCase("is.multiplier.")) {
                    continue;
                }
                // Then check if the player has a valid value
                String spl = type.substring(14);
                if (!spl.isEmpty() && Utils.isNumeric(spl)) {
                    // Get the max value should there be more than one
                    multiplier = Math.max(multiplier, Integer.valueOf(spl));
                    // Do some sanity checking
                    if (multiplier < 1) {
                        multiplier = 1;
                    }
                } else {
                    Utils.send("&cPlayer " + targetPlayer.getName() + " has permission: " + type + " <-- the last part MUST be a number! Ignoring...");
                }
            }


        }
        return true;
    }

}
