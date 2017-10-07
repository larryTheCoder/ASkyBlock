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
 */
package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityMinecartTNT;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.item.EntityVehicle;
import cn.nukkit.entity.mob.EntityCreeper;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.*;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.potion.PotionCollideEvent;
import cn.nukkit.event.vehicle.VehicleMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BlockIterator;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.IslandEnterEvent;
import com.larryTheCoder.events.IslandExitEvent;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.BlockUtil;
import com.larryTheCoder.utils.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static cn.nukkit.item.Item.*;

/**
 * Provides full protection to islands
 *
 * @author Adam Matthew
 */
public class IslandGuard implements Listener {

    private final ASkyBlock plugin;
    private final HashMap<UUID, Vector3> onPlate = new HashMap<>();
    private MainLogger deb = Server.getInstance().getLogger();

    /**
     * Island guard main instance Listener is under development
     *
     * @param plugin ASkyBlock instance
     */
    public IslandGuard(final ASkyBlock plugin) {
        this.plugin = plugin;

    }

    /**
     * Determines if an entity is in the island world or not or in the new
     * nether if it is activated
     *
     * @param entity
     * @return
     */
    protected boolean inWorld(Entity entity) {
        return inWorld(entity.getLocation());
    }

    /**
     * Determines if a block is in the island world or not
     *
     * @param block
     * @return true if in the island world
     */
    protected boolean inWorld(Block block) {
        return inWorld(block.getLocation());
    }

    /**
     * Determines if a location is in the island world or not or in the new
     * nether if it is activated
     *
     * @param loc
     * @return true if in the island world
     */
    protected boolean inWorld(Location loc) {
        return ASkyBlock.get().level.contains(loc.getLevel().getName());
    }

    /**
     * Checks if action is allowed for player in location for flag
     *
     * @param player
     * @param location
     * @param flag
     * @return true if allowed
     */
    private boolean actionAllowed(Player player, Location location, IslandData.SettingsFlag flag) {
        if (player == null) {
            return actionAllowed(location, flag);
        }
        // This permission bypasses protection
        if (player.isOp() || player.hasPermission("is.mod.bypassprotect")) {
            return true;
        }
        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        if (island.owner == null) {
            return false;
        }
        if (island.getIgsFlag(flag) || island.owner.equalsIgnoreCase(player.getName())) {
            return true;
        }
        return Settings.defaultWorldSettings.get(flag);
    }

    /**
     * Action allowed in this location
     *
     * @param location
     * @param flag
     * @return true if allowed
     */
    private boolean actionAllowed(Location location, IslandData.SettingsFlag flag) {
        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        if (island != null && island.getIgsFlag(flag)) {
            return true;
        }
        // Sometimes this can be null (So default is false)
        return Settings.defaultWorldSettings.get(flag) == null ? false : Settings.defaultWorldSettings.get(flag);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleMove(final VehicleMoveEvent e) {
        deb.debug("vehicle move = " + e.getVehicle());
        if (!inWorld(e.getVehicle())) {
            return;
        }

        Entity passenger = e.getVehicle().riding;
        if (passenger == null || !(passenger instanceof Player)) {
            return;
        }

        Player player = (Player) passenger;
        if (plugin.getGrid() == null) {
            deb.debug("DEBUG: grid = null");
            return;
        }

        IslandData islandTo = plugin.getGrid().getProtectedIslandAt(e.getTo());
        // Announcement entering
        IslandData islandFrom = plugin.getGrid().getProtectedIslandAt(e.getFrom());
        // Only says something if there is a change in islands
        /*
         * Situations:
         * islandTo == null && islandFrom != null - exit
         * islandTo == null && islandFrom == null - nothing
         * islandTo != null && islandFrom == null - enter
         * islandTo != null && islandFrom != null - same PlayerIsland or teleport?
         * islandTo == islandFrom
         */
//        deb.debug("islandTo = " + islandTo);
//        deb.debug("islandFrom = " + islandFrom);

        if (islandTo != null && (islandTo.owner != null || islandTo.isSpawn())) {
            // Lock check
            if (islandTo.locked) {//|| plugin.getPlayers().isBanned(islandTo.owner, player)) {
                if (!islandTo.getMembers().contains(player.getName()) && !player.isOp()
                    && !player.hasPermission("is.mod.bypassprotect")
                    && !player.hasPermission("is.mod.bypasslock")) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This island is locked");

                    // Get the vector away from this island
                    Vector3 v = e.getVehicle().subtract(islandTo.getCenter()).normalize();
                    v.x *= 1.2;
                    v.z *= 1.2;
                    e.getVehicle().setMotion(v);
                    return;
                }
            }
        }
        if (islandTo != null && islandFrom == null && (islandTo.owner != null || islandTo.isSpawn())) {
            // Entering
            if (islandTo.locked) {
                player.sendMessage(plugin.getPrefix() + TextFormat.RED + "This island is locked");
            }
            if (islandTo.isSpawn()) {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering spawn area");
                }
            } else {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.name + "'s island");
                }
            }
            // Fire entry event
            final IslandEnterEvent event = new IslandEnterEvent(player, islandTo, e.getTo());
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (islandTo == null && islandFrom != null && (islandFrom.owner != null || islandFrom.isSpawn())) {
            // Leaving
            if (islandFrom.isSpawn()) {
                // Leaving
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving spawn area");
                }

            } else {
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.name + "'s island");
                }

            }
            // Fire exit event
            final IslandExitEvent event = new IslandExitEvent(player, islandFrom, e.getFrom());
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (islandTo != null && islandFrom != null && !islandTo.equals(islandFrom)) {
            // Adjacent islands or overlapping protections
            if (islandFrom.isSpawn()) {
                // Leaving
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving spawn area");
                }
            } else if (islandFrom.owner != null) {
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.name + "'s island");
                }
            }
            if (islandTo.isSpawn()) {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering spawn area");
                }
            } else if (islandTo.owner != null) {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    player.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.name + "'s island");
                }
            }
            // Fire exit event
            final IslandExitEvent event = new IslandExitEvent(player, islandFrom, e.getFrom());
            plugin.getServer().getPluginManager().callEvent(event);
            // Fire entry event
            final IslandEnterEvent event2 = new IslandEnterEvent(player, islandTo, e.getTo());
            plugin.getServer().getPluginManager().callEvent(event2);
        }
    }

    /**
     * Adds island lock function
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent e) {
        if (!e.getPlayer().isAlive()) {
            return;
        }
        if (!inWorld(e.getPlayer())) {
            return;
        }
        if (plugin.getGrid() == null) {
            return;
        }
        // Only do something if there is a definite x or z movement
        if (e.getTo().getFloorX() - e.getFrom().getFloorX() == 0 && e.getTo().getFloorZ() - e.getFrom()
            .getFloorZ() == 0) {
            return;
        }
        final IslandData islandTo = plugin.getGrid().getProtectedIslandAt(e.getTo());
        // Announcement entering
        final IslandData islandFrom = plugin.getGrid().getProtectedIslandAt(e.getFrom());
        Player p = e.getPlayer();
        // Only says something if there is a change in islands
        /*
         * Situations:
         * islandTo == null && islandFrom != null - exit
         * islandTo == null && islandFrom == null - nothing
         * islandTo != null && islandFrom == null - enter
         * islandTo != null && islandFrom != null - same PlayerIsland or teleport?
         * islandTo == islandFrom
         */
//        deb.debug("islandTo = " + islandTo);
//        deb.debug("islandFrom = " + islandFrom);
        if (islandTo != null && (islandTo.owner != null || islandTo.isSpawn())) {
            // Lock check
            if (islandTo.locked) {
                if (!islandTo.getMembers().contains(p.getName()) && !p.isOp()
                    && !p.hasPermission("is.mod.bypassprotect")
                    && !p.hasPermission("is.mod.bypasslock")) {
                    if (p.riding != null) {
                        // Dismount
                        ((EntityVehicle) p.riding).mountEntity(p);
                        e.setCancelled(true);
                    } else {
                        Vector3 v = p.subtract(islandTo.getCenter()).normalize();
                        v.x *= 1.2;
                        v.z *= 1.2;
                        p.setMotion(v);
                    }
                    return;
                }
            }
        }

        if (islandTo != null && islandFrom == null && (islandTo.owner != null || islandTo.isSpawn())) {
            // Entering
            if (islandTo.locked) {
                p.sendMessage(plugin.getPrefix() + TextFormat.RED + "This island is locked");
            }
            if (islandTo.isSpawn()) {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering spawn area");
                }
            } else {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.name + "'s island");
                }
            }
            // Fire entry event
            final IslandEnterEvent event = new IslandEnterEvent(p, islandTo, e.getTo());
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (islandTo == null && islandFrom != null && (islandFrom.owner != null || islandFrom.isSpawn())) {
            // Leaving
            if (islandFrom.isSpawn()) {
                // Leaving
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving spawn area");
                }

            } else {
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.name + "'s island");
                }

            }
            // Fire exit event
            final IslandExitEvent event = new IslandExitEvent(p, islandFrom, e.getFrom());
            plugin.getServer().getPluginManager().callEvent(event);
        } else if (islandTo != null && islandFrom != null && !islandTo.equals(islandFrom)) {
            // Adjacent islands or overlapping protections
            if (islandFrom.isSpawn()) {
                // Leaving
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving spawn area");
                }
            } else if (islandFrom.owner != null) {
                if (islandFrom.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.name + "'s island");
                }
            }
            if (islandTo.isSpawn()) {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering spawn area");
                }
            } else if (islandTo.owner != null) {
                if (islandTo.getIgsFlag(IslandData.SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering " + islandTo.name + "'s island");
                }
            }
            // Fire exit event
            final IslandExitEvent event = new IslandExitEvent(p, islandFrom, e.getFrom());
            plugin.getServer().getPluginManager().callEvent(event);
            // Fire entry event
            final IslandEnterEvent event2 = new IslandEnterEvent(p, islandTo, e.getTo());
            plugin.getServer().getPluginManager().callEvent(event2);
        }
    }

    /**
     * Handles interaction with objects
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (!inWorld(e.getPlayer())) {
            return;
        }
        Player p = e.getPlayer();
        if (e.getPlayer().isOp() || p.hasPermission("is.mod.bypassprotect")) {
            return;
        }

        if (plugin.getIsland().locationIsOnIsland(e.getPlayer(), e.getBlock())) {
            // You can do anything on your island
            deb.debug("The player in on his island");
            return;
        }
        // Player is not clicking a block, they are clicking a material so this
        // is driven by where the player is
        if (e.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && (e.getBlock() != null && plugin.getGrid().playerIsOnIsland(e.getPlayer()))) {
            deb.debug("The player in clicking on the block");
            return;
        }
        // Get island
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getPlayer().getLocation());
        // Check for disallowed clicked blocks
        if (e.getBlock() != null) {
            // Look along player's sight line to see if any blocks are fire
            try {
                //Level level, Vector3 start, Vector3 direction, double yOffset,
                BlockIterator iter = new BlockIterator(p.getLevel(), p.getLocation(), p.getDirectionVector(), p.getEyeHeight(), 10);
                Block lastBlock;
                while (iter.hasNext()) {
                    lastBlock = iter.next();
                    if (lastBlock.equals(e.getBlock())) {
                        break;
                    }
                    if (lastBlock.getId() == Block.FIRE) {
                        if (island != null) {
                            if (!island.getIgsFlag(IslandData.SettingsFlag.FIRE_EXTINGUISH)) {
                                p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                                e.setCancelled(true);
                                return;
                            }
                        } else {
                            if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.FIRE_EXTINGUISH)) {
                            } else {
                                p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // To catch at block iterator exceptions that can happen in the void or at the very top of blocks
                deb.debug("DEBUG: block iterator error");
                ex.printStackTrace();

            }
            // Handle Shulker Boxes
            if (e.getBlock().getName().contains("SHULKER_BOX")) {
                if (island == null) {
                    if (!Settings.defaultWorldSettings.get(IslandData.SettingsFlag.CHEST)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                    }
                } else if (!island.getIgsFlag(IslandData.SettingsFlag.CHEST)) {
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
                return;
            }
            // Handle fireworks
            if (e.getItem() != null && e.getItem().equals(Item.FIRE_CHARGE)) {
                if (island == null) {
                    if (!Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PLACE_BLOCKS)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                    }
                } else if (!island.getIgsFlag(IslandData.SettingsFlag.PLACE_BLOCKS)) {
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
                return;
            }

            switch (e.getBlock().getId()) {
                case WOODEN_DOOR:
                case SPRUCE_DOOR:
                case ACACIA_DOOR:
                case DARK_OAK_DOOR:
                case BIRCH_DOOR:
                case JUNGLE_DOOR:
                case TRAPDOOR:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.DOOR)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.DOOR)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case FENCE_GATE:
                case FENCE_GATE_SPRUCE:
                case FENCE_GATE_ACACIA:
                case FENCE_GATE_DARK_OAK:
                case FENCE_GATE_BIRCH:
                case FENCE_GATE_JUNGLE:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.GATE)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.GATE)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case ENDER_CHEST:
                    break;
                case CHEST:
                case TRAPPED_CHEST:
                case DISPENSER:
                case DROPPER:
                case HOPPER:
                case MINECART_WITH_HOPPER:
                case MINECART_WITH_CHEST:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.CHEST)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.CHEST)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case GRASS:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.CROP_TRAMPLE)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.CROP_TRAMPLE)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case BREWING_STAND:
                case CAULDRON:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.BREWING)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.BREWING)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case REDSTONE:
                case REPEATER:
                case UNPOWERED_REPEATER:
                case COMPARATOR:
                case DAYLIGHT_DETECTOR:
                case DAYLIGHT_DETECTOR_INVERTED:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.REDSTONE)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.REDSTONE)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case ENCHANTMENT_TABLE:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.ENCHANTING)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.ENCHANTING)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case FURNACE:
                case BURNING_FURNACE:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.FURNACE)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.FURNACE)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case ICE:
                    break;
                case ITEM_FRAME:
                    break;
                case NOTEBLOCK:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.MUSIC)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.MUSIC)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case PACKED_ICE:
                    break;
                case STONE_BUTTON:
                case WOODEN_BUTTON:
                case LEVER:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.LEVER_BUTTON)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.LEVER_BUTTON)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case TNT:
                    break;
                case WORKBENCH:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.CRAFTING)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.CRAFTING)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case ANVIL:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.ANVIL)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.ANVIL)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case RAIL:
                case POWERED_RAIL:
                case DETECTOR_RAIL:
                case ACTIVATOR_RAIL:
                    // If they are not on an island, it's protected
                    if (island == null) {
                        if (!Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PLACE_BLOCKS)) {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                        }
                        return;
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.PLACE_BLOCKS)) {
                        if (e.getItem().equals(MINECART) || e.getItem().equals(MINECART_WITH_CHEST) || e.getItem().equals(MINECART_WITH_HOPPER)
                            || e.getItem().equals(MINECART_WITH_TNT)) {
                            e.setCancelled(true);
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            return;
                        }
                    }
                case BEACON:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.BEACON)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.BEACON)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case CAKE_BLOCK:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.BREAK_BLOCKS)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.getPlayer().getFoodData().setLevel(e.getPlayer().getFoodData().getLevel() - 2);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.BREAK_BLOCKS)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.getPlayer().getFoodData().setLevel(e.getPlayer().getFoodData().getLevel() - 2);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case DRAGON_EGG:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.BREAK_BLOCKS)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.BREAK_BLOCKS)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case MONSTER_SPAWNER:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.BREAK_BLOCKS)) {
                            return;
                        } else {
                            p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(IslandData.SettingsFlag.BREAK_BLOCKS)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case BED_BLOCK:
                    if (e.getPlayer().getLevel().getDimension() == Level.DIMENSION_NETHER) {
                        // Prevent explosions
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                        return;
                    }
                default:
                    break;
            }
        }
        // Check for disallowed in-hand items
        if (e.getItem() != null) {
            // This check protects against an exploit in 1.7.9 against cactus
            // and sugar cane
            if (e.getItem().equals(WOODEN_DOOR)
                || e.getItem().equals(CHEST)
                || e.getItem().equals(TRAPPED_CHEST)
                || e.getItem().equals(IRON_DOOR)) {
                if ((island == null && Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PLACE_BLOCKS))
                    || (island != null && !island.getIgsFlag(IslandData.SettingsFlag.PLACE_BLOCKS))) {
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
            } else if (e.getItem().getName().contains("BOAT") && (e.getBlock() != null && !BlockUtil.isFluid(e.getBlock()))) {
                // Trying to put a boat on non-liquid
                if ((island == null && Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PLACE_BLOCKS))
                    || (island != null && !island.getIgsFlag(IslandData.SettingsFlag.PLACE_BLOCKS))) {
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
            } else if (e.getItem().equals(ENDER_PEARL)) {
                if ((island == null && Settings.defaultWorldSettings.get(IslandData.SettingsFlag.ENDER_PEARL))
                    || (island != null && !island.getIgsFlag(IslandData.SettingsFlag.ENDER_PEARL))) {
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
            } else if (e.getItem().equals(FLINT_AND_STEEL)) {
                deb.debug("DEBUG: flint & steel");
                if (e.getBlock() != null) {
                    if (e.getItem().equals(OBSIDIAN)) {
                        deb.debug("DEBUG: flint & steel on obsidian");
                        //return;
                    }
                    if (!actionAllowed(e.getPlayer(), e.getBlock().getLocation(), IslandData.SettingsFlag.FIRE)) {
                        p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                        e.setCancelled(true);
                    }
                }
            } else if (e.getItem().equals(MONSTER_EGG)) {
                if (!actionAllowed(e.getPlayer(), e.getBlock().getLocation(), IslandData.SettingsFlag.SPAWN_EGGS)) {
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
            } else if (e.getItem().equals(SPLASH_POTION)) {
                // Potion
                deb.debug("DEBUG: potion");
                try {
                    // Check PVP
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PVP)) {
                            return;
                        }
                    }
                    // Not allowed
                    p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                } catch (Exception ex) {
                }
            }
            // Everything else is okay
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onExplosion(final EntityExplodeEvent e) {
        if (!inWorld(e.getPosition().getLocation())) {
            return;
        }
        // Find out what is exploding
        Entity expl = e.getEntity();
        if (expl == null) {
            // This allows beds to explode or other null entities, but still curtail the damage
            // Note player can still die from beds exploding in the nether.
            if (!Settings.allowTNTDamage) {
                deb.debug("TNT block damage prevented");
                e.getBlockList().clear();
            } else {
                if (!Settings.allowChestDamage) {
                    List<Block> toberemoved = new ArrayList<>();
                    // Save the chest blocks in a list
                    for (Block b : e.getBlockList()) {
                        switch (b.getId()) {
                            case CHEST:
                            case ENDER_CHEST:
                            case MINECART_WITH_CHEST:
                            case TRAPPED_CHEST:
                                toberemoved.add(b);
                                break;
                            default:
                                break;
                        }
                    }
                    // Now delete them
                    toberemoved.forEach((b) -> {
                        e.getBlockList().remove(b);
                    });
                }
            }
            return;
        }
        // prevent at spawn
        if (plugin.getGrid().isAtSpawn(e.getPosition().getLocation())) {
            e.setCancelled(true);
        }
        // Find out what is exploding
        Entity exploding = e.getEntity();
        if (exploding == null) {
            return;
        }
        switch (exploding.getNetworkId()) {
            case EntityCreeper.NETWORK_ID:
                if (!Settings.allowCreeperDamage) {
                    deb.debug("Creeper block damage prevented");
                    e.getBlockList().clear();
                } else {
                    // Check if creeper griefing is allowed
                    if (!Settings.allowCreeperGriefing) {
                        // Find out who the creeper was targeting
                        EntityCreeper creeper = (EntityCreeper) e.getEntity();
//                        if (creeper.get() instanceof Player) {
//                            Player target = (Player) creeper.getTarget();
//                            // Check if the target is on their own island or not
//                            if (!plugin.getGrid().locationIsOnIsland(target, e.getLocation())) {
//                                // They are a visitor tsk tsk
//                                // Stop the blocks from being damaged, but allow hurt still
//                                e.getBlockList().clear();
//                            }
//                        }
                    }
                    if (!Settings.allowChestDamage) {
                        List<Block> toberemoved = new ArrayList<>();
                        // Save the chest blocks in a list
                        for (Block b : e.getBlockList()) {
                            switch (b.getId()) {
                                case CHEST:
                                case ENDER_CHEST:
                                case MINECART_WITH_CHEST:
                                case TRAPPED_CHEST:
                                    toberemoved.add(b);
                                    break;
                                default:
                                    break;
                            }
                        }
                        // Now delete them
                        toberemoved.forEach((b) -> {
                            e.getBlockList().remove(b);
                        });
                    }
                }
                break;
            case EntityPrimedTNT.NETWORK_ID:
            case EntityMinecartTNT.NETWORK_ID:
                if (!Settings.allowTNTDamage) {
                    deb.debug("TNT block damage prevented");
                    e.getBlockList().clear();
                } else {
                    if (!Settings.allowChestDamage) {
                        List<Block> toberemoved = new ArrayList<>();
                        // Save the chest blocks in a list
                        for (Block b : e.getBlockList()) {
                            switch (b.getId()) {
                                case CHEST:
                                case ENDER_CHEST:
                                case MINECART_WITH_CHEST:
                                case TRAPPED_CHEST:
                                    toberemoved.add(b);
                                    break;
                                default:
                                    break;
                            }
                        }
                        // Now delete them
                        toberemoved.forEach((b) -> {
                            e.getBlockList().remove(b);
                        });
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * This method protects players from PVP if it is not allowed and from
     * arrows fired by other players
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageByEntityEvent e) {
        deb.debug(e.getEventName());
        deb.debug("DEBUG: Damager = " + e.getDamager().toString());
        deb.debug("DEBUG: Entity = " + e.getEntity());

        // Check world
        if (!inWorld(e.getEntity())) {
            return;
        }
        // Get the island where the damage is occurring
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getEntity().getLocation());
        // Stop TNT damage if it is disallowed
        if (!Settings.allowTNTDamage && (e.getDamager().getNetworkId() == EntityPrimedTNT.NETWORK_ID)) {
            deb.debug("DEBUG: cancelling tnt or fireball damage");
            e.setCancelled(true);
            return;
        }
        // Check for creeper damage at spawn
        if (island != null && island.isSpawn() && e.getDamager().getNetworkId() == EntityCreeper.NETWORK_ID && island.getIgsFlag(IslandData.SettingsFlag.CREEPER_PAIN)) {
            return;
        }
        // Stop Creeper damager if it is disallowed
        if (!Settings.allowCreeperDamage && e.getDamager().getNetworkId() == EntityCreeper.NETWORK_ID && !(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            return;
        }
        // Stop Creeper griefing if it is disallowed
        if (!Settings.allowCreeperGriefing && e.getDamager().getNetworkId() == EntityCreeper.NETWORK_ID) {
            // Now we have to check what the target was
            EntityCreeper creeper = (EntityCreeper) e.getDamager();
//            deb.debug("DEBUG: creeper is damager");
//            deb.debug("DEBUG: entity being damaged is " + e.getEntity());
//            if (creeper.getinstanceof Player) {
//                deb.debug("DEBUG: target is a player");
//                Player target = (Player) creeper.getTarget();
//                deb.debug("DEBUG: player = " + target.getName());
//                // Check if the target is on their own island or not
//                if (!plugin.getGrid().locationIsOnIsland(target, e.getEntity().getLocation())) {
//                    // They are a visitor tsk tsk
//                    deb.debug("DEBUG: player is a visitor");
//                    e.setCancelled(true);
//                    return;
//                }
//            }
        }
        // Ops can do anything
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (p.isOp() || p.hasPermission("is.mod.bypassprotect")) {
                return;
            }
        }
        // Get the real attacker
        boolean flamingArrow = false;
        boolean projectile = false;
        Player attacker = null;
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof EntityProjectile) {
            deb.debug("DEBUG: Projectile damage");
            projectile = true;
            // Find out who fired the arrow
            EntityProjectile p = (EntityProjectile) e.getDamager();
            deb.debug("DEBUG: Shooter is " + p.shootingEntity.toString());
            if (p.shootingEntity instanceof Player) {
                attacker = (Player) p.shootingEntity;
                // Check if this is a flaming arrow
                // Not available #PPPLA
            }
        }
        if (attacker == null) {
            // Not a player
            return;
        }
        // Self damage
        if (e.getEntity() instanceof Player && attacker.equals(e.getEntity())) {
            deb.debug("Self damage!");
            return;
        }
        deb.debug("DEBUG: Another player");

        // Establish whether PVP is allowed or not.
        boolean pvp = false;
        if ((island != null && island.getIgsFlag(IslandData.SettingsFlag.NETHER_PVP) || (island != null && island.getIgsFlag(IslandData.SettingsFlag.PVP)))) {
            deb.debug("DEBUG: PVP allowed");
            pvp = true;
        }

        // Players being hurt PvP
        if (e.getEntity() instanceof Player) {
            if (pvp) {
            } else {
                attacker.sendMessage(TextFormat.RED + "This island is PVP protected");
                if (flamingArrow) {
                    e.getEntity().setOnFire(0);
                }
                if (projectile) {
                    e.getDamager().kill();
                }
                e.setCancelled(true);
            }
        }
    }

    /**
     * Prevents placing of blocks
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBlockPlace(final BlockPlaceEvent e) {
        deb.debug("DEBUG: " + e.getEventName());
        if (e.getPlayer() == null) {
            deb.debug("DEBUG: player is null");
        } else {
            deb.debug("DEBUG: block placed by " + e.getPlayer().getName());
        }
        deb.debug("DEBUG: Block is " + e.getBlock().toString());

        deb.debug(e.getEventName());
        if (inWorld(e.getPlayer())) {
            // This permission bypasses protection
            if (e.getPlayer().isOp() && e.getPlayer().hasPermission("is.mod.bypassprotect")) {
                return;
            }
            deb.debug("DEBUG: checking is inside protection area");
            IslandData island = plugin.getGrid().getProtectedIslandAt(e.getBlock().getLocation());
            // Outside of island protection zone
            if (island == null) {
                if (!Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PLACE_BLOCKS)) {
                    e.getPlayer().sendMessage(plugin.getLocale(e.getPlayer()).islandProtected);
                    e.setCancelled(true);
                }
                return;
            }
            if (actionAllowed(e.getPlayer(), e.getBlock().getLocation(), IslandData.SettingsFlag.PLACE_BLOCKS)) {
                // Maybe in team or else
            } else {
                // Visitor
                e.getPlayer().sendMessage(plugin.getLocale(e.getPlayer()).islandProtected);
                e.setCancelled(true);
            }
        }
    }

    public void onSplashPotion(PotionCollideEvent event) {
    }

    /**
     * Prevents crafting of EnderChest unless the player has permission
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onCraft(CraftItemEvent event) {
        Player player = event.getPlayer();
        if (inWorld(player)) {
            if (event.getRecipe().getResult().getId() == ENDER_CHEST) {
                if (!(player.hasPermission("is.craft.enderchest"))) {
                    player.sendMessage(plugin.getLocale(player).errorNoPermission);
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Prevents usage of an Ender Chest
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnderChestEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (inWorld(player)) {
            if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                if (event.getBlock().getId() == ENDER_CHEST) {
                    if (!(event.getPlayer().hasPermission("is.craft.enderchest"))) {
                        player.sendMessage(plugin.getLocale(player).errorNoPermission);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Handles hitting minecarts or feeding animals
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerHitEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!inWorld(p)) {
            return;
        }
        if (p.isOp() || p.hasPermission("is.mod.bypassprotect")) {
            // You can do anything if you are Op of have the bypass
            return;
        }
        // Leashes are dealt with elsewhere
        if (p.getInventory().getItemInHand() != null && p.getInventory().getItemInHand().getId() == LEAD) {
            return;
        }
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getPlayer().getLocation());
        if (!plugin.getGrid().playerIsOnIsland(e.getPlayer())) {
            // Not on island
            // Minecarts and other storage entities
            deb.debug("DEBUG: " + e.getItem().toString());
            deb.debug("DEBUG: " + p.getInventory().getItemInHand());
            // Handle name tags and dyes
            if (p.getInventory().getItemInHand() != null && (p.getInventory().getItemInHand().getId() == NAME_TAG
                || p.getInventory().getItemInHand().getId() == Item.DYE)) {
                p.sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
                e.setCancelled(true);
            }
        }
    }

    /**
     * Prevents fire spread
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (!inWorld(e.getBlock())) {
            deb.debug("DEBUG: Not in world");
            return;
        }
        if (actionAllowed(e.getBlock().getLocation(), IslandData.SettingsFlag.FIRE_SPREAD)) {
            return;
        }
        e.setCancelled(true);
    }

    /**
     * Prevents blocks from being broken
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        deb.debug(e.getEventName());

        if (inWorld(e.getPlayer())) {
            if (actionAllowed(e.getPlayer(), e.getBlock().getLocation(), IslandData.SettingsFlag.BREAK_BLOCKS)) {
                return;
            }
            // Everyone else - not allowed
            e.getPlayer().sendMessage(getPrefix() + plugin.getLocale(e.getPlayer()).islandProtected);
            e.setCancelled(true);
        }
    }

    /**
     * Prevent fire spread
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        if (e.getSource().getId() == Block.FIRE) {
            if (!inWorld(e.getBlock())) {
                deb.debug("DEBUG: Not in world");
                return;
            }
            if (actionAllowed(e.getBlock().getLocation(), IslandData.SettingsFlag.FIRE_SPREAD)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        deb.debug(e.getEventName());
        deb.debug(e.getCause().name());

        if (!inWorld(e.getBlock())) {
            deb.debug("DEBUG: Not in world");
            return;
        }
        // Check if this is a portal lighting
        if (e.getBlock() != null && e.getBlock().getId() == Block.OBSIDIAN) {
            deb.debug("DEBUG: portal lighting");
            return;
        }
        if (e.getCause() != null) {
            deb.debug("DEBUG: ignite cause = " + e.getCause());

            switch (e.getCause()) {
                //case ENDER_CRYSTAL:
                case EXPLOSION:
                case FIREBALL:
                case LIGHTNING:
                    if (!actionAllowed(e.getBlock().getLocation(), IslandData.SettingsFlag.FIRE)) {
                        deb.debug("DEBUG: canceling fire");
                        e.setCancelled(true);
                    }
                    break;
                case FLINT_AND_STEEL:
                    deb.debug("DEBUG: block = " + e.getBlock());

                    // Check if this is allowed
                    if (e.getEntity() != null
                        && e.getEntity() instanceof Player
                        && (((Player) e.getEntity()).isOp()
                        || ((Player) e.getEntity()).hasPermission("is.mod.bypass"))) {
                        return;
                    }
                    if (!actionAllowed(e.getBlock().getLocation(), IslandData.SettingsFlag.FIRE)) {
                        deb.debug("DEBUG: canceling fire");

                        // If this was not a player, just stop it
                        if (e.getEntity() == null) {
                            e.setCancelled(true);
                            break;
                        }
                        // Get target block
                        Block targetFloor = e.getBlock().down();
                        if (targetFloor.getId() == Block.OBSIDIAN) {

                        } else {
                            e.setCancelled(true);
                        }
                    }
                    break;

                case LAVA:
                case SPREAD:
                    // Check if this is a portal lighting
                    if (e.getBlock() != null && e.getBlock().getId() == Block.OBSIDIAN) {
                        deb.debug("DEBUG: obsidian lighting");
                        return;
                    }
                    if (!actionAllowed(e.getBlock().getLocation(), IslandData.SettingsFlag.FIRE_SPREAD)) {
                        deb.debug("DEBUG: canceling fire spread");
                        e.setCancelled(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Pressure plates
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlateStep(PlayerInteractEvent e) {
        if (!inWorld(e.getPlayer())
            || e.getAction() != PlayerInteractEvent.Action.PHYSICAL
            || e.getPlayer().isOp()
            || e.getPlayer().hasPermission("is.mod.bypassprotect")
            || plugin.getGrid().playerIsOnIsland(e.getPlayer())) {
            deb.debug("DEBUG: Haz permission to do this");
            return;
        }
        // Check island
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getPlayer().getLocation());
        if ((island == null && Settings.defaultWorldSettings.get(IslandData.SettingsFlag.PRESSURE_PLATE))) {
            return;
        }
        if (island != null && island.getIgsFlag(IslandData.SettingsFlag.PRESSURE_PLATE)) {
            return;
        }
        // Block action
        UUID playerUUID = e.getPlayer().getUniqueId();
        if (!onPlate.containsKey(playerUUID)) {
            Vector3 v = e.getPlayer().getLocation();
            onPlate.put(playerUUID, new Vector3(v.getFloorX(), v.getFloorY(), v.getFloorZ()));
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerLogin(PlayerPreLoginEvent ex) {
        Player p = ex.getPlayer();
        plugin.getIslandInfo(p); // laod the player islands
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ex) {
        // load player inventory if exsits
        Player p = ex.getPlayer();
        plugin.getInventory().loadPlayerInventory(p);
        // Load player datatatatata tadaaaa
        if (plugin.getPlayerInfo(p) == null) {
            com.larryTheCoder.utils.Utils.send(p.getName() + " &adata doesn`t exsits. Creating new ones");
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

    private String getPrefix() {
        return plugin.getPrefix();
    }
}
