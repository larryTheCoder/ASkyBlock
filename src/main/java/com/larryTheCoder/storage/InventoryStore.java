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

package com.larryTheCoder.storage;

import cn.nukkit.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Matthew
 */
class InventoryStore {
    private ArrayList<Item[]> armor = new ArrayList<>();
    private Map<Integer, Item> inventory = new HashMap<>();

    public InventoryStore(Map<Integer, Item> contents, Item[] armorContents) {
        this.inventory = contents;
        this.armor.add(armorContents);
    }

    public Map<Integer, Item> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    /**
     * @param inventory the inventory to set
     */
    public void setInventory(Map<Integer, Item> inventory) {
        inventory.entrySet().stream().forEach((ev) -> {
            this.inventory.put(ev.getKey(), ev.getValue());
        });
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public ArrayList<Item[]> getArmor() {
        return armor;
    }

    /**
     * @param armor the armor to set
     */
    public void setArmor(Item[] armor) {
        this.armor.add(armor);
    }

}
