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
package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.weather.WeatherChangeEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import com.larryTheCoder.ASkyBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Matthew
 */
public class AcidEffect implements Listener {

    private final ASkyBlock plugin;
    private final List<Player> burningPlayers = new ArrayList<>();
    private final List<Player> wetPlayers = new ArrayList<>();
    private boolean isRaining = false;

    public AcidEffect(final ASkyBlock pluginI) {
        plugin = pluginI;
    }

    /**
     * @param p
     * @return A double between 0.0 and 0.80 that reflects how much armor the
     * player has on. The higher the value, the more protection they have.
     */
    static public double getDamageReduced(Player p) {
        Inventory inv = p.getInventory();
        double red = 0.0;
        for (Item item : inv.getContents().values()) {
            switch (item.getId()) {
                case Item.LEATHER_CAP:
                    red += 0.04;
                    break;
                case Item.GOLD_HELMET:
                    red += 0.08;
                    break;
                case Item.CHAIN_HELMET:
                    red += 0.08;
                    break;
                case Item.IRON_HELMET:
                    red += 0.08;
                    break;
                case Item.DIAMOND_HELMET:
                    red += 0.12;
                    break;
                case Item.LEATHER_BOOTS:
                    red += 0.04;
                    break;
                case Item.GOLD_BOOTS:
                    red += 0.04;
                    break;
                case Item.CHAIN_BOOTS:
                    red += 0.04;
                    break;
                case Item.IRON_BOOTS:
                    red += 0.08;
                    break;
                case Item.DIAMOND_BOOTS:
                    red += 0.12;
                    break;
                case Item.LEATHER_PANTS:
                    red += 0.08;
                    break;
                case Item.GOLD_LEGGINGS:
                    red += 0.12;
                    break;
                case Item.CHAIN_LEGGINGS:
                    red += 0.16;
                    break;
                case Item.IRON_LEGGINGS:
                    red += 0.20;
                    break;
                case Item.DIAMOND_LEGGINGS:
                    red += 0.24;
                    break;
                case Item.LEATHER_TUNIC:
                    red += 0.12;
                    break;
                case Item.GOLD_CHESTPLATE:
                    red += 0.20;
                    break;
                case Item.CHAIN_CHESTPLATE:
                    red += 0.20;
                    break;
                case Item.IRON_CHESTPLATE:
                    red += 0.24;
                    break;
                case Item.DIAMOND_CHESTPLATE:
                    red += 0.32;
                    break;
                default:
                    break;
            }
        }
        return red;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        burningPlayers.remove(e.getEntity());
        wetPlayers.remove(e.getEntity());
    }

    /**
     * Tracks weather changes and acid rain
     *
     * @param e
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWeatherChange(final WeatherChangeEvent e) {
        // Check that they are in the ASkyBlock world
        if (!plugin.level.contains(e.getLevel().getName())) {
            return;
        }
        this.isRaining = e.toWeatherState();
    }
}
