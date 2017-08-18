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
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntityFlowerPot;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
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
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import static com.larryTheCoder.utils.Utils.*;

import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
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
    // Chest contents
    private final HashMap<Integer, Item> chestContents;
    public static final HashMap<String, Integer> WETOME = new HashMap<>();
    // Pot items
    private Block potItem;
    private int potItemData;

    private static final HashMap<String, Integer> POT_ITEM_LISTS;

    static {
        POT_ITEM_LISTS = new HashMap<>();
        POT_ITEM_LISTS.put("", Item.AIR);
        POT_ITEM_LISTS.put("minecraft:red_flower", Item.ROSE);
        POT_ITEM_LISTS.put("minecraft:yellow_flower", Item.FLOWER);
        POT_ITEM_LISTS.put("minecraft:sapling", Item.SAPLING);
        POT_ITEM_LISTS.put("minecraft:red_mushroom", Item.RED_MUSHROOM);
        POT_ITEM_LISTS.put("minecraft:brown_mushroom", Item.BROWN_MUSHROOM);
        POT_ITEM_LISTS.put("minecraft:cactus", Item.CACTUS);
        POT_ITEM_LISTS.put("minecraft:deadbush", Item.DEAD_BUSH);
        POT_ITEM_LISTS.put("minecraft:tallgrass", Item.TALL_GRASS);
    }

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
     * @param player
     * @param usePhysics
     * @param blockLoc
     */
    public void paste(Player p, Location blockLoc, boolean usePhysics) {
        Location loc = new Location(x, y, z, 0, 0, blockLoc.getLevel()).add(blockLoc);
        loadChunkAt(loc);
        blockLoc.getLevel().setBlock(loc, Block.get(typeId, data), true, usePhysics);

        // BlockEntities
        if (signText != null) {
            scheduleTextPlacement(p, loc);
        } else if (potItem != null) {
            schedulePotPlacement(p, loc);
        } else if (Block.get(typeId, data).getId() == Block.CHEST) {
            scheduleChestPlacement(p, loc);
        }

    }

    /**
     * @return Vector for where this block is in the schematic
     */
    public Vector3 getVector() {
        return new Vector3(x, y, z);
    }

    // --- Task Scheduling --- // 
    private void scheduleChestPlacement(Player p, Location loc) {
        new NukkitRunnable() {
            @Override
            public void run() {
                if (!p.chunk.isGenerated()
                        && !p.chunk.getProvider().getLevel().getName().equalsIgnoreCase(loc.level.getName())
                        && !p.chunk.isLoaded()
                        && loc.level.isChunkGenerated(loc.getFloorX(), loc.getFloorZ())) {
                    TaskManager.runTaskLater(this, 20); // It will not be a problem if the player use /is create
                    return;
                }
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putList(new cn.nukkit.nbt.tag.ListTag<>("Items"))
                        .putString("id", BlockEntity.CHEST)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z);
                BlockEntityChest e = (BlockEntityChest) BlockEntity.createBlockEntity(
                        BlockEntity.CHEST,
                        p.chunk,
                        nbt);
                loc.level.addBlockEntity(e);
                if (Settings.chestInventoryOverride || chestContents.isEmpty()) {
                    int count = 0;
                    for (Item item : Settings.chestItems) {
                        e.getInventory().setItem(count, item);
                        count++;
                    }
                } else {
                    e.getInventory().setContents(chestContents);
                }
                e.spawnToAll();
            }
        }.runTaskLater(ASkyBlock.get(), Utils.secondsAsMillis(TeleportLogic.teleportDelay + 1));
    }

    private void schedulePotPlacement(Player p, Location loc) {
        new NukkitRunnable() {
            @Override
            public void run() {
                if (!p.chunk.isGenerated()
                        && !p.chunk.getProvider().getLevel().getName().equalsIgnoreCase(loc.level.getName())
                        && !p.chunk.isLoaded()
                        && loc.level.isChunkGenerated(loc.getFloorX(), loc.getFloorZ())) {
                    TaskManager.runTaskLater(this, 20); // It will not be a problem if the player use /is create
                    return;
                }
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putString("id", BlockEntity.FLOWER_POT)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z)
                        .putShort("item", potItem.getId())
                        .putInt("data", potItemData);

                BlockEntityFlowerPot potBlock = (BlockEntityFlowerPot) BlockEntity.createBlockEntity(
                        BlockEntity.FLOWER_POT,
                        p.chunk,
                        nbt);

                loc.level.addBlockEntity(potBlock);
                potBlock.spawnToAll();
            }
        }.runTaskLater(ASkyBlock.get(), Utils.secondsAsMillis(TeleportLogic.teleportDelay + 1));

    }

    private void scheduleTextPlacement(Player p, Location loc) {
        new NukkitRunnable() {
            @Override
            public void run() {
                if (!p.chunk.isGenerated()
                        && !p.chunk.getProvider().getLevel().getName().equalsIgnoreCase(loc.level.getName())
                        && !p.chunk.isLoaded()
                        && loc.level.isChunkGenerated(loc.getFloorX(), loc.getFloorZ())) {
                    TaskManager.runTaskLater(this, 20); // It will not be a problem if the player use /is create
                    return;
                }
                cn.nukkit.nbt.tag.CompoundTag nbt = new cn.nukkit.nbt.tag.CompoundTag()
                        .putString("id", BlockEntity.SIGN)
                        .putInt("x", (int) loc.x)
                        .putInt("y", (int) loc.y)
                        .putInt("z", (int) loc.z)
                        .putString("Text1", signText.get(0).replace("[player]", p.getName()))
                        .putString("Text2", signText.get(1).replace("[player]", p.getName()))
                        .putString("Text3", signText.get(2).replace("[player]", p.getName()))
                        .putString("Text4", signText.get(3).replace("[player]", p.getName()));
                BlockEntitySign sign = (BlockEntitySign) BlockEntity.createBlockEntity(
                        BlockEntity.SIGN,
                        p.chunk,
                        nbt);
                loc.level.addBlockEntity(sign);
                sign.spawnToAll();
            }
        }.runTaskLater(ASkyBlock.get(), Utils.secondsAsMillis(TeleportLogic.teleportDelay + 1));
    }

}
