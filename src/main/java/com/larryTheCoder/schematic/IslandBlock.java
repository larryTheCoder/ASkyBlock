/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
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
package com.larryTheCoder.schematic;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.*;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import org.jnbt.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.larryTheCoder.utils.Utils.loadChunkAt;

/**
 * The package will rules every object in Schematic without this, the schematic
 * is useless
 *
 * @author larryTheCoder
 * @author tastybento
 */
class IslandBlock extends BlockMinecraftId {

    private final int x;
    private final int y;
    private final int z;
    // Current island id
    private final int islandId;
    // Chest contents
    private final HashMap<Integer, Item> chestContents;
    private short typeId;
    private int data;
    private String[] signText;
    // Pot items
    private Block potItem;
    private int potItemData;

    /**
     * @param x
     * @param y
     * @param z
     */
    IslandBlock(int x, int y, int z, int islandId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.islandId = islandId;
        signText = null;
        chestContents = new HashMap<>();
    }

    /**
     * @return the type
     */
    int getTypeId() {
        return typeId;
    }

    /**
     * @param type the type to set
     */
    public void setTypeId(short type) {
        this.typeId = type;
    }

    /**
     * @return the data
     */
    public int getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte data) {
        this.data = data;
    }

    void setBlock(int s, byte b) {
        this.typeId = (short) s;
        this.data = b;
    }

    void setFlowerPot(Map<String, Tag> tileData) {
        // Initialize as default
        potItem = Block.get(Item.AIR);
        potItemData = 0;
        try {
            if (tileData.containsKey("Item")) {

                // Get the item in the pot
                if (tileData.get("Item") instanceof IntTag) {
                    // Item is a number, not a material
                    int id = ((IntTag) tileData.get("Item")).getValue();
                    potItem = Block.get(id);
                    // Check it's a viable pot item
                    if (!POT_ITEM_LISTS.containsValue(id)) {
                        // No, so reset to AIR
                        potItem = Block.get(Item.AIR);
                    }
                } else if (tileData.get("Item") instanceof StringTag) {
                    // Item is a material
                    String itemName = ((StringTag) tileData.get("Item")).getValue();
                    if (POT_ITEM_LISTS.containsKey(itemName)) {
                        // Check it's a viable pot item
                        if (POT_ITEM_LISTS.containsKey(itemName)) {
                            potItem = Block.get(POT_ITEM_LISTS.get(itemName));
                        }
                    }
                }

                if (tileData.containsKey("Data")) {
                    int dataTag = ((IntTag) tileData.get("Data")).getValue();
                    // We should check data for each type of potItem
                    if (potItem == Block.get(Item.ROSE)) {
                        if (dataTag >= 0 && dataTag <= 8) {
                            potItemData = dataTag;
                        } else {
                            // Prevent hacks
                            potItemData = 0;
                        }
                    } else if (potItem == Block.get(Item.FLOWER)
                            || potItem == Block.get(Item.RED_MUSHROOM)
                            || potItem == Block.get(Item.BROWN_MUSHROOM)
                            || potItem == Block.get(Item.CACTUS)) {
                        // Set to 0 anyway
                        potItemData = 0;
                    } else if (potItem == Block.get(Item.SAPLING)) {
                        if (dataTag >= 0 && dataTag <= 4) {
                            potItemData = dataTag;
                        } else {
                            // Prevent hacks
                            potItemData = 0;
                        }
                    } else if (potItem == Block.get(Item.TALL_GRASS)) {
                        // Only 0 or 2
                        if (dataTag == 0 || dataTag == 2) {
                            potItemData = dataTag;
                        } else {
                            potItemData = 0;
                        }
                    } else {
                        // ERROR ?
                        potItemData = 0;
                    }
                } else {
                    potItemData = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the block sign data
     */
    void setSign(Map<String, Tag> tileData) {
        signText = new String[4];

        // I wonder why tastybento write the code too... complex?
        for (int i = 1; i < 5; i++) {
            String line = ((StringTag) tileData.get("Text" + i)).getValue();

            if (line.equalsIgnoreCase("null")) {
                line = "";
            } else if (line.startsWith("{") && line.endsWith("}")) { // JSON Format
                JSONObject jsonObject = new JSONObject(new JSONTokener(line));

                line = jsonObject.getString("text"); // TODO: Find more references for this JSON Object.
            } else if (line.startsWith("\"") && line.endsWith("\"")) { // Something
                line = line.substring(0, line.length() - 1).substring(1);
            }

            signText[i - 1] = line;
        }
    }

    void setChest(Map<String, Tag> tileData) {
        try {
            ListTag chestItems = (ListTag) tileData.get("Items");
            if (chestItems != null) {
                //int number = 0;
                chestItems.getValue().stream().filter((item) -> (item instanceof CompoundTag)).forEach((item) -> {
                    try {
                        // Id is a number
                        short itemType = (short) ((CompoundTag) item).getValue().get("id").getValue();
                        short itemDamage = (short) ((CompoundTag) item).getValue().get("Damage").getValue();
                        byte itemAmount = (byte) ((CompoundTag) item).getValue().get("Count").getValue();
                        Item itemConfirm = Item.get(itemType, (int) itemDamage, itemAmount);
                        byte itemSlot = (byte) ((CompoundTag) item).getValue().get("Slot").getValue();
                        if (itemConfirm.getId() != 0 && !itemConfirm.getName().equalsIgnoreCase("Unknown")) {
                            chestContents.put((int) itemSlot, itemConfirm);
                        }
                    } catch (ClassCastException ex) {
                        // Id is a material
                        String itemType = (String) ((CompoundTag) item).getValue().get("id").getValue();
                        try {
                            // Get the material
                            if (itemType.startsWith("minecraft:")) {
                                String material = itemType.substring(10).toUpperCase();
                                // Special case for non-standard material names
                                int itemMaterial;

                                //Bukkit.getLogger().info("DEBUG: " + material);
                                if (WETOME.containsKey(material)) {
                                    itemMaterial = WETOME.get(material);
                                } else {
                                    itemMaterial = Item.fromString(material).getId();
                                }
                                byte itemAmount = (byte) ((CompoundTag) item).getValue().get("Count").getValue();
                                short itemDamage = (short) ((CompoundTag) item).getValue().get("Damage").getValue();
                                byte itemSlot = (byte) ((CompoundTag) item).getValue().get("Slot").getValue();
                                Item itemConfirm = Item.get(itemMaterial, (int) itemDamage, itemAmount);
                                if (itemConfirm.getId() != 0 && !itemConfirm.getName().equalsIgnoreCase("Unknown")) {
                                    chestContents.put((int) itemSlot, itemConfirm);
                                }
                            }
                        } catch (Exception exx) {
                            Utils.send("Could not parse item [" + itemType.substring(10).toUpperCase() + "] in schematic");
                            exx.printStackTrace();
                        }
                    }
                }); // Format for chest items is:
                // id = short value of item id
                // Damage = short value of item damage
                // Count = the number of items
                // Slot = the slot in the chest
                // inventory
            }
        } catch (Exception e) {
            Utils.send("Could not parse schematic file item, skipping!");
            if (ASkyBlock.get().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Paste this block at blockLoc
     *
     * @param p        The player who created this island
     * @param blockLoc The block location
     */
    void paste(Player p, Position blockLoc, EnumBiome biome) {
        Vector3 loc = new Vector3(x, y, z).add(blockLoc);
        loadChunkAt(new Position(loc.getFloorX(), loc.getFloorY(), loc.getFloorZ(), blockLoc.getLevel()));

        try {
            blockLoc.getLevel().setBlock(loc, Block.get(typeId, data), true, true);
            blockLoc.getLevel().setBiomeId(loc.getFloorX(), loc.getFloorZ(), (byte) biome.id);

            BlockEntitySpawnable e = null;
            if (signText != null) {
                BaseFullChunk chunk = blockLoc.getLevel().getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
                BlockEntitySign signDb = (BlockEntitySign) (e = (BlockEntitySign) BlockEntity.createBlockEntity(BlockEntity.SIGN, chunk, BlockEntity.getDefaultCompound(loc, BlockEntity.SIGN)));

                int intVal = 0;

                String[] currentText = signText;

                boolean isCleanLine = Arrays.stream(signText).allMatch(i -> TextFormat.clean(i).replace("\n", "").isEmpty());
                if (isCleanLine) currentText = ASkyBlock.get().getSettings(blockLoc.getLevel().getName()).signConfig;

                String[] replacedString = new String[4];
                for (String result : currentText) {
                    replacedString[intVal++] = TextFormat.colorize(result.replace("[player]", p.getName()));
                }
                signDb.setText(replacedString);

                blockLoc.getLevel().addBlockEntity(e);
            } else if (potItem != null) {
                BaseFullChunk chunk = blockLoc.getLevel().getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putString("id", BlockEntity.FLOWER_POT)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z)
                        .putShort("item", potItem.getId())
                        .putInt("data", potItemData);

                e = (BlockEntityFlowerPot) BlockEntity.createBlockEntity(BlockEntity.FLOWER_POT, chunk, nbt);

                blockLoc.getLevel().addBlockEntity(e);
            } else if (Block.get(typeId, data).getId() == Block.CHEST) {
                BaseFullChunk chunk = blockLoc.getLevel().getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                        .putString("id", BlockEntity.CHEST)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z);

                e = (BlockEntityChest) BlockEntity.createBlockEntity(BlockEntity.CHEST, chunk, nbt);

                if (ASkyBlock.get().getSchematics().isUsingDefaultChest(islandId) || chestContents.isEmpty()) {
                    int count = 0;
                    for (Item item : Settings.chestItems) {
                        ((BlockEntityChest) e).getInventory().setItem(count, item);
                        count++;
                    }
                } else {
                    ((BlockEntityChest) e).getInventory().setContents(chestContents);
                }

                blockLoc.getLevel().addBlockEntity(e);
            }

            final BlockEntitySpawnable blockEntity = e;

            // Run as runnable, spawn them in next move.
            if (blockEntity != null) {
                blockLoc.getLevel().scheduleBlockEntityUpdate(blockEntity);

                blockEntity.spawnToAll();
            }
        } catch (Exception ignored) {
            Utils.sendDebug("&7Warning: Block " + typeId + ":" + data + " not found. Ignoring...");
        }
    }

    /**
     * This is the function where the Minecraft PC block bugs (Ex. vine)
     * Were placed and crapping the server
     * <p>
     * Revert function is multi-purposes cause
     */
    void revert(Position blockLoc) {
        try {
            Location loc = new Location(x, y, z, 0, 0, blockLoc.getLevel()).add(blockLoc);
            loadChunkAt(loc);
            blockLoc.getLevel().setBlock(loc, Block.get(Block.AIR), true, true);

            // Remove block entity
            BlockEntity entity = blockLoc.getLevel().getBlockEntity(loc);
            if (entity != null) {
                blockLoc.getLevel().removeBlockEntity(entity);
            }
        } catch (Exception ex) {
            // Nope do noting. This just avoiding a crap message on console
        }
    }

    /**
     * @return Vector for where this block is in the schematic
     */
    public Vector3 getVector() {
        return new Vector3(x, y, z);
    }

}
