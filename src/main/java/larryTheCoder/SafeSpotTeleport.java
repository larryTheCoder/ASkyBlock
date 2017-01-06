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
package larryTheCoder;

import larryTheCoder.database.purger.IslandData;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import com.intellectiualcrafters.TaskManager;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that calculates finds a safe spot asynchronously and then teleports
 * the player there.
 *
 * @author tastybento & larryTheCoder
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

    public final void calculateSafePosition(Entity entity, Location landLoc, int homeNumber, final String failureMessage, final boolean setHome, boolean teleport) {
        Player p;
        if (entity instanceof Player) {
            p = (Player) entity;
        } else {
            Utils.ConsoleMsg("Who is this guy? " + entity.getNameTag());
            return;
        }
        IslandData pd = plugin.getDatabase().getIslandAndId(p.getName(), homeNumber);
        if (pd != null) {
            final Level world = plugin.getServer().getLevelByName("SkyBlock");
            // Get the chunks
            List<BaseFullChunk> chunkSnapshot = new ArrayList<>();
            // Add the center of the chunk
            chunkSnapshot.add(world.getChunk(pd.X, pd.Z));
            // Add immediately adjacent chunks
            for (int x = landLoc.getFloorX() - 1; x <= landLoc.getFloorX() + 1; x++) {
                for (int z = landLoc.getFloorZ() - 1; z <= landLoc.getFloorZ() + 1; z++) {
                    if (x != landLoc.getFloorX() || z != landLoc.getFloorZ()) {
                        chunkSnapshot.add(world.getChunk(x, z));
                    }
                }
            }
            // Add the rest of the island protected area
            for (int x = pd.getMinProtectedX() / 16; x <= (pd.getMinProtectedX() + Settings.islandSize - 1) / 16; x++) {
                for (int z = pd.getMinProtectedZ() / 16; z <= (pd.getMinProtectedZ() + Settings.islandSize - 1) / 16; z++) {
                    // This includes the center spots again, so is not as efficient...
                    chunkSnapshot.add(world.getChunk(x, z));
                }
            }
            Utils.ConsoleMsg("SPOT: " + chunkSnapshot.size());
            final List<BaseFullChunk> finalChunk = chunkSnapshot;
            int worldHeight = 127;
            plugin.getServer().getScheduler().scheduleAsyncTask(new AsyncTask() {
                @Override
                public void onRun() {
                    // Find a safe spot, defined as a solid block, with 2 air spaces above it                   
                    BaseFullChunk safeChunk = null;
                    BaseFullChunk portalChunk = null;
                    boolean safeSpotFound = false;
                    Vector3 safeSpotInChunk = null;
                    Vector3 portalPart = null;
                    double distance = 0D;
                    double safeDistance = 0D;
                    for (BaseFullChunk chunk : finalChunk) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = Math.min(chunk.getHighestBlockAt(x, z), worldHeight); y >= 0; y--) {
                                    // Check for portal - only if this is not a safe home search
                                    if (!setHome && chunk.getBlockId(x, y, z) == Item.END_PORTAL || chunk.getBlockId(x, y, z) == Item.NETHER_PORTAL) {
                                        if (portalPart == null || (distance > landLoc.normalize().distanceSquared(new Vector3(x, y, z)))) {
                                            // First one found or a closer one, save the chunk the position and the distance
                                            portalChunk = chunk;
                                            portalPart = new Vector3(x, y, z);
                                            distance = portalPart.distanceSquared(landLoc.normalize());
                                        }
                                    }
                                    // Check for safe spot, but only if it is closer than one we have found already
                                    if (!safeSpotFound || (safeDistance > landLoc.normalize().distanceSquared(new Vector3(x, y, z)))) {
                                        // No safe spot yet, or closer distance
                                        if (checkBlock(chunk, x, y, z)) {
                                            safeChunk = chunk;
                                            safeSpotFound = true;
                                            safeSpotInChunk = new Vector3(x, y, z);
                                            safeDistance = landLoc.normalize().distanceSquared(safeSpotInChunk);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // End search
                    // Check if the portal is safe (it should be)
                    if (portalPart != null) {
                        if (portalChunk == null) {
                            return;
                        }
                        //System.out.print("DEBUG: Portal found");
                        // There is a portal available, but is it safe?
                        // Get the lowest portal spot
                        int x = portalPart.getFloorX();
                        int y = portalPart.getFloorY();
                        int z = portalPart.getFloorZ();
                        while (portalChunk.getBlockId(x, y, z) == Item.END_PORTAL || portalChunk.getBlockId(x, y, z) == Item.NETHER_PORTAL) {
                            y--;
                        }
                        //System.out.print("DEBUG: Portal teleport loc = " + (16 * portalChunk.getX() + x) + "," + (y) + "," + (16 * portalChunk.getZ() + z));
                        // Now check if this is a safe location
                        if (checkBlock(portalChunk, x, y, z)) {
                            // Yes, so use this instead of the highest location
                            //System.out.print("DEBUG: Portal is safe");
                            safeSpotFound = true;
                            safeSpotInChunk = new Vector3(x, y, z);
                            safeChunk = portalChunk;
                            // TODO: Add safe portal spot to island
                        }
                    }
                    if (safeChunk != null && safeSpotFound) {
                        //final Vector spot = new Vector((16 *currentChunk.getX()) + x + 0.5D, y +1, (16 * currentChunk.getZ()) + z + 0.5D)
                        final Vector3 spot = new Vector3((16 * safeChunk.getX()) + 0.5D, 1, (16 * safeChunk.getZ()) + 0.5D).add(safeSpotInChunk);
                        // Return to main thread and teleport the player
                        plugin.getServer().getScheduler().scheduleTask(() -> {
                            Location destination = new Location(spot.x, spot.y, spot.z, 0, 0, landLoc.getLevel());
                            if (setHome && entity instanceof Player) {
                                plugin.getDatabase().setPosition(destination, homeNumber, p.getName());
                            }
                            entity.teleport(destination);
                        });
                    } else if (entity instanceof Player) {
                        if (!failureMessage.isEmpty()) {
                            p.sendMessage(failureMessage);
                        } else {
                            p.sendMessage(TextFormat.RED + "Error: your home is not safe");
                        }
                    }
                }
            });
        }
    }

    /**
     * Returns true if the location is a safe one.
     *
     * @param chunk
     * @param x
     * @param y
     * @param z
     * @return
     */
    private boolean checkBlock(FullChunk chunk, int x, int y, int z) {
        int type = chunk.getBlockId(x, y, z);
        if (type != 0) { // AIR
            int space1 = chunk.getBlockId(x, y + 1, z);
            int space2 = chunk.getBlockId(x, y + 2, z);
            if ((space1 == 0 && space2 == 0) || (space1 == Item.END_PORTAL && space2 == Item.END_PORTAL)) {
                // Now there is a chance that this is a safe spot
                // Check for safe ground
                Item mat = Item.get(type);
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
