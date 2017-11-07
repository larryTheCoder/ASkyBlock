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
import com.larryTheCoder.panels.Panel;
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
    default ChatHandler getChatHandlers() {
        return null;
    }


    /**
     * Get The InvitationHandler instance
     *
     * @return InvitationHandler
     */
    default InvitationHandler getInvitationHandler() {
        return null;
    }

    /**
     * Get the island Manager section
     *
     * @return IslandManager
     */
    default IslandManager getIsland() {
        return null;
    }

    /**
     * Get the GridManager Manager section
     *
     * @return GridManager
     */
    default GridManager getGrid() {
        return null;
    }

    /**
     * Get the GridManager Manager section
     *
     * @return InventorySave
     */
    default InventorySave getInventory() {
        return null;
    }

    /**
     * Get the TeamManager section
     *
     * @return BaseEntity
     */
    default TeamManager getTManager() {
        return null;
    }

    /**
     * Return of IslandData class for a player
     *
     * @param player The player class parameters
     * @param homeId The island home number
     * @return IslandData|null
     */
    default IslandData getIslandInfo(Player player, int homeId) {
        return null;
    }

    /**
     * Return of IslandData class for a player name
     *
     * @param player The player class parameters
     * @return IslandData|null
     */
    default IslandData getIslandInfo(String player) {
        return null;
    }

    /**
     * Return of IslandData class for a player name and homeId
     *
     * @param player The player class parameters
     * @param homeId The island home number
     * @return IslandData|null
     */
    default IslandData getIslandInfo(String player, int homeId) {
        return null;
    }

    /**
     * Get the default challenges module
     *
     * @return ChallangesCMD
     */
    default ChallangesCMD getChallenges() {
        return null;
    }

    /**
     * Retrieve if the player IN the island world
     *
     * @param player The player class parameters
     * @return boolean
     */
    default boolean inIslandWorld(Player player) {
        return false;
    }

    /**
     * Gets the player info
     *
     * @param player The player class parameters
     * @return PlayerData|null
     */
    default PlayerData getPlayerInfo(Player player) {
        return null;
    }

    /**
     * Gets the island data from the location
     *
     * @param location Target of a location
     * @return IslandData|null
     */
    default IslandData getIslandInfo(Location location) {
        return null;
    }

    /**
     * Gets the Player Panel Module
     *
     * @return Panel|null
     */
    default Panel getPanel() {
        return null;
    }

    /**
     * Get the teleportation logic for Player
     *
     * @return TeleportLogic|null
     */
    default TeleportLogic getTeleportLogic() {
        return null;
    }

    /**
     * Get the island level for player
     *
     * @param player The player class parameters
     * @return Integer|null
     */
    default Integer getIslandLevel(Player player) {
        return 0;
    }

    /**
     * Get this island data for player
     *
     * @param player The player class parameters
     * @return
     */
    IslandData getIslandInfo(Player player);
}
