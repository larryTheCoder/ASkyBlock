/*
 * Copyright (C) 2016 larryTheHarry 
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
package com.larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.intellectiualcrafters.TaskManager;

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import com.larryTheCoder.schematic.Schematic;
import com.larryTheCoder.task.DeleteIslandTask;
import java.util.List;

/**
 * @author larryTheCoder
 */
public class IslandManager {

    public ASkyBlock plugin;

    public IslandManager(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    public void handleIslandCommand(Player p) {
        handleIslandCommand(p, false, 1);
    }

    public void handleIslandCommand(Player p, int homes) {
        handleIslandCommand(p, false, homes);
    }

    public void handleIslandCommand(Player p, boolean reset, int homes) {
        if (!reset) {
            if (!checkIsland(p, homes)) {
                createIsland(p);
            }
            IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName(), homes);
            if (pd == null || pd.owner == null) {
                p.sendMessage(plugin.getPrefix() +TextFormat.RED + "No island found or could be created for you.");
                return;
            }
            if (plugin.cfg.getBoolean("island.saveInventory")) {
                plugin.getInventory().savePlayerInventory(p);
            }
            plugin.getGrid().homeTeleport(p, homes);
            if (Settings.gamemode != -1) {
                p.setGamemode(Settings.gamemode);
            }
        } else {
            createIsland(p);
        }

    }

    public void kickPlayerByName(final Player pOwner, final String victimName) {
        final Location loc = pOwner.getLocation();
        final IslandData pd = GetIslandAt(loc);
        if (pd == null || pd.owner == null || !pd.owner.equals(pOwner.getName())) {
            pOwner.sendMessage(plugin.getPrefix() +TextFormat.RED + "You don't own a island where you stand.");
            return;
        }
        final int orgKey = generateIslandKey(loc);
        final Player pVictim = Server.getInstance().getPlayer(victimName);
        if (pVictim == null || !pVictim.isOnline()) {
            pOwner.sendMessage(plugin.getPrefix() +TextFormat.RED + "No player found: " + TextFormat.YELLOW + victimName);
            return;
        }
        if (!(pOwner.isOp())) {
            if (pVictim.isOp()) {
                pOwner.sendMessage(plugin.getPrefix() +TextFormat.RED + "You can't island kick admins...");
                return;
            }
        }
        if (victimName.equalsIgnoreCase(pOwner.getName())) {
            pOwner.sendMessage(plugin.getPrefix() +TextFormat.RED + "You can't island kick yourself!");
            return;
        }
        final Location locVict = pVictim.getLocation();
        final int tgtKey = generateIslandKey(locVict);
        if (tgtKey != orgKey) {
            pOwner.sendMessage(plugin.getPrefix() +TextFormat.RED + "They are not currently on this island.");
            return;
        }
        final String msg = "Island kick by " + pOwner.getName() + " @ " + Utils.LocStringShort(loc) + " -- kicking " + victimName + " @ " + Utils.LocStringShortNoWorld(locVict);
        Utils.ConsoleMsg(msg);
        pOwner.sendMessage(plugin.getPrefix() +TextFormat.GREEN + "Success! You send " + TextFormat.YELLOW + victimName + TextFormat.GREEN + " to spawn!");
        pVictim.sendMessage(plugin.getPrefix() +TextFormat.RED + "You were kicked from island owned by " + TextFormat.YELLOW + pOwner.getName());
        pVictim.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
    }

    public void kickPlayerByAdmin(CommandSender sender, String arg) {
        Player p = Server.getInstance().getPlayer(arg);
        if (p == null) {
            sender.sendMessage(plugin.getPrefix() +plugin.getMsg("player_error").replace("[player]", arg));
            return;
        }
        Location locVict = p.getLocation();
        for (String lvl : ASkyBlock.get().level) {
            if (!locVict.getLevel().getName().equalsIgnoreCase(lvl)) {
                sender.sendMessage(plugin.getPrefix() +plugin.getMsg("player_error").replace("[player]", arg));
                return;
            }
        }
        sender.sendMessage(plugin.getPrefix() +plugin.getMsg("kick").replace("[player]", arg));
        p.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
    }

    public boolean checkIsland(Player p) {
        return checkIsland(p, 1);
    }

    public boolean checkIsland(Player p, int homes) {
        return plugin.getDatabase().getIsland(p.getName(), homes) != null;
    }

    public void createIsland(Player p) {
        this.createIsland(p, null, "");
    }

    public void createIsland(Player p, Schematic stmt, String home) {
        p.sendMessage(plugin.getPrefix() +TextFormat.GREEN + "Creating a new island for you...");
        this.createIsland(p.getName(), stmt, home);
    }

    public void createIsland(String p, Schematic stmt, String home) {
        for (int i = 0; i < 1000000; ++i) {
            int width = i * Settings.islandSize * 2;
            int wx = (int) (Math.random() * width);
            int wz = (int) (Math.random() * width);
            int wy = Settings.islandHieght;
            wx = wx - wx % Settings.islandSize + Settings.islandSize / 2;
            wz = wz - wz % Settings.islandSize + Settings.islandSize / 2;
            IslandData pd = plugin.getDatabase().getIslandById(generateIslandKey(wx, wz));
            if (pd == null) {
                Level world = Server.getInstance().getLevelByName("SkyBlock");
                Location locIsland = new Location(wx, wy, wz, world);
                if (stmt != null) {
                    stmt.pasteSchematic(locIsland);
                } else {
                    plugin.getSchematic("default").pasteSchematic(locIsland);
                }
                claim(p, locIsland, home);
                break;
            }
        }
    }

    public int generateIslandKey(Location loc) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        return generateIslandKey(x, z);
    }

    public int generateIslandKey(int x, int z) {
        return x / Settings.islandSize + z / Settings.islandSize * 10000;
    }

    public boolean claim(String p, Location loc, String home) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        if (!checkIslandAt(loc)) {
            return false;
        }

        int iKey = generateIslandKey(loc);
        IslandData pd = plugin.getDatabase().getIslandLocation(loc.getLevel().getName(), x, z);
        List<IslandData> number = plugin.getDatabase().getIslands(p);
        pd.id = number.size() + 1;
        pd.biome = Settings.defaultBiome.getName();
        pd.name = home;
        pd.islandId = iKey;
        pd.owner = p;
        pd.X = x;
        pd.Y = loc.getFloorY();
        pd.Z = z;
        pd.levelName = loc.getLevel().getName();
        pd.locked = false;

        boolean result = plugin.getDatabase().saveIsland(pd);
        if (Server.getInstance().getPlayer(p) != null) {
            Player pr = Server.getInstance().getPlayer(p);
            if (result) {
                pr.sendMessage(plugin.getPrefix() +plugin.getMsg("create"));
            } else {
                pr.sendMessage(plugin.getPrefix() + "&cUnable to save your island SQL Error");
            }
        } else {

        }
        return true;
    }

    public void reset(Player p, boolean reset, int homes) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName(), homes);
        if (pd == null || pd.owner == null) {
            p.sendMessage(plugin.getPrefix() +plugin.getMsg("no_island_error"));
            return;
        }
        TaskManager.runTask(new DeleteIslandTask(plugin, pd));
        if (reset) {
            p.sendMessage(plugin.getPrefix() + plugin.getMsg("reset").replace("[min]", "30"));
            handleIslandCommand(p, true, homes);
        } else {
            p.sendMessage(plugin.getPrefix() + plugin.getMsg("restart").replace("[min]", "30"));
        }
    }

    public boolean checkIslandAt(Location loc) {
        return loc.getLevel().getName().equalsIgnoreCase("skyblock");
    }

    public boolean CanPlayerAccess(Player p, Location loc) {
        String pName = p.getName();
        if (p.isOp()) {
            return true;
        }
        if (!checkIslandAt(loc)) {
            return true;
        }
        IslandData pd = GetIslandAt(loc);
        if (pd == null) {
            return false;
        }
        if (pd.owner == null) {
            return false;
        }
        if (pd.owner.equals(pName)) {
            return true;
        }
        PlayerData pd2 = plugin.getDatabase().getPlayerData(p);
        return pd2.members.contains(pName);
    }

    public IslandData GetIslandAt(Location loc) {
        if (!checkIslandAt(loc)) {
            return null;
        }
        int iKey = generateIslandKey(loc);
        IslandData res = ASkyBlock.get().getDatabase().getIslandById(iKey);
        if (res == null) {
            return null;
        }
        if (res.owner == null) {
            return null;
        }
        return res;
    }

    public void islandInfo(Player p, Location loc) {
        if (!checkIslandAt(loc)) {
            p.sendMessage(plugin.getPrefix() +plugin.getMsg("level_error"));
            return;
        }
        final IslandData pd = GetIslandAt(loc);
        PlayerData pd2 = plugin.getDatabase().getPlayerData(p);
        if (pd == null) {
            p.sendMessage(TextFormat.LIGHT_PURPLE + plugin.getMsg("no_claim"));
            return;
        }
        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Island Owner: " + TextFormat.YELLOW + pd.owner);
        String strMembers = Utils.arrayToString(pd2.members);
        if (pd2.members.size() <= 0) {
            strMembers = "none";
        }
        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Members: " + TextFormat.AQUA + strMembers);

        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Flags: " + TextFormat.GOLD + "Allow Teleport: " + pd.locked);
    }

    public String GetFlagString(final boolean b) {
        if (b) {
            return TextFormat.RED + "false";
        }
        return TextFormat.GREEN + "true";
    }

    public void teleportPlayer(Player p, String arg) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(arg, 1);
        if (pd.owner != null) {
            p.sendMessage(plugin.getPrefix() +plugin.getMsg("player_error").replace("[player]", arg));
            return;
        }
        Level lvl = ASkyBlock.get().getServer().getLevelByName(pd.levelName);
        p.teleport(new Location(pd.X, pd.Y, pd.Z, 0, 0, lvl));
    }

    public void setHomeLocation(Player p, Position lPlusOne, int number) {
        Database db = ASkyBlock.get().getDatabase();
        Position loc = lPlusOne;
        db.setPosition(loc, number, p.getName());
    }
}
