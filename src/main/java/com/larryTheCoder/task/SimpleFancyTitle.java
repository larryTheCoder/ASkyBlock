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
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Utils;

import java.util.Map;

public class SimpleFancyTitle extends Task {

    private final ASkyBlock plugin;
    private final Player p;
    private Position lastPos;
    private int times = 0;

    public SimpleFancyTitle(ASkyBlock plugin, Player player) {
        this.plugin = plugin;
        this.p = player;
    }

    @Override
    public void onRun(int currentTick) {
        if (lastPos == null) {
            lastPos = p.clone();
        }

        boolean shouldLoopBack;

        // Automatically cancel this task when player moved or something
        if (TeleportLogic.isPlayerMoved(p.getName())) {
            this.cancel();
            return;
        }

        double shouldDo = Math.sqrt(Math.pow(lastPos.x - p.x, 2) + Math.pow(lastPos.z - p.z, 2));
        // Now let wait till player moved or something
        // Do not count on it height. It might be falling from spawn pedestal
        shouldLoopBack = !plugin.inIslandWorld(p) || !(shouldDo >= 0.5);

        if (shouldLoopBack) {
            // If player still not moving or something. Cancel this task
            if (times >= 30) {
                return;
            }
            // This class interfered to task class
            // Keep this task in here until player moved
            TaskManager.runTaskLater(this, 20);
            lastPos = p.clone();
            times++;
            return;
        }

        IslandData ownership = plugin.getIslandInfo(p.getLocation());
        if (!plugin.getLocale(p).islandSubTitle.isEmpty()) {
            p.setSubtitle(TextFormat.GOLD + plugin.getLocale(p).islandSubTitle.replace("[player]", ownership.getOwner()));
        }
        if (!plugin.getLocale(p).islandTitle.isEmpty()) {
            p.sendTitle(TextFormat.GOLD + plugin.getLocale(p).islandTitle.replace("[player]", ownership.getOwner()));
        }
        if (!plugin.getLocale(p).islandDonate.isEmpty() && !plugin.getLocale(p).islandURL.isEmpty()) {
            // These are useful for me not for you xD
            //p.sendMessage(plugin.getLocale(p).islandDonate.replace("[player]", p.getName()));
            //p.sendMessage(plugin.getLocale(p).islandSupport);
            //p.sendMessage(plugin.getLocale(p).islandURL);
        }

        Map<String, Runnable> task = Utils.TASK_SCHEDULED;
        if (task.containsKey(p.getName())) {
            Utils.sendDebug("Running a runnable task");
            Runnable tasking = task.get(p.getName());
            tasking.run();
            task.remove(p.getName());
        }
    }
}
