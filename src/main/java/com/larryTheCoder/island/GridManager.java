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
package com.larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.BlockUtil;
import java.util.HashSet;
import java.util.Set;

import static cn.nukkit.math.BlockFace.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Adam Matthew
 */
public class GridManager {

    private ASkyBlock plugin;

    public GridManager(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    public boolean onGrid(Location pos) {
        return onGrid(pos.getFloorX(), pos.getFloorZ());
    }

    public boolean onGrid(int x, int z) {
        return x % Settings.islandDistance == 0 || z % Settings.islandDistance == 0;
    }

    /**
     * Checks if an online player is on their island, on a team island or on a
     * coop island
     *
     * @param player
     * @return true if on valid island, false if not
     */
    public boolean playerIsOnIsland(final Player player) {
        return locationIsAtHome(player, player.getLocation());
    }

    /**
     * Checks if a location is within the home boundaries of a player.
     *
     * @param player The player
     * @param loc The geo location of
     * @return true if the location is within home boundaries
     */
    public boolean locationIsAtHome(final Player player, Location loc) {
        // Make a list of test locations and test them
        Set<Location> islandTestLocations = new HashSet<>();
        if (plugin.getIsland().checkIsland(player)) {
            IslandData pd = plugin.getIslandInfo(player);
            islandTestLocations.add(new Location(pd.X, pd.Y, pd.Z, 0, 0, plugin.getServer().getLevelByName(pd.levelName)));
        } else if (plugin.getTManager().hasTeam(player)) {
//            islandTestLocations.add(plugin.getPlayers().getTeamIslandLocation(player.getUniqueId()));
//            if (Settings.createNether && Settings.newNether && ASkyBlock.getNetherWorld() != null) {
//                islandTestLocations.add(netherIsland(plugin.getPlayers().getTeamIslandLocation(player.getUniqueId())));
//            }
        }
        if (islandTestLocations.isEmpty()) {
            return false;
        }
        // Run through all the locations
        for (Iterator<Location> it = islandTestLocations.iterator(); it.hasNext();) {
            Location islandTestLocation = it.next();
            // Must be in the same world as the locations being checked
            // Note that getWorld can return null if a world has been deleted on the server
            if (islandTestLocation != null && islandTestLocation.getLevel() != null && islandTestLocation.getLevel().equals(loc.getLevel())) {
                int protectionRange = Settings.protectionrange;
                if (plugin.getIsland().checkIslandAt(islandTestLocation.getLevel()) == true) {
                    // Get the protection range for this location if possible
                    IslandData island = plugin.getIsland().GetIslandAt(islandTestLocation);
                    if (island != null) {
                        // We are in a protected island area.
                        protectionRange = island.getProtectionSize();
                    }
                }
                if (loc.getX() > islandTestLocation.getX() - protectionRange / 2
                        && loc.getX() < islandTestLocation.getX() + protectionRange / 2
                        && loc.getZ() > islandTestLocation.getZ() - protectionRange / 2
                        && loc.getZ() < islandTestLocation.getZ() + protectionRange / 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if this location is safe for a player to teleport to. Used by
     * warps and boat exits Unsafe is any liquid or air and also if there's no
     * space
     *
     * @param l - Location to be checked
     * @return true if safe, otherwise false
     */
    public static boolean isSafeLocation(final Position l) {
        if (l == null) {
            return false;
        }
        final Block ground = l.getLevelBlock().getSide(DOWN);
        final Block space1 = l.getLevelBlock();
        final Block space2 = l.getLevelBlock().getSide(UP);
        return ground.isSolid()
                && BlockUtil.isBreathable(space1)
                && BlockUtil.isBreathable(space2);
    }

    /**
     * Determines a safe teleport spot on player's island or the team island
     * they belong to.
     *
     * @param p       The player
     * @param number  Starting home location e.g., 1
     * @return Location of a safe teleport spot or null if one cannot be found
     */
    public Location getSafeHomeLocation(Player p, int number) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName(), number);
        Level world = Server.getInstance().getLevelByName(pd.levelName);
        int px = pd.X;
        int pz = pd.Z;
        int py = pd.Y;
        // Recommended SafeSpawn settings
        ArrayList<Location> predictedList = new ArrayList<>();
        for (int dy = 1; dy <= pd.getProtectionSize(); dy++) {
            for (int dx = 1; dx <= pd.getProtectionSize(); dx++) {
                for (int dz = 1; dz <= pd.getProtectionSize(); dz++) {
                    int x = px + (dx % 2 == 0 ? dx / 2 : -dx / 2);
                    int z = pz + (dz % 2 == 0 ? dz / 2 : -dz / 2);
                    int y = py + (dy % 2 == 0 ? dy / 2 : -dy / 2);
                    Location loc = new Location(x, y, z, world);
                    if (isSafeLocation(loc)) {
                        // look at the old location
                        loc.yaw = 0;
                        loc.pitch = 0;
                        predictedList.add(loc);
                    }
                }
            }
        }

        // Recommended nearby void settings
        if (!predictedList.isEmpty()) {
            Location spawnLocation = predictedList.get(1);
            for (int i = 0; i < predictedList.size(); i++) {
                if (adjacentNearbyVoid(predictedList.get(i)) != null) {
                    return adjacentNearbyVoid(spawnLocation);
                }
            }
        }
        
        // Unsuccessful
        return null;
    }

    public Location adjacentNearbyVoid(Location loc) {
        // Handle if the player nearby void... (Death)
        Block block1 = loc.getLevelBlock();
        // Type Positive
        Block block2 = loc.getLevelBlock().east(1);
        if (block1.getId() != 0 && block2.getId() != 0) {
            return loc;
        }
        block2 = loc.getLevelBlock().west(1);
        if (block1.getId() != 0 && block2.getId() != 0) {
            return loc;
        }
        // Type Negative
        block2 = loc.getLevelBlock().east(-1);
        if (block1.getId() != 0 && block2.getId() != 0) {
            return loc;
        }
        block2 = loc.getLevelBlock().west(-1);
        if (block1.getId() != 0 && block2.getId() != 0) {
            return loc;
        }
        return null;
    }

    /**
     * This teleports player to their island. If not safe place can be found
     * then the player is sent to spawn via /is command
     *
     * @param player The target
     * @return true if the home teleport is successful
     */
    public boolean homeTeleport(final Player player) {
        return homeTeleport(player, 1);
    }

    /**
     * Teleport player to a home location. If one cannot be found a search is
     * done to find a safe place.
     *
     * @param player The target
     * @param number Starting home location e.g., 1
     * @return true if successful, false if not
     */
    public boolean homeTeleport(Player player, int number) {
        Location home;
        home = getSafeHomeLocation(player, number);
        //if the home null
        if (home == null) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "Your island could not be found! Error?");
            return false;
        }
        plugin.getTeleportLogic().safeTeleport(player, home, false, number);
        return true;

    }

    public IslandData getProtectedIslandAt(Location location) {
        //plugin.getLogger().info("DEBUG: getProtectedIslandAt " + location);
        IslandData island = plugin.getIslandInfo(location);
        if (island == null) {
            //plugin.getLogger().info("DEBUG: no island at this location");
            return null;
        }
        if (island.onIsland(location)) {
            return island;
        }
        return null;
    }

    public boolean isAtSpawn(Location location) {
        return plugin.getDatabase().getSpawn().onIsland(location);
    }

}
