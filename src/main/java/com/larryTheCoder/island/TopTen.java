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

package com.larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TopTen implements Listener {

    // Top ten list of players
    private static Map<String, Integer> topTenList = new HashMap<>();

    /**
     * Adds a player to the top ten, if the level is good enough
     *
     * @param player The player entry
     * @param level  Level that need to be bind
     */
    public static void topTenAddEntry(String player, int level) {
        // Special case for removals. If a level of zero is given the player
        // needs to be removed from the list
        if (level < 1) {
            topTenList.remove(player);
            return;
        }
        Player p = Server.getInstance().getPlayer(player);
        if (p != null && !p.hasPermission("is.topten")) {
            topTenList.remove(player);
            return;
        }

        topTenList.put(player, level);
        topTenList = Utils.sortByValue(topTenList);
    }

    /**
     * Removes the player from the top ten
     * entry list.
     *
     * @param player The player itself
     */
    public static void topTenRemoveEntry(String player) {
        topTenList.remove(player);
    }

    /**
     * Generates a sorted map of islands for the
     * TopTen list from all player files
     */
    public static void topTenCreate() {
        topTenCreate(null);
    }

    /**
     * Creates the top ten list from scratch. Does not get the level of each island. Just
     * takes the level from the player's file.
     * Runs asynchronously from the main thread.
     *
     * @param sender The command sender, for whoever executed this command
     */
    public static void topTenCreate(CommandSender sender) {
        TaskManager.runTaskAsync(() -> {
            // TODO: Top 10 islands that is good.
        });
    }

    public static void topTenSave() {
        if (topTenList == null) {
            return;
        }
        Utils.send("&7Saving top ten list");
        // Make file
        Config config = new Config(new File(ASkyBlock.get().getDataFolder(), "topten.yml"), Config.YAML);
        // Save config

        int rank = 0;
        for (Map.Entry<String, Integer> m : topTenList.entrySet()) {
            if (rank++ == 10) {
                break;
            }
            config.set("topten." + m.getKey(), m.getValue());
        }
        try {
            config.save();
            Utils.send("&7Saved top ten list");
        } catch (Exception e) {
            Utils.send("&cCould not save top ten list!");
            e.printStackTrace();
        }
    }

    /**
     * Loads the top ten from the file system topten.yml. If it does not exist
     * then the top ten is created
     */
    public static void topTenLoad() {
        topTenList.clear();
        // Check to see if the top ten list exists
        File topTenFile = new File(ASkyBlock.get().getDataFolder(), "topten.yml");
        if (!topTenFile.exists()) {
            Utils.send("&eTop ten file does not exist - creating it. This could take some time with a large number of players");
            topTenCreate();
        } else {
            // Load the top ten
            Config topTenConfig = new Config(new File(ASkyBlock.get().getDataFolder(), "topten.yml"), Config.YAML);
            // Check if there is a section in config.
            if (!topTenConfig.isSection("topten")) {
                Utils.send("&cProblem loading top ten list - section is invalid");
                Utils.send("&cCreating a new top ten list - may take some time");
                topTenCreate();
                return;
            }
            // Get the list of topten
            for (String player : topTenConfig.getSection("topten").getKeys(false)) {
                try {
                    int level = topTenConfig.getInt("topten." + player);
                    TopTen.topTenAddEntry(player, level);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.send("&cProblem loading top ten list - recreating - this may take some time");
                    topTenCreate();
                }
            }
        }
        Utils.send("&7Loaded all top ranked islands - TopTen");
    }

    /**
     * Displays the Top Ten list if it exists in chat
     *
     * @param player - the requesting player
     */
    public static void topTenShow(CommandSender player) {
        // Old chat display
        player.sendMessage(TextFormat.GOLD + "These are the Top 10 islands:");
        if (topTenList == null) {
            topTenCreate();
        }
        topTenList = Utils.sortByValue(topTenList);
        int i = 1;

        Iterator<Map.Entry<String, Integer>> it = topTenList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> m = it.next();
            String playerString = m.getKey();
            // Remove from TopTen if the player is online and has the permission
            Player entry = Server.getInstance().getPlayer(playerString);
            boolean show = true;
            if (entry != null) {
                if (!entry.hasPermission("is.topten")) {
                    it.remove();
                    show = false;
                }
            }
            if (show) {
                // Island name + Island level
                player.sendMessage(TextFormat.AQUA + "#" + i + ": " + playerString + TextFormat.AQUA + " - Island level: " + m.getValue());

                if (i++ == 10) {
                    break;
                }
            }
        }
    }

    static void remove(String owner) {
        topTenList.remove(owner);
    }

    /**
     * Get a sorted descending map of the top players
     *
     * @return the topTenList - may be more or less than ten
     */
    public static Map<String, Integer> getTopTenList() {
        return topTenList;
    }
}

