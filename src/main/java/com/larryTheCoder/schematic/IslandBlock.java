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
package com.larryTheCoder.schematic;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntityFlowerPot;
import cn.nukkit.blockentity.BlockEntitySign;
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
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

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
    private List<String> signText;
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

    /**
     * @return the signText
     */
    public List<String> getSignText() {
        return signText;
    }

    /**
     * @param signText the signText to set
     */
    public void setSignText(List<String> signText) {
        this.signText = signText;
    }

    /**
     * @param s
     * @param b
     */
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
     * Sets this block's sign data
     *
     * @param tileData
     */
    void setSign(Map<String, Tag> tileData) {
        signText = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            String line = ((StringTag) tileData.get("Text" + String.valueOf(i))).getValue();
            // This value can actually be a string that says null sometimes.
            if (line.equalsIgnoreCase("null")) {
                line = "";
            }
            //System.out.println("DEBUG: line " + i + " = '"+ line + "' of length " + line.length());
            text.add(line);
        }

        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory() {
            @Override
            public List createArrayContainer() {
                return new LinkedList<>();
            }

            @Override
            public Map createObjectContainer() {
                return new LinkedHashMap<>();
            }

        };
        // This just removes all the JSON formatting and provides the raw text
        for (int line = 0; line < 4; line++) {
            String lineText = "";
            if (!text.get(line).equals("\"\"") && !text.get(line).isEmpty()) {
                //String lineText = text.get(line).replace("{\"extra\":[\"", "").replace("\"],\"text\":\"\"}", "");
                //Bukkit.getLogger().info("DEBUG: sign text = '" + text.get(line) + "'");
                if (text.get(line).startsWith("{")) {
                    // JSON string
                    try {

                        Map json = (Map) parser.parse(text.get(line), containerFactory);
                        List list = (List) json.get("extra");
                        //System.out.println("DEBUG1:" + JSONValue.toJSONString(list));
                        if (list != null) {
                            Iterator iter = list.iterator();
                            StringBuilder lineTextBuilder = new StringBuilder();
                            while (iter.hasNext()) {
                                Object next = iter.next();
                                String format = JSONValue.toJSONString(next);
                                //System.out.println("DEBUG2:" + format);
                                // This doesn't see right, but appears to be the easiest way to identify this string as JSON...
                                if (format.startsWith("{")) {
                                    // JSON string
                                    Map jsonFormat = (Map) parser.parse(format, containerFactory);
                                    for (Object o : jsonFormat.entrySet()) {
                                        Map.Entry entry = (Map.Entry) o;
                                        //System.out.println("DEBUG3:" + entry.getKey() + "=>" + entry.getValue());
                                        String key = entry.getKey().toString();
                                        String value = entry.getValue().toString();
                                        if (key.equalsIgnoreCase("color")) {
                                            try {
                                                lineTextBuilder.append(TextFormat.valueOf(value.toUpperCase()));
                                            } catch (Exception noColor) {
                                                Utils.send("Unknown color " + value + " in sign when pasting schematic, skipping...");
                                            }
                                        } else if (key.equalsIgnoreCase("text")) {
                                            lineTextBuilder.append(value);
                                        } else // Formatting - usually the value is always true, but check just in case
                                            if (key.equalsIgnoreCase("obfuscated") && value.equalsIgnoreCase("true")) {
                                                lineTextBuilder.append(TextFormat.OBFUSCATED);
                                            } else if (key.equalsIgnoreCase("underlined") && value.equalsIgnoreCase("true")) {
                                                lineTextBuilder.append(TextFormat.UNDERLINE);
                                            } else {
                                                // The rest of the formats
                                                try {
                                                    lineTextBuilder.append(TextFormat.valueOf(key.toUpperCase()));
                                                } catch (Exception noFormat) {
                                                    // Ignore
                                                    //System.out.println("DEBUG3:" + key + "=>" + value);
                                                    Utils.send("Unknown format " + value + " in sign when pasting schematic, skipping...");
                                                }
                                            }
                                    }
                                } else// This is unformatted text. It is included in "". A reset is required to clear
                                // any previous formatting
                                {
                                    if (format.length() > 1) {
                                        lineTextBuilder.append(TextFormat.RESET).append(format, format.indexOf('"') + 1, format.lastIndexOf('"'));
                                    }
                                }
                            }
                            lineText = lineTextBuilder.toString();
                        } else {
                            // No extra tag
                            json = (Map) parser.parse(text.get(line), containerFactory);
                            String value = (String) json.get("text");
                            //System.out.println("DEBUG text only?:" + value);
                            lineText += value;
                        }
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else // This is unformatted text (not JSON). It is included in "".
                    if (text.get(line).length() > 1) {
                        try {
                            lineText = text.get(line).substring(text.get(line).indexOf('"') + 1, text.get(line).lastIndexOf('"'));
                        } catch (Exception e) {
                            //There may not be those "'s, so just use the raw line
                            lineText = text.get(line);
                        }
                    } else {
                        // just in case it isn't - show the raw line
                        lineText = text.get(line);
                    }
                //Bukkit.getLogger().info("Line " + line + " is " + lineText);
            }
            signText.add(lineText);
        }

        boolean change = true;
        for (String texts : signText) {
            if (!texts.isEmpty()) {
                change = false;
                break;
            }
        }

        if (change) {
            signText.clear();
            signText.add("§aWelcome to");
            signText.add("§e[player]'s");
            signText.add("§aisland! Enjoy.");
            signText.add("");
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
        Location loc = new Location(x, y, z, 0, 0, blockLoc.getLevel()).add(blockLoc);
        while (!loc.getLevel().getChunk((int) loc.getX() >> 4, (int) loc.getZ() >> 4).isLoaded()) {
            loadChunkAt(loc);
        }
        try {
            blockLoc.getLevel().setBlock(loc, Block.get(typeId, data), true, true);
            blockLoc.getLevel().setBiomeId(loc.getFloorX(), loc.getFloorZ(), (byte) biome.id);

            // Usually when the chunk is loaded it will be fully loaded, no need task anymore
            if (signText != null) {
                // Various bug fixed (Nukkit bug)
                BaseFullChunk chunk = blockLoc.getLevel().getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                        .putString("id", BlockEntity.SIGN)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z)
                        .putString("Text1", signText.get(0).replace("[player]", p.getName()))
                        .putString("Text2", signText.get(1).replace("[player]", p.getName()))
                        .putString("Text3", signText.get(2).replace("[player]", p.getName()))
                        .putString("Text4", signText.get(3).replace("[player]", p.getName()));

                BlockEntitySign e = (BlockEntitySign) BlockEntity.createBlockEntity(
                        BlockEntity.SIGN,
                        chunk,
                        nbt);

                blockLoc.getLevel().addBlockEntity(e);
                e.spawnToAll();
            } else if (potItem != null) {
                BaseFullChunk chunk = blockLoc.getLevel().getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putString("id", BlockEntity.FLOWER_POT)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z)
                        .putShort("item", potItem.getId())
                        .putInt("data", potItemData);

                BlockEntityFlowerPot potBlock = (BlockEntityFlowerPot) BlockEntity.createBlockEntity(
                        BlockEntity.FLOWER_POT,
                        chunk,
                        nbt);

                blockLoc.getLevel().addBlockEntity(potBlock);
            } else if (Block.get(typeId, data).getId() == Block.CHEST) {
                BaseFullChunk chunk = blockLoc.getLevel().getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                        .putString("id", BlockEntity.CHEST)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z);
                BlockEntityChest e = (BlockEntityChest) BlockEntity.createBlockEntity(
                        BlockEntity.CHEST,
                        chunk,
                        nbt);
                if (ASkyBlock.get().getSchematics().isUsingDefaultChest(islandId) || chestContents.isEmpty()) {
                    int count = 0;
                    for (Item item : Settings.chestItems) {
                        e.getInventory().setItem(count, item);
                        count++;
                    }
                } else {
                    e.getInventory().setContents(chestContents);
                }

                blockLoc.getLevel().addBlockEntity(e);
                e.spawnToAll();
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
