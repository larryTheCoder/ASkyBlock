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
package com.larryTheCoder.schematic;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityPainting.Motive; // Art
import static cn.nukkit.entity.item.EntityPainting.motives;
import static cn.nukkit.math.BlockFace.*;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import org.jnbt.ByteArrayTag;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.FloatTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

/**
 * @author Adam Matthew
 */
public class Schematic {

    private File schematicFolder;
    public ASkyBlock plugin;
    public boolean running = false;
    //Utils
    private short width;
    private short length;
    private short height;
    private Map<Vector3, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
    private Map<String, Motive> paintingList = new HashMap<>();
    private Set<Integer> attachable = new HashSet<>();
    private Map<Byte, BlockFace> facingList = new HashMap<>();
    public HashMap<Integer, Position> spot = new HashMap<>();
    private Vector3 bedrock;
    private Vector3 chest;
    private Vector3 welcomeSign;
    private Vector3 topGrass;
    private Vector3 playerSpawn;
    private int durability;
    private int levelHandicap;
    private List<IslandBlock> islandBlocks;
    private List<String> companionNames;
    private Item icon;
    private String heading;
    private String name;
    private String perm;
    private String description;
    private int rating;
    private boolean useDefaultChest;
    private Biome biome;
    private boolean usePhysics = true;
    private boolean pasteEntities;
    private boolean visible;
    private int order;
    // Name of a schematic this one is paired with
    private String partnerName = "";
    private List<String> islandCompanion;
    private Item[] defaultChestItems;
    public int count = 0;
    // DEBUG
    private MainLogger debug = Server.getInstance().getLogger();

    @SuppressWarnings("deprecation")

    public Schematic(ASkyBlock plugin) {
        this.plugin = plugin;
        // Initialize 
        name = "";
        heading = "";
        description = "Default Island";
        perm = "";
        icon = Item.get(Item.MAP);
        rating = 50;
        useDefaultChest = true;
        biome = Settings.defaultBiome;
        schematicFolder = null;
        islandCompanion = new ArrayList<>();
        islandCompanion.add(Settings.islandCompanion);
        companionNames = Settings.companionNames;
        defaultChestItems = Settings.chestItems;
        visible = true;
        order = 0;
        bedrock = null;
        chest = null;
        welcomeSign = null;
        topGrass = null;
        playerSpawn = null;
        partnerName = "";
    }

    public Schematic(ASkyBlock plugin, File folder) throws IOException {
        if (plugin == null) {
            Server.getInstance().getLogger().alert("plugin cant be null");
            return;
        }
        // Initialize
        this.plugin = plugin;
        name = folder.getName().replace(".schematic", "");
        heading = "";
        description = "";
        perm = "";
        icon = Item.get(Item.MAP);
        rating = 50;
        schematicFolder = folder;
        useDefaultChest = true;
        biome = Settings.defaultBiome;
        islandCompanion = new ArrayList<>();
        islandCompanion.add(Settings.islandCompanion);
        companionNames = Settings.companionNames;
        defaultChestItems = Settings.chestItems;
        pasteEntities = false;
        visible = true;
        order = 0;
        bedrock = null;
        chest = null;
        welcomeSign = null;
        topGrass = null;
        playerSpawn = null;
        //playerSpawnBlock = null;
        partnerName = "";
        this.init();
        this.initAttachable();
        this.initArt();
    }

    protected final void initAttachable() {
        attachable.add(Item.STONE_BUTTON);
        attachable.add(Item.WOODEN_BUTTON);
//        attachable.add(Item);
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

    @SuppressWarnings("deprecation")
    protected final void init() throws IOException {
        short[] blocks;
        byte[] data;
        try {
            CompoundTag schematicTag;
            try (FileInputStream stream = new FileInputStream(schematicFolder); NBTInputStream nbtStream = new NBTInputStream(stream)) {
                schematicTag = (CompoundTag) nbtStream.readTag();
            }
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

            List<Tag> entities = getChildTag(schematic, "Entities", ListTag.class).getValue();
            for (Tag tag : entities) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }

                CompoundTag t = (CompoundTag) tag;
                EntityObject ent = new EntityObject();
                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    if (entry.getKey().equals("id")) {
                        String id = ((StringTag) entry.getValue()).getValue().toUpperCase();
                        //Bukkit.getLogger().info("DEBUG: ID is '" + id + "'");
                        // The mob type might be prefixed with "Minecraft:"
                        if (id.startsWith("MINECRAFT:")) {
                            id = id.substring(10);
                        }
                        // todo
                    }
                    
                    switch (entry.getKey()) {
                        case "Pos":
                            //Bukkit.getLogger().info("DEBUG Pos fond");
                            if (entry.getValue() instanceof ListTag) {
                                //Bukkit.getLogger().info("DEBUG coord found");
                                List<Tag> pos = new ArrayList<>();
                                pos = ((ListTag) entry.getValue()).getValue();
                                //Bukkit.getLogger().info("DEBUG pos: " + pos);
                                double x = (double) pos.get(0).getValue() - origin.getX();
                                double y = (double) pos.get(1).getValue() - origin.getY();
                                double z = (double) pos.get(2).getValue() - origin.getZ();
                                ent.setLocation(new Vector3(x, y, z));
                            }
                            break;
                        case "Motion":
                            //Bukkit.getLogger().info("DEBUG Pos fond");
                            if (entry.getValue() instanceof ListTag) {
                                //Bukkit.getLogger().info("DEBUG coord found");
                                List<Tag> pos = new ArrayList<>();
                                pos = ((ListTag) entry.getValue()).getValue();
                                //Bukkit.getLogger().info("DEBUG pos: " + pos);
                                ent.setMotion(new Vector3((double) pos.get(0).getValue(), (double) pos.get(1).getValue(),
                                        (double) pos.get(2).getValue()));
                            }
                            break;
                        case "Rotation":
                            //Bukkit.getLogger().info("DEBUG Pos fond");
                            if (entry.getValue() instanceof ListTag) {
                                //Bukkit.getLogger().info("DEBUG coord found");
                                List<Tag> pos = new ArrayList<>();
                                pos = ((ListTag) entry.getValue()).getValue();
                                //Bukkit.getLogger().info("DEBUG pos: " + pos);
                                ent.setYaw((float) pos.get(0).getValue());
                                ent.setPitch((float) pos.get(1).getValue());
                            }
                            break;
                        case "Color":
                            if (entry.getValue() instanceof ByteTag) {
                                ent.setColor(((ByteTag) entry.getValue()).getValue());
                            }
                            break;
                        case "Sheared":
                            if (entry.getValue() instanceof ByteTag) {
                                if (((ByteTag) entry.getValue()).getValue() != (byte) 0) {
                                    ent.setSheared(true);
                                } else {
                                    ent.setSheared(false);
                                }
                            }
                            break;
                        case "RabbitType":
                            if (entry.getValue() instanceof IntTag) {
                                ent.setRabbitType(((IntTag) entry.getValue()).getValue());
                            }
                            break;
                        case "Profession":
                            if (entry.getValue() instanceof IntTag) {
                                ent.setProfession(((IntTag) entry.getValue()).getValue());
                            }
                            break;
                        case "CarryingChest":
                            if (entry.getValue() instanceof ByteTag) {
                                ent.setCarryingChest(((ByteTag) entry.getValue()).getValue());
                            }
                            break;
                        case "OwnerUUID":
                            ent.setOwned(true);
                            break;
                        case "CollarColor":
                            if (entry.getValue() instanceof ByteTag) {
                                ent.setCollarColor(((ByteTag) entry.getValue()).getValue());
                            }
                            break;
                        case "Facing":
                            if (entry.getValue() instanceof ByteTag) {
                                ent.setFacing(((ByteTag) entry.getValue()).getValue());
                            }
                            break;
                        case "Motive":
                            if (entry.getValue() instanceof StringTag) {
                                ent.setMotive(((StringTag) entry.getValue()).getValue());
                            }
                            break;
                        case "ItemDropChance":
                            if (entry.getValue() instanceof FloatTag) {
                                ent.setItemDropChance(((FloatTag) entry.getValue()).getValue());
                            }
                            break;
                        case "ItemRotation":
                            if (entry.getValue() instanceof ByteTag) {
                                ent.setItemRotation(((ByteTag) entry.getValue()).getValue());
                            }
                            break;
                        case "Item":
                            if (entry.getValue() instanceof CompoundTag) {
                                CompoundTag itemTag = (CompoundTag) entry.getValue();
                                for (Map.Entry<String, Tag> itemEntry : itemTag.getValue().entrySet()) {
                                    if (itemEntry.getKey().equals("Count")) {
                                        if (itemEntry.getValue() instanceof ByteTag) {
                                            ent.setCount(((ByteTag) itemEntry.getValue()).getValue());
                                        }
                                    } else if (itemEntry.getKey().equals("Damage")) {
                                        if (itemEntry.getValue() instanceof ShortTag) {
                                            ent.setDamage(((ShortTag) itemEntry.getValue()).getValue());
                                        }
                                    } else if (itemEntry.getKey().equals("id")) {
                                        if (itemEntry.getValue() instanceof StringTag) {
                                            ent.setId(((StringTag) itemEntry.getValue()).getValue());
                                        }
                                    }
                                }
                            }
                            break;
                        case "TileX":
                            if (entry.getValue() instanceof IntTag) {
                                ent.setTileX((double) ((IntTag) entry.getValue()).getValue() - origin.getX());
                            }
                            break;
                        case "TileY":
                            if (entry.getValue() instanceof IntTag) {
                                ent.setTileY((double) ((IntTag) entry.getValue()).getValue() - origin.getY());
                            }
                            break;
                        case "TileZ":
                            if (entry.getValue() instanceof IntTag) {
                                ent.setTileZ((double) ((IntTag) entry.getValue()).getValue() - origin.getZ());
                            }
                            break;
                        default:
                            break;
                    }

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

                Vector3 vec = new Vector3(x, y, z);
                tileEntitiesMap.put(vec, values);
            }

        } catch (IOException e) {
            Server.getInstance().getLogger().info(TextFormat.RED + "An error occured while attemping to load Schematic File!");
            throw new IOException();
        }
        List<Vector3> grassBlocks = new ArrayList<>();
        boolean loop = true;
        int countBlocks = 0;
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
                            }
                            break;
                        case 54:
                            // Last chest
                            if (chest == null || chest.getY() < y) {
                                chest = new Vector3(x, y, z);
                            }
                            break;
                        case 63:
                            // Sign
                            if (welcomeSign == null || welcomeSign.getY() < y) {
                                welcomeSign = new Vector3(x, y, z);
                            }
                            break;
                        case 2:
                            // Grass
                            grassBlocks.add(new Vector3(x, y, z));
                            break;

                        default:
                            break;
                    }
                    if (blocks[index] != 0) {
                        countBlocks += 1;
                    }
                }
            }
        }
        if (bedrock == null) {
            Server.getInstance().getLogger().error("Schematic must have at least one bedrock in it!");
            throw new IOException();
        }
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
        if (countBlocks != 0) {
            Server.getInstance().getLogger().info(TextFormat.YELLOW + " - " + schematicFolder.getName().toUpperCase().replace(".SCHEMATIC", "") + " Info:");
            Server.getInstance().getLogger().info(TextFormat.GRAY + "   - Blocks: " + TextFormat.GREEN + countBlocks + " ");
            Server.getInstance().getLogger().info(TextFormat.GRAY + "   - SafeSpot: " + TextFormat.RED + "Currently deprecated");
        }
        facingList.put((byte) 0, SOUTH);
        facingList.put((byte) 1, WEST);
        facingList.put((byte) 2, NORTH);
        facingList.put((byte) 3, EAST);
        prePasteSchematic(blocks, data);
    }

    /**
     * This method prepares to pastes a schematic.
     *
     * @param blocks
     * @param data
     */
    public void prePasteSchematic(short[] blocks, byte[] data) {
        islandBlocks = new ArrayList<>();
        Map<Vector3, Map<String, Tag>> TileEntities = this.getTileEntitiesMap();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    // Only bother if this block is above ground zero and 
                    // only bother with air if it is below sea level
                    // TODO: need to check max world height too?
                    int h = Settings.islandHieght + y - bedrock.getFloorY();
                    if (h >= 0 && h < 255 && (blocks[index] != 0 || h < Settings.islandHieght)) {
                        // Only bother if the schematic blocks are within the range that y can be
                        //plugin.getLogger().info("DEBUG: height " + (count++) + ":" +h);
                        IslandBlock block = new IslandBlock(x, y, z);
                        if (!attachable.contains((int) blocks[index]) || blocks[index] == 179) {
                            block.setBlock(blocks[index], data[index]);
                            // Tile Entities
                            if (TileEntities.containsKey(new Vector3(x, y, z))) {
                                switch (block.getTypeId()) {
                                    case Item.FLOWER_POT:
                                        block.setFlowerPot(TileEntities.get(new Vector3(x, y, z)));
                                        break;
                                    case Item.SIGN_POST:
                                        block.setSign(TileEntities.get(new Vector3(x, y, z)));
                                        break;
                                    case Item.CHEST:
                                        block.setChest(TileEntities.get(new Vector3(x, y, z)));
                                        break;
                                    default:
                                        break;
                                }
                            }
                            islandBlocks.add(block);
                        }
                    }
                }
            }
        }
        // Second pass - just paste attachables and deal with chests etc.
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int h = Settings.seaLevel + y - bedrock.getFloorY();
                    if (h >= 0 && h < 255) {
                        int index = y * width * length + z * width + x;
                        IslandBlock block = new IslandBlock(x, y, z);
                        if (attachable.contains((int) blocks[index])) {
                            block.setBlock(blocks[index], data[index]);
                            // Tile Entities
                            if (TileEntities.containsKey(new Vector3(x, y, z))) {
                                // Wall Sign
                                if (block.getTypeId() == Item.WALL_SIGN) {
                                    block.setSign(TileEntities.get(new Vector3(x, y, z)));
                                }
                            }
                            islandBlocks.add(block);
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Position> getSafeSpots() {
        ArrayList<Position> list = new ArrayList<>();
        spot.values().stream().forEach((pos) -> {
            list.add(pos);
        });
        return list;
    }

    /**
     * This method pastes a schematic
     *
     * @param loc
     */
    public void pasteSchematic(Location loc) {
        // If this is not a file schematic, paste the default island
        if (this.schematicFolder == null) {
            //plugin.getFallback().createIsland(loc.getLevel(), loc.getFloorX() - 13, loc.getFloorY(), loc.getFloorZ() - 14, player);
            Utils.ConsoleMsg(TextFormat.RED + "Missing schematic - using default block only");
            return;
        }
        Level world = loc.getLevel();
        Location blockLoc = new Location(loc.getX(), loc.getY(), loc.getZ(), 0, 0, world);
        blockLoc.subtract(bedrock);
        // Paste the island blocks
        islandBlocks.stream().forEach((b) -> {
            b.paste(blockLoc, true);
        });

        // Find the grass spot
        final Location grass;
        if (topGrass != null) {
            Location gr = new Location().add(topGrass).subtract(bedrock);
            gr.add(loc.normalize());
            gr.add(new Vector3(0.5D, 1.1D, 0.5D)); // Center of block and a bit up so the animal drops a bit
            grass = gr;
        } else {
            grass = null;
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

    /**
     * @return the biome
     */
    public Biome getBiome() {
        return biome;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return schematicFolder;
    }

    /**
     * @return the heading
     */
    public String getHeading() {
        return heading;
    }

    /**
     * @return the height
     */
    public short getHeight() {
        return height;
    }

    /**
     * @return the durability of the icon
     */
    public int getDurability() {
        return durability;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the perm
     */
    public String getPerm() {
        return perm;
    }

    /**
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * @return the tileEntitiesMap
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Map<Vector3, Map<String, Tag>> getTileEntitiesMap() {
        return tileEntitiesMap;
    }

    /**
     * @return the width
     */
    public short getWidth() {
        return width;
    }

    /**
     * @return the useDefaultChest
     */
    public boolean isUseDefaultChest() {
        return useDefaultChest;
    }

    /**
     * @return the usePhysics
     */
    public boolean isUsePhysics() {
        return usePhysics;
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
     * @param rating the rating to set
     */
    public void setRating(int rating) {
        this.rating = rating;
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
        for (IslandBlock islandBlock : islandBlocks) {
            if (islandBlock.getTypeId() == playerSpawnBlock.getId()) {
                playerSpawn = islandBlock.getVector().subtract(bedrock).add(new Vector3(0.5D, 0D, 0.5D));
                // Set the block to air
                islandBlock.setTypeId((short) 0);
                return true;
            }
        }
        return false;
    }

    /**
     * @return the levelHandicap
     */
    public int getLevelHandicap() {
        return levelHandicap;
    }

    public void setIcon(Item icon, int damage) {
        this.icon = icon;
        this.durability = damage; // META
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(int icon) {
        this.icon = Item.get(icon);
    }

    /**
     * @param useDefaultChest the useDefaultChest to set
     */
    public void setUseDefaultChest(boolean useDefaultChest) {
        this.useDefaultChest = useDefaultChest;
    }

    /**
     * @param levelHandicap the levelHandicap to set
     */
    public void setLevelHandicap(int levelHandicap) {
        this.levelHandicap = levelHandicap;
    }

    /**
     * Spawns a random companion for the player with a random name at the
     * location given
     *
     * @param player
     * @param location
     */
    protected void spawnCompanion(Player player, Location location) {
        // Older versions of the server require custom names to only apply to Living Entities
        //Bukkit.getLogger().info("DEBUG: spawning compantion at " + location);
        if (!islandCompanion.isEmpty() && location != null) {
            Random rand = new Random();
            int randomNum = rand.nextInt(islandCompanion.size());
            String type = islandCompanion.get(randomNum);
            if (type != null) {
                //EntityLiving companion = Entity.createEntity(name, chunk, nbt, motives).spawnEntity(location, type);
                if (!companionNames.isEmpty()) {
                    randomNum = rand.nextInt(companionNames.size());
                    String var = companionNames.get(randomNum).replace("[player]", player.getDisplayName());
                    //plugin.getLogger().info("DEBUG: name is " + name);
                    //companion.setCustomName(name);
                    //companion.setCustomNameVisible(true);
                }
            }
        }
    }

    /**
     * @param islandCompanion the islandCompanion to set
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setIslandCompanion(List<String> islandCompanion) {
        this.islandCompanion = islandCompanion;
    }

    /**
     * @param companionNames the companionNames to set
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setCompanionNames(List<String> companionNames) {
        this.companionNames = companionNames;
    }

    /**
     * @param defaultChestItems the defaultChestItems to set
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void setDefaultChestItems(Item[] defaultChestItems) {
        this.defaultChestItems = defaultChestItems;
    }

    /**
     * @return the partnerName
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public String getPartnerName() {
        return partnerName;
    }

    /**
     * @param partnerName the partnerName to set
     */
    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    /**
     * @param biome the biome to set
     */
    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the pasteEntities
     */
    public boolean isPasteEntities() {
        return pasteEntities;
    }

    /**
     * @param pasteEntities the pasteEntities to set
     */
    public void setPasteEntities(boolean pasteEntities) {
        this.pasteEntities = pasteEntities;
    }

    /**
     * Whether the schematic is visible or not
     *
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets if the schematic can be seen in the schematics GUI or not by the
     * player
     *
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public void setBiome(int HELL) {
        setBiome(Biome.getBiome(HELL));
    }

    /**
     * @param perm the perm to set
     */
    public void setPerm(String perm) {
        this.perm = perm;
    }
}
