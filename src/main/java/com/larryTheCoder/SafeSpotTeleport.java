/*
 * Copyright (C) 2016 larryTheHarry 
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
package com.larryTheCoder;

import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.storage.IslandData;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import java.util.ArrayList;

/**
 * A class that calculates finds a safe spot asynchronously and then teleports
 * the player there.
 *
 * @author larryTheCoder
 *
 */
public class SafeSpotTeleport {

    private ASkyBlock plugin;

    /**
     * Teleport to a safe place and if it fails, show a failure message
     *
     * @param plugin
     * @param player
     * @param l
     * @param failureMessage
     */
    public SafeSpotTeleport(final ASkyBlock plugin, final Entity player, final Location l, final String failureMessage) {
        new SafeSpotTeleport(plugin, player, l, 1, failureMessage, false, false);
    }

    /**
     * Teleport to a safe place and set home
     *
     * @param plugin
     * @param player
     * @param l
     * @param number
     */
    public SafeSpotTeleport(final ASkyBlock plugin, final Entity player, final Location l, final int number) {
        new SafeSpotTeleport(plugin, player, l, number, "", true, true);
    }

    /**
     * Teleport to a safe spot on an island
     *
     * @param plugin
     * @param player
     * @param l
     */
    public SafeSpotTeleport(final ASkyBlock plugin, final Entity player, final Location l) {
        new SafeSpotTeleport(plugin, player, l, 1, "", false, false);
    }

    /**
     * Teleport to a safe spot on an island
     *
     * @param plugin
     * @param entity
     * @param islandLoc
     * @param homeNumber
     * @param failureMessage
     * @param setHome
     * @param teleport
     */
    public SafeSpotTeleport(final ASkyBlock plugin, final Entity entity, final Location islandLoc, final int homeNumber, final String failureMessage, final boolean setHome, final boolean teleport) {
        this.plugin = plugin;
        this.calculateSafePosition(entity, islandLoc, homeNumber, failureMessage, setHome, teleport);
    }

    public final void calculateSafePosition(Entity entity, Location lc, int homeNumber, final String failureMessage, final boolean setHome, boolean teleport) {
        Player p;
        if (entity instanceof Player) {
            p = (Player) entity;
        } else {
            Utils.ConsoleMsg("Who is this guy? " + entity.getNameTag());
            return;
        }
        IslandData pd = plugin.getDatabase().getIsland(p.getName(), homeNumber);
        if (pd == null) {
            Utils.ConsoleMsg("An error has occurred while trying to find SAFESPOT.");//This should not be happends
            return;
        }
        ArrayList<Location> pos = new ArrayList<>();
        int maxX = pd.X + (Settings.islandSize / 2);
        int maxZ = pd.Z + (Settings.islandSize / 2);
        for (int minX = pd.X - (Settings.islandSize / 2); minX < maxX; minX++) {
            for (int y = 157; y > 0; y--) { // Start from up to down
                for (int minZ = pd.Z - (Settings.islandSize / 2); minZ < maxZ; minX++) {
                    if (checkBlock(lc.getLevel(), minX, y, minZ)) {
                        pos.add(new Location(minX, y, minZ));
                    }
                }
            }
        }
        if (pos.isEmpty()) {
            if (!failureMessage.isEmpty()) {
                p.sendMessage(failureMessage);
            } else {
                p.sendMessage("§cError: Unable to find your home location");
            }
        }
    }

    public boolean isExactLocation(Level level, int x, int y, int z) {
        Block type = level.getBlock(new Vector3(x, y, z));
        int space1 = level.getBlock(new Vector3(x, y + 1, z)).getId();
        int down1 = level.getBlock(new Vector3(x, y - 1, z)).getId();
        // todo checks if player spawned on the tree 
        // todo checks if player spawned nearby block entities
        // trapped in box?
        // trapped in cave?
        // nearby collisions course?
        // in a hole?
        // spawned on hostile (useless)
        // This will going to be laggy...
        return true;
    }

    /**
     * Returns true if the location is a safe one.
     *
     * @param level
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean checkBlock(Level level, int x, int y, int z) {
        Block type = level.getBlock(new Vector3(x, y, z));
        if (type.getId() != 0) { // AIR
            int space1 = level.getBlock(new Vector3(x, y + 1, z)).getId();
            int space2 = level.getBlock(new Vector3(x, y + 2, z)).getId();
            if ((space1 == 0 && space2 == 0) || (space1 == Item.END_PORTAL && space2 == Item.END_PORTAL)) {
                // Now there is a chance that this is a safe spot
                // Check for safe ground
                Item mat = Item.get(type.getId());
                if (!mat.toString().contains("FENCE")
                        && !mat.toString().contains("DOOR")
                        && !mat.toString().contains("GATE")
                        && !mat.toString().contains("PLATE")) {
                    switch (mat.getId()) {
                        // Unsafe
                        case Item.ANVIL:
                        case Item.BOAT:
                        case Item.CACTUS:
                        case Item.DOUBLE_PLANT:
                        case Item.END_PORTAL:
                        case Item.FIRE:
                        case Item.FLOWER_POT:
                        case Item.LADDER:
                        case Item.LAVA:
                        case Item.LEVER:
                        case Item.TALL_GRASS:
                        case Item.NETHER_PORTAL:
                        case Item.SIGN_POST:
                        case Item.SKULL:
                        case Item.STONE_BUTTON:
                        case Item.TORCH:
                        case Item.TRIPWIRE_HOOK:
                        case Item.WATER:
                        case Item.COBWEB:
                        case Item.WOODEN_BUTTON:
                            break;
                        default:
                            // Safe
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
