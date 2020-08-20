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
package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.passive.EntityAnimal;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.events.IslandEnterEvent;
import com.larryTheCoder.events.IslandExitEvent;
import com.larryTheCoder.events.SkyBlockEvent;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.SettingsFlag;
import com.larryTheCoder.utils.Utils;
import lombok.extern.log4j.Log4j2;

import static cn.nukkit.block.BlockID.ENDER_CHEST;

/**
 * @author larryTheCoder
 */
@Log4j2
public class IslandListener extends Action implements Listener {

    private final ASkyBlock plugin;

    public IslandListener(ASkyBlock plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    private String getPrefix() {
        return plugin.getPrefix();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!p.isAlive()) return;
        if (notInWorld(p)) return;
        if (plugin.getGrid() == null) return;

        // Only do something if there is a definite x or z movement
        if (e.getTo().getFloorX() - e.getFrom().getFloorX() == 0 && e.getTo().getFloorZ() - e.getFrom().getFloorZ() == 0) {
            return;
        }
        if (e.getPlayer().isOp() && hasPermission(e.getPlayer(), "is.mod.bypassprotect")) {
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

        if (islandTo != null && islandFrom == null && (islandTo.getPlotOwner() != null)) {
            // Entering
            if (islandTo.isLocked()) {
                p.sendMessage(plugin.getPrefix() + TextFormat.RED + "This island is locked");
                e.setCancelled();
                return;
            }

            if (islandTo.getIgsSettings().getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.getIslandName() + "'s island");
            }

            // Fire entry event
            SkyBlockEvent.eventCall(new IslandEnterEvent(p, islandTo, e.getTo()));
        } else if (islandTo == null && islandFrom != null && (islandFrom.getPlotOwner() != null)) {
            // Leaving
            if (islandFrom.getIgsSettings().getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.getIslandName() + "'s island");
            }

            // Fire exit event
            SkyBlockEvent.eventCall(new IslandExitEvent(p, islandFrom, e.getFrom()));
        } else if (islandTo != null && islandFrom != null && !islandTo.equals(islandFrom)) {
            // Fire exit event
            SkyBlockEvent.eventCall(new IslandExitEvent(p, islandFrom, e.getFrom()));
            // Fire entry event
            SkyBlockEvent.eventCall(new IslandEnterEvent(p, islandTo, e.getTo()));
        } else if (islandTo != null && (islandTo.getPlotOwner() != null)) {
            // Lock check
            if (islandTo.isLocked()) {
                if (!p.isOp() && !hasPermission(e.getPlayer(), "is.mod.bypassprotect") && !hasPermission(e.getPlayer(), "is.mod.bypasslock")) {
                    if (p.riding != null) {
                        // Dismount
                        p.riding.mountEntity(p);
                    }
                    e.setCancelled();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerExecuteCommand(PlayerCommandPreprocessEvent event) {
        log.debug("DEBUG: " + event.getEventName());
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1);

        if (notInWorld(player)) return;
        if (Settings.bannedCommands.stream().anyMatch(i -> i.equalsIgnoreCase(command))) {
            event.setCancelled();

            player.sendMessage("This command is not allowed to be ran in this world!");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerHitEvent(EntityDamageEvent e) {
        Entity target = e.getEntity();

        if (notInWorld(target)) return;
        if (e instanceof EntityDamageByEntityEvent) {
            // Identifier for player mobs attack.
            if (!(target instanceof Player)) {
                if (target instanceof EntityAnimal) {
                    if (actionAllowed(target.getLocation(), SettingsFlag.HURT_MOBS)) return;
                } else if (target instanceof EntityMob) {
                    if (actionAllowed(target.getLocation(), SettingsFlag.HURT_MONSTERS)) return;
                }
            } else {
                if (actionAllowed(target.getLocation(), SettingsFlag.PVP)) return;
            }

            e.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        log.debug("DEBUG: " + e.getEventName());
        log.debug("DEBUG: Item is " + e.getItem().toString());

        Player p = e.getPlayer();
        if (notInWorld(p)) {
            log.debug("Event is not in world");
            return;
        }
        if (p.isOp() || hasPermission(p, "is.mod.bypassprotect")) {
            return;
        }
        // Too bad that the item is not a vector3
        if (plugin.getIslandManager().locationIsOnIsland(p, e.getPlayer())) {
            log.debug("Action is allowed: Player on island");
            // You can do anything on your island
            return;
        }
        if (actionAllowed(e.getPlayer().getLocation(), SettingsFlag.VISITOR_ITEM_DROP)) {
            log.debug("Action is allowed: Drop item is allowed");
            return;
        }

        log.debug("Action not allowed: Drop item");
        e.setCancelled();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(InventoryPickupItemEvent e) {
        log.debug("DEBUG: " + e.getEventName());
        log.debug("DEBUG: Item is " + e.getItem().toString());

        Player p = (Player) e.getInventory().getHolder();
        if (notInWorld(p)) {
            log.debug("Event is not in world");
            return;
        }
        if (p.isOp() || hasPermission(p, "is.mod.bypassprotect")) {
            return;
        }

        // Too bad that the item is not a vector3
        if (plugin.getIslandManager().locationIsOnIsland(p, e.getItem())) {
            log.debug("Action is allowed: Player on island");
            // You can do anything on your island
            return;
        }

        if (actionAllowed(e.getItem().getLocation(), SettingsFlag.VISITOR_ITEM_PICKUP)) {
            log.debug("Action is allowed: Item pickup is allowed");
            return;
        }

        log.debug("Action not allowed: Pickup item");
        e.setCancelled();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        log.debug("DEBUG: " + e.getEventName());
        log.debug("DEBUG: Block is " + e.getBlock().getName());
        Player p = e.getPlayer();
        if (notInWorld(p)) {
            log.debug("Event is not in world");
            return;
        }
        if (p.isOp() || hasPermission(p, "is.mod.bypassprotect")) {
            return;
        }
        if (plugin.getIslandManager().locationIsOnIsland(p, e.getBlock()) || plugin.getIslandManager().locationIsOnIsland(p, p.getLocation())) {
            log.debug("Action is allowed: Player on island");
            // You can do anything on your island
            return;
        }
        // Player is not clicking a block, they are clicking a material so this
        // is driven by where the player is
        if (e.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && e.getBlock() == null) {
            log.debug("Action is allowed: Use clicking air");
            return;
        }

        switch (e.getAction()) {
            case PHYSICAL:
                // Only 2 of them check either its a Pressure Plate or Farmlands
                if (!Utils.actionPhysical(p, e.getBlock())) {
                    // No need stupid checks. Just cancel them
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled();
                    log.debug("Action is blocked PHYSICAL");
                    return;
                }
                break;
            case LEFT_CLICK_BLOCK:
                // Player is interacting with an item. Check if it allowed
                if (!Utils.isItemAllowed(p, e.getItem(), e.getBlock())) {
                    // No need stupid checks. Just cancel them
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled();
                    log.debug("Action is blocked LEFT_CLICK_BLOCK");
                    return;
                }
                break;
            case RIGHT_CLICK_BLOCK:
                // Player is clicking on something. Check if it allowed
                if (!Utils.isInventoryAllowed(p, e.getItem(), e.getBlock())) {
                    // No need stupid checks. Just cancel them
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled();
                    log.debug("Action is blocked RIGHT_CLICK_BLOCK");
                    return;
                }
                break;
        }

        log.debug("Action is allowed: Settings defined");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent e) {
        if (notInWorld(e.getPosition().getLocation())) {
            return;
        }
        if (e.getEntity() instanceof EntityPrimedTNT && ((EntityPrimedTNT) e.getEntity()).getSource() instanceof Player) {
            Player p = (Player) ((EntityPrimedTNT) e.getEntity()).getSource();
            if (actionAllowed(p, e.getEntity().getLocation(), SettingsFlag.BREAK_BLOCKS)) {
                return;
            }
            if (p.isOp() && hasPermission(p, "is.mod.bypassprotect")) {
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
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        // The damager is in the world but the entity who got attacked it is not? Oh no
//        if (notInWorld(e.getDamager().getLocation()) || notInWorld(e.getEntity())) {
//            return;
//        }
        // TODO: A subject
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        log.debug("DEBUG: " + e.getEventName());
        log.debug("DEBUG: Block is " + e.getBlock().toString());
        // Check if the player in the world
        if (notInWorld(e.getPlayer())) {
            log.debug("Event is not in world");
            return;
        }

        if (actionAllowed(e.getPlayer(), e.getBlock().getLocation(), SettingsFlag.PLACE_BLOCKS)) {
            log.debug("Action is allowed");
            return;
        }

        // Cancel them, obviously
        e.getPlayer().sendMessage(plugin.getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
        e.setCancelled();
        log.debug("Action is not allowed and cancelled");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(final BlockBreakEvent e) {
        log.debug("DEBUG: " + e.getEventName());
        log.debug("DEBUG: Block is " + e.getBlock().toString());
        if (notInWorld(e.getPlayer())) {
            log.debug("Event is not in world");
            return;
        }
        if (actionAllowed(e.getPlayer(), e.getBlock().getLocation(), SettingsFlag.BREAK_BLOCKS)) {
            log.debug("Action is allowed");
            return;
        }

        // Everyone else - not allowed
        e.getPlayer().sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
        e.setCancelled();
        log.debug("Action is not allowed and cancelled");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraft(CraftItemEvent event) {
        Player player = event.getPlayer();
        if (notInWorld(player)) {
            return;
        }
        if (event.getRecipe().getResult().getId() == ENDER_CHEST && !hasPermission(player, "is.craft.enderchest")) {
            player.sendMessage(plugin.getLocale(player).errorNoPermission);
            event.setCancelled(true);
        }
    }
}
