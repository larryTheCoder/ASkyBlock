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
package com.larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.events.IslandCreateEvent;
import com.larryTheCoder.player.CoopData;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.task.DeleteIslandTask;
import com.larryTheCoder.task.SimpleFancyTitle;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.List;

/**
 * Core management for SkyBlock world and
 * execution.
 *
 * @author larryTheCoder
 */
public class IslandManager {

    private final ASkyBlock plugin;

    public IslandManager(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    public void handleIslandCommand(Player p, boolean reset) {
        if (!reset) {
            if (!checkIsland(p)) {
                plugin.getPanel().addIslandFormOverlay(p);
                return;
            }
            IslandData pd = plugin.getDatabase().getIsland(p.getName(), 1);
            if (pd == null || pd.getOwner() == null) {
                p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorFailedCritical);
                return;
            }
            p.sendMessage(plugin.getLocale(p).hangInThere);
            // teleport to grid
            plugin.getGrid().homeTeleport(p);
            TopTen.topTenAddEntry(p.getName(), 0);
        } else {
            createIsland(p);
        }
    }

    void showFancyTitle(Player p) {
        // The problem solved. The task `while` are pushing the CPU far more to load makes it
        // Glitching and corrupted half chunk data. #20 Cannot teleport to island
        TaskManager.runTaskLater(new SimpleFancyTitle(plugin, p), 20);
    }

    public void kickPlayerByName(final Player pOwner, final String victimName) {
        final Location loc = pOwner.getLocation();
        final IslandData pd = getIslandAt(loc);
        if (pd == null || pd.getOwner() == null || !pd.getOwner().equals(pOwner.getName())) {
            pOwner.sendMessage(plugin.getPrefix() + plugin.getLocale(pOwner).errorNotOnIsland);
            return;
        }
        final int orgKey = generateIslandKey(loc);
        final Player pVictim = Server.getInstance().getPlayer(victimName);
        if (pVictim == null || !pVictim.isOnline()) {
            pOwner.sendMessage(plugin.getPrefix() + plugin.getLocale(pOwner).errorOfflinePlayer);
            return;
        }
        if (!(pOwner.isOp())) {
            if (pVictim.isOp()) {
                pOwner.sendMessage(plugin.getPrefix() + plugin.getLocale(pOwner).errorAdminOnly);
                return;
            }
        }
        if (victimName.equalsIgnoreCase(pOwner.getName())) {
            pOwner.sendMessage(plugin.getPrefix() + plugin.getLocale(pOwner).errorKickOwner);
            return;
        }
        final Location locVict = pVictim.getLocation();
        final int tgtKey = generateIslandKey(locVict);
        if (tgtKey != orgKey) {
            pOwner.sendMessage(plugin.getPrefix() + plugin.getLocale(pOwner).errorOfflinePlayer);
            return;
        }
        Utils.send("&cAn island owner, " + pOwner.getName() + " attempt to "
                + "execute kick command to " + pVictim.getName() + " At "
                + Utils.locationShorted(locVict));
        pOwner.sendMessage(plugin.getPrefix() + TextFormat.GREEN + "Success! You send " + TextFormat.YELLOW + victimName + TextFormat.GREEN + " to spawn!");
        pVictim.sendMessage(plugin.getPrefix() + plugin.getLocale(pVictim).kickedFromOwner.replace("[name]", pOwner.getName())); //TextFormat.RED + "You were kicked from island owned by " + TextFormat.YELLOW + pOwner.getName());
        // Teleport
        if (plugin.getDatabase().getSpawn() != null) {
            pVictim.teleport(plugin.getDatabase().getSpawn().getCenter());
        } else {
            Utils.send("The default spawn world not found. Please use /is setspawn in-game. Using default world");
            pVictim.teleport(plugin.getServer().getDefaultLevel().getSafeSpawn());
        }
    }

    public void kickPlayerByAdmin(CommandSender sender, String arg) {
        Player p = Server.getInstance().getPlayer(arg);
        Player kicker = sender.isPlayer() ? Server.getInstance().getPlayer(sender.getName()) : null;
        if (p == null) {
            sender.sendMessage(plugin.getPrefix() + plugin.getLocale(kicker).errorOfflinePlayer.replace("[player]", arg));
            return;
        }
        Location locVict = p.getLocation();
        for (String lvl : plugin.getLevels()) {
            if (!locVict.getLevel().getName().equalsIgnoreCase(lvl)) {
                sender.sendMessage(plugin.getPrefix() + plugin.getLocale(kicker).errorOfflinePlayer.replace("[player]", arg));
                return;
            }
        }
        sender.sendMessage(plugin.getPrefix() + plugin.getLocale(kicker).kickSuccess.replace("[player]", arg));
        p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).kickedFromAdmin);
        // Teleport
        if (plugin.getDatabase().getSpawn() != null) {
            p.teleport(plugin.getDatabase().getSpawn().getCenter());
        } else {
            Utils.send("The default spawn world not found. Please use /is "
                    + "setspawn in-game. Using default world");
            p.teleport(plugin.getServer().getDefaultLevel().getSafeSpawn());
        }
    }

    public boolean checkIsland(Player p) {
        return plugin.getDatabase().getIsland(p.getName(), 1) != null;
    }

    private void createIsland(Player p) {
        this.createIsland(p, 1, "", plugin.getDefaultWorld(), false, EnumBiome.PLAINS, false);
    }

    public void createIsland(Player p, int templateId, String levelName, String home, boolean locked, EnumBiome biome, boolean teleport) {
        if (Settings.useEconomy) {
            double money = ASkyBlock.econ.getMoney(p);
            if (Settings.islandCost > money && Settings.islandCost != money) {
                // check if the starting island is FREE
                if (plugin.getIslandInfo(p) == null && Settings.firstIslandFree) {
                    p.sendMessage(plugin.getLocale(p).firstIslandFree);
                    p.sendMessage(plugin.getLocale(p).nextIslandPrice.replace("[price]", Double.toString(Settings.islandCost)));
                } else {
                    p.sendMessage(plugin.getLocale(p).errorNotEnoughMoney.replace("[price]", Double.toString(Settings.islandCost)));
                    return;
                }
            }
        }

        WorldSettings settings = plugin.getSettings(levelName);
        Level world = Server.getInstance().getLevelByName(levelName);
        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            int width = i * settings.getIslandDistance() * 2;
            int wx = (int) (Math.random() * width);
            int wz = (int) (Math.random() * width);
            int wy = Settings.islandHeight;
            wx = wx - wx % settings.getIslandDistance() + settings.getIslandDistance() / 2;
            wz = wz - wz % settings.getIslandDistance() + settings.getIslandDistance() / 2;
            IslandData pd = plugin.getDatabase().getIslandById(generateIslandKey(wx, wz, levelName), levelName);
            if (pd == null) {
                Location locIsland = new Location(wx, wy, wz, world);
                pd = claim(p, locIsland, home, locked);
                if (pd == null) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorFailedCritical);
                    Utils.send("&cUnable to claim level at:" + locIsland.toString());
                    return;
                }
                // Call an event
                IslandCreateEvent event = new IslandCreateEvent(p, templateId, pd);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorBlockedByAPI);
                    return;
                }
                plugin.getSchematics().pasteSchematic(p, locIsland, templateId, biome);

                boolean result = plugin.getDatabase().createIsland(pd);
                if (result) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).createSuccess);
                } else {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorFailedCritical);
                    return;
                }
                if (teleport) {
                    plugin.getGrid().homeTeleport(p, pd.getId());
                }
                return;
            }
        }
    }

    private IslandData claim(Player p, Location loc, String home, boolean locked) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        if (!checkIslandAt(loc.getLevel())) {
            return null;
        }

        int iKey = generateIslandKey(loc);
        List<IslandData> number = plugin.getDatabase().getIslands(p.getName());
        IslandData pd = plugin.getDatabase().getIslandLocation(loc.getLevel().getName(), x, z);
        pd.setId(number.size() + 1);
        pd.setIslandId(iKey);
        pd.setOwner(p.getName());
        pd.setCenter(x, loc.getFloorY(), z);
        pd.setLevelName(loc.getLevel().getName());
        pd.setLocked(locked);
        pd.setBiome("Plains");
        pd.setName(home);
        return pd;
    }

    public int generateIslandKey(Location loc) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        return generateIslandKey(x, z, loc.level.getName());
    }

    /**
     * Generates a new public key by checking the vector values
     * with their island distances, this will make sure every key
     * will not be duplicated and will make sure that each key is
     * unique every X distance.
     *
     * @param x     The position of the x vector
     * @param z     The position of the z vector
     * @param level This level will be checked on their distances / level
     * @return A unique island key
     */
    public int generateIslandKey(int x, int z, String level) {
        WorldSettings settings = plugin.getSettings(level);
        return x / settings.getIslandDistance() + z / settings.getIslandDistance() * Integer.MAX_VALUE;
    }

    public void deleteIsland(Player p, IslandData pd) {
        if (pd == null || pd.getOwner() == null) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorNoIsland);
            return;
        }
        if (!Utils.canBypassTimer(p, p.getName() + pd.getIslandId(), Settings.resetTime)) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorTooSoon.replace("[secs]", Utils.getPlayerRTime(p, p.getName() + pd.getIslandId(), 0)).replace("[cmds]", "delete"));
            return;
        }

        // Reset then wait :P
        TaskManager.runTask(new DeleteIslandTask(plugin, pd, p));

        PlayerData pda = plugin.getPlayerInfo(p);
        pda.setPlayerReset(pda.getPlayerReset() + 1);
        plugin.getDatabase().savePlayerData(pda);

        p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).resetSuccess.replace("[mili]", "" + Settings.resetTime));
    }

    public boolean checkIslandAt(Level level) {
        return plugin.getLevels().contains(level.getName());
    }

    public IslandData getIslandAt(Location loc) {
        if (!checkIslandAt(loc.getLevel())) {
            return null;
        }
        int iKey = generateIslandKey(loc);
        IslandData res = plugin.getDatabase().getIslandById(iKey, loc.getLevel().getName());
        if (res == null) {
            return null;
        }
        if (res.getOwner() == null) {
            return null;
        }
        return res;
    }

    public void islandInfo(Player p, Location loc) {
        if (!checkIslandAt(loc.getLevel())) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorWrongWorld);
            return;
        }
        final CoopData coopData = plugin.getTManager().getLeaderCoop(p.getName());
        final IslandData pd = getIslandAt(loc);
        if (pd == null) {
            p.sendMessage(TextFormat.LIGHT_PURPLE + plugin.getLocale(p).errorNotOnIsland);
            return;
        }
        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Island Owner: " + TextFormat.YELLOW + pd.getOwner());
        String strMembers;
        if (coopData == null || coopData.getMembers().isEmpty()) {
            strMembers = "none";
        } else {
            strMembers = Utils.arrayToString(coopData.getMembers());
        }

        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Members: " + TextFormat.AQUA + strMembers);
        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Flags: " + TextFormat.GOLD + "Allow Teleport: " + pd.isLocked());
    }

    public void teleportPlayer(Player p, String arg) {
        IslandData pd = plugin.getDatabase().getIsland(arg, 1);
        if (pd == null) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorNoIslandOther);
            return;
        }
        if (pd.getOwner() != null) {
            p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).errorOfflinePlayer.replace("[player]", arg));
            return;
        }
        Location home = plugin.getGrid().getSafeHomeLocation(pd.getOwner(), pd.getId());
        //if the home null
        if (home == null) {
            p.sendMessage(plugin.getPrefix() + TextFormat.RED + "Failed to find your island safe spawn");
            return;
        }
        plugin.getTeleportLogic().safeTeleport(p, home, false, pd.getId());
    }

    /**
     * Check either the location given is the player
     * island
     *
     * @param player The player to be check
     * @param loc    Location to be checked
     * @return true if the location is player's island
     */
    public boolean locationIsOnIsland(Player player, Vector3 loc) {
        if (player == null) {
            return false;
        }
        Location local = new Location(loc.x, loc.y, loc.z, player.getLevel());
        // Get the player's island from the grid if it exists
        IslandData island = getIslandAt(local);
        CoopData pd = plugin.getTManager().getLeaderCoop(island.getOwner());
        if (island != null) {
            // On an island in the grid
            // In a protected zone but is on the list of acceptable players
            // Otherwise return false
            return island.onIsland(local) && ((pd == null || pd.getMembers().contains(player.getName())) || island.getOwner().equalsIgnoreCase(player.getName()));
        }
        return false;
    }


}
