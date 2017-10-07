/*
 * Copyright (C) 2017 Adam Matthew
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 *
 * You should have received a copy of the GNU General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import com.larryTheCoder.command.ChallangesCMD;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.listener.ChatHandler;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.player.TeamManager;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.storage.InventorySave;
import com.larryTheCoder.storage.IslandData;

interface ASkyBlockAPI {

    /**
     * Get The ChatHandler instance
     * Since v0.1.5
     *
     * @return ChatHandler
     */
    ChatHandler getChatHandlers();


    /**
     * Get The InvitationHandler instance
     *
     * @return InvitationHandler
     */
    InvitationHandler getInvitationHandler();

    /**
     * Get the island Manager section
     *
     * @return IslandManager
     */
    IslandManager getIsland();

    /**
     * Get the GridManager Manager section
     *
     * @return GridManager
     */
    GridManager getGrid();

    /**
     * Get the GridManager Manager section
     *
     * @return InventorySave
     */
    InventorySave getInventory();

    /**
     * Get the TeamManager section
     *
     * @return BaseEntity
     */
    TeamManager getTManager();

    /**
     * Return of IslandData class for a player
     *
     * @param player The player class parameters
     * @param homeId The island home number
     * @return IslandData|null
     */
    IslandData getIslandInfo(Player player, int homeId);

    /**
     * Return of IslandData class for a player name
     *
     * @param player The player class parameters
     * @return IslandData|null
     */
    IslandData getIslandInfo(String player);

    /**
     * Return of IslandData class for a player name and homeId
     *
     * @param player The player class parameters
     * @param homeId The island home number
     * @return IslandData|null
     */
    IslandData getIslandInfo(String player, int homeId);

    /**
     * Get the default challenges module
     *
     * @return ChallangesCMD
     */
    ChallangesCMD getChallenges();

    /**
     * Retrieve if the player IN the island world
     *
     * @param player The player class parameters
     * @return boolean
     */
    boolean inIslandWorld(Player player);

    /**
     * Gets the player default island generation
     *
     * @param player The player class parameters
     * @return Default world of player
     */
    String getDefaultWorld(Player player);

    /**
     * Gets the player info
     *
     * @param player The player class parameters
     * @return PlayerData|null
     */
    PlayerData getPlayerInfo(Player player);

    /**
     *
     */
    IslandData getIslandInfo(Location location);

    TeleportLogic getTeleportLogic();

    Integer getIslandLevel(Player player);

    IslandData getIslandInfo(Player player);
}
