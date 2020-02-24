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
package com.larryTheCoder.storage;

import cn.nukkit.Player;
import cn.nukkit.item.Item;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author larryTheCoder
 * @author tastybento
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
