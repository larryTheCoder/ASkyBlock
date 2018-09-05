/*
 * Copyright (C) 2016-2018 Adam Matthew
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

package com.larryTheCoder.storage;

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
    COLLECT_LAVA("Collect lava", 11),
    /**
     * Can collect water
     * [Added]
     */
    COLLECT_WATER("Collect water", 12),
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
     * Can hurt friendly mobs, e.g. cows
     */
    HURT_MOBS("Hurt mobs", 27),
    /**
     * Can hurt monsters
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
     * Can do PVP in the overworld
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
     * Visitors can pick up items
     */
    VISITOR_ITEM_PICKUP("Visitor pickup item", 40);

    private int id;
    private String name;

    /**
     * Create a value for the enum
     *
     * @param name The name for the enum
     * @param id The id for the enum
     */
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

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }
}
