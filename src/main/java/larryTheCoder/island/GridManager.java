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
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import java.util.UUID;
import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;
import larryTheCoder.Settings;

/**
 * @author larryTheCoder
 */
public class GridManager {

    private ASkyBlock plugin;

    public GridManager(ASkyBlock plugin){
        this.plugin = plugin;
    }
    
    public boolean onGrid(Location pos){
        return onGrid(pos.getFloorX(), pos.getFloorZ());
    }
    
    public boolean onGrid(int x, int z){
        return x % Settings.islandSize == 0 || z % Settings.islandSize == 0;
    }
    
    /**
     * Checks if this location is safe for a player to teleport to. Used by
     * warps and boat exits Unsafe is any liquid or air and also if there's no
     * space
     *
     * @param l - Location to be checked
     * @return true if safe, otherwise false
     */
    public boolean isSafeLocation(final Position l) {
        if (l == null) {
            return false;
        }
        // TODO: improve the safe location finding.
        final Block ground = l.getLevelBlock().getSide(Vector3.SIDE_DOWN);
        final Block space1 = l.getLevelBlock();
        final Block space2 = l.getLevelBlock().getSide(Vector3.SIDE_UP);
        // Portals are not "safe"
        if (space1.getId() == Block.NETHER_PORTAL || ground.getId() == Block.NETHER_PORTAL || space2.getId() == Block.NETHER_PORTAL) {
            return false;
        }
        // If ground is AIR, then this is either not good, or they are on slab,
        // stair, etc.
        if (ground.getId() == Block.AIR) {
            return false;
        }
        if (!ground.isSolid() || !space1.isSolid() || !space2.isSolid()) {
            if (ground.getId() == Block.LAVA || ground.getId() == Block.LAVA
                    || space1.getId() == Block.LAVA || space1.getId() == Block.LAVA
                    || space2.getId() == Block.LAVA || space2.getId() == Block.LAVA) {
                return false;
            }
        }
        if (ground.getId() == Block.TRAPDOOR) {
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
    public Position getSafeHomeLocation(Player p, int number) {
        IslandData pd = ASkyBlock.get().getDatabase().getIslandAndId(p.getName(), number);
        // Try the numbered home location first
        Position l = new Position(pd.X, pd.floor_y, pd.Z, ASkyBlock.get().getServer().getLevelByName(pd.levelName));

        // Homes are stored as integers and need correcting to be more central
        if (isSafeLocation(l)) {
            return l.clone().add(new Vector3(0.5D, 0, 0.5D));
        }
        // To cover slabs, stairs and other half blocks, try one block above
        Position lPlusOne = l.clone();
        lPlusOne.add(new Vector3(0, 1, 0));
        if (isSafeLocation(lPlusOne)) {
            // Adjust the home location accordingly
            plugin.getIsland().setHomeLocation(p, lPlusOne, number);
            return lPlusOne.clone().add(new Vector3(0.5D, 0, 0.5D));
        }

        // Home location either isn't safe, or does not exist so try the island location
        if (isSafeLocation(l)) {
            plugin.getIsland().setHomeLocation(p, l, number);
            return l.clone().add(new Vector3(0.5D, 0, 0.5D));
        }

        if (pd.owner == null) {
            plugin.getLogger().warning(p.getName() + " player has no island! : NULL");
            return null;
        }
        // If these island locations are not safe, then we need to get creative
        // Try the default location
        Location dl = new Location(l.getX() + 0.5D, l.getY() + 5D, l.getZ() + 2.5D, 0F, 30F, l.getLevel());
        if (isSafeLocation(dl)) {
            plugin.getIsland().setHomeLocation(p, dl, number);
            return dl;
        }
        // Try just above the bedrock
        dl = new Location(l.getX() + 0.5D, l.getY() + 5D, l.getZ() + 0.5D, 0F, 30F, l.getLevel());
        if (isSafeLocation(dl)) {
            plugin.getIsland().setHomeLocation(p, dl, number);
            return dl;
        }
        // Try all the way up to the sky
        for (int y = l.getFloorY(); y < 255; y++) {
            final Location n = new Location(l.getX() + 0.5D, y, l.getZ() + 0.5D, 0F, 30F, l.getLevel());
            if (isSafeLocation(n)) {
                plugin.getIsland().setHomeLocation(p, n, number);
                return n;
            }
        }
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
    public boolean homeTeleport(final Player player) {
        return homeTeleport(player, 1);
    }
    
    /**
     * Teleport player to a home location. If one cannot be found a search is done to
     * find a safe place.
     * @param player
     * @param number - home location to do to
     * @return true if successful, false if not
     */
    public boolean homeTeleport(final Player player, int number) {
        Position home;
        home = getSafeHomeLocation(player, number);
        if (home == null) {
            // No Solution for this
            return false;
        }
        home.getLevel().loadChunk(home.getFloorX(), home.getFloorZ());
        player.teleport(home);
        if (number ==1 ) {
            player.sendMessage(TextFormat.GREEN + "Teleported to your island");
        } else {
            player.sendMessage(TextFormat.GREEN + "Teleported to your island #" + number);
        }
        return true;

    }
}
