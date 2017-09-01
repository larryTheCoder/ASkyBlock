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

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import com.larryTheCoder.ASkyBlock;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Adam Matthew
 */
public class InventorySave {
    private static final InventorySave object = new InventorySave(ASkyBlock.get());
    private final HashMap<UUID, InventoryStore> inventories;

    /**
     * Saves the inventory of a player
     *
     * @param plugin
     */
    public InventorySave(ASkyBlock plugin) {
        inventories = new HashMap<>();
    }

    public static InventorySave getInstance() {
        return object;
    }

    /**
     * Save player's inventory
     *
     * @param player
     */
    public void savePlayerInventory(Player player) {
        //plugin.getLogger().info("DEBUG: Saving inventory");
        // Save the player's armor and things
        inventories.put(player.getUniqueId(), new InventoryStore(player.getInventory().getContents(), player.getInventory().getArmorContents()));
    }

    /**
     * Clears any saved inventory
     *
     * @param player
     */
    public void clearSavedInventory(Player player) {
        //plugin.getLogger().info("DEBUG: Clearing inventory");
        inventories.remove(player.getUniqueId());
    }

    /**
     * Load the player's inventory
     *
     * @param player
     */
    public void loadPlayerInventory(Player player) {
        //plugin.getLogger().info("DEBUG: Loading inventory");
        // Get the info for this player
        if (inventories.containsKey(player.getUniqueId())) {
            InventoryStore inv = inventories.get(player.getUniqueId());
            //plugin.getLogger().info("DEBUG: player is known");
            player.getInventory().setContents(inv.getInventory());
            for (Item[] ec : inv.getArmor()) {
                player.getInventory().setArmorContents(ec);
            }
            inventories.remove(player.getUniqueId());
        }
    }

}
