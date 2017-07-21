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

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.intellectiualcrafters.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.task.ChestPopulateTask;
import com.larryTheCoder.utils.Utils;
import static com.larryTheCoder.utils.Utils.*;

import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The package will rules every object in Schematic without this, the schematic
 * is useless
 *
 * @author Adam Matthew
 */
public class IslandBlock {

    private short typeId;
    private int data;
    private final int x;
    private final int y;
    private final int z;
    private List<String> signText;
    private PotBlock pot;
    // Chest contents
    private final HashMap<Integer, Item> chestContents;
    public static final HashMap<String, Integer> WETOME = new HashMap<>();

    static {
        // Establish the World Edit to Material look up
        WETOME.put("ARMORSTAND", 0);
        WETOME.put("ACACIA_DOOR", Item.ACACIA_DOOR);
        WETOME.put("BIRCH_DOOR", Item.BIRCH_DOOR);
        WETOME.put("BIRCH_STAIRS", Item.BIRCH_WOOD_STAIRS);
        WETOME.put("DARK_OAK_DOOR", Item.DARK_OAK_DOOR);
        WETOME.put("JUNGLE_DOOR", Item.JUNGLE_DOOR);
        WETOME.put("SLIME", Item.SLIME_BLOCK);
        WETOME.put("SPRUCE_DOOR", Item.SPRUCE_DOOR);
        WETOME.put("BREWING_STAND", Item.BREWING_STAND);
        WETOME.put("CARROT_ON_A_STICK", Item.AIR);
        WETOME.put("CARROT", Item.CARROT);
        WETOME.put("CAULDRON", Item.CAULDRON);
        WETOME.put("CLOCK", Item.CLOCK);
        WETOME.put("COBBLESTONE_WALL", Item.COBBLE_WALL);
        WETOME.put("COMPARATOR", Item.COMPARATOR);
        WETOME.put("COOKED_PORKCHOP", Item.COOKED_PORKCHOP);
        WETOME.put("DIAMOND_HORSE_ARMOR", Item.DIAMOND_HORSE_ARMOR);
        WETOME.put("DIAMOND_SHOVEL", Item.DIAMOND_SHOVEL);
        WETOME.put("DYE", Item.DYE);
        WETOME.put("END_PORTAL_FRAME", Item.END_PORTAL_FRAME);
        WETOME.put("END_STONE", Item.END_STONE);
        WETOME.put("EXPERIENCE_BOTTLE", Item.EXPERIENCE_BOTTLE);
        WETOME.put("FILLED_MAP", Item.MAP);
        WETOME.put("FIRE_CHARGE", Item.AIR);
        WETOME.put("FIREWORKS", Item.AIR);
        WETOME.put("FLOWER_POT", Item.FLOWER_POT);
        WETOME.put("GLASS_PANE", Item.GLASS_PANE);
        WETOME.put("GOLDEN_CHESTPLATE", Item.GOLD_CHESTPLATE);
        WETOME.put("GOLDEN_HORSE_ARMOR", Item.GOLD_HORSE_ARMOR);
        WETOME.put("GOLDEN_LEGGINGS", Item.GOLD_LEGGINGS);
        WETOME.put("GOLDEN_PICKAXE", Item.GOLD_PICKAXE);
        WETOME.put("GOLDEN_RAIL", Item.POWERED_RAIL);
        WETOME.put("GOLDEN_SHOVEL", Item.GOLD_SHOVEL);
        WETOME.put("GOLDEN_SWORD", Item.GOLD_SWORD);
        WETOME.put("GOLDEN_HELMET", Item.GOLD_HELMET);
        WETOME.put("GOLDEN_HOE", Item.GOLD_HOE);
        WETOME.put("GOLDEN_AXE", Item.GOLD_AXE);
        WETOME.put("GOLDEN_BOOTS", Item.GOLD_BOOTS);
        WETOME.put("HARDENED_CLAY", Item.TERRACOTTA); // TODO: get rip this out
        WETOME.put("HEAVY_WEIGHTED_PRESSURE_PLATE", Item.HEAVY_WEIGHTED_PRESSURE_PLATE);
        WETOME.put("IRON_BARS", Item.IRON_BARS);
        WETOME.put("IRON_HORSE_ARMOR", Item.IRON_HORSE_ARMOR);
        WETOME.put("IRON_SHOVEL", Item.IRON_SHOVEL);
        WETOME.put("LEAD", Item.AIR);
        WETOME.put("LEAVES2", Item.LEAVES2);
        WETOME.put("LIGHT_WEIGHTED_PRESSURE_PLATE", Item.LIGHT_WEIGHTED_PRESSURE_PLATE);
        WETOME.put("LOG2", Item.LOG2);
        WETOME.put("MAP", Item.EMPTY_MAP);
        WETOME.put("MYCELIUM", Item.MYCELIUM);
        WETOME.put("NETHER_BRICK_FENCE", Item.NETHER_BRICK_FENCE);
        WETOME.put("NETHER_WART", Item.NETHER_WART);
        WETOME.put("NETHERBRICK", Item.NETHER_BRICK);
        WETOME.put("OAK_STAIRS", Item.WOOD_STAIRS);
        WETOME.put("POTATO", Item.POTATO);
        WETOME.put("RAIL", Item.RAIL);
        WETOME.put("RECORD_11", Item.AIR);
        WETOME.put("RECORD_13", Item.AIR);
        WETOME.put("RECORD_BLOCKS", Item.AIR);
        WETOME.put("RECORD_CAT", Item.AIR);
        WETOME.put("RECORD_CHIRP", Item.AIR);
        WETOME.put("RECORD_FAR", Item.AIR);
        WETOME.put("RECORD_MALL", Item.AIR);
        WETOME.put("RECORD_MELLOHI", Item.AIR);
        WETOME.put("RECORD_STAL", Item.AIR);
        WETOME.put("RECORD_STRAD", Item.AIR);
        WETOME.put("RECORD_WAIT", Item.AIR);
        WETOME.put("RECORD_WARD", Item.AIR);
        WETOME.put("RED_FLOWER", Item.AIR);
        WETOME.put("REEDS", Item.SUGAR_CANE);
        WETOME.put("REPEATER", Item.REPEATER);
        WETOME.put("SKULL", Item.SKULL);
        WETOME.put("SPAWN_EGG", Item.SPAWN_EGG);
        WETOME.put("STONE_BRICK_STAIRS", Item.BRICK_STAIRS);
        WETOME.put("STONE_BRICK_STAIRS", Item.STONE_BRICK_STAIRS);
        WETOME.put("STONE_SHOVEL", Item.STONE_SHOVEL);
        WETOME.put("STONE_SLAB", Item.SLAB);
        WETOME.put("STONE_STAIRS", Item.COBBLESTONE_STAIRS);
        WETOME.put("TNT_MINECART", Item.MINECART_WITH_TNT);
        WETOME.put("WATERLILY", Item.WATER_LILY);
        WETOME.put("WHEAT_SEEDS", Item.SEEDS);
        WETOME.put("WOODEN_AXE", Item.WOODEN_AXE);
        WETOME.put("WOODEN_BUTTON", Item.WOODEN_BUTTON);
        WETOME.put("WOODEN_DOOR", Item.WOODEN_DOOR);
        WETOME.put("WOODEN_HOE", Item.WOODEN_HOE);
        WETOME.put("WOODEN_PICKAXE", Item.WOODEN_PICKAXE);
        WETOME.put("WOODEN_PRESSURE_PLATE", Item.WOODEN_PRESSURE_PLATE);
        WETOME.put("WOODEN_SHOVEL", Item.WOODEN_SHOVEL);
        WETOME.put("WOODEN_SLAB", Item.WOODEN_SLAB);
        WETOME.put("WOODEN_SWORD", Item.WOODEN_SWORD);
    }

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
        pot = null;
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

    public void setFlowerPot(Map<String, Tag> map) {
        pot = new PotBlock();
        pot.prep(map);
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
            public List creatArrayContainer() {
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
                                                Utils.ConsoleMsg("Unknown color " + value + " in sign when pasting schematic, skipping...");
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
                                                Utils.ConsoleMsg("Unknown format " + value + " in sign when pasting schematic, skipping...");
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
    }

    public void setBook(Map<String, Tag> tileData) {
        //Bukkit.getLogger().info("DEBUG: Book data ");
        Utils.ConsoleMsg(tileData.toString());
    }

    @SuppressWarnings("deprecation")
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
                            Utils.ConsoleMsg("Could not parse item [" + itemType.substring(10).toUpperCase() + "] in schematic");
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
            Utils.ConsoleMsg("Could not parse schematic file item, skipping!");
            if (ASkyBlock.get().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Paste this block at blockLoc
     *
     * @param usePhysics
     * @param blockLoc
     */
    public void paste(Location blockLoc, boolean usePhysics) {
        Location loc = new Location(x, y, z, 0, 0, blockLoc.getLevel()).add(blockLoc);
        loadChunkAt(loc);
        // Only paste air if it is below the sea level and in the overworld
        // found the problem why blocks didnt shows up
        // prevent the block to show up
        blockLoc.getLevel().setBlock(loc, Block.get(typeId, data), true, usePhysics);

        // DO NOT MAKE THIS RUN!
        if (signText != null) {
//            blockLoc.getLevel().setBlock(loc, Block.get(Block.SIGN_POST), usePhysics, true);
//            //some time the sign wont appear
//            cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
//                    .putString("id", BlockEntity.SIGN)
//                    .putInt("x", (int) x)
//                    .putInt("y", (int) y)
//                    .putInt("z", (int) z)
//                    .putString("Text1", signText.get(0))
//                    .putString("Text2", signText.get(1))
//                    .putString("Text3", signText.get(2))
//                    .putString("Text4", signText.get(3));
//            BlockEntitySign sign = new BlockEntitySign(blockLoc.getLevel().getChunk((int) x >> 4, (int) z >> 4), nbt);
//            sign.spawnToAll();
        } else if (pot != null) {
            //pot.set(blockLoc, block);
        } else if (Block.get(typeId, data).getId() == Block.CHEST) {
            TaskManager.runTaskLater(new ChestPopulateTask(loc, chestContents), 10);
        }
    }

    /**
     * @return Vector for where this block is in the schematic
     */
    public Vector3 getVector() {
        return new Vector3(x, y, z);
    }
}
