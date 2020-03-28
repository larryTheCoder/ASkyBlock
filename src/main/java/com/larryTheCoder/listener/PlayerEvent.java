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
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.level.Location;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.DatabaseManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import org.sql2o.Connection;

import java.util.ArrayList;
import java.util.List;

import static com.larryTheCoder.database.TableSet.PLAYER_INSERT_MAIN;

/**
 * Events that associate to player
 * behaviors and etc
 */
@SuppressWarnings("unused")
public class PlayerEvent implements Listener {

    private final ASkyBlock plugin;
    private final List<String> respawn;

    public PlayerEvent(ASkyBlock plugin) {
        this.plugin = plugin;
        this.respawn = new ArrayList<>();
    }

    /**
     * Determines if a location is in the island world or not or in the new
     * nether if it is activated
     *
     * @param loc Location of the entity to be checked
     * @return true if in the island world
     */
    private boolean notInWorld(Location loc) {
        return !ASkyBlock.get().getLevels().contains(loc.getLevel().getName());
    }

    /**
     * Places player back on their island if the setting is true
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!Settings.respawnOnIsland) {
            return;
        }
        if (respawn.contains(p.getName())) {
            respawn.remove(p.getName());
            Location respawnLocation = plugin.getGrid().getSafeHomeLocation(p.getName(), 1);
            if (respawnLocation != null) {
                e.setRespawnPosition(respawnLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (notInWorld(e.getEntity())) {
            return;
        }

        plugin.getFastCache().getIslandData(p.getLocation(), pd -> {
            if (pd == null) {
                return;
            }

            if (Settings.respawnOnIsland) {
                // Add them to the list to be re-spawned on their island
                respawn.add(p.getName());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ex) {
        // load player inventory if exists
        Player p = ex.getPlayer();
        plugin.getInventory().loadPlayerInventory(p);

        plugin.getFastCache().getPlayerData(p, pd -> {
            if (pd != null) {
                return;
            }

            Utils.send(p.getName() + "&a data doesn't exists. Creating new ones");

            plugin.getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
                @Override
                public void executeQuery(Connection connection) {
                    connection.createQuery(PLAYER_INSERT_MAIN.getQuery())
                            .addParameter("playerName", p.getName())
                            .addParameter("playerUUID", p.getLoginChainData().getXUID())
                            .addParameter("locale", p.getLoginChainData().getLanguageCode())
                            .addParameter("resetLeft", Settings.reset)
                            .addParameter("banList", "")
                            .executeUpdate();

                    Utils.sendDebug("Executed.");
                }

                @Override
                public void onCompletion(Exception ex) {
                    if (ex != null) {
                        ex.printStackTrace();
                        return;
                    }

                    List<String> news = plugin.getMessages().getMessages(p.getName());

                    if (news != null && news.isEmpty()) {
                        p.sendMessage(plugin.getLocale(p).newNews.replace("[count]", Integer.toString(news.size())));
                    }
                }
            });
        });


        // Load messages
        List<String> news = plugin.getMessages().getMessages(p.getName());

        if (news != null && news.isEmpty()) {
            p.sendMessage(plugin.getLocale(p).newNews.replace("[count]", Integer.toString(news.size())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommand(PlayerCommandPreprocessEvent ex) {
        // todo: Block player messages.
    }
}
