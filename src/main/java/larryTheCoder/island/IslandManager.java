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

package larryTheCoder.island;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;

import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;
import larryTheCoder.Settings;
import larryTheCoder.Utils;
import larryTheCoder.database.ASConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author larryTheCoder
 */
public class IslandManager {

    public ASkyBlock plugin;
    public HashMap<UUID, Boolean> teleports = new HashMap<>();

    public IslandManager(ASkyBlock plugin){
        this.plugin = plugin;
        this.teleports.clear();
    }
    
    public boolean setTeleport(Player p, boolean type){
        this.teleports.put(p.getUniqueId(), type);
        return true;
    }
    
    public boolean isTeleport(Player p){
        return this.teleports.containsKey(p.getUniqueId());
    }
    
    public String getPlayerMembers(Player p) {
        IslandData pd = plugin.getDatabase().getIsland(p.getName());
        for (String st : pd.members) {
            return st;
        }
        return null;
    }
    
    public boolean addMember(Player p, IslandData pd, String argName) {
        Player exactName = plugin.getServer().getPlayer(argName);
        if (exactName == null) {
            p.sendMessage(TextFormat.RED + "No player known by name: " + TextFormat.YELLOW + argName);
            return true;
        }
        if (!pd.owner.equals(p.getName()) && !p.isOp()) {
            p.sendMessage(TextFormat.RED + "You don't own an island here.");
            return true;
        }
        if (pd.members.contains(exactName.getName())) {
            p.sendMessage(TextFormat.RED + "You already added " + TextFormat.YELLOW + exactName);
            return true;
        }
        pd.members.add(exactName.getName());
        p.sendMessage(TextFormat.GREEN + "You add " + TextFormat.YELLOW + exactName + TextFormat.GREEN + " as an island member.");
        return true;
    }
    
    public boolean removeMember(Player p, Location loc, String argName) {
        Player exactName = plugin.getServer().getPlayer(argName);
        if (exactName == null) {
            p.sendMessage(TextFormat.RED + "No player known by name: " + TextFormat.YELLOW + argName);
            return true;
        }
        IslandData pd = GetIslandAt(loc);
        if (pd == null || pd.owner == null) {
            p.sendMessage(TextFormat.RED + "There is no island here.");
            return true;
        }
        if (!pd.owner.equals(p.getName()) && !p.isOp()) {
            p.sendMessage(TextFormat.RED + "You don't own a island here.");
            return true;
        }
        if (!pd.members.contains(exactName.getName())) {
            p.sendMessage(TextFormat.RED + "No island member named " + TextFormat.YELLOW + exactName);
            return true;
        }
        pd.members.remove(exactName.getName());
        p.sendMessage(TextFormat.GREEN + "You removed " + TextFormat.YELLOW + exactName + TextFormat.GREEN + " as an island member.");
        return true;
    }
     
    public void handleIslandCommand(Player p){
        handleIslandCommand(p, false, 0);
    }

    public void handleIslandCommand(Player p, boolean reset, int homes) {
        if (!reset) {
            if (!checkIsland(p)) {
                createIsland(p);
            }
            IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName());
            if (pd == null || pd.owner == null) {
                p.sendMessage(TextFormat.RED + "No island found or could be created for you.");
                return;
            }
            if(plugin.cfg.getBoolean("island.saveInventory")){
                plugin.getInventory().savePlayerInventory(p);
            }
            p.setGamemode(Player.SURVIVAL);

        } else {
            createIsland(p);
        }

    }
    
    public void kickPlayerByName(final Player pOwner, final String victimName) {
        final Location loc = pOwner.getLocation();
        final IslandData pd = GetIslandAt(loc);
        if (pd == null || pd.owner == null || !pd.owner.equals(pOwner.getName())) {
            pOwner.sendMessage(TextFormat.RED + "You don't own a island where you stand.");
            return;
        }
        final int orgKey = generateIslandKey(loc);
        final Player pVictim = Server.getInstance().getPlayer(victimName);
        if (pVictim == null || !pVictim.isOnline()) {
            pOwner.sendMessage(TextFormat.RED + "No player found: " + TextFormat.YELLOW + victimName);
            return;
        }
        if (!(pOwner.isOp())) {
            if (pVictim.isOp()) {
                pOwner.sendMessage(TextFormat.RED + "You can't island kick admins...");
                return;
            }
        }
        if (victimName.equalsIgnoreCase(pOwner.getName())) {
            pOwner.sendMessage(TextFormat.RED + "You can't island kick yourself!");
            return;
        }
        final Location locVict = pVictim.getLocation();
        final int tgtKey = generateIslandKey(locVict);
        if (tgtKey != orgKey) {
            pOwner.sendMessage(TextFormat.RED + "They are not currently on this island.");
            return;
        }
        final String msg = "Island kick by " + pOwner.getName() + " @ " + Utils.LocStringShort(loc) + " -- kicking " + victimName + " @ " + Utils.LocStringShortNoWorld(locVict);
        Utils.ConsoleMsg(msg);
        pOwner.sendMessage(TextFormat.GREEN + "Success! You send " + TextFormat.YELLOW + victimName + TextFormat.GREEN + " to spawn!");
        pVictim.sendMessage(TextFormat.RED + "You were kicked from island owned by " + TextFormat.YELLOW + pOwner.getName());
        pVictim.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
    }
    
    public void kickPlayerByAdmin(CommandSender sender, String arg) {
        Player p = Server.getInstance().getPlayer(arg);
        if (p == null) {
            sender.sendMessage(plugin.getMsg("player_error").replace("[player]", arg));
            return;
        }
        Location locVict = p.getLocation();
        for (String lvl : ASkyBlock.get().level) {
            if (!locVict.getLevel().getName().equalsIgnoreCase(lvl)) {
                sender.sendMessage(plugin.getMsg("player_error").replace("[player]", arg));
                return;
            }
        }
        sender.sendMessage(plugin.getMsg("kick").replace("[player]", arg));
        p.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
    }
    
    public boolean checkIsland(Player p) {
        return plugin.getDatabase().getIsland(p.getName()) != null;
    }
    
    @SuppressWarnings("deprecation")
    public void createIsland(Player p){
        p.sendMessage(TextFormat.GREEN + "Creating a new island for you...");
        int i = 0;
        while (i < 1000000) {
            int width = i * Settings.islandSize * 2;
            int wx = (int) (Math.random() * width);
            int wz = (int) (Math.random() * width);
            IslandData pd = new IslandData("SkyBlock", wx, wz);
            if (pd.owner == null) {
                Location locIsland;
                int wy = Settings.islandHieght;
                Level world = Server.getInstance().getLevelByName("SkyBlock");
                locIsland = new Location(wx, wy, wz, world);
                plugin.getSchematic("default").pasteSchematic(locIsland, p, true);
                claim(p, locIsland);
                return;
            }
            ++i;
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
    
    public boolean claim(Player p, Location loc) {
        int x = loc.getFloorX();
        x = x - x % Settings.islandSize + Settings.islandSize / 2;
        int z = loc.getFloorZ();
        z = z - z % Settings.islandSize + Settings.islandSize / 2;
        if (!checkIslandAt(loc)) {
            return false;
        }
        
        int iKey = generateIslandKey(loc);
        IslandData pd = plugin.getDatabase().getIslandLocation(loc.getLevel().getName(), x, z);
        pd.biome = "PLAINS";
        pd.name = "My Island";
        pd.team = "";
        pd.islandId = iKey;
        pd.owner = p.getName();
        pd.members = new ArrayList<>();
        pd.X = x;
        pd.floor_y = loc.getFloorY();
        pd.Z = z;
        pd.levelName = loc.getLevel().getName();
        pd.locked = 0;
        
        boolean result = plugin.getDatabase().saveIsland(pd);
        if(result){
            p.sendMessage(plugin.getMsg("create"));
        } else {
            p.sendMessage("Unable to save your island SQL Error");
        }
        return true;
    }
    
    public void reset(Player p, boolean reset, int homes) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName());
        if (pd == null || pd.owner == null) {
            p.sendMessage(plugin.getMsg("no_island_error"));
            return;
        }
        Level world = Server.getInstance().getLevelByName("SkyBlock");
        Location loc = new Location(pd.X, pd.floor_y, pd.Z, world);
        Utils.ConsoleMsg(plugin.getMsg("att_reset").replace("[location]", Utils.LocStringShort(loc)).replace("[player]", p.getName()));
        int sx = loc.getFloorX();
        sx -= sx % Settings.islandSize;
        int sz = loc.getFloorX();
        sz -= sz % Settings.islandSize;
        if (sx < 0 || sz < 0) {
            return;
        }
        int ex = sx + Settings.islandSize - 1;
        int ez = sz + Settings.islandSize - 1;
        int y = 0;
        while (y < 128) {
            int x = sx;
            while (x <= ex) {
                int z = sz;
                while (z <= ez) {
                    world.setBlockIdAt(x, y, z, Block.AIR);
                    ++z;
                }
                ++x;
            }
            ++y;
        }
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
        if (pd.members.contains(pName)) {
            return true;
        }
        return false;
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
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        if (!checkIslandAt(loc)) {
            p.sendMessage(plugin.getMsg("level_error"));
            return;
        }
        final IslandData pd = GetIslandAt(loc);
        if (pd == null) {
            p.sendMessage(TextFormat.LIGHT_PURPLE + plugin.getMsg("no_claim"));
            return;
        }
        p.sendMessage(TextFormat.LIGHT_PURPLE + "- Island Owner: " + TextFormat.YELLOW + pd.owner);
        String strMembers = Utils.GetCommaList(pd.members);
        if (pd.members.size() <= 0) {
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
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(arg);
        if (pd.owner != null) {
            p.sendMessage(plugin.getMsg("player_error").replace("[player]", arg));
            return;
        }
        // TO-DO support homes!
        Level lvl = ASkyBlock.get().getServer().getLevelByName(pd.levelName);
        p.teleport(new Location(pd.X, pd.floor_y, pd.Z, lvl));
    }

    public void setHomeLocation(Player p, Position lPlusOne, int number) {
        ASConnection db = ASkyBlock.get().getDatabase();
        Position loc = lPlusOne;
        db.setPosition(loc, number);
    }
}
