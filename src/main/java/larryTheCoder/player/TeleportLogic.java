/*
 * Copyright (C) 2017 larryTheHarry 
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
 */
package larryTheCoder.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.TaskHandler;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import larryTheCoder.ASkyBlock;
import larryTheCoder.utils.Utils;

/**
 * Responsible for teleporting (and canceling teleporting) of players.
 */
public class TeleportLogic implements Listener {

    private final ASkyBlock plugin;
    private final int teleportDelay;
    private final Map<UUID, PendingTeleport> pendingTPs = new ConcurrentHashMap<>();
    private final double cancelDistance;

    @SuppressWarnings("LeakingThisInConstructor")
    public TeleportLogic(ASkyBlock plugin) {
        this.plugin = plugin;
        teleportDelay = plugin.getConfig().getInt("options.island.islandTeleportDelay", 2);
        cancelDistance = plugin.getConfig().getDouble("options.island.teleportCancelDistance", 0.2);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void safeTeleport(final Player player, final Location homeSweetHome, boolean force) {
        final Location targetLoc = homeSweetHome.clone().add(0.5, 0, 0.5);
        if (player.hasPermission("usb.mod.bypassteleport") || (teleportDelay == 0) || force) {
            player.teleport(targetLoc);
        } else {
            player.sendMessage("\u00a7aYou will be teleported in {0} seconds.".replace("{0}", "" + teleportDelay));
            TaskHandler task = plugin.getServer().getScheduler().scheduleDelayedTask(new Runnable() {
                @Override
                public void run() {
                    pendingTPs.remove(player.getUniqueId());
                    Location loc = targetLoc.clone();
                    if (loc != null && !loc.getLevel().isChunkLoaded((int) loc.getX() >> 4, (int) loc.getZ() >> 4)) {
                        loc.getLevel().loadChunk((int) loc.getX() >> 4, (int) loc.getZ() >> 4);
                    }
                    player.teleport(targetLoc);
                }
            }, (int) Utils.secondsAsMillis(teleportDelay));
            pendingTPs.put(player.getUniqueId(), new PendingTeleport(player.getLocation(), task));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled() || e.getPlayer() == null || e.getPlayer().getLocation() == null) {
            return;
        }
        UUID uniqueId = e.getPlayer().getUniqueId();
        PendingTeleport pendingTeleport = pendingTPs.get(uniqueId);
        if (pendingTeleport != null) {
            pendingTeleport.playerMoved(e.getPlayer());
        }
    }

    private class PendingTeleport {

        private final Location location;
        private final TaskHandler task;

        private PendingTeleport(Location location, TaskHandler task) {
            this.location = location != null ? location.clone() : null;
            this.task = task;
        }

        public Location getLocation() {
            return location;
        }

        public TaskHandler getTask() {
            return task;
        }

        public void playerMoved(Player player) {
            Location newLocation = player.getLocation();
            if (location != null && location.getLevel().equals(newLocation.getLevel())) {
                double distance = location.distance(newLocation);
                if (distance > cancelDistance) {
                    task.cancel();
                    pendingTPs.remove(player.getUniqueId());
                    player.sendMessage("\u00a77Teleport cancelled");
                }
            }
        }
    }
}
