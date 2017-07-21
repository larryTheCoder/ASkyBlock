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
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.item.EntityMinecartTNT;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.mob.EntityCreeper;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBurnEvent;
import cn.nukkit.event.block.BlockSpreadEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.potion.PotionCollideEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BlockIterator;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.IslandEnterEvent;
import com.larryTheCoder.events.IslandExitEvent;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.IslandData.SettingsFlag;
import com.larryTheCoder.utils.Settings;
import java.util.HashMap;
import java.util.UUID;
import static cn.nukkit.item.Item.*;
import com.larryTheCoder.utils.BlockUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides full protection to islands
 *
 * @author Adam Matthew
 */
public class IslandGuard implements Listener {

    private final ASkyBlock plugin;
    private final HashMap<UUID, Vector3> onPlate = new HashMap<>();
    private final Set<Location> tntBlocks = new HashSet<>();
    private final Set<Long> litCreeper = new HashSet<>();

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
    protected static boolean inWorld(Entity entity) {
        return inWorld(entity.getLocation());
    }

    /**
     * Determines if a block is in the island world or not
     *
     * @param block
     * @return true if in the island world
     */
    protected static boolean inWorld(Block block) {
        return inWorld(block.getLocation());
    }

    /**
     * Determines if a location is in the island world or not or in the new
     * nether if it is activated
     *
     * @param loc
     * @return true if in the island world
     */
    protected static boolean inWorld(Location loc) {
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
    private boolean actionAllowed(Player player, Location location, SettingsFlag flag) {
        if (player == null) {
            return actionAllowed(location, flag);
        }
        // This permission bypasses protection
        if (player.isOp() || player.hasPermission("is.mod.bypassprotect")) {
            return true;
        }
        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        if (island != null && (island.getIgsFlag(flag) || island.getMembers().contains(player.getName()))) {
            return true;
        }
        return island == null && Settings.defaultWorldSettings.get(flag);
    }

    /**
     * Action allowed in this location
     *
     * @param location
     * @param flag
     * @return true if allowed
     */
    private boolean actionAllowed(Location location, SettingsFlag flag) {
        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        if (island != null && island.getIgsFlag(flag)) {
            return true;
        }
        return island == null && Settings.defaultWorldSettings.get(flag);
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
        // plugin.getLogger().info("islandTo = " + islandTo);
        // plugin.getLogger().info("islandFrom = " + islandFrom);
        if (islandTo != null && (islandTo.owner != null || islandTo.isSpawn())) {
            // Lock check
            if (islandTo.locked) {
                if (!islandTo.getMembers().contains(p.getName()) && !p.isOp()
                        && !p.hasPermission("is.mod.bypassprotect")
                        && !p.hasPermission("is.mod.bypasslock")) {

                    // Get the vector away from this island
                    if (p.isImmobile()) {
                        if (p.riding instanceof EntityLiving) {
                            // Dismount
                            p.riding.despawnFrom(p);
                            e.setCancelled(true);
                        }

                    } else {
                        e.setCancelled(true);
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
                if (islandTo.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering spawn area");
                }
            } else {
                if (islandTo.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
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
                if (islandFrom.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving spawn area");
                }

            } else {
                if (islandFrom.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
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
                if (islandFrom.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving spawn area");
                }
            } else if (islandFrom.owner != null) {
                if (islandFrom.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Leaving " + islandFrom.name + "'s island");
                }
            }
            if (islandTo.isSpawn()) {
                if (islandTo.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
                    p.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Entering spawn area");
                }
            } else if (islandTo.owner != null) {
                if (islandTo.getIgsFlag(SettingsFlag.ENTER_EXIT_MESSAGES)) {
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

    // Protect sheep
//    @EventHandler(priority = EventPriority.LOW)
//    public void onShear(final PlayerShearEntityEvent e) {
//        if (DEBUG) {
//            plugin.getLogger().info(e.getEventName());
//        }
//        if (inWorld(e.getPlayer())) {
//            if (actionAllowed(e.getPlayer(), e.getEntity().getLocation(), SettingsFlag.SHEARING)) {
//                return;
//            }
//            // Not allowed
//            p.sendMessage(TextFormat.RED + "Island protected.");
//            e.setCancelled(true);
//            return;
//        }
//    }
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
        if ((e.getBlock() != null && plugin.getIsland().locationIsOnIsland(e.getPlayer(), e.getBlock()))) {
            // You can do anything on your island
            return;
        }
        // Player is not clicking a block, they are clicking a material so this
        // is driven by where the player is
        if (e.getBlock() == null && (e.getBlock() != null && plugin.getGrid().playerIsOnIsland(e.getPlayer()))) {
            return;
        }
        // Get island
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getPlayer().getLocation());
        // Check for disallowed clicked blocks
        if (e.getBlock() != null) {
            // Look along player's sight line to see if any blocks are fire
            try {
                //Level level, Vector3 start, Vector3 direction, double yOffset, 
                BlockIterator iter = new BlockIterator(p.getLevel(), null, null, 0, 10);
                Block lastBlock;
                while (iter.hasNext()) {
                    lastBlock = iter.next();
                    if (lastBlock.equals(e.getBlock())) {
                        break;
                    }
                    if (lastBlock.getId() == Block.FIRE) {
                        if (island != null) {
                            if (!island.getIgsFlag(SettingsFlag.FIRE_EXTINGUISH)) {
                                p.sendMessage(TextFormat.RED + "Island protected.");
                                e.setCancelled(true);
                                return;
                            }
                        } else {
                            if (Settings.defaultWorldSettings.get(SettingsFlag.FIRE_EXTINGUISH)) {
                            } else {
                                p.sendMessage(TextFormat.RED + "Island protected.");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // To catch at block iterator exceptions that can happen in the void or at the very top of blocks
                plugin.getLogger().info("DEBUG: block iterator error");
                ex.printStackTrace();

            }
            // Handle Shulker Boxes
            if (e.getBlock().getName().contains("SHULKER_BOX")) {
                if (island == null) {
                    if (!Settings.defaultWorldSettings.get(SettingsFlag.CHEST)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                    }
                } else if (!island.getIgsFlag(SettingsFlag.CHEST)) {
                    p.sendMessage(TextFormat.RED + "Island protected.");
                    e.setCancelled(true);
                }
                return;
            }
            // Handle fireworks
            if (e.getItem() != null && e.getItem().equals(Item.FIRE_CHARGE)) {
                if (island == null) {
                    if (!Settings.defaultWorldSettings.get(SettingsFlag.PLACE_BLOCKS)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                    }
                } else if (!island.getIgsFlag(SettingsFlag.PLACE_BLOCKS)) {
                    p.sendMessage(TextFormat.RED + "Island protected.");
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
                        if (Settings.defaultWorldSettings.get(SettingsFlag.DOOR)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.DOOR)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
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
                        if (Settings.defaultWorldSettings.get(SettingsFlag.GATE)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.GATE)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
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
                        if (Settings.defaultWorldSettings.get(SettingsFlag.CHEST)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.CHEST)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case GRASS:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.CROP_TRAMPLE)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.CROP_TRAMPLE)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case BREWING_STAND:
                case CAULDRON:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.BREWING)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.BREWING)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
//                case DIODE:
//                case DIODE_BLOCK_OFF:
//                case DIODE_BLOCK_ON:
                case COMPARATOR:
                case DAYLIGHT_DETECTOR:
                case DAYLIGHT_DETECTOR_INVERTED:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.REDSTONE)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.REDSTONE)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case ENCHANTMENT_TABLE:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.ENCHANTING)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.ENCHANTING)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case FURNACE:
                case BURNING_FURNACE:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.FURNACE)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.FURNACE)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
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
                        if (Settings.defaultWorldSettings.get(SettingsFlag.MUSIC)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.MUSIC)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
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
                        if (Settings.defaultWorldSettings.get(SettingsFlag.LEVER_BUTTON)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.LEVER_BUTTON)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case TNT:
                    break;
                case WORKBENCH:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.CRAFTING)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.CRAFTING)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case ANVIL:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.ANVIL)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.ANVIL)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
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
                        if (!Settings.defaultWorldSettings.get(SettingsFlag.PLACE_BLOCKS)) {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                        }
                        return;
                    }
                    if (!island.getIgsFlag(SettingsFlag.PLACE_BLOCKS)) {
                        if (e.getItem().equals(MINECART) || e.getItem().equals(MINECART_WITH_CHEST) || e.getItem().equals(MINECART_WITH_HOPPER)
                                || e.getItem().equals(MINECART_WITH_TNT)) {
                            e.setCancelled(true);
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            return;
                        }
                    }
                case BEACON:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.BEACON)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.BEACON)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case CAKE_BLOCK:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.BREAK_BLOCKS)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.getPlayer().getFoodData().setFoodLevel(e.getPlayer().getFoodData().getLevel() - 2);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.BREAK_BLOCKS)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.getPlayer().getFoodData().setFoodLevel(e.getPlayer().getFoodData().getLevel() - 2);
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case DRAGON_EGG:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.BREAK_BLOCKS)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.BREAK_BLOCKS)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case MONSTER_SPAWNER:
                    if (island == null) {
                        if (Settings.defaultWorldSettings.get(SettingsFlag.BREAK_BLOCKS)) {
                            return;
                        } else {
                            p.sendMessage(TextFormat.RED + "Island protected.");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!island.getIgsFlag(SettingsFlag.BREAK_BLOCKS)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                        return;
                    }
                    break;
                case BED_BLOCK:
//                    if (e.getPlayer().getLevel().getDimension() == Dimension) {
//                        // Prevent explosions
//                        p.sendMessage(TextFormat.RED + "Island protected.");
//                        e.setCancelled(true);
//                        return;
//                    }
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
                if ((island == null && Settings.defaultWorldSettings.get(SettingsFlag.PLACE_BLOCKS))
                        || (island != null && !island.getIgsFlag(SettingsFlag.PLACE_BLOCKS))) {
                    p.sendMessage(TextFormat.RED + "Island protected.");
                    e.setCancelled(true);
                }
            } else if (e.getItem().getName().contains("BOAT") && (e.getBlock() != null && !BlockUtil.isFluid(e.getBlock()))) {
                // Trying to put a boat on non-liquid
                if ((island == null && Settings.defaultWorldSettings.get(SettingsFlag.PLACE_BLOCKS))
                        || (island != null && !island.getIgsFlag(SettingsFlag.PLACE_BLOCKS))) {
                    p.sendMessage(TextFormat.RED + "Island protected.");
                    e.setCancelled(true);
                }
            } else if (e.getItem().equals(ENDER_PEARL)) {
                if ((island == null && Settings.defaultWorldSettings.get(SettingsFlag.ENDER_PEARL))
                        || (island != null && !island.getIgsFlag(SettingsFlag.ENDER_PEARL))) {
                    p.sendMessage(TextFormat.RED + "Island protected.");
                    e.setCancelled(true);
                }
            } else if (e.getItem().equals(FLINT_AND_STEEL)) {
                plugin.getLogger().info("DEBUG: flint & steel");
                if (e.getBlock() != null) {
                    if (e.getItem().equals(OBSIDIAN)) {
                        plugin.getLogger().info("DEBUG: flint & steel on obsidian");
                        //return;
                    }
                    if (!actionAllowed(e.getPlayer(), e.getBlock().getLocation(), SettingsFlag.FIRE)) {
                        p.sendMessage(TextFormat.RED + "Island protected.");
                        e.setCancelled(true);
                    }
                }
            } else if (e.getItem().equals(MONSTER_EGG)) {
                if (!actionAllowed(e.getPlayer(), e.getBlock().getLocation(), SettingsFlag.SPAWN_EGGS)) {
                    p.sendMessage(TextFormat.RED + "Island protected.");
                    e.setCancelled(true);
                }
            } else if (e.getItem().equals(POTION)) {
                // Potion
                // plugin.getLogger().info("DEBUG: potion");
                try {
//                    if (pot.isSplash()) {
//                        // Splash potions are allowed only if PVP is allowed
//                        boolean inNether = false;
//                        if (e.getPlayer().getWorld().equals(ASkyBlock.getNetherWorld())) {
//                            inNether = true;
//                        }
//                        // Check PVP
//                        if (island == null) {
//                            if ((inNether && Settings.defaultWorldSettings.get(SettingsFlag.NETHER_PVP)
//                                    || (!inNether && Settings.defaultWorldSettings.get(SettingsFlag.PVP)))) {
//                                return;
//                            }
//                        } else {
//                            if ((inNether && island.getIgsFlag(SettingsFlag.NETHER_PVP) || (!inNether && island.getIgsFlag(SettingsFlag.PVP)))) {
//                                return;
//                            }
//                        }
//                        // Not allowed
//                        p.sendMessage(TextFormat.RED + "Island protected.");
//                        e.setCancelled(true);
//                    }
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
                //plugin.getLogger().info("TNT block damage prevented");
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
                    // plugin.getLogger().info("Creeper block damage prevented");
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
                        // Check if this creeper was lit by a visitor
                        if (litCreeper.contains(creeper.getId())) {
                            litCreeper.remove(creeper.getId());
                            e.setCancelled(true);
                            return;
                        }
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
                    // plugin.getLogger().info("TNT block damage prevented");
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
        plugin.getLogger().info(e.getEventName());
        plugin.getLogger().info("DEBUG: Damager = " + e.getDamager().toString());
        plugin.getLogger().info("DEBUG: Entity = " + e.getEntity());

        // Check world
        if (!inWorld(e.getEntity())) {
            return;
        }
        // Get the island where the damage is occurring
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getEntity().getLocation());
        // Stop TNT damage if it is disallowed
        if (!Settings.allowTNTDamage && (e.getDamager().getNetworkId() == EntityPrimedTNT.NETWORK_ID)) {
            plugin.getLogger().info("DEBUG: cancelling tnt or fireball damage");
            e.setCancelled(true);
            return;
        }
        // Check for creeper damage at spawn
        if (island != null && island.isSpawn() && e.getDamager().getNetworkId() == EntityCreeper.NETWORK_ID && island.getIgsFlag(SettingsFlag.CREEPER_PAIN)) {
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
//            //plugin.getLogger().info("DEBUG: creeper is damager");
//            //plugin.getLogger().info("DEBUG: entity being damaged is " + e.getEntity());
//            if (creeper.getinstanceof Player) {
//                //plugin.getLogger().info("DEBUG: target is a player");
//                Player target = (Player) creeper.getTarget();
//                //plugin.getLogger().info("DEBUG: player = " + target.getName());
//                // Check if the target is on their own island or not
//                if (!plugin.getGrid().locationIsOnIsland(target, e.getEntity().getLocation())) {
//                    // They are a visitor tsk tsk
//                    //plugin.getLogger().info("DEBUG: player is a visitor");
//                    e.setCancelled(true);
//                    return;
//                }
//            }
            // Check if this creeper was lit by a visitor
            if (litCreeper.contains(creeper.getId())) {
                plugin.getLogger().info("DEBUG: preventing creeeper from damaging");
                e.setCancelled(true);
                return;
            }
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
            plugin.getLogger().info("DEBUG: Projectile damage");
            projectile = true;
            // Find out who fired the arrow
            EntityProjectile p = (EntityProjectile) e.getDamager();
            plugin.getLogger().info("DEBUG: Shooter is " + p.shootingEntity.toString());
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
        if (e.getEntity() instanceof Player && attacker.equals((Player) e.getEntity())) {
            plugin.getLogger().info("Self damage!");
            return;
        }
        plugin.getLogger().info("DEBUG: Another player");

        // Establish whether PVP is allowed or not.
        boolean pvp = false;
        if ((island != null && island.getIgsFlag(SettingsFlag.NETHER_PVP) || (island != null && island.getIgsFlag(SettingsFlag.PVP)))) {
            plugin.getLogger().info("DEBUG: PVP allowed");
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

//    /**
//     * Prevents placing of blocks
//     *
//     * @param e
//     */
//    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//    public void onPlayerBlockPlace(final BlockPlaceEvent e) {
//        plugin.getLogger().info("DEBUG: " + e.getEventName());
//        if (e.getPlayer() == null) {
//            plugin.getLogger().info("DEBUG: player is null");
//        } else {
//            plugin.getLogger().info("DEBUG: block placed by " + e.getPlayer().getName());
//        }
//        plugin.getLogger().info("DEBUG: Block is " + e.getBlock().toString());
//
//        if (Settings.allowAutoActivator && e.getPlayer().getName().equals("[CoFH]")) {
//            return;
//        }
//        // plugin.getLogger().info(e.getEventName());
//        if (inWorld(e.getPlayer())) {
//            // This permission bypasses protection
//            if (e.getPlayer().isOp() || VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypassprotect")) {
//                return;
//            }
//            //plugin.getLogger().info("DEBUG: checking is inside protection area");
//            Island island = plugin.getGrid().getProtectedIslandAt(e.getBlock().getLocation());
//            // Outside of island protection zone
//            if (island == null) {
//                if (!Settings.defaultWorldSettings.get(SettingsFlag.PLACE_BLOCKS)) {
//                    Util.sendMessage(e.getPlayer(), ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).islandProtected);
//                    e.setCancelled(true);
//                }
//                return;
//            }
//            if (actionAllowed(e.getPlayer(), e.getBlock().getLocation(), SettingsFlag.PLACE_BLOCKS)) {
//                // Check how many placed
//                //plugin.getLogger().info("DEBUG: block placed " + e.getBlock().getType());
//                String type = e.getBlock().getType().toString();
//                if (!e.getBlock().getState().getClass().getName().endsWith("CraftBlockState")
//                        // Not all blocks have that type of class, so we have to do some explicit checking...
//                        || e.getBlock().getType().equals(Material.REDSTONE_COMPARATOR_OFF)
//                        || type.endsWith("BANNER") // Avoids V1.7 issues
//                        || e.getBlock().getType().equals(Material.ENDER_CHEST)
//                        || e.getBlock().getType().equals(Material.ENCHANTMENT_TABLE)
//                        || e.getBlock().getType().equals(Material.DAYLIGHT_DETECTOR)
//                        || e.getBlock().getType().equals(Material.FLOWER_POT)) {
//                    // tile entity placed
//                    if (Settings.limitedBlocks.containsKey(type) && Settings.limitedBlocks.get(type) > -1) {
//                        int count = island.getTileEntityCount(e.getBlock().getType(), e.getBlock().getWorld());
//                        //plugin.getLogger().info("DEBUG: count is "+ count);
//                        if (Settings.limitedBlocks.get(type) <= count) {
//                            Util.sendMessage(e.getPlayer(), ChatColor.RED + (plugin.myLocale(e.getPlayer().getUniqueId()).entityLimitReached.replace("[entity]",
//                                    Util.prettifyText(type))).replace("[number]", String.valueOf(Settings.limitedBlocks.get(type))));
//                            e.setCancelled(true);
//                            return;
//                        }
//                    }
//                }
//            } else {
//                // Visitor
//                Util.sendMessage(e.getPlayer(), ChatColor.RED + plugin.myLocale(e.getPlayer().getUniqueId()).islandProtected);
//                e.setCancelled(true);
//            }
//        }
//    }

    public void onSplashPotion(PotionCollideEvent event) {
    }

    /**
     * Prevents crafting of EnderChest unless the player has permission
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onCraft(CraftItemEvent event) {
        Player player = (Player) event.getPlayer();
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
        Player player = (Player) event.getPlayer();
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
//        if (p.getInventory().getInventory().getItemInHand() != null && p.getInventory().getInventory().getItemInHand().getId() == LEASH) {
//            return;
//        }
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getPlayer().getLocation());
        if (!plugin.getGrid().playerIsOnIsland(e.getPlayer())) {
            // Not on island
            // Minecarts and other storage entities
            //plugin.getLogger().info("DEBUG: " + e.getRightClicked().getType().toString());
            //plugin.getLogger().info("DEBUG: " + p.getInventory().getItemInHand());
            // Handle name tags and dyes
            if (p.getInventory().getItemInHand() != null && (p.getInventory().getItemInHand().getId() == NAME_TAG
                    || p.getInventory().getItemInHand().getId() == Item.DYE)) {
                p.sendMessage(TextFormat.RED + "Island protected.");
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
            //plugin.getLogger().info("DEBUG: Not in world");
            return;
        }
        if (actionAllowed(e.getBlock().getLocation(), SettingsFlag.FIRE_SPREAD)) {
            return;
        }
        e.setCancelled(true);
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
                //plugin.getLogger().info("DEBUG: Not in world");
                return;
            }
            if (actionAllowed(e.getBlock().getLocation(), SettingsFlag.FIRE_SPREAD)) {
                return;
            }
            e.setCancelled(true);
        }
    }
//
//    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
//    public void onBlockIgnite(final BlockIgniteEvent e) {
//        if (DEBUG) {
//            plugin.getLogger().info(e.getEventName());
//            plugin.getLogger().info(e.getCause().name());
//        }
//        if (!inWorld(e.getFloor())) {
//            //plugin.getLogger().info("DEBUG: Not in world");
//            return;
//        }
//        // Check if this is a portal lighting
//        if (e.getFloor() != null && e.getFloor().getType().equals(Material.OBSIDIAN)) {
//            if (DEBUG) {
//                plugin.getLogger().info("DEBUG: portal lighting");
//            }
//            return;
//        }
//        if (e.getCause() != null) {
//            if (DEBUG) {
//                plugin.getLogger().info("DEBUG: ignite cause = " + e.getCause());
//            }
//            switch (e.getCause()) {
//                case ENDER_CRYSTAL:
//                case EXPLOSION:
//                case FIREBALL:
//                case LIGHTNING:
//                    if (!actionAllowed(e.getFloor().getLocation(), SettingsFlag.FIRE)) {
//                        if (DEBUG) {
//                            plugin.getLogger().info("DEBUG: canceling fire");
//                        }
//                        e.setCancelled(true);
//                    }
//                    break;
//                case FLINT_AND_STEEL:
//                    Set<Material> transparent = new HashSet<Material>();
//                    transparent.add(Material.AIR);
//                    if (DEBUG) {
//                        plugin.getLogger().info("DEBUG: block = " + e.getFloor());
//                        plugin.getLogger().info("DEBUG: target block = " + e.getPlayer().getTargetFloor(transparent, 10));
//                    }
//                    // Check if this is allowed
//                    if (e.getPlayer() != null && (e.getPlayer().isOp() || VaultHelper.checkPerm(e.getPlayer(), Settings.PERMPREFIX + "mod.bypass"))) {
//                        return;
//                    }
//                    if (!actionAllowed(e.getFloor().getLocation(), SettingsFlag.FIRE)) {
//                        if (DEBUG) {
//                            plugin.getLogger().info("DEBUG: canceling fire");
//                        }
//                        // If this was not a player, just stop it
//                        if (e.getPlayer() == null) {
//                            e.setCancelled(true);
//                            break;
//                        }
//                        // Get target block
//                        Block targetFloor = e.getPlayer().getTargetFloor(transparent, 10);
//                        if (targetFloor.getType().equals(Material.OBSIDIAN)) {
//                            final MaterialData md = new MaterialData(e.getFloor().getType(), e.getFloor().getData());
//                            new BukkitRunnable() {
//
//                                @Override
//                                public void run() {
//                                    if (e.getFloor().getType().equals(Material.FIRE)) {
//                                        e.getFloor().setType(md.getItemType());
//                                        e.getFloor().setData(md.getData());
//                                    }
//
//                                }
//                            }.runTask(plugin);
//                        } else {
//                            e.setCancelled(true);
//                        }
//                    }
//                    break;
//
//                case LAVA:
//                case SPREAD:
//                    // Check if this is a portal lighting
//                    if (e.getFloor() != null && e.getFloor().getType().equals(Material.OBSIDIAN)) {
//                        if (DEBUG) {
//                            plugin.getLogger().info("DEBUG: obsidian lighting");
//                        }
//                        return;
//                    }
//                    if (!actionAllowed(e.getFloor().getLocation(), SettingsFlag.FIRE_SPREAD)) {
//                        if (DEBUG) {
//                            plugin.getLogger().info("DEBUG: canceling fire spread");
//                        }
//                        e.setCancelled(true);
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
//

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
            //plugin.getLogger().info("DEBUG: Not in world");
            return;
        }
        // Check island
        IslandData island = plugin.getGrid().getProtectedIslandAt(e.getPlayer().getLocation());
        if ((island == null && Settings.defaultWorldSettings.get(SettingsFlag.PRESSURE_PLATE))) {
            return;
        }
        if (island != null && island.getIgsFlag(SettingsFlag.PRESSURE_PLATE)) {
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

    /**
     * Removes the player from the plate map
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onStepOffPlate(PlayerMoveEvent e) {
        if (!onPlate.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        Vector3 v = e.getPlayer().getLocation();
        if (!(new Vector3(v.getFloorX(), v.getFloorY(), v.getFloorZ())).equals(onPlate.get(e.getPlayer().getUniqueId()))) {
            onPlate.remove(e.getPlayer().getUniqueId());
        }
    }

}
