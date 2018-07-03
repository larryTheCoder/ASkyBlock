/*
 * Copyright (C) 2016-2018 Adam Matthew
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
package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.item.EntityVehicle;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.IslandEnterEvent;
import com.larryTheCoder.events.IslandExitEvent;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.List;

import static cn.nukkit.block.BlockID.ENDER_CHEST;

/**
 * Rewrite class for IslandGuard messy code
 * Timestamp: 10:57 AM 6/11/2018
 */
public class IslandListener implements Listener {

    private final ASkyBlock plugin;
    private final MainLogger deb = Server.getInstance().getLogger();

    public IslandListener(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Determines if a location is in the island world or not or in the new
     * nether if it is activated
     *
     * @param loc Location of the entity to be checked
     * @return true if in the island world
     */
    private boolean notInWorld(Location loc) {
        return !ASkyBlock.get().getLevels().contains(loc.getLevel().getName());
    }

    /**
     * Checks if action is allowed for player in location for flag
     *
     * @param player   The player or entity
     * @param location The location to be checked
     * @return true if allowed
     */
    private boolean actionAllowed(Player player, Location location) {
        if (player == null) {
            return false;
        }
        // This permission bypasses protection
        if (player.isOp() || player.hasPermission("is.mod.bypassprotect")) {
            return true;
        }
        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        if (island == null || island.getOwner() == null) {
            return false;
        }

        // todo fix getIgsFlag for islands
        return island.getOwner().equalsIgnoreCase(player.getName());
    }

    private String getPrefix() {
        return plugin.getPrefix();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!p.isAlive()) {
            return;
        }
        if (notInWorld(p)) {
            return;
        }
        if (plugin.getGrid() == null) {
            return;
        }
        // Only do something if there is a definite x or z movement
        if (e.getTo().getFloorX() - e.getFrom().getFloorX() == 0 && e.getTo().getFloorZ() - e.getFrom().getFloorZ() == 0) {
            return;
        }
        if (e.getPlayer().isOp() && e.getPlayer().hasPermission("is.mod.bypassprotect")) {
            return;
        }

        final IslandData islandTo = plugin.getGrid().getProtectedIslandAt(e.getTo());
        final IslandData islandFrom = plugin.getGrid().getProtectedIslandAt(e.getFrom()); // Announcement entering
        /*
         * Only says something if there is a change in islands
         *
         * Situations:
         * islandTo == null && islandFrom != null - exit
         * islandTo == null && islandFrom == null - nothing
         * islandTo != null && islandFrom == null - enter
         * islandTo != null && islandFrom != null - same PlayerIsland or teleport?
         * islandTo == islandFrom
         */

        if (islandTo != null && islandFrom == null && (islandTo.getOwner() != null || islandTo.isSpawn())) {
            // Entering
            if (islandTo.isLocked()) {
                p.sendMessage(plugin.getPrefix() + TextFormat.RED + "This island is locked");
                Vector3 vec = p.getMotion();
                Vector3 vecNew = new Vector3(-vec.x, -vec.y, -vec.z);
                p.setMotion(vecNew);
                return;
            }

            //p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.getName() + "'s island");

            // Fire entry event
            final IslandEnterEvent event = new IslandEnterEvent(p, islandTo, e.getTo());
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (islandTo == null && islandFrom != null && (islandFrom.getOwner() != null || islandFrom.isSpawn())) {
            // Leaving
            // p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.getName() + "'s island");

            // Fire exit event
            final IslandExitEvent event = new IslandExitEvent(p, islandFrom, e.getFrom());
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (islandTo != null && islandFrom != null && !islandTo.equals(islandFrom)) {
            // Adjacent islands or overlapping protections
            if (islandFrom.getOwner() != null) {
                //p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.getName() + "'s island");
            }
            if (islandTo.getOwner() != null) {
                //p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.getName() + "'s island");
            }

            // Fire exit event
            final IslandExitEvent event = new IslandExitEvent(p, islandFrom, e.getFrom());
            plugin.getServer().getPluginManager().callEvent(event);
            // Fire entry event
            final IslandEnterEvent event2 = new IslandEnterEvent(p, islandTo, e.getTo());
            plugin.getServer().getPluginManager().callEvent(event2);
        } else if (islandTo != null && (islandTo.getOwner() != null || islandTo.isSpawn())) {
            // Lock check
            if (islandTo.isLocked()) {
                if (!p.isOp() && !p.hasPermission("is.mod.bypassprotect") && !p.hasPermission("is.mod.bypasslock")) {
                    if (p.riding != null) {
                        // Dismount
                        ((EntityVehicle) p.riding).mountEntity(p);
                        e.setCancelled();
                    } else {
                        Vector3 vec = p.getMotion();
                        Vector3 vecNew = new Vector3(-vec.x, -vec.y, -vec.z);
                        p.setMotion(vecNew);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (notInWorld(p)) {
            return;
        }
        if (p.isOp() || p.hasPermission("is.mod.bypassprotect")) {
            return;
        }
        if (plugin.getIsland().locationIsOnIsland(p, e.getBlock())) {
            // You can do anything on your island
            return;
        }
        // Player is not clicking a block, they are clicking a material so this
        // is driven by where the player is
        if (e.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && (e.getBlock() != null && plugin.getGrid().playerIsOnIsland(p))) {
            return;
        }
        // No need stupid checks. Just cancel them
        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
        e.setCancelled();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent e) {
        if (notInWorld(e.getPosition().getLocation())) {
            return;
        }
        if (e.getEntity() instanceof EntityPrimedTNT && ((EntityPrimedTNT) e.getEntity()).getSource() instanceof Player) {
            Player p = (Player) ((EntityPrimedTNT) e.getEntity()).getSource();
            if (actionAllowed(p, e.getEntity().getLocation())) {
                return;
            }
            if (p.isOp() && p.hasPermission("is.mod.bypassprotect")) {
                return;
            }
        }

        // As what I said, no need stupid checks
        e.getBlockList().clear();
        e.setCancelled();
    }

    /**
     * This method protects players from PVP if it is not allowed and from
     * arrows fired by other players
     *
     * @param e Event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        // TODO: Forms with PVP Protection flags
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        deb.debug("DEBUG: " + e.getEventName());
        deb.debug("DEBUG: Block is " + e.getBlock().toString());
        // Check if the player in the world
        if (notInWorld(e.getPlayer())) {
            return;
        }
        if (actionAllowed(e.getPlayer(), e.getBlock().getLocation())) {
            return;
        }

        // Cancel them, obviously
        e.getPlayer().sendMessage(plugin.getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (notInWorld(e.getPlayer())) {
            return;
        }
        if (actionAllowed(e.getPlayer(), e.getBlock().getLocation())) {
            return;
        }

        // Everyone else - not allowed
        e.getPlayer().sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraft(CraftItemEvent event) {
        Player player = event.getPlayer();
        if (notInWorld(player)) {
            return;
        }
        if (event.getRecipe().getResult().getId() == ENDER_CHEST && !player.hasPermission("is.craft.enderchest")) {
            player.sendMessage(plugin.getLocale(player).errorNoPermission);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerLogin(PlayerPreLoginEvent ex) {
        Player p = ex.getPlayer();
        plugin.getIslandInfo(p);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ex) {
        // load player inventory if exists
        Player p = ex.getPlayer();
        plugin.getInventory().loadPlayerInventory(p);
        // Load player data
        if (plugin.getPlayerInfo(p) == null) {
            Utils.send(p.getName() + "&a data doesn't exists. Creating new ones");
            plugin.getDatabase().createPlayer(p.getName());
        }

        // Load messages
        List<String> news = plugin.getMessages().getMessages(p.getName());

        if (news != null && news.isEmpty()) {
            p.sendMessage(plugin.getLocale(p).newNews.replace("[count]", Integer.toString(news.size())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent ex) {
        Player p = ex.getPlayer();
        IslandData pd = plugin.getIslandInfo(p);
        if (pd != null) {
            // Remove the island data from cache provides the memory to server
            plugin.getDatabase().removeIslandFromCache(pd);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent ex) {
        Player p = ex.getPlayer();
        String command = ex.getMessage().substring(1);
//        if (notInWorld(p) && !Settings.bannedCommands.contains(command)) {
//            return;
//        }
//        if (p.isOp()) {
//            p.sendMessage(plugin.getLocale(p).adminOverride);
//            return;
//        }
//        p.sendMessage(plugin.getLocale(p).errorCommandBlocked);
//        ex.setCancelled();
    }
}
