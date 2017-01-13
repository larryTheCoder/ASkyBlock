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

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockChest;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import larryTheCoder.ASkyBlock;
import larryTheCoder.utils.Utils;
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
 * @author larryTheCoder
 */
public class IslandBlock {

    private short typeId;
    private int data;
    private int x;
    private int y;
    private int z;
    private List<String> signText;
    private PotBlock pot;
    // Chest contents
    private HashMap<Integer, Item> chestContents = new HashMap<>();
    public static final HashMap<String, Integer> WEtoM = new HashMap<>();
    public static final HashMap<String, Integer> WEtoME = new HashMap<>();

    static {
        // Establish the World Edit to Material look up
        // V1.8 items
        WEtoM.put("ARMORSTAND", 0);
        WEtoM.put("ACACIA_DOOR", Item.ACACIA_DOOR);
        WEtoM.put("BIRCH_DOOR", Item.BIRCH_DOOR);
        WEtoM.put("BIRCH_STAIRS", Item.BIRCH_WOOD_STAIRS);
        WEtoM.put("DARK_OAK_DOOR", Item.DARK_OAK_DOOR);
        WEtoM.put("JUNGLE_DOOR", Item.JUNGLE_DOOR);
        WEtoM.put("SLIME", Item.SLIME_BLOCK);
        WEtoM.put("SPRUCE_DOOR", Item.SPRUCE_DOOR);
        WEtoM.put("BREWING_STAND", Item.BREWING_STAND);
        WEtoM.put("CARROT_ON_A_STICK", Item.AIR);
        WEtoM.put("CARROT", Item.CARROT);
        WEtoM.put("CAULDRON", Item.CAULDRON);
        WEtoM.put("CLOCK", Item.CLOCK);
        WEtoM.put("COBBLESTONE_WALL", Item.COBBLE_WALL);
        WEtoM.put("COMPARATOR", Item.COMPARATOR);
        WEtoM.put("COOKED_PORKCHOP", Item.COOKED_PORKCHOP);
//        WEtoM.put("DIAMOND_HORSE_ARMOR", Item.DIAMOND_HORSE_ARMOR);
        WEtoM.put("DIAMOND_SHOVEL", Item.DIAMOND_SHOVEL);
        WEtoM.put("DYE", Item.DYE);
        WEtoM.put("END_PORTAL_FRAME", Item.END_PORTAL_FRAME);
        WEtoM.put("END_STONE", Item.END_STONE);
        WEtoM.put("EXPERIENCE_BOTTLE", Item.EXPERIENCE_BOTTLE);
        WEtoM.put("FILLED_MAP", Item.MAP);
        WEtoM.put("FIRE_CHARGE", Item.AIR);
        WEtoM.put("FIREWORKS", Item.AIR);
        WEtoM.put("FLOWER_POT", Item.FLOWER_POT);
        WEtoM.put("GLASS_PANE", Item.GLASS_PANE);
        WEtoM.put("GOLDEN_CHESTPLATE", Item.GOLD_CHESTPLATE);
//        WEtoM.put("GOLDEN_HORSE_ARMOR", Item.GOLD_HORSE_ARMOR);
        WEtoM.put("GOLDEN_LEGGINGS", Item.GOLD_LEGGINGS);
        WEtoM.put("GOLDEN_PICKAXE", Item.GOLD_PICKAXE);
        WEtoM.put("GOLDEN_RAIL", Item.POWERED_RAIL);
        WEtoM.put("GOLDEN_SHOVEL", Item.GOLD_SHOVEL);
        WEtoM.put("GOLDEN_SWORD", Item.GOLD_SWORD);
        WEtoM.put("GOLDEN_HELMET", Item.GOLD_HELMET);
        WEtoM.put("GOLDEN_HOE", Item.GOLD_HOE);
        WEtoM.put("GOLDEN_AXE", Item.GOLD_AXE);
        WEtoM.put("GOLDEN_BOOTS", Item.GOLD_BOOTS);
        WEtoM.put("HARDENED_CLAY", Item.HARDENED_CLAY);
        WEtoM.put("HEAVY_WEIGHTED_PRESSURE_PLATE", Item.HEAVY_WEIGHTED_PRESSURE_PLATE);
        WEtoM.put("IRON_BARS", Item.IRON_BARS);
        WEtoM.put("IRON_HORSE_ARMOR", Item.IRON_HORSE_ARMOR);
        WEtoM.put("IRON_SHOVEL", Item.IRON_SHOVEL);
        WEtoM.put("LEAD", Item.AIR);
        WEtoM.put("LEAVES2", Item.LEAVES2);
        WEtoM.put("LIGHT_WEIGHTED_PRESSURE_PLATE", Item.LIGHT_WEIGHTED_PRESSURE_PLATE);
        WEtoM.put("LOG2", Item.LOG2);
        WEtoM.put("MAP", Item.EMPTY_MAP);
        WEtoM.put("MYCELIUM", Item.MYCELIUM);
        WEtoM.put("NETHER_BRICK_FENCE", Item.NETHER_BRICK_FENCE);
        WEtoM.put("NETHER_WART", Item.NETHER_WART);
        WEtoM.put("NETHERBRICK", Item.NETHER_BRICK);
        WEtoM.put("OAK_STAIRS", Item.WOOD_STAIRS);
        WEtoM.put("POTATO", Item.POTATO);
        WEtoM.put("RAIL", Item.RAIL);
        WEtoM.put("RECORD_11", Item.AIR);
        WEtoM.put("RECORD_13", Item.AIR);
        WEtoM.put("RECORD_BLOCKS", Item.AIR);
        WEtoM.put("RECORD_CAT", Item.AIR);
        WEtoM.put("RECORD_CHIRP", Item.AIR);
        WEtoM.put("RECORD_FAR", Item.AIR);
        WEtoM.put("RECORD_MALL", Item.AIR);
        WEtoM.put("RECORD_MELLOHI", Item.AIR);
        WEtoM.put("RECORD_STAL", Item.AIR);
        WEtoM.put("RECORD_STRAD", Item.AIR);
        WEtoM.put("RECORD_WAIT", Item.AIR);
        WEtoM.put("RECORD_WARD", Item.AIR);
        WEtoM.put("RED_FLOWER", Item.AIR);
        WEtoM.put("REEDS", Item.SUGAR_CANE);
        WEtoM.put("REPEATER", Item.REPEATER);
        WEtoM.put("SKULL", Item.SKULL);
        WEtoM.put("SPAWN_EGG", Item.SPAWN_EGG);
        WEtoM.put("STONE_BRICK_STAIRS", Item.BRICK_STAIRS);
        WEtoM.put("STONE_BRICK_STAIRS", Item.STONE_BRICK_STAIRS);
        WEtoM.put("STONE_SHOVEL", Item.STONE_SHOVEL);
        WEtoM.put("STONE_SLAB", Item.SLAB);
        WEtoM.put("STONE_STAIRS", Item.COBBLESTONE_STAIRS);
        WEtoM.put("TNT_MINECART", Item.MINECART_WITH_TNT);
        WEtoM.put("WATERLILY", Item.WATER_LILY);
        WEtoM.put("WHEAT_SEEDS", Item.SEEDS);
        WEtoM.put("WOODEN_AXE", Item.WOODEN_AXE);
        WEtoM.put("WOODEN_BUTTON", Item.WOODEN_BUTTON);
        WEtoM.put("WOODEN_DOOR", Item.WOODEN_DOOR);
        WEtoM.put("WOODEN_HOE", Item.WOODEN_HOE);
        WEtoM.put("WOODEN_PICKAXE", Item.WOODEN_PICKAXE);
        WEtoM.put("WOODEN_PRESSURE_PLATE", Item.WOODEN_PRESSURE_PLATE);
        WEtoM.put("WOODEN_SHOVEL", Item.WOODEN_SHOVEL);
        WEtoM.put("WOODEN_SLAB", Item.WOODEN_SLAB);
        WEtoM.put("WOODEN_SWORD", Item.WOODEN_SWORD);
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
        signText = new ArrayList<String>();
        List<String> text = new ArrayList<String>();
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
//                                                lineText += TextFormat.valueOf(value.toUpperCase());
                                            } catch (Exception noColor) {
                                                Utils.ConsoleMsg("Unknown color " + value + " in sign when pasting schematic, skipping...");
                                            }
                                        } else if (key.equalsIgnoreCase("text")) {
                                            lineText += value;
                                        } else // Formatting - usually the value is always true, but check just in case
                                        {
                                            if (key.equalsIgnoreCase("obfuscated") && value.equalsIgnoreCase("true")) {
                                                lineText += TextFormat.OBFUSCATED;
                                            } else if (key.equalsIgnoreCase("underlined") && value.equalsIgnoreCase("true")) {
                                                lineText += TextFormat.UNDERLINE;
                                            } else {
                                                // The rest of the formats
                                                try {
//                                                lineText += TextFormat.valueOf(key.toUpperCase());
                                                } catch (Exception noFormat) {
                                                    // Ignore
                                                    //System.out.println("DEBUG3:" + key + "=>" + value);
                                                    Utils.ConsoleMsg("Unknown format " + value + " in sign when pasting schematic, skipping...");
                                                }
                                            }
                                        }
                                    }
                                } else // This is unformatted text. It is included in "". A reset is required to clear
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
                {
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
                for (Tag item : chestItems.getValue()) {
                    // Format for chest items is:
                    // id = short value of item id
                    // Damage = short value of item damage
                    // Count = the number of items
                    // Slot = the slot in the chest
                    // inventory

                    if (item instanceof CompoundTag) {
                        try {
                            // Id is a number
                            short itemType = (Short) ((CompoundTag) item).getValue().get("id").getValue();
                            short itemDamage = (short) ((CompoundTag) item).getValue().get("Damage").getValue();
                            byte itemAmount = (Byte) ((CompoundTag) item).getValue().get("Count").getValue();
                            byte itemSlot = (byte) ((CompoundTag) item).getValue().get("Slot").getValue();
                            Item chestItem = new Item(itemType, (int) itemDamage, itemAmount);
                            chestContents.put((int) itemSlot, chestItem);
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
                                    if (WEtoM.containsKey(material)) {
                                        itemMaterial = WEtoM.get(material);
                                    } else {
                                        itemMaterial = Item.fromString(material).getId();
                                    }
                                    byte itemAmount = (byte) ((CompoundTag) item).getValue().get("Count").getValue();
                                    short itemDamage = (short) ((CompoundTag) item).getValue().get("Damage").getValue();
                                    byte itemSlot = (byte) ((CompoundTag) item).getValue().get("Slot").getValue();
                                    Item chestItem = new Item(itemMaterial, (int) itemDamage, itemAmount);
                                    chestContents.put((int) itemSlot, chestItem);
                                }
                            } catch (Exception exx) {
                                Utils.ConsoleMsg("Could not parse item [" + itemType.substring(10).toUpperCase() + "] in schematic");
                                exx.printStackTrace();
                            }

                        }
                    }
                }
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
        // Only paste air if it is below the sea level and in the overworld
        Block block = new Location(x, y, z, 0, 0, blockLoc.getLevel()).add(blockLoc).getLevelBlock();
        // found the problem why blocks didnt shows up
        blockLoc.getLevel().setBlock(block, Block.get(typeId, data), usePhysics, true);
        if (signText != null) {
            cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                    .putString("id", BlockEntity.SIGN)
                    .putInt("x", (int) block.x)
                    .putInt("y", (int) block.y)
                    .putInt("z", (int) block.z)
                    .putString("Text1", signText.get(0))
                    .putString("Text2", signText.get(1))
                    .putString("Text3", signText.get(2))
                    .putString("Text4", signText.get(3));
            BlockEntity.createBlockEntity(BlockEntity.SIGN, blockLoc.getLevel().getChunk((int) block.x >> 4, (int) block.z >> 4), nbt);
            new BlockEntitySign(blockLoc.getLevel().getChunk((int) block.x >> 4, (int) block.z >> 4), nbt);
        } else if (pot != null) {
            pot.set(blockLoc, block);
        } else if (!chestContents.isEmpty()) {
            cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                    .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                    .putString("id", BlockEntity.CHEST)
                    .putInt("x", x)
                    .putInt("y", y)
                    .putInt("z", z);
            BlockEntity.createBlockEntity(BlockEntity.CHEST, blockLoc.getLevel().getChunk(x >> 4, z >> 4), nbt);
            BlockEntityChest e = new BlockEntityChest(blockLoc.getLevel().getChunk(x >> 4, z >> 4), nbt);
            e.getInventory().setContents(chestContents);
        }
    }

    /**
     * @return Vector for where this block is in the schematic
     */
    public Vector3 getVector() {
        return new Vector3(x, y, z);
    }
}
