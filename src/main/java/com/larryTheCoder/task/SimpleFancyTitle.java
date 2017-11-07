/*
 * Copyright (C) 2017 Adam Matthew
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.larryTheCoder.task;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.storage.IslandData;

public class SimpleFancyTitle extends Task {

    private ASkyBlock plugin;
    private Player p;
    private Position lastPos;

    public SimpleFancyTitle(ASkyBlock plugin, Player player) {
        this.plugin = plugin;
        this.p = player;
    }

    @Override
    public void onRun(int currentTick) {
        lastPos = p.clone();
        boolean shouldLoopBack;

        // Automatically cancel this task when player moved or something
        if (TeleportLogic.isPlayerMoved(p.getName())) {
            this.cancel();
            return;
        }
        // Logical statement
        if (!plugin.inIslandWorld(p)) {
            shouldLoopBack = true;
        } else {
            // Reset the lastPos (Not in world)
            if (!plugin.level.contains(lastPos.level.getName())) {
                lastPos = p.clone();
            }
            // Now let wait till player moved or something
            shouldLoopBack = lastPos.getFloorX() == p.getFloorX() && lastPos.getFloorZ() == p.getFloorZ();
        }

        if (shouldLoopBack) {
            return;
        }

        IslandData ownership = plugin.getIslandInfo(p.getLocation());
        if (!plugin.getLocale(p).islandSubTitle.isEmpty()) {
            p.setSubtitle(TextFormat.GOLD + plugin.getLocale(p).islandSupport.replace("[player]", ownership.getOwner()));
        }
        if (!plugin.getLocale(p).islandSupport.isEmpty()) {
            p.sendTitle(TextFormat.GOLD + plugin.getLocale(p).islandTitle.replace("[player]", ownership.getOwner()));
        }
        if (!plugin.getLocale(p).islandDonate.isEmpty() && !plugin.getLocale(p).islandURL.isEmpty()) {
            // These are useful for me not for you xD
            //p.sendMessage(plugin.getLocale(p).islandDonate.replace("[player]", p.getName()));
            //p.sendMessage(plugin.getLocale(p).islandSupport);
            //p.sendMessage(plugin.getLocale(p).islandURL);
        }
        // Cancel the task (Complete the repeat task)
        this.cancel();
    }
}
