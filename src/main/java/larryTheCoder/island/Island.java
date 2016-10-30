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
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
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
import larryTheCoder.ASkyBlock;
import larryTheCoder.PlayerData;
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
            File file = new File(String.valueOf(Utils.Directory) + "Islands.dat");
            FileInputStream f = new FileInputStream(file);
            try (ObjectInputStream s = new ObjectInputStream(f)) {
                hashIslandOwner = (ConcurrentHashMap) s.readObject();
                hash_SkyBlock_PlayerData = (ConcurrentHashMap) s.readObject();
                hash_OtherWorld_PlayerData = (ConcurrentHashMap) s.readObject();
                hashIslandOwner.keySet().stream().map((key) -> hashIslandOwner.get(key)).filter((pd) -> !(pd.owner == null)).forEach((pd) -> {
                    hashNameToIsland.put(pd.owner, pd);
                });
            }
            long msEnd = System.currentTimeMillis();
            Utils.ConsoleMsg(TextFormat.YELLOW + String.format("%-20s: %5d islands.   Took %3d ms", "Island DB Load", hashNameToIsland.size(), msEnd - msStart));
        } catch (IOException | ClassNotFoundException e) {
            Utils.ConsoleMsg(TextFormat.GRAY + "Starting New Island Database...");
        }
    }

    public static void SaveIslands() {
        try {
            long msStart = System.currentTimeMillis();
            File file = new File(String.valueOf(Utils.Directory) + "Islands.dat");
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

    public static void ResetIsland(Player p) {
        IslandData pd = hashNameToIsland.get(p.getName());
        if (pd == null || pd.owner == null) {
            p.sendMessage(TextFormat.RED + "You don't have an island!");
            return;
        }
        Level world = Server.getInstance().getLevelByName("skyblock");

        Location loc = new Location((int) pd.x, (int) pd.y, (int) pd.z, world);
        long msStart = System.currentTimeMillis();
        Utils.ConsoleMsg("Resetting Island " + TextFormat.YELLOW + p.getName() + TextFormat.WHITE + " @ "); //Utils.LocStringShort(loc));
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

    public static int GetIslandKey(Location loc) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        return Island.GetIslandKey(x, z);
    }

    public static int GetIslandKey(int x, int z) {
        return x / islandSize + z / islandSize * maxIslandsAlongX;
    }

    public static boolean IsInIslandWorld(Location loc) {
        ArrayList<String> keyset = ASkyBlock.object.level;
        return loc.getLevel().getName().equalsIgnoreCase(keyset.toString());
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

/************************************ ISLAND CREATE ************************************/
    
    public static void claimIsland(){
        
    }
}
