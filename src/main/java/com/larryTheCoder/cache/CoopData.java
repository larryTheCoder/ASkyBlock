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

package com.larryTheCoder.cache;

import cn.nukkit.Player;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.DatabaseManager;
import com.larryTheCoder.database.TableSet;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;
import org.sql2o.Connection;
import org.sql2o.data.Row;

import java.util.List;

/**
 * Stores coop data.
 */
public class CoopData {

    @Getter
    private final String islandUniqueId;

    @Getter
    private String leaderName;
    @Getter
    private String teamName;
    @Getter
    private List<String> members;
    @Getter
    private List<String> admins;

    public CoopData(Row coopData) {
        this.islandUniqueId = coopData.getString("defaultIsland");
        this.leaderName = coopData.getString("islandLeader");
        this.teamName = coopData.getString("islandLeader");
        this.members = Utils.stringToArray(coopData.getString("islandMembers"), ", ");
        this.admins = Utils.stringToArray(coopData.getString("islandAdmins"), ", ");
    }

    /**
     * Sets this island leader name, this overrides
     * the original island owner from island data.
     *
     * @param playerName The player name.
     */
    public void setLeaderName(String playerName) {
        this.leaderName = playerName;

        updateData();
    }

    /**
     * Sets the name for this coop island.
     * Self-explanatory.
     *
     * @param teamName The name of the team.
     */
    public void setTeamName(String teamName) {
        this.teamName = teamName;

        updateData();
    }

    /**
     * Check either this player is an admin in this island.
     *
     * @param pl The player class
     * @return true if the player is an admin, false if otherwise.
     */
    public boolean isAdmin(Player pl) {
        return admins.stream().anyMatch(o -> o.equalsIgnoreCase(pl.getName()));
    }

    /**
     * Check if the player with this name is a member of this
     * island relations.
     *
     * @param plName The player name itself.
     * @return {@code true} if the player is the member of this island
     */
    public boolean isMember(String plName) {
        return members.stream().anyMatch(i -> i.equalsIgnoreCase(plName)) || plName.equalsIgnoreCase(leaderName);
    }

    /**
     * Adds a player name into this coop list, the new member will
     * always be related to this island.
     *
     * @param plName self-explanatory
     */
    public void addMember(String plName) {
        this.members.add(plName);

        updateData();
    }

    /**
     * Removes a player from this coop list.
     *
     * @param member self-explanatory
     */
    public void removeMember(String member) {
        this.members.remove(member);

        updateData();
    }

    private void updateData() {
        ASkyBlock.get().getDatabase().pushQuery(new DatabaseManager.DatabaseImpl() {
            @Override
            public void executeQuery(Connection connection) {
                connection.createQuery(TableSet.ISLAND_UPDATE_RELATIONS.getQuery())
                        .addParameter("islandUniqueId", islandUniqueId)
                        .addParameter("leaderName", leaderName)
                        .addParameter("teamName", teamName)
                        .addParameter("admins", Utils.arrayToString(admins))
                        .addParameter("members", Utils.arrayToString(members))
                        .executeUpdate();
            }
        });
    }

    @Override
    public int hashCode() {
        int i = 60;
        i += leaderName.hashCode();
        i += teamName.hashCode() / 32;

        return i + super.hashCode();
    }

    public boolean isAdmin(Player p) {
        return admins.stream().anyMatch(i -> i.equalsIgnoreCase(p.getName()));
    }
}
