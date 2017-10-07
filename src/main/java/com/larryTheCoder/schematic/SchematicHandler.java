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
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSapling;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.level.generator.object.tree.ObjectTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import org.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;

/**
 * Author: Adam Matthew
 * <p>
 * High end Minecraft: PE custom schematic generator. Stored central in integers
 * To find the schematic more efficiently.
 */
public final class SchematicHandler {

    // Avoid OOM during startup OR reload
    private Map<Integer, SoftReference<ArrayList<IslandBlock>>> islandBlocks;
    private Map<Vector3, Map<String, Tag>> tileEntitiesMap;
    private Map<Integer, Map<Configuration, Object>> schemaConfiguration;
    private Map<Integer, String> schematicKey;
    // Schematic size
    private short width;
    private short length;
    private short height;
    // Bedrock location (can be null)
    private Map<Integer, SoftReference<Vector3>> bedrock;
    private Map<Integer, ArrayList<SoftReference<Vector3>>> chest;
    private Map<Integer, ArrayList<SoftReference<Vector3>>> welcomeSign;
    // Panel configuration
    private List<String> schematicList;
    // Configuration
    private Config configFolder;
    // Default island
    private Integer defaultIsland;

    public SchematicHandler(ASkyBlock plugin, File path) {
        Objects.requireNonNull(plugin, "ASkyBlock instance cannot be null");
        if (path == null) {
            Utils.send("&aYou are now using build-in island generation.");
            Utils.send("&aYou also can use schematic to paste island (Without WorldEdit)!");
            return;
        }
        if (!path.isDirectory()) {
            Utils.send("&cThe directory cannot be a file or absolute folder");
            return;
        }
        // Start the schematic handler
        islandBlocks = Maps.newHashMap();
        bedrock = Maps.newHashMap();
        chest = Maps.newHashMap();
        welcomeSign = Maps.newHashMap();
        tileEntitiesMap = Maps.newHashMap();
        schemaConfiguration = Maps.newHashMap();
        schematicKey = Maps.newHashMap();
        schematicList = Lists.newArrayList();
        defaultIsland = -1;
        // List all of the files
        File[] lists = path.listFiles();
        List<File> list = new ArrayList<>();
        File configPath = new File(path, "configuration.yml");

        if (configPath.exists()) {
            configFolder = new Config(configPath);
        } else if (plugin.getResource("schematics/configuration.yml") != null) {
            plugin.saveResource("schematics/configuration.yml", false);
            configFolder = new Config(configPath);
        } else {
            Utils.send("&cCannot find the schematic configuration section");
            Utils.send("&eYou are now using build-in island generation.");
            return;
        }

        if (!configFolder.getBoolean("enable", false)) {
            Utils.send("&aYou are now using build-in island generation.");
            Utils.send("&aYou also can use schematic to paste island (Without WorldEdit)!");
            return;
        }

        Utils.send("&7Loading schematic framework"); // Schematic base-framework

        int id = 0; // The id
        ConfigSection configSection = configFolder.getSection("schematicList");
        for (String key : configSection.getSection("schematic").getKeys(false)) {
            String fileName = configSection.getString("schematic." + key + ".FILE_NAME");
            if (!fileName.isEmpty()) {
                // Check if this file exists or if it is in the jar
                File schematicFile = new File(path, fileName);
                // See if the file exists
                if (schematicFile.exists()) {
                    list.add(schematicFile);
                    schematicKey.put(id, fileName);
                    id++;
                } else if (plugin.getResource("schematics/" + fileName) != null) {
                    plugin.saveResource("schematics/" + fileName, false);
                    list.add(schematicFile);
                    schematicKey.put(id, fileName);
                    id++;
                } else {
                    Utils.send("&e" + fileName + " &adoes not have a filename. Skipping!");
                }
            } else {
                Utils.send("&e" + fileName + " &adoes not have a filename. Skipping!");
            }
        }

        id = 0; // Reset back the id
        Iterator<File> iter = list.iterator();
        while (iter.hasNext()) {
            id++;
            File file = iter.next();
            short[] blocks;
            byte[] data;

            try {
                CompoundTag schematicTag;
                try (FileInputStream stream = new FileInputStream(file); NBTInputStream nbtStream = new NBTInputStream(stream)) {
                    schematicTag = (CompoundTag) nbtStream.readTag();
                }
                if (!schematicTag.getName().equals("Schematic")) {
                    throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
                }
                Map<String, Tag> schematic = schematicTag.getValue();
                Vector3 origin;
                try {
                    int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
                    int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
                    int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
                    Vector3 min = new Vector3(originX, originY, originZ);
                    origin = min.clone();
                } catch (IllegalArgumentException shouldNotHappend) {
                    throw new IOException("Missing piece of WEOriginXYZ");
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

                    CompoundTag nbt = (CompoundTag) tag;
                    EntityObject ent = new EntityObject();
                    for (Map.Entry<String, Tag> entry : nbt.getValue().entrySet()) {
                        if (entry.getKey().equals("id")) {
                            String ide = ((StringTag) entry.getValue()).getValue().toUpperCase();
                            // todo
                        }

                        switch (entry.getKey()) {
                            case "Pos":
                                if (entry.getValue() instanceof ListTag) {
                                    List<Tag> pos;
                                    pos = ((ListTag) entry.getValue()).getValue();
                                    double x = (double) pos.get(0).getValue() - origin.getX();
                                    double y = (double) pos.get(1).getValue() - origin.getY();
                                    double z = (double) pos.get(2).getValue() - origin.getZ();
                                    ent.setLocation(new Vector3(x, y, z));
                                }
                                break;
                            case "Motion":
                                if (entry.getValue() instanceof ListTag) {
                                    List<Tag> pos;
                                    pos = ((ListTag) entry.getValue()).getValue();
                                    ent.setMotion(new Vector3((double) pos.get(0).getValue(), (double) pos.get(1).getValue(),
                                        (double) pos.get(2).getValue()));
                                }
                                break;
                            case "Rotation":
                                if (entry.getValue() instanceof ListTag) {
                                    List<Tag> pos;
                                    pos = ((ListTag) entry.getValue()).getValue();
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
                                    itemTag.getValue().entrySet().forEach((itemEntry) -> {
                                        switch (itemEntry.getKey()) {
                                            case "Count":
                                                if (itemEntry.getValue() instanceof ByteTag) {
                                                    ent.setCount(((ByteTag) itemEntry.getValue()).getValue());
                                                }
                                                break;
                                            case "Damage":
                                                if (itemEntry.getValue() instanceof ShortTag) {
                                                    ent.setDamage(((ShortTag) itemEntry.getValue()).getValue());
                                                }
                                                break;
                                            case "id":
                                                if (itemEntry.getValue() instanceof StringTag) {
                                                    ent.setId(((StringTag) itemEntry.getValue()).getValue());
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                    });
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
                Utils.send(TextFormat.RED + "Error while attempt to register schematic: " + file.getName());
                continue;
            }
            Vector3 bedrockLocation = null;
            ArrayList<SoftReference<Vector3>> chestLocation = Lists.newArrayList();
            ArrayList<SoftReference<Vector3>> signLocation = Lists.newArrayList();
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    for (int z = 0; z < length; ++z) {
                        int index = y * width * length + z * width + x;
                        switch (blocks[index]) {
                            case 7:
                                // Bedrock
                                if (bedrockLocation == null || bedrockLocation.getY() < y) {
                                    bedrockLocation = new Vector3(x, y, z);
                                }
                                break;
                            case 54:
                                // Chest
                                chestLocation.add(new SoftReference(new Vector3(x, y, z)));
                                break;
                            case 63:
                                // Sign
                                signLocation.add(new SoftReference(new Vector3(x, y, z)));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            // Put the attachable blocks into list
            bedrock.put(id, new SoftReference(bedrockLocation));
            chest.put(id, chestLocation);
            welcomeSign.put(id, signLocation);

            handleSchematic(blocks, data, id);
            iter.remove();
        }
        Utils.send("&aSeccessfully loaded &e" + islandBlocks.size() + " &aschematic");
        this.sendTip();
    }

    /**
     * This method prepares to pastes a schematic.
     *
     * @param blocks The Blocks (same as data)
     * @param data   Data (damage or meta)
     */
    public void handleSchematic(short[] blocks, byte[] data) {
        handleSchematic(blocks, data, 1);
    }

    public void handleSchematic(short[] blocks, byte[] data, int id) {
        List blockToAdded = new ArrayList<>();
        Vector3 EndRock = new Vector3();
        if (this.bedrock.get(id) != null) {
            EndRock = this.bedrock.get(id).get();
        }
        Map<Vector3, Map<String, Tag>> TileEntities = tileEntitiesMap;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    // Only bother if this block is above ground zero and
                    // only bother with air if it is below sea level
                    // TODO: need to check max world height too?
                    int h = Settings.islandHieght + y - EndRock.getFloorY();
                    if (h >= 0 && h < 255 && (blocks[index] != 0 || h < Settings.islandHieght)) {
                        // Only bother if the schematic blocks are within the range that y can be
                        IslandBlock block = new IslandBlock(x, y, z);
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
                                case Item.WALL_SIGN:
                                    block.setSign(TileEntities.get(new Vector3(x, y, z)));
                                    break;
                                default:
                                    break;
                            }
                        }
                        blockToAdded.add(block);
                    }

                }
            }
        }
        // Clear all of the island blocks (API)
        if (islandBlocks.get(id) != null) {
            islandBlocks.get(id).clear();
        }
        islandBlocks.put(id, new SoftReference(blockToAdded));
        setDefaultValue(id);
        prepareIslandValue(id);
    }

    /**
     * This method handling player island blocks-by-blocks
     *
     * @param p   The player
     * @param pos The position to pasting the blocks
     * @return True if the player island were generated|null
     */
    public boolean pasteSchematic(Player p, Position pos) {
        return pasteSchematic(p, pos, 1);
    }

    public boolean pasteSchematic(Player p, Position pos, int id) {
        if (islandBlocks == null || islandBlocks.isEmpty() || islandBlocks.get(id) != null) {
            createIsland(p, pos);
            return true;
        }

        List<IslandBlock> blocks = getIslandBlocks(id);
        for (IslandBlock block : blocks) {
            block.paste(pos, true);
        }
        return true;
    }

    /**
     * Get the list of available islands
     *
     * @return An array of the listed blocks
     */
    public List<IslandBlock> getIslandBlocks() {
        return getIslandBlocks(1);
    }

    public List<IslandBlock> getIslandBlocks(int id) {
        return islandBlocks.get(id).get();
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items    The parent tag map
     * @param key      The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     */
    private <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IllegalArgumentException {
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
     * Reset or set the schematic value to default
     *
     * @param id The schematic id
     */
    public void setDefaultValue(int id) {
        if (schemaConfiguration.get(id) == null) {
            schemaConfiguration.put(id, new HashMap<>());
        }
        schemaConfiguration.get(id).clear();
        schemaConfiguration.get(id).put(Configuration.BIOME, Biome.getBiome(Biome.PLAINS));
        schemaConfiguration.get(id).put(Configuration.BLOCK_SPAWN, null);
        schemaConfiguration.get(id).put(Configuration.DESCRIPTION, "Best cozy world");
        schemaConfiguration.get(id).put(Configuration.PASTE_ENTITIES, false);
        schemaConfiguration.get(id).put(Configuration.PERMISSION, "");
        schemaConfiguration.get(id).put(Configuration.RATING, 1.5D);
        schemaConfiguration.get(id).put(Configuration.USE_CONFIG_CHEST, true);
    }

    /**
     * Set the island schematic configuration
     *
     * @param id    The schematic id
     * @param type  Type of the configuration
     * @param value Value of the configuration
     */
    public void setIslandValue(int id, Configuration type, Object value) {
        if (schemaConfiguration.get(id).containsKey(type)) {
            schemaConfiguration.get(id).remove(type);
            schemaConfiguration.get(id).put(type, value);
            return;
        }
        schemaConfiguration.get(id).put(type, value);
    }

    /**
     * @param id
     */
    private void prepareIslandValue(int id) {
        String key = schematicKey.get(id);
        ConfigSection section = configFolder.getSection("schematicList");

        // configuration keys
        String[] blockSpawn = section.getString("schematic." + key + ".BLOCK_SPAWN", "").split(":");
        String description = section.getString("schematic." + key + ".DESCRIPTION", "The island");
        String islandName = section.getString("schematic." + key + ".NAME", "Island");
        String permission = section.getString("schematic." + key + ".PERMISSION", "");
        String biome = section.getString("schematic." + key + ".BIOME", "Plains");
        double rating = section.getDouble("schematic." + key + ".RATING", 0);
        boolean defaultPriority = section.getBoolean("schematic." + key + ".DEFAULT", false);
        boolean useConfigChest = section.getBoolean("schematic." + key + ".USE_CONFIG_CHEST", false);
        boolean usePasteEntity = section.getBoolean("schematic." + key + ".PASTE_ENTITIES", false);
        Block block = null;
        schematicList.add(islandName);

        if (defaultPriority && defaultIsland == -1) {
            defaultIsland = id;
        }
        // Check for configuration type
        for (String sting : blockSpawn) {
            if (!Utils.isNumeric(sting)) {
                break;
            }
            if (block == null) {
                block = Block.get(Integer.parseInt(sting));
            } else if (block.getDamage() == 0) {
                block.setDamage(Integer.parseInt(sting));
            }
        }

        // Set the configuration into system (can be null)
        this.setIslandValue(id, Configuration.BLOCK_SPAWN, block);
        this.setIslandValue(id, Configuration.DESCRIPTION, description);
        this.setIslandValue(id, Configuration.PERMISSION, permission);
        this.setIslandValue(id, Configuration.USE_CONFIG_CHEST, useConfigChest);
        this.setIslandValue(id, Configuration.RATING, rating);
        this.setIslandValue(id, Configuration.BIOME, Biome.getBiome(biome));
        this.setIslandValue(id, Configuration.PASTE_ENTITIES, usePasteEntity);
    }

    /**
     * Return if the schematic using the default chest in config
     *
     * @param id The schematic id
     * @return A boolean
     */
    public boolean isUsingDefaultChest(int id) {
        return Boolean.getBoolean((String) schemaConfiguration.get(id).get(Configuration.USE_CONFIG_CHEST));
    }

    private void createIsland(Player p, Position pos) {
        int groundHeight = pos.getFloorY();
        int X = pos.getFloorX();
        int Z = pos.getFloorZ();
        Level world = pos.level;
        // bedrock - ensures island are not overwritten
        for (int x = X + 13; x < X + 14; ++x) {
            for (int z = Z + 13; z < Z + 14; ++z) {
                world.setBlockIdAt(x, groundHeight, z, Block.BEDROCK);
            }
        }
        // Add some dirt and grass
        for (int x = X + 12; x < X + 15; ++x) {
            for (int z = X + 12; z < X + 15; ++z) {
                world.setBlockIdAt(x, groundHeight + 1, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 2, z, Block.DIRT);
            }
        }
        for (int x = X + 11; x < X + 16; ++x) {
            for (int z = Z + 11; z < Z + 16; ++z) {
                world.setBlockIdAt(x, groundHeight + 3, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 4, z, Block.DIRT);
            }
        }
        for (int x = X + 10; x < X + 17; ++x) {
            for (int z = Z + 10; z < Z + 17; ++z) {
                world.setBlockIdAt(x, groundHeight + 5, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 6, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 7, z, Block.GRASS);
            }
        }
        // Then cut off the corners to make it round-ish
        for (int x_space = X + 13 - 2; x_space <= X + 13 + 2; x_space += 4) {
            for (int z_space = Z + 13 - 2; z_space <= Z + 13 + 2; z_space += 4) {
                world.setBlockIdAt(x_space, groundHeight + 3, z_space, Block.AIR);
                world.setBlockIdAt(x_space, groundHeight + 4, z_space, Block.AIR);
            }
        }

        for (int y = groundHeight - 1; y < groundHeight + 8; ++y) {
            for (int x_space = X + 13 - 3; x_space <= X + 13 + 3; x_space += 6) {
                for (int z_space = Z + 13 - 3; z_space <= Z + 13 + 3; z_space += 6) {
                    world.setBlockIdAt(x_space, y, z_space, Block.AIR);
                }
            }
        }
        int Xt = X + 13;
        int Zt = X + 13;
        // First place
        world.setBlockIdAt(Xt - 1, groundHeight + 1, Zt + 1, Block.AIR);
        world.setBlockIdAt(Xt - 2, groundHeight + 1, Zt + 2, Block.AIR);
        world.setBlockIdAt(Xt - 1, groundHeight + 1, Zt - 1, Block.AIR);
        world.setBlockIdAt(Xt - 2, groundHeight + 1, Zt - 2, Block.AIR);
        // tree
        ObjectTree.growTree(world, X + 10, groundHeight + 8, Z + 11, new NukkitRandom(), BlockSapling.OAK);
        this.initChest(world, X, groundHeight, Z, p);
    }

    private void initChest(Level lvl, int x, int y, int z, Player p) {
        BaseFullChunk chunk = lvl.getChunk(x >> 4, z >> 4);
        lvl.setBlockIdAt(x, y, z, Block.CHEST);

        cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
            .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
            .putString("id", BlockEntity.CHEST)
            .putInt("x", x)
            .putInt("y", y)
            .putInt("z", z);

        BlockEntityChest e = new BlockEntityChest(chunk, nbt);

        // Items
        if (Settings.chestItems.length != 0) {
            int count = 0;
            for (Item item : Settings.chestItems) {
                e.getInventory().setItem(count, item);
                count++;
            }
        } else {
            Map<Integer, Item> items = new HashMap<>();
            items.put(0, Item.get(Item.ICE, 0, 2));
            items.put(1, Item.get(Item.BUCKET, 10, 1));
            items.put(2, Item.get(Item.BONE, 0, 2));
            items.put(3, Item.get(Item.SUGARCANE, 0, 1));
            items.put(4, Item.get(Item.RED_MUSHROOM, 0, 1));
            items.put(5, Item.get(Item.BROWN_MUSHROOM, 0, 2));
            items.put(6, Item.get(Item.PUMPKIN_SEEDS, 0, 2));
            items.put(7, Item.get(Item.MELON, 0, 1));
            items.put(8, Item.get(Item.SAPLING, 0, 1));
            items.put(9, Item.get(Item.STRING, 0, 12));
            items.put(10, Item.get(Item.POISONOUS_POTATO, 0, 32));
            e.getInventory().setContents(items);
        }

    }

    public void sendTip() {
        Random rand = new Random();
        switch (rand.nextInt(5)) {
            case 1:
                Utils.send("&aTIP: &eUse the Minecraft: PE Schematic. MCPC Schematic might result wrong blocks");
                break;
            case 2:
                Utils.send("&aTIP: &eWhile you standing nearby island offset. Don't forget to sneak before getting there.");
                break;
            case 3:
                Utils.send("&aTIP: &eDid you know that this schematic are provided by @tastybento original code?");
                break;
            case 4:
                Utils.send("&aTIP: &eThis plugin can also demand on single world production server. Only use /asc setlobby in world");
                break;
            default:
                Utils.send("&aFrom author: Have a great time. I hope that this plugin could help your server more better!");

        }
    }

    public List<String> getSchemaList() {
        return schematicList;
    }

    public Integer getDefaultIsland() {
        return defaultIsland;
    }
}
