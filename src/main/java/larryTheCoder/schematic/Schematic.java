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
package larryTheCoder.schematic;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.item.EntityPainting.Motive; // Art
import static cn.nukkit.entity.item.EntityPainting.motives;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.TextFormat;
import com.boydti.fawe.nukkit.core.NukkitWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.world.DataException;
import java.util.HashSet;
import java.util.Set;
import larryTheCoder.ASkyBlock;
import larryTheCoder.Settings;

import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.Tag;

/**
 * @author larryTheCoder
 */
public class Schematic {
    
    private File schematicFolder;
    private ASkyBlock plugin;
    public boolean running = false;
    //Utils
    private short width;
    private short length;
    private short height;
    private Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
    private Map<String, Motive> paintingList = new HashMap<>();
    private Set<Integer> attachable = new HashSet<>();
    private Map<Byte, Integer> facingList = new HashMap<>();
    private Vector3 bedrock;
    private Vector3 chest;
    private Vector3 welcomeSign;
    private Vector3 topGrass;
    private Vector3 playerSpawn;
    private int durability;
    private int levelHandicap;

    private String heading;
    private String name;
    private String perm;
    private String description;
    private int rating;
    private boolean useDefaultChest;
    private Biome biome;
    private boolean usePhysics;
    private boolean pasteEntities;
    private boolean visible;
    private int order;
    // Name of a schematic this one is paired with
    private String partnerName = "";

    public Schematic(ASkyBlock plugin) {
        this.plugin = plugin;
        // Initialize 
        name = "";
        heading = "";
        description = "Default Island";
        perm = "";
        rating = 50;
        useDefaultChest = true;
        biome = Biome.getBiome(Biome.MOUNTAINS);
        usePhysics = false;
        schematicFolder = null;
        visible = true;
        order = 0;
        bedrock = null;
        chest = null;
        welcomeSign = null;
        topGrass = null;
        playerSpawn = null;
        //playerSpawnBlock = null;
        partnerName = "";
    }

    public Schematic(ASkyBlock plugin, File folder) throws IOException {
        if (plugin == null) {
            Server.getInstance().getLogger().alert("plugin cant be null");
            return;
        }
        this.plugin = plugin;
        this.schematicFolder = folder;
        this.init();
        this.initAttachable();
        // Painting list, useful to check if painting exsits or nor
        this.initArt();
    }

    protected final void initAttachable() {
        attachable.add(Item.STONE_BUTTON);
        attachable.add(Item.WOODEN_BUTTON);
        attachable.add(Item.COCOA);
        attachable.add(Item.LADDER);
        attachable.add(Item.LEVER);
        attachable.add(Item.REDSTONE_TORCH);
        attachable.add(Item.WALL_SIGN);
        attachable.add(Item.TORCH);
        attachable.add(Item.TRAPDOOR);
        attachable.add(Item.TRIPWIRE_HOOK);
        attachable.add(Item.VINE);
        attachable.add(Item.WOODEN_DOOR);
        attachable.add(Item.IRON_DOOR);
        attachable.add(Item.RED_MUSHROOM);
        attachable.add(Item.BROWN_MUSHROOM);
        attachable.add(Item.NETHER_PORTAL);
    }

    protected final void initArt() {
        paintingList.put("Kebab", motives[0]);
        paintingList.put("Aztec", motives[1]);
        paintingList.put("Alban", motives[2]);
        paintingList.put("Aztec2", motives[3]);
        paintingList.put("Bomb", motives[4]);
        paintingList.put("Plant", motives[5]);
        paintingList.put("Wasteland", motives[6]);
        paintingList.put("Wanderer", motives[7]);
        paintingList.put("Graham", motives[8]);
        paintingList.put("Pool", motives[9]);
        paintingList.put("Courbet", motives[10]);
        paintingList.put("Sunset", motives[11]);
        paintingList.put("Sea", motives[12]);
        paintingList.put("Creebet", motives[13]);
        paintingList.put("Match", motives[14]);
        paintingList.put("Bust", motives[15]);
        paintingList.put("Stage", motives[16]);
        paintingList.put("Void", motives[17]);
        paintingList.put("SkullAndRoses", motives[18]);
        paintingList.put("Fighters", motives[19]);
        paintingList.put("Skeleton", motives[20]);
        paintingList.put("DonkeyKong", motives[21]);
        paintingList.put("Pointer", motives[22]);
        paintingList.put("Pigscene", motives[23]);
        paintingList.put("BurningSkull", motives[24]);
    }

    protected final void init() throws IOException {
        // Initialize
        short[] blocks;
        byte[] data;
        try {
            FileInputStream stream = new FileInputStream(schematicFolder);
            // InputStream is = new DataInputStream(new
            // GZIPInputStream(stream));
            NBTInputStream nbtStream = new NBTInputStream(stream);

            CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
            nbtStream.close();
            stream.close();
            if (!schematicTag.getName().equals("Schematic")) {
                throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
            }
            Map<String, Tag> schematic = schematicTag.getValue();
            Vector3 origin = null;
            try {
                int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
                int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
                int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
                Vector3 min = new Vector3(originX, originY, originZ);
                origin = min.clone();
            } catch (Exception ignored) {
            }
            if (!schematic.containsKey("Blocks")) {
                throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
            }
            width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            height = getChildTag(schematic, "Height", ShortTag.class).getValue();
            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];
            blocks = new short[blockId.length]; // Have to later combine IDs
            // We support 4096 block IDs using the same method as vanilla
            // Minecraft, where
            // the highest 4 bits are stored in a separate byte array.
            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }

            // Combine the AddBlocks data with the first 8-bit block ID
            for (int index = 0; index < blockId.length; index++) {
                if ((index >> 1) >= addId.length) { // No corresponding
                    // AddBlocks index
                    blocks[index] = (short) (blockId[index] & 0xFF);
                } else if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
            // to-do An animal
            List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
            for (Tag tag : tileEntities) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag t = (CompoundTag) tag;

                int x = 0;
                int y = 0;
                int z = 0;

                Map<String, Tag> values = new HashMap<>();

                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    switch (entry.getKey()) {
                        case "x":
                            if (entry.getValue() instanceof IntTag) {
                                x = ((IntTag) entry.getValue()).getValue();
                            }
                            break;
                        case "y":
                            if (entry.getValue() instanceof IntTag) {
                                y = ((IntTag) entry.getValue()).getValue();
                            }
                            break;
                        case "z":
                            if (entry.getValue() instanceof IntTag) {
                                z = ((IntTag) entry.getValue()).getValue();
                            }
                            break;
                        default:
                            break;
                    }

                    values.put(entry.getKey(), entry.getValue());
                }

                BlockVector vec = new BlockVector(x, y, z);
                tileEntitiesMap.put(vec, values);
            }

        } catch (IOException e) {
            Server.getInstance().getLogger().info(TextFormat.RED + "An error occured while attemping to load Schematic File!");
            throw new IOException();
        }
        // Check for key blocks
        // Find top most bedrock - this is the key stone
        // Find top most chest
        // Find top most grass
        List<Vector3> grassBlocks = new ArrayList<>();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    // Bukkit.getLogger().info("DEBUG " + index +
                    // " changing to ID:"+blocks[index] + " data = " +
                    // blockData[index]);
                    switch (blocks[index]) {
                        case 7:
                            // Last bedrock
                            if (bedrock == null || bedrock.getY() < y) {
                                bedrock = new Vector3(x, y, z);
                                //Bukkit.getLogger().info("DEBUG higher bedrock found:" + bedrock.toString());
                            }
                            break;
                        case 54:
                            // Last chest
                            if (chest == null || chest.getY() < y) {
                                chest = new Vector3(x, y, z);
                                // Bukkit.getLogger().info("Island loc:" +
                                // loc.toString());
                                // Bukkit.getLogger().info("Chest relative location is "
                                // + chest.toString());
                            }
                            break;
                        case 63:
                            // Sign
                            if (welcomeSign == null || welcomeSign.getY() < y) {
                                welcomeSign = new Vector3(x, y, z);
                                // Bukkit.getLogger().info("DEBUG higher sign found:"
                                // + welcomeSign.toString());
                            }
                            break;
                        case 2:
                            // Grass
                            grassBlocks.add(new Vector3(x, y, z));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (bedrock == null) {
            Server.getInstance().getLogger().error("Schematic must have at least one bedrock in it!");
            //throw new IOException();
        }
        // Find other key blocks
        if (!grassBlocks.isEmpty()) {
            // Sort by height
            List<Vector3> sorted = new ArrayList<>();
            for (Vector3 v : grassBlocks) {
                //if (GridManager.isSafeLocation(v.toLocation(world))) {
                // Add to sorted list
                boolean inserted = false;
                for (int i = 0; i < sorted.size(); i++) {
                    if (v.getY() > sorted.get(i).getY()) {
                        sorted.add(i, v);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    // just add to the end of the list
                    sorted.add(v);
                }
            }
            topGrass = sorted.get(0);
        } else {
            topGrass = null;
        }
        facingList.put((byte) 0, Vector3.getOppositeSide(Vector3.SIDE_SOUTH));
        facingList.put((byte) 1, Vector3.getOppositeSide(Vector3.SIDE_WEST));
        facingList.put((byte) 2, Vector3.getOppositeSide(Vector3.SIDE_NORTH));
        facingList.put((byte) 3, Vector3.getOppositeSide(Vector3.SIDE_EAST));
        this.prePasteSchematic(blocks, data);
    }
    
    /**
     * Paste the schematic to current Players(s) island!
     * TO-DO: add a schematic loader WITHOUT using WorldEdit!
     * Warning: Might be slow!
     * 
     * @param loc       - The location need to paste
     * @param player    - the player island
     * @param teleport  - Teleport?
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public void pasteSchematic(final Location loc, final Player player)  {
	plugin.getLogger().info("WE Pasting");
	com.sk89q.worldedit.Vector WEorigin = new com.sk89q.worldedit.Vector(loc.getFloorX() - 3,loc.getFloorY(),loc.getFloorZ() - 3);
        EditSession es = new EditSession(new NukkitWorld(loc.getLevel()), 999999999);
        try {
        
        CuboidClipboard cc = CuboidClipboard.loadSchematic(schematicFolder);
        cc.paste(es, WEorigin, false);
        cc.pasteEntities(WEorigin);
        } catch (DataException | IOException | MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }   

    /**
     * Get child tag of a NBT structure.
     *
     * @param items The parent tag map
     * @param key The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    private void prePasteSchematic(short[] blocks, byte[] data) {
        //islandBlocks = new ArrayList<IslandBlock>();
        Map<BlockVector, Map<String, Tag>> tileEntitiesMap = this.getTileEntitiesMap();
//                for (int x = 0; x < width; ++x) {
//            for (int y = 0; y < height; ++y) {
//                for (int z = 0; z < length; ++z) {
//                    int index = y * width * length + z * width + x;
//                    // Only bother if this block is above ground zero and 
//                    // only bother with air if it is below sea level
//                    // TODO: need to check max world height too?
//                    int h = Settings.islandHieght + y - bedrock.getFloorY();
//                    if (h >= 0 && h < 255 && (blocks[index] != 0 || h < Settings.islandHieght)){
//                        // Only bother if the schematic blocks are within the range that y can be
//                        //plugin.getLogger().info("DEBUG: height " + (count++) + ":" +h);
//                        IslandBlock block = new IslandBlock(x, y, z);
//                        if (!attachable.contains((int)blocks[index]) || blocks[index] == 179) {
//                            if (Bukkit.getServer().getVersion().contains("(MC: 1.7") && blocks[index] == 179) {
//                                // Red sandstone - use red sand instead
//                                block.setBlock(12, (byte)1);
//                            } else {
//                                block.setBlock(blocks[index], data[index]);
//                            }
//                            // Tile Entities
//                            if (tileEntitiesMap.containsKey(new BlockVector(x, y, z))) {
//                                if (plugin.isOnePointEight()) {
//                                    if (block.getTypeId() == Material.STANDING_BANNER.getId()) {
//                                        block.setBanner(tileEntitiesMap.get(new BlockVector(x, y, z)));
//                                    }
//                                    else if (block.getTypeId() == Material.SKULL.getId()) {
//                                        block.setSkull(tileEntitiesMap.get(new BlockVector(x, y, z)), block.getData());
//                                    }
//                                    else if (block.getTypeId() == Material.FLOWER_POT.getId()) {
//                                        block.setFlowerPot(tileEntitiesMap.get(new BlockVector(x, y, z)));
//                                    }
//                                }
//                                // Monster spawner blocks
//                                if (block.getTypeId() == Material.MOB_SPAWNER.getId()) {
//                                    block.setSpawnerType(tileEntitiesMap.get(new BlockVector(x, y, z)));
//                                } else if ((block.getTypeId() == Material.SIGN_POST.getId())) {
//                                    block.setSign(tileEntitiesMap.get(new BlockVector(x, y, z)));
//                                } else if (block.getTypeId() == Material.CHEST.getId()) {
//                                    block.setChest(nms, tileEntitiesMap.get(new BlockVector(x, y, z)));
//                                }
//                            }
//                            //islandBlocks.add(block);
//                        }
//                    }
//                }
//            }
//        }
    }

    /**
     * @return true if player spawn exists in this schematic
     */
    public boolean isPlayerSpawn() {
        return playerSpawn != null;
    }

    /**
     * @param pasteLocation the location to paste
     * @return the playerSpawn Location given a paste location
     */
    public Position getPlayerSpawn(Position pasteLocation) {
        return pasteLocation.clone().add(playerSpawn);
    }

    /**
     * @return the tileEntitiesMap
     */
    public Map<BlockVector, Map<String, Tag>> getTileEntitiesMap() {
        return tileEntitiesMap;
    }

    /**
     * @param playerSpawnBlock the playerSpawnBlock to set
     * @return true if block is found otherwise false
     */
    @SuppressWarnings("deprecation")
    public boolean setPlayerSpawnBlock(Block playerSpawnBlock) {
        if (bedrock == null) {
            return false;
        }
        playerSpawn = null;
        // Run through the schematic and try and find the spawnBlock
//        for (IslandBlock islandBlock : islandBlocks) {
//            if (islandBlock.getTypeId() == playerSpawnBlock.getId()) {
//                playerSpawn = islandBlock.getVector().subtract(bedrock).add(new Vector(0.5D,0D,0.5D));
//                // Set the block to air
//                islandBlock.setTypeId((short)0);
//                return true;
//            }
//        }
        return false;
    }

    /**
     * @return the levelHandicap
     */
    public int getLevelHandicap() {
        return levelHandicap;
    }

    /**
     * @param levelHandicap the levelHandicap to set
     */
    public void setLevelHandicap(int levelHandicap) {
        this.levelHandicap = levelHandicap;
    }

}
