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

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import java.util.HashMap;
import java.util.Map;
import org.jnbt.IntTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

/**
 * This class describes pots and is used in schematic importing
 *
 * @author SpyL1nk
 *
 */
public class PotBlock {

    private Block potItem;
    private int potItemData;

    private static final HashMap<String, Integer> potItemList;

    static {
        potItemList = new HashMap<>();
        potItemList.put("", Item.AIR);
        potItemList.put("minecraft:red_flower", Item.ROSE);
        potItemList.put("minecraft:yellow_flower", Item.FLOWER);
        potItemList.put("minecraft:sapling", Item.SAPLING);
        potItemList.put("minecraft:red_mushroom", Item.RED_MUSHROOM);
        potItemList.put("minecraft:brown_mushroom", Item.BROWN_MUSHROOM);
        potItemList.put("minecraft:cactus", Item.CACTUS);
        potItemList.put("minecraft:deadbush", Item.DEAD_BUSH);
        potItemList.put("minecraft:tallgrass", Item.TALL_GRASS);
    }

    public boolean set(Position pos, Block block) {
        if (potItem != Block.get(Item.AIR)) {
            pos.getLevel().setBlock(block, Block.get(potItem.getId(), potItemData));
        }
        return true;
    }

    public boolean prep(Map<String, Tag> tileData) {
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
                    if (!potItemList.containsValue(id)) {
                        // No, so reset to AIR
                        potItem = Block.get(Item.AIR);
                    }
                } else if (tileData.get("Item") instanceof StringTag) {
                    // Item is a material
                    String itemName = ((StringTag) tileData.get("Item")).getValue();
                    if (potItemList.containsKey(itemName)) {
                        // Check it's a viable pot item
                        if (potItemList.containsKey(itemName)) {
                            potItem = Block.get(potItemList.get(itemName));
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
        return true;
    }
}
