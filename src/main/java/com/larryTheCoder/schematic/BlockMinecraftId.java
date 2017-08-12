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

import cn.nukkit.item.Item;

import java.util.HashMap;

/**
 * Author: Adam Matthew
 * <p>
 * Represents a block id from bukkit to Nukkit
 * This is to ensure that the block will returns to its real block
 * ex: wood slab returns to detector rail
 */
public class BlockMinecraftId {

    public static final HashMap<String, Integer> WETOME = new HashMap<>();
    public static final HashMap<String, Integer> POT_ITEM_LISTS;

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
        WETOME.put("HARDENED_CLAY", Item.TERRACOTTA);
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
}
