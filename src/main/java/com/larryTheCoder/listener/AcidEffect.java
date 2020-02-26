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
 * @author tastybento
 */
class AcidEffect implements Listener {

    private final ASkyBlock plugin;
    private final List<Player> burningPlayers = new ArrayList<>();
    private final List<Player> wetPlayers = new ArrayList<>();

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
        if (!plugin.getLevel().contains(e.getLevel().getName())) {
            return;
        }
        boolean isRaining = e.toWeatherState();
    }
}
