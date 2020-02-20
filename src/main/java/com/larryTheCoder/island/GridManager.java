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
package com.larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.BlockUtil;
import com.larryTheCoder.utils.Utils;

import java.util.HashSet;
import java.util.Set;

import static cn.nukkit.math.BlockFace.DOWN;
import static cn.nukkit.math.BlockFace.UP;

/**
 * @author larryTheCoder
 * @author tastybento
 */
public class GridManager {

    private final ASkyBlock plugin;

    public GridManager(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if this location is safe for a player to teleport to. Used by
     * warps and boat exits Unsafe is any liquid or air and also if there's no
     * space
     *
     * @param l - Location to be checked
     * @return true if safe, otherwise false
     */
    private static boolean isSafeLocation(final Position l) {
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
     * Checks the surrounding of the specific area that
     * a safe location on isSafeLocation
     *
     * @param l Location to be checked
     * @return true that the surrounding is ok
     */
    private static boolean checkSurrounding(Position l) {
        Block ground = l.getLevelBlock().getSide(DOWN);

        int safeBlock = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Position pos = ground.add(x, 0, z);
                Block block = pos.getLevelBlock();
                if (block.isSolid() && BlockUtil.isFluid(block)) {
                    safeBlock++;
                }
            }
        }

        Utils.sendDebug("DEBUG: Safe Blocks " + safeBlock);
        return safeBlock > 6;
    }

    /**
     * Checks if an online player is on their island, on a team island or on a
     * coop island
     *
     * @param player The target of player
     * @return true if on valid island, false if not
     */
    public boolean playerIsOnIsland(final Player player) {
        return locationIsAtHome(player, player.getLocation());
    }

    /**
     * Checks if a location is within the home boundaries of a player.
     *
     * @param player The player
     * @param loc    The geo location of
     * @return true if the location is within home boundaries
     */
    private boolean locationIsAtHome(final Player player, Location loc) {
        // Make a list of test locations and test them
        Set<Location> islandTestLocations = new HashSet<>();
        if (plugin.getIsland().checkIsland(player)) {
            IslandData pd = plugin.getIslandInfo(player);
            islandTestLocations.add(new Location(0, 0, 0, 0, 0, plugin.getServer().getLevelByName(pd.getLevelName())).add(pd.getCenter()));
        }
        if (islandTestLocations.isEmpty()) {
            return false;
        }
        // Run through all the locations
        for (Location islandTestLocation : islandTestLocations) {
            // Must be in the same world as the locations being checked
            // Note that getWorld can return null if a world has been deleted on the server
            if (islandTestLocation != null && islandTestLocation.getLevel() != null && islandTestLocation.getLevel().equals(loc.getLevel())) {
                int protectionRange = plugin.getSettings(islandTestLocation.getLevel().getName()).getProtectionRange();
                if (plugin.getIsland().checkIslandAt(islandTestLocation.getLevel())) {
                    // Get the protection range for this location if possible
                    IslandData island = plugin.getIsland().getIslandAt(islandTestLocation);
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
     * Determines a safe teleport spot on player's island or the team island
     * they belong to.
     *
     * @param p      The player
     * @param number Starting home location e.g., 1
     * @return Location of a safe teleport spot or null if one cannot be found
     */
    public Location getSafeHomeLocation(String p, int number) {
        IslandData pd = plugin.getDatabase().getIsland(p, number);
        if (pd == null) {
            // Get the default home, which may be null too, but that's okay
            number = 1;
            pd = plugin.getDatabase().getIsland(p, number);
        }

        if (pd != null) {
            if (pd.isSpawn()) {
                return pd.getHome();
            }

            Location locationSafe;
            if (pd.homeX != 0 || pd.homeY != 0 || pd.homeZ != 0) {
                locationSafe = pd.getHome();
            } else {
                locationSafe = new Location(0, 0, 0, 0, 0, plugin.getServer().getLevelByName(pd.getLevelName())).add(pd.getCenter());
            }

            // Load the chunks (Pretend that the island chunks has not loaded)
            // This is an actual fix for #28
            Utils.loadChunkAt(locationSafe);
            // Check if it is safe
            // Homes are stored as integers and need correcting to be more central
            if (isSafeLocation(locationSafe)) {
                return locationSafe;
            }

            Utils.sendDebug("Adding a block up");

            // To cover slabs, stairs and other half blocks, try one block above
            Location locPlusOne = locationSafe.clone();
            locPlusOne.add(new Vector3(0, 1, 0));
            if (isSafeLocation(locPlusOne) && checkSurrounding(locPlusOne)) {
                // Adjust the home location accordingly
                pd.setHomeLocation(locPlusOne);
                return locPlusOne;
            }

            Utils.sendDebug("Trying to check all way up");
            // Try to find all the way up
            for (int y = 0; y < 255; y++) {
                Position locPlusY = locPlusOne.setComponents(locationSafe.getX(), y, locationSafe.getZ());
                if (isSafeLocation(locPlusY) && checkSurrounding(locPlusY)) {
                    // Adjust the home location accordingly
                    pd.setHomeLocation(locPlusY);
                    return locPlusY.getLocation();
                }
            }

            Utils.sendDebug("Trying to check another way");
            Vector3 center = pd.getCenter();
            for (int dy = 0; dy <= 128; dy++) {
                for (int dx = center.getFloorX() - 25; dx <= center.getFloorX() + 25; dx++) {
                    for (int dz = center.getFloorZ() - 25; dz <= center.getFloorZ() + 25; dz++) {
                        Position pos = locPlusOne.setComponents(dx, dy, dz);
                        if (isSafeLocation(pos) && checkSurrounding(pos)) {
                            pd.setHomeLocation(pos);
                            return pos.getLocation();
                        }
                    }
                }
            }
        }

        // Unsuccessful
        return null;
    }

    /**
     * This teleports player to their island. If not safe place can be found
     * then the player is sent to spawn via /is command
     *
     * @param player The target
     */
    public void homeTeleport(final Player player) {
        homeTeleport(player, 1);
    }

    /**
     * Teleport player to a home location. If one cannot be found a search is
     * done to find a safe place.
     *
     * @param player The target
     * @param number Starting home location e.g., 1
     */
    public void homeTeleport(Player player, int number) {
        Location home = getSafeHomeLocation(player.getName(), number);
        //if the home null
        if (home == null) {
            player.sendMessage(plugin.getPrefix() + TextFormat.RED + "Failed to find your island safe spawn");
            return;
        }
        plugin.getTeleportLogic().safeTeleport(player, home, false, number);
        plugin.getIsland().showFancyTitle(player);
    }

    public IslandData getProtectedIslandAt(Location location) {
        IslandData island = plugin.getIslandInfo(location);
        if (island == null) {
            return null;
        }
        if (island.onIsland(location)) {
            return island;
        }
        return null;
    }

    public boolean isAtSpawn(Location location) {
        IslandData spawn = plugin.getDatabase().getSpawn();
        return spawn != null && spawn.onIsland(location);
    }

}
