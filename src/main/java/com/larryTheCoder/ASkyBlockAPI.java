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
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.Plugin;
import com.larryTheCoder.database.ASConnection;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.schematic.Schematic;
import com.larryTheCoder.storage.IslandData;

import java.io.File;

/**
 * The public API of the ASkyBlock plugin.
 * <p>
 * This API will not be changed without prior notice - allowing other plugins a
 * way to access data from the ASkyBlock plugin.
 * <p>
 * To get hold of an API object, make sure your plugin depends on ASkyBlock, and
 * then do:
 * <pre>{@code
 *     ASkyBlockAPI api = (ASkyBlockAPI) Server.getInstance().getPluginManager().getPlugin("ASkyBlock");
 *     if (api != null && api.isEnabled()) {
 *         // Access the api here...
 *     } else {
 *         // Complain here
 *     }
 * }</pre>
 *
 * @since v0.2.5
 */
public interface ASkyBlockAPI extends Plugin {

    /**
     * Returns the island level from the last time it was calculated. Note this
     * does not calculate the island level.
     *
     * @param player
     * @return the last level calculated for the island or zero if none.
     * @since 0.3.0
     */
    Integer getIslandLevel(Player player);

    /**
     * Returns the island-information for the player, or <code>null</code> if
     * none exist.
     *
     * @param player The player to query island-data for.
     * @return the island-information for the player, or <code>null</code> if
     * none exist.
     * @since 0.2.5
     */
    IslandData getIslandInfo(Player player);

    /**
     * Returns the island-information for the supplied location, or
     * <code>null</code> if none exist.
     *
     * @param location The location to test for the existence of an island.
     * @return the island-information for the supplied location, or
     * <code>null</code> if none exist.
     * @since 0.2.5
     */
    IslandData getIslandInfo(Location location);

    /**
     * Returns a PlayerInfo object for the player.
     *
     * @param player The player to get a PlayerInfo for.
     * @return a PlayerInfo object for the player.
     * @since 0.2.5
     */
    PlayerData getPlayerInfo(Player player);

    /**
     * Try to register a schematic manually
     *
     * @param schematic File of that target
     * @param name      The name of that file e.g SkyBlock
     * @since 0.1.0
     */
    void registerSchematic(File schematic, String name);

    /**
     * Returns of the Schematic
     *
     * @param key - The key of that file e.g SkyBlock
     * @return Schematic
     * @since 0.1.0
     */
    Schematic getSchematic(String key);

    /**
     * Get the current ASkyBlock version.
     *
     * @return current version in config or null
     * @since 0.1.0
     */
    int[] getVersion();

    /**
     * Gets the Database Connection
     *
     * @return ASConnection
     * @since 0.1.0
     */
    ASConnection getDatabase();

    /**
     * get the plug-in version
     *
     * @return String Version of ASkyBlock
     * @since 0.1.0
     */
    String getPluginVersionString();

    /**
     * Gets the current version
     *
     * @return int[]
     * @since 0.1.0
     */
    int[] getPluginVersion();

    /**
     * Check if `version` is >= `version2`.
     *
     * @param version
     * @param version2
     * @return true if `version` is >= `version2`
     * @since 0.1.0
     */
    boolean checkVersion(int[] version, int... version2);
}
