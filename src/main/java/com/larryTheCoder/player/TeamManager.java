/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
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
package com.larryTheCoder.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is to handle player's teammate
 * Stored in yml class, easier to handle with
 * automated-save-async-task.
 *
 * @author larryTheCoder
 */
public class TeamManager {

    private final ASkyBlock plugin;
    private volatile Map<String, CoopData> dataList;

    public TeamManager(ASkyBlock plugin) {
        this.plugin = plugin;
        this.dataList = getCoopData();
        TaskManager.runTaskRepeatAsync(() -> {
            Config cfg = new Config(new File(Utils.DIRECTORY + "coop.yml"), Config.YAML);
            for (Map.Entry<String, CoopData> data : dataList.entrySet()) {
                String leader = data.getKey().toLowerCase();
                CoopData pd = data.getValue();

                cfg.set(leader + ".teamName", pd.getTeamName());
                cfg.set(leader + ".members", Utils.arrayToString(pd.getMembers()));
            }

            cfg.save();
        }, 40); // Update config every 2 seconds
    }

    /**
     * loads the coop data from a yml file
     */
    private Map<String, CoopData> getCoopData() {
        // More easier, no sql.
        Map<String, CoopData> depthData = new HashMap<>();
        Config cfg = new Config(new File(Utils.DIRECTORY + "coop.yml"), Config.YAML);
        for (String leader : cfg.getKeys(false)) {
            ConfigSection section = cfg.getSection(leader);
            CoopData data = new CoopData(leader,
                    section.getString("teamName"),
                    Utils.stringToArray(section.getString("members"), ":"));
            depthData.put(leader, data);
        }
        return depthData;
    }

    public void saveData() {
        Utils.send("&7Storing coop data, this may take a while...");

        Config cfg = new Config(new File(Utils.DIRECTORY + "coop.yml"), Config.YAML);
        for (Map.Entry<String, CoopData> data : dataList.entrySet()) {
            String leader = data.getKey().toLowerCase();
            CoopData pd = data.getValue();

            cfg.set(leader + ".teamName", pd.getTeamName());
            cfg.set(leader + ".members", Utils.arrayToString(pd.getMembers()));
        }

        cfg.save();
    }

    /**
     * This checks either the player has a team
     * or not.
     *
     * @param player The current player
     * @return {@code true} if not null
     */
    public boolean hasTeam(String player) {
        return getPlayerCoop(player) != null;
    }

    /**
     * Get the player coop data. This checks
     * if the player is a member of one of these
     * coop leaders.
     *
     * @param player The player itself
     * @return The team of the player in, or null if not available.
     */
    public CoopData getPlayerCoop(String player) {
        return dataList.entrySet().stream()
                .filter(data -> data.getValue().isMember(player) || data.getValue().getLeaderName().equalsIgnoreCase(player))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * Returns the coop data of the player.
     * null if not available yet.
     *
     * @param player The player itself
     * @return CoopData for the player, null if not available.
     */
    public CoopData getLeaderCoop(String player) {
        return dataList.entrySet().stream()
                .filter(data -> data.getValue().getLeaderName().equalsIgnoreCase(player))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * Set the player into a team.
     * Warning: If the player already in a team,
     * the player will automatically removed from their old team
     * and transferred to a new one without a prior notice.
     *
     * @param leader The leader itself
     * @param member The member to be set.
     */
    public void setTeam(String leader, String member) {
        // Check if the player in team.
        CoopData data = getPlayerCoop(member);
        if (data != null) {
            data.removeMembers(member);
        }

        // Get the leader of the team, and put it in it.
        data = getLeaderCoop(leader);
        if (data != null) {
            data.addMembers(member);
        }
    }

    /**
     * Creates a new team for a leader.
     * Return false if the leader already have
     * a team.
     *
     * @param leader The leader itself
     * @return true if creation was successful.
     */
    public boolean createTeam(String leader) {
        // Check if the player has a team.
        if (hasTeam(leader)) {
            return false;
        }
        CoopData data = new CoopData(leader, "", new ArrayList<>());
        dataList.put(leader.toLowerCase(), data);

        return true;
    }

    void storeCoopData(CoopData pd) {
        dataList.put(pd.getLeaderName().toLowerCase(), pd);
    }

    public void kickMember(Player leader, String member, String message) {
        String kickMessage = TextFormat.RED + plugin.getLocale(member).kickedFromTeam.replace("[name]", leader.getName());
        if (!message.isEmpty()) {
            kickMessage = message;
        }
        CoopData pd = getLeaderCoop(leader.getName());
        if (!pd.isMember(member)) {
            leader.sendMessage(plugin.getPrefix() + plugin.getLocale(leader).errorUnknownPlayer);
            return;
        }
        pd.removeMembers(member);

        Player p = Server.getInstance().getPlayer(member);
        if (p.isOnline()) {
            p.sendMessage(plugin.getPrefix() + kickMessage);
        } else {
            plugin.getMessages().setMessage(member, message);
        }
    }
}
