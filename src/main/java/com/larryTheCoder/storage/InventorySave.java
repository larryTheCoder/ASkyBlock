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

import cn.nukkit.Player;
import cn.nukkit.item.Item;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Adam Matthew
 */
public class InventorySave {

    private static InventorySave object;
    private final HashMap<UUID, InventoryStore> inventories;

    public InventorySave() {
        inventories = new HashMap<>();
        object = this;
    }

    public static InventorySave getInstance() {
        return object;
    }

    /**
     * Save player's inventory
     *
     * @param player The player to be saved
     */
    public void savePlayerInventory(Player player) {
        // Save the player's armor and things
        inventories.put(player.getUniqueId(), new InventoryStore(player.getInventory().getContents(), player.getInventory().getArmorContents()));
    }

    /**
     * Clears any saved inventory
     *
     * @param player The player to be cleared
     */
    public void clearSavedInventory(Player player) {
        inventories.remove(player.getUniqueId());
    }

    /**
     * Clears all data in the inventory list
     */
    public void clearSavedInventory() {
        inventories.clear();
    }
    /**
     * Load the player's inventory
     *
     * @param player The player to be loaded
     */
    public void loadPlayerInventory(Player player) {
        // Get the info for this player
        if (inventories.containsKey(player.getUniqueId())) {
            InventoryStore inv = inventories.get(player.getUniqueId());
            player.getInventory().setContents(inv.getInventory());
            for (Item[] ec : inv.getArmor()) {
                player.getInventory().setArmorContents(ec);
            }
            clearSavedInventory(player);
        }
    }

}
