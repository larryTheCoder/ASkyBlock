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
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntityFlowerPot;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.task.TaskManager;
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
 * @author Adam Matthew
 */
public class IslandBlock extends BlockMinecraftId {

    private final int x;
    private final int y;
    private final int z;
    // Chest contents
    private final HashMap<Integer, Item> chestContents;
    private short typeId;
    private int data;
    private List<String> signText;
    // Pot items
    private Block potItem;
    private int potItemData;
    // Debugging
    private MainLogger deb = Server.getInstance().getLogger();

    /**
     * @param x
     * @param y
     * @param z
     */
    public IslandBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        signText = null;
        chestContents = new HashMap<>();
    }

    /**
     * @return the type
     */
    public int getTypeId() {
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
    public void setBlock(int s, byte b) {
        this.typeId = (short) s;
        this.data = b;
    }

    /**
     * Sets this block up with all the skull data required
     *
     * @param map
     * @param dataValue
     */
    public void setSkull(Map<String, Tag> map, int dataValue) {
        //skull = new SkullBlock();
        //skull.prep(map, dataValue);
    }

    public void setFlowerPot(Map<String, Tag> tileData) {
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
    public void setSign(Map<String, Tag> tileData) {
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
                            while (iter.hasNext()) {
                                Object next = iter.next();
                                String format = JSONValue.toJSONString(next);
                                //System.out.println("DEBUG2:" + format);
                                // This doesn't see right, but appears to be the easiest way to identify this string as JSON...
                                if (format.startsWith("{")) {
                                    // JSON string
                                    Map jsonFormat = (Map) parser.parse(format, containerFactory);
                                    Iterator formatIter = jsonFormat.entrySet().iterator();
                                    while (formatIter.hasNext()) {
                                        Map.Entry entry = (Map.Entry) formatIter.next();
                                        //System.out.println("DEBUG3:" + entry.getKey() + "=>" + entry.getValue());
                                        String key = entry.getKey().toString();
                                        String value = entry.getValue().toString();
                                        if (key.equalsIgnoreCase("color")) {
                                            try {
                                                lineText += TextFormat.valueOf(value.toUpperCase());
                                            } catch (Exception noColor) {
                                                Utils.send("Unknown color " + value + " in sign when pasting schematic, skipping...");
                                            }
                                        } else if (key.equalsIgnoreCase("text")) {
                                            lineText += value;
                                        } else // Formatting - usually the value is always true, but check just in case
                                            if (key.equalsIgnoreCase("obfuscated") && value.equalsIgnoreCase("true")) {
                                                lineText += TextFormat.OBFUSCATED;
                                            } else if (key.equalsIgnoreCase("underlined") && value.equalsIgnoreCase("true")) {
                                                lineText += TextFormat.UNDERLINE;
                                            } else {
                                                // The rest of the formats
                                                try {
                                                    lineText += TextFormat.valueOf(key.toUpperCase());
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
                                        lineText += TextFormat.RESET + format.substring(format.indexOf('"') + 1, format.lastIndexOf('"'));
                                    }
                                }
                            }
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

    public void setBook(Map<String, Tag> tileData) {
        //Bukkit.getLogger().info("DEBUG: Book data ");
        Utils.send(tileData.toString());
    }

    public void setChest(Map<String, Tag> tileData) {
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
                        byte itemSlot = (byte) ((CompoundTag) item).getValue().get("Slot").getValue();
                        chestContents.put((int) itemSlot, Item.get(itemType, (int) itemDamage, itemAmount));

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
                                chestContents.put((int) itemSlot, Item.get(itemMaterial, (int) itemDamage, itemAmount));
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
     * @param p
     * @param usePhysics
     * @param blockLoc
     */
    public void paste(Position blockLoc, boolean usePhysics) {
        Location loc = new Location(x, y, z, 0, 0, blockLoc.getLevel()).add(blockLoc);
        loadChunkAt(loc);
        blockLoc.getLevel().setBlock(loc, Block.get(typeId, data), true, usePhysics);

        // BlockEntities
        if (signText != null) {
            // Various bug fixed (Nukkit bug)
            BaseFullChunk chunk = loc.level.getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
            cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                .putString("id", BlockEntity.SIGN)
                .putInt("x", (int) loc.x)
                .putInt("y", (int) loc.y)
                .putInt("z", (int) loc.z);
            BlockEntitySign e = (BlockEntitySign) BlockEntity.createBlockEntity(
                BlockEntity.SIGN,
                chunk,
                nbt);
            e.spawnToAll();
        } else if (potItem != null) {
            BaseFullChunk chunk = loc.level.getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
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
        } else if (Block.get(typeId, data).getId() == Block.CHEST) {
            BaseFullChunk chunk = loc.level.getChunk(loc.getFloorX() >> 4, loc.getFloorZ() >> 4);
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
            e.spawnToAll();
        }

    }

    /**
     * @return Vector for where this block is in the schematic
     */
    public Vector3 getVector() {
        return new Vector3(x, y, z);
    }

}
