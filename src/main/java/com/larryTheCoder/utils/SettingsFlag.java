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
package com.larryTheCoder.utils;

import lombok.Getter;

/**
 * @author tastybento
 */
public enum SettingsFlag {
    /**
     * Water is acid above sea level
     */
    ACID_DAMAGE("Acid Damage", 1),
    /**
     * Anvil use
     * [Added]
     */
    ANVIL("Anvil", 2),
    /**
     * Armor stand use
     * [Not yet implemented]
     */
    ARMOR_STAND("Armor Stand", 3),
    /**
     * Beacon use
     * [Not yet implemented]
     */
    BEACON("Beacon", 4),
    /**
     * Bed use
     * [Added]
     */
    BED("Bed", 5),
    /**
     * Can break blocks
     * [Added]
     */
    BREAK_BLOCKS("Break blocks", 6),
    /**
     * Can place blocks
     * [Added]
     */
    PLACE_BLOCKS("Place blocks", 7),
    /**
     * Can breed animals
     * [No option to use]
     */
    BREEDING("Breeding", 8),
    /**
     * Can use brewing stand
     * [Added]
     */
    BREWING("Brewing", 9),
    /**
     * Can empty or fill buckets
     * [Added]
     */
    BUCKET("Fill/Empty buckets", 10),
    /**
     * Can collect lava
     * [Added]
     */
    COLLECT_LAVA("Collect/Fill lava", 11),
    /**
     * Can collect water
     * [Added]
     */
    COLLECT_WATER("Collect/Fill water", 12),
    /**
     * Can open chests or hoppers or dispensers
     * [Added]
     */
    CHEST("Chest", 13),
    /**
     * Can use the work bench
     * [Added]
     */
    CRAFTING("Crafting", 14),
    /**
     * Allow creepers to hurt players (but not damage blocks)
     */
    CREEPER_PAIN("Creeper damage", 15),
    /**
     * Can trample crops
     */
    CROP_TRAMPLE("Trample crops", 16),
    /**
     * Can open doors or trapdoors
     * [Added]
     */
    DOOR("Open doors", 17),
    /**
     * Chicken eggs can be thrown
     * [Added]
     */
    EGGS("Trow eggs", 18),
    /**
     * Can use the enchanting table
     * [Added]
     */
    ENCHANTING("Enchanting", 19),
    /**
     * Can throw ender pearls
     */
    ENDER_PEARL("Ender pearls", 20),
    /**
     * Can toggle enter/exit names to island
     * [Added]
     */
    ENTER_EXIT_MESSAGES("Enter exit message", 21),
    /**
     * Fire use/placement in general
     * [Added]
     */
    FIRE("Fire", 22),
    /**
     * Can extinguish fires by punching them
     * [Added]
     */
    FIRE_EXTINGUISH("Fire extinguish", 23),
    /**
     * Allow fire spread
     * [No way to use]
     */
    FIRE_SPREAD("Spread fire", 24),
    /**
     * Can use furnaces
     * [Added]
     */
    FURNACE("Furnace", 25),
    /**
     * Can use gates
     * [Added]
     */
    GATE("Use gates", 26),
    /**
     * Can hurt friendly mobs, e.g. cows.
     * [Added]
     */
    HURT_MOBS("Hurt mobs", 27),
    /**
     * Can hurt monsters.
     * [Added]
     */
    HURT_MONSTERS("Hurt monsters", 28),
    /**
     * Can leash or unleash animals
     */
    LEASH("Leash animals", 29),
    /**
     * Can use buttons or levers
     * [Added]
     */
    LEVER_BUTTON("Use levers", 30),
    /**
     * Animals, etc. can spawn
     */
    MILKING("Milking animals", 31),
    /**
     * Monsters can spawn
     */
    MONSTER_SPAWN("Spawn monsters", 32),
    /**
     * Can operate jukeboxes, note boxes etc.
     * [Added]
     */
    MUSIC("Allow music", 33),
    /**
     * Will activate pressure plates
     * [Added]
     */
    PRESSURE_PLATE("Activate pressure plates", 34),
    /**
     * Allows PvP inside the island compound
     * [Added]
     */
    PVP("Allow PVP", 35),
    /**
     * Spawn eggs can be used
     * [Added]
     */
    SPAWN_EGGS("Use spawn eggs", 36),
    /**
     * Can shear sheep
     */
    SHEARING("Sheer sheep", 37),
    /**
     * Can trade with villagers
     */
    VILLAGER_TRADING("Trade with villagers", 38),
    /**
     * Visitors can drop items
     * [Added]
     */
    VISITOR_ITEM_DROP("Visitor drop item", 39),
    /**
     * Visitors can pick up items.
     * [Added]
     */
    VISITOR_ITEM_PICKUP("Visitor pickup item", 40);

    @Getter
    private final int id;
    @Getter
    private final String name;

    SettingsFlag(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static SettingsFlag getFlag(int name) {
        for (SettingsFlag flag : SettingsFlag.values()) {
            if (flag.getId() == name) {
                return flag;
            }
        }
        return null;
    }

    public static SettingsFlag getFlag(String name) {
        return getFlag(Integer.parseInt(name));
    }
}
