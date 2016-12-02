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
package larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockTrapdoor;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import java.util.UUID;
import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;

/**
 * @author larryTheCoder
 */
public class GridManager {

    /**
     * Checks if this location is safe for a player to teleport to. Used by
     * warps and boat exits Unsafe is any liquid or air and also if there's no
     * space
     *
     * @param l - Location to be checked
     * @return true if safe, otherwise false
     */
    public static boolean isSafeLocation(final Location l) {
        if (l == null) {
            return false;
        }
        // TODO: improve the safe location finding.
        final Block ground = l.getLevelBlock().getSide(Vector3.SIDE_DOWN);
        final Block space1 = l.getLevelBlock();
        final Block space2 = l.getLevelBlock().getSide(Vector3.SIDE_UP);
        // Portals are not "safe"
        if (space1.getId() == Item.NETHER_PORTAL || ground.getId() == Item.NETHER_PORTAL || space2.getId() == Item.NETHER_PORTAL) {
            return false;
        }
        // If ground is AIR, then this is either not good, or they are on slab,
        // stair, etc.
        if (ground.getId() == Item.AIR) {
            return false;
        }
        if (!ground.isSolid() || !space1.isSolid() || !space2.isSolid()) {
            if (ground.getId() == Item.LAVA || ground.getId() == Item.LAVA
                    || space1.getId() == Item.LAVA || space1.getId() == Item.LAVA
                    || space2.getId() == Item.LAVA || space2.getId() == Item.LAVA) {
                return false;
            }
        }
        if (ground.getId() == Block.TRAPDOOR) {
            if (ground instanceof BlockTrapdoor) {
                return false;
            }
        } else {
            return false;
        }
        if (ground.getId() == Block.CACTUS || ground.getId() == Item.BOAT || ground.getId() == Block.FENCE
                || ground.getId() == Block.NETHER_BRICK_FENCE || ground.getId() == Block.SIGN_POST || ground.getId() == Block.WALL_SIGN) {
            return false;
        }
        // Check that the space is not solid
        // The isSolid function is not fully accurate (yet) so we have to
        // check
        // a few other items
        // isSolid thinks that PLATEs and SIGNS are solid, but they are not
        if (space1.isSolid() && space1.getId() != Block.SIGN_POST && space1.getId() != Block.WALL_SIGN) {
            return false;
        }
        if (space2.isSolid() && space2.getId() != Block.SIGN_POST && space2.getId() != Block.WALL_SIGN) {
            return false;
        }
        // Safe
        return true;
    }

    /**
     * Determines a safe teleport spot on player's island or the team island
     * they belong to.
     *
     * @param p UUID of player
     * @param number - starting home location e.g., 1
     * @return Location of a safe teleport spot or null if one cannot be found
     */
    public static Location getSafeHomeLocation(Player p, int number) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName());
        // Try the numbered home location first
        Location l = new Location(pd.X, pd.floor_y, pd.Z, 0, 0, ASkyBlock.get().getServer().getLevelByName(pd.levelName));

        // Homes are stored as integers and need correcting to be more central
        if (isSafeLocation(l)) {
            return l.clone().add(new Vector3(0.5D, 0, 0.5D));
        }
        // To cover slabs, stairs and other half blocks, try one block above
        Location lPlusOne = l.clone();
        lPlusOne.add(new Vector3(0, 1, 0));
        if (isSafeLocation(lPlusOne)) {
            // Adjust the home location accordingly
            Island.setHomeLocation(p, lPlusOne, number);
            return lPlusOne.clone().add(new Vector3(0.5D, 0, 0.5D));
        }

        //plugin.getLogger().info("DEBUG: Home location either isn't safe, or does not exist so try the island");
        // Home location either isn't safe, or does not exist so try the island
        // location
        if (isSafeLocation(l)) {
            Island.setHomeLocation(p, l, number);
            return l.clone().add(new Vector3(0.5D, 0, 0.5D));
        }

        if (pd.owner == null) {
            //plugin.getLogger().warning(plugin.getPlayers().getName(p) + " player has no island!");
            return null;
        }
        //plugin.getLogger().info("DEBUG: If these island locations are not safe, then we need to get creative");
        // If these island locations are not safe, then we need to get creative
        // Try the default location
        //plugin.getLogger().info("DEBUG: default");
        Location dl = new Location(l.getX() + 0.5D, l.getY() + 5D, l.getZ() + 2.5D, 0F, 30F, l.getLevel());
        if (isSafeLocation(dl)) {
            Island.setHomeLocation(p, dl, number);
            return dl;
        }
        // Try just above the bedrock
        //plugin.getLogger().info("DEBUG: above bedrock");
        dl = new Location(l.getX() + 0.5D, l.getY() + 5D, l.getZ() + 0.5D, 0F, 30F, l.getLevel());
        if (isSafeLocation(dl)) {
            Island.setHomeLocation(p, dl, number);
            return dl;
        }
        // Try all the way up to the sky
        //plugin.getLogger().info("DEBUG: try all the way to the sky");
        for (int y = l.getFloorY(); y < 255; y++) {
            final Location n = new Location(l.getX() + 0.5D, y, l.getZ() + 0.5D, 0F, 30F, l.getLevel());
            if (isSafeLocation(n)) {
                Island.setHomeLocation(p, n, number);
                return n;
            }
        }
        //plugin.getLogger().info("DEBUG: unsuccessful");
        // Unsuccessful
        return null;
    }
    
    /**
     * This teleports player to their island. If not safe place can be found
     * then the player is sent to spawn via /spawn command
     * 
     * @param player
     * @return true if the home teleport is successful
     */
    public static boolean homeTeleport(final Player player) {
        return homeTeleport(player, 1);
    }
    /**
     * Teleport player to a home location. If one cannot be found a search is done to
     * find a safe place.
     * @param player
     * @param number - home location to do to
     * @return true if successful, false if not
     */
    public static boolean homeTeleport(final Player player, int number) {
        Location home = null;
        //plugin.getLogger().info("home teleport called for #" + number);
        home = getSafeHomeLocation(player, number);
        //plugin.getLogger().info("home get safe loc = " + home);
        // Check if the player is a passenger in a boat
        if (home == null) {
            //plugin.getLogger().info("Fixing home location using safe spot teleport");
            // Try to fix this teleport location and teleport the player if possible
            //new SafeSpotTeleport(plugin, player, plugin.getPlayers().getHomeLocation(player.getUniqueId(), number), number);
            // No Solution for this
            return false;
        }
        //plugin.getLogger().info("DEBUG: home loc = " + home + " teleporting");
        //home.getChunk().load();
        player.teleport(home);
        //player.sendBlockChange(home, Material.GLOWSTONE, (byte)0);
        if (number ==1 ) {
            player.sendMessage(TextFormat.GREEN + "Teleported to your island");
        } else {
            player.sendMessage(TextFormat.GREEN + "Teleported to your island #" + number);
        }
        return true;

    }
}
