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
import cn.nukkit.block.BlockSapling;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.object.tree.ObjectTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import larryTheCoder.PlayerData;
import larryTheCoder.PlayerDiary;
import larryTheCoder.Utils;

/**
 * @author larryTheCoder
 */
public class Island {

    public static int island_y = 60;
    public static int islandSize = 100;
    public static int maxIslandsAlongX = 10000;
    public static ConcurrentHashMap<Integer, IslandData> hashIslandOwner = new ConcurrentHashMap();
    public static ConcurrentHashMap<String, IslandData> hashNameToIsland = new ConcurrentHashMap();
    public static ConcurrentHashMap<String, PlayerData> hash_SkyBlock_PlayerData = new ConcurrentHashMap();
    public static ConcurrentHashMap<String, PlayerData> hash_OtherWorld_PlayerData = new ConcurrentHashMap();

    public static void LoadIslands() {
        try {
            long msStart = System.currentTimeMillis();
            File file = new File(String.valueOf(Utils.DIRECTORY) + "Islands.dat");
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f);
            hashIslandOwner = (ConcurrentHashMap) s.readObject();
            hash_SkyBlock_PlayerData = (ConcurrentHashMap) s.readObject();
            hash_OtherWorld_PlayerData = (ConcurrentHashMap) s.readObject();
            for (Integer key : hashIslandOwner.keySet()) {
                IslandData pd = hashIslandOwner.get(key);
                if (pd.owner == null) {
                    continue;
                }
                hashNameToIsland.put(pd.owner, pd);
            }
            s.close();
            long msEnd = System.currentTimeMillis();
            Utils.ConsoleMsg(TextFormat.YELLOW + String.format("%-20s: %5d islands.   Took %3d ms", "Island DB Load", hashNameToIsland.size(), msEnd - msStart));
        } catch (Throwable e) {
            Utils.ConsoleMsg(TextFormat.GRAY + "Starting New Island Database...");           
        }
    }

    public static void kickPlayerByName(final Player pOwner, final String victimName) {
        final Location loc = pOwner.getLocation();
        final IslandData pd = GetIslandAt(loc);
        if (pd == null || pd.owner == null || !pd.owner.equals(pOwner.getName())) {
            pOwner.sendMessage(TextFormat.RED + "You don't own a island where you stand.");
            return;
        }
        final int orgKey = GetIslandKey(loc);
        final Player pVictim = Server.getInstance().getPlayer(victimName);
        if (pVictim == null || !pVictim.isOnline()) {
            pOwner.sendMessage(TextFormat.RED + "No player found: " + TextFormat.YELLOW + victimName);
            return;
        }
        if (pVictim.isOp()) {
            pOwner.sendMessage(TextFormat.RED + "You can't island kick admins...");
            return;
        }
        if (victimName.equalsIgnoreCase(pOwner.getName())) {
            pOwner.sendMessage(TextFormat.RED + "You can't island kick yourself!");
            return;
        }
        final Location locVict = pVictim.getLocation();
        final int tgtKey = GetIslandKey(locVict);
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
        
    public static void SaveIslands() {
        try {
            long msStart = System.currentTimeMillis();
            File file = new File(String.valueOf(Utils.DIRECTORY) + "Islands.dat");
            FileOutputStream f = new FileOutputStream(file);
            try (ObjectOutputStream s = new ObjectOutputStream(f)) {
                s.writeObject(hashIslandOwner);
                s.writeObject(hash_SkyBlock_PlayerData);
                s.writeObject(hash_OtherWorld_PlayerData);
            }
            long msEnd = System.currentTimeMillis();
            Utils.ConsoleMsg(TextFormat.YELLOW + String.format("%-20s: %5d islands.   Took %3d ms", "Island DB Save", hashIslandOwner.size(), msEnd - msStart));
        } catch (Throwable exc) {
            System.out.println("**********************************************");
            System.out.println("SaveIslands: " + exc.toString());
            System.out.println("**********************************************");
        }
    }

    public static int GetIslandKey(Location loc) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        return Island.GetIslandKey(x, z);
    }

    public static int GetIslandKey(int x, int z) {
        return x / islandSize + z / islandSize * maxIslandsAlongX;
    }

    public static boolean IsInIslandWorld(Location loc) {
        return loc.getLevel().getName().equalsIgnoreCase("skyblock");
    }

    public static boolean CanPlayerAccess(Player p, Location loc, String msg) {
        String pName = p.getName();
        if (p.isOp()) {
            return true;
        }
        if (!Island.IsInIslandWorld(loc)) {
            return true;
        }
        IslandData pd = Island.GetIslandAt(loc);
        if (pd == null) {
            return true;
        }
        if (pd.owner == null) {
            return true;
        }
        if (pd.owner.equals(pName)) {
            return true;
        }
        if (pd.members.contains(pName)) {
            return true;
        }
        if (msg != null) {
            p.sendMessage(String.valueOf(msg) + TextFormat.LIGHT_PURPLE + " Island Owner: " + TextFormat.YELLOW + pd.owner);
        }
        return false;
    }

    public static IslandData GetIslandAt(Location loc) {
        if (!Island.IsInIslandWorld(loc)) {
            return null;
        }
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        int iKey = Island.GetIslandKey(loc);
        IslandData res = hashIslandOwner.get(iKey);
        if (res == null) {
            return null;
        }
        if (res.owner == null) {
            return null;
        }
        return res;
    }

    public static void ShowIslandInfo(Player p, Location loc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void CreateIsland(Player p) {
        p.sendMessage(TextFormat.GREEN + "Creating a new island for you...");
        int i = 0;
        while (i < 1000000) {
            int width = i * islandSize * 2;
            int wx = (int) (Math.random() * (double) width);
            int wz = (int) (Math.random() * (double) width);
            Integer key = Island.GetIslandKey(wx = wx - wx % islandSize + islandSize / 2, wz = wz - wz % islandSize + islandSize / 2);
            IslandData pd = hashIslandOwner.get(key);
            if (pd == null || pd.owner == null) {
                Location locIsland;
                int wy = island_y;
                Level world = Server.getInstance().getLevelByName("SkyBlock");
                //FullChunk chk = world.getChunk(wx, wz);//(locIsland = new Location(world, (double) wx, (double) wy, (double) wz));
                locIsland = new Location(wx, wy, wz, 0, 0, world);
                Island.MakeIsland(world, wx -13, wy, wz-13);
                world.setBlock(new Vector3(wx + 1, wy, wz + 1), Block.get(Block.CHEST));
                //populateChest thread = new populateChest();
                //thread.DoIt(p.getName(), wx + 1, wy, wz + 1, 1);
                Island.ClaimIslandAt(p, locIsland);
                return;
            }
            ++i;
        }
    }
    
    static void MakeIsland(Level world, int X, int Y, int Z) {
        int groundHeight = island_y;
        // bedrock - ensures island are not overwritten
        for (int x = X + 13; x < X + 14; ++x) {
            for (int z = Z + 13; z < Z + 14; ++z) {
                world.setBlockIdAt(x, groundHeight, z, Block.BEDROCK);
            }
        }
        // Add some dirt and grass
        for (int x = X + 11; x < X + 16; ++x) {
            for (int z = Z + 11; z < Z + 16; ++z) {
                world.setBlockIdAt(x, groundHeight + 1, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 2, z, Block.DIRT);
            }
        }
        for (int x = X + 10; x < X + 17; ++x) {
            for (int z = Z + 10; z < Z + 17; ++z) {
                world.setBlockIdAt(x, groundHeight + 3, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 4, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 5, z, Block.GRASS);
            }
        }
        // Then cut off the corners to make it round-ish
        for (int x = X + 10; x < X + 17; x += 2) {
            for (int z = Z + 10; z < Z + 17; z += 2) {
                world.setBlockIdAt(x, groundHeight + 1, z, Block.AIR);
            }
        }

        for (int y = groundHeight - 1; y < groundHeight + 6; ++y) {
            for (int x_space = X + 13 - 2; x_space <= X + 13 + 2; x_space += 5) {
                for (int z_space = Z + 13 - 2; z_space <= Z + 13 + 2; z_space += 5) {
                    world.setBlockIdAt(x_space, y, z_space, Block.AIR);
                }
            }
        }
        ObjectTree.growTree(world, X + 10, groundHeight + 6, Z + 11, new NukkitRandom(), BlockSapling.OAK);
    }
        
    public static boolean ClaimIslandAt(Player p, Location loc) {
        int x = loc.getFloorX();
        x = x - x % islandSize + islandSize / 2;
        int z = loc.getFloorZ();
        z = z - z % islandSize + islandSize / 2;
        if (!Island.IsInIslandWorld(loc)) {
            return false;
        }
        int iKey = Island.GetIslandKey(loc);
        IslandData pd = new IslandData();
        pd.owner = p.getName();
        pd.msClaimed = System.currentTimeMillis();
        pd.members = new ArrayList();
        pd.x = x;
        pd.y = (loc.getFloorY() + 6);
        pd.z = z;
        hashIslandOwner.put(iKey, pd);
        hashNameToIsland.put(p.getName(), pd);
        p.sendMessage(TextFormat.GREEN + "You just created a new island!");
        p.sendMessage(TextFormat.GREEN + "Use /is help for a bunch of commands");
//        Island.RemoveFloor(loc);
//        Island.HighlightIslandBorders(loc, null);
        return true;
    }

    public static void ResetIsland(Player p) {
        IslandData pd = hashNameToIsland.get(p.getName());
        if (pd == null || pd.owner == null) {
            p.sendMessage(TextFormat.RED + "You don't have an island!");
            return;
        }
        Level world = Server.getInstance().getLevelByName("SkyBlock");
        Location loc = new Location((int) pd.x, (int) pd.y, (int) pd.z, world);
        long msStart = System.currentTimeMillis();
        Utils.ConsoleMsg("Resetting Island " + TextFormat.YELLOW + p.getName() + TextFormat.WHITE + " @ " + Utils.LocStringShort(loc));
        int sx = loc.getFloorX();
        sx -= sx % islandSize;
        int sz = loc.getFloorX();
        sz -= sz % islandSize;
        if (sx < 0 || sz < 0) {
            return;
        }
        int ex = sx + islandSize - 1;
        int ez = sz + islandSize - 1;
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
        hashNameToIsland.remove(p.getName());
        Integer key = Island.GetIslandKey(loc);
        if (hashIslandOwner.containsKey(key)) {
            hashIslandOwner.remove(key);
        }
        ArrayList<Entity> entRemove = new ArrayList<>();
        for (Entity ent : world.getEntities()) {
            Location entLoc;
            if (ent == null || !Objects.equals(Island.GetIslandKey(entLoc = ent.getLocation()), key)) {
                continue;
            }
            entRemove.add(ent);
        }
        int i = entRemove.size() - 1;
        while (i >= 0) {
            Entity ent2 = (Entity) entRemove.get(i);
            ent2.kill();
            --i;
        }
        long msEnd = System.currentTimeMillis();
        Utils.ConsoleMsg(TextFormat.WHITE + String.format("* Reset Island: Took %d ms", msEnd - msStart));
        p.sendMessage(TextFormat.GREEN + "Your island was deleted! " + TextFormat.GRAY + "(Once every 30 minutes)");
    }

    public static boolean AddMember(Player p, Location loc, String argName) {
        String exactName = PlayerDiary.GetPlayerExactName(argName);
        if (exactName == null) {
            p.sendMessage(TextFormat.RED + "No player known by name: " + TextFormat.YELLOW + argName);
            return true;
        }
        IslandData pd = Island.GetIslandAt(loc);
        if (pd == null || pd.owner == null) {
            p.sendMessage(TextFormat.RED + "There is no island here.");
            return true;
        }
        if (!pd.owner.equals(p.getName()) && !p.isOp()) {
            p.sendMessage(TextFormat.RED + "You don't own an island here.");
            return true;
        }
        if (pd.members.contains(exactName)) {
            p.sendMessage(TextFormat.RED + "You already added " + TextFormat.YELLOW + exactName);
            return true;
        }
        pd.members.add(exactName);
        int iKey = Island.GetIslandKey(loc);
        hashIslandOwner.put(iKey, pd);
        p.sendMessage(TextFormat.GREEN + "You add " + TextFormat.YELLOW + exactName + TextFormat.GREEN + " as an island member.");
        return true;
    }

    public static boolean RemoveMember(Player p, Location loc, String argName) {
        String exactName = PlayerDiary.GetPlayerExactName(argName);
        if (exactName == null) {
            p.sendMessage(TextFormat.RED + "No player known by name: " + TextFormat.YELLOW + argName);
            return true;
        }
        IslandData pd = Island.GetIslandAt(loc);
        if (pd == null || pd.owner == null) {
            p.sendMessage(TextFormat.RED + "There is no island here.");
            return true;
        }
        if (!pd.owner.equals(p.getName()) && !p.isOp()) {
            p.sendMessage(TextFormat.RED + "You don't own a island here.");
            return true;
        }
        if (!pd.members.contains(exactName)) {
            p.sendMessage(TextFormat.RED + "No island member named " + TextFormat.YELLOW + exactName);
            return true;
        }
        pd.members.remove(exactName);
        int iKey = Island.GetIslandKey(loc);
        hashIslandOwner.put(iKey, pd);
        p.sendMessage(TextFormat.GREEN + "You removed " + TextFormat.YELLOW + exactName + TextFormat.GREEN + " as an island member.");
        return true;
    }
    
    public static void GoToIsland(Player p) {
        if (!Island.PlayerHasIsland(p)) {
            Island.CreateIsland(p);
        } else {
            p.sendMessage(TextFormat.GREEN + "You go to your island!");
        }
        IslandData pd = hashNameToIsland.get(p.getName());
        if (pd == null || pd.owner == null) {
            p.sendMessage(TextFormat.RED + "[SkyBlock] No island found or could be created for you.");
            return;
        }
        p.teleport(new Location(pd.x,  pd.y + 6,  pd.z, Server.getInstance().getLevelByName("SkyBlock")));
        p.setGamemode(Player.SURVIVAL);
    }
    
    public static boolean PlayerHasIsland(Player p) {
        if (hashNameToIsland.get(p.getName()) != null) {
            return true;
        }
        return false;
    }
}
