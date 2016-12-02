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
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.generator.object.tree.ObjectTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;
import larryTheCoder.Settings;
import larryTheCoder.Utils;
import larryTheCoder.database.ASConnection;

/**
 * @author larryTheCoder
 */
public class Island {

    // #######################################################################  
    // #                      Island Members Section                         #
    // #######################################################################  
    public static String getPlayerMembers(Player p) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName());
        for (String st : pd.members) {
            return st;
        }
        return null;
    }

    public static boolean addMember(Player p, IslandData pd, String argName) {
        Player exactName = ASkyBlock.get().getServer().getPlayer(argName);
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

    public static boolean removeMember(Player p, Location loc, String argName) {
        Player exactName = ASkyBlock.get().getServer().getPlayer(argName);
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
        if (!pd.members.contains(exactName.getName())) {
            p.sendMessage(TextFormat.RED + "No island member named " + TextFormat.YELLOW + exactName);
            return true;
        }
        pd.members.remove(exactName.getName());
        int iKey = Island.generateIslandKey(loc);
        p.sendMessage(TextFormat.GREEN + "You removed " + TextFormat.YELLOW + exactName + TextFormat.GREEN + " as an island member.");
        return true;
    }

    // #######################################################################
    // #                   Island Command Executor Section                   #
    // #######################################################################
//    TO-DO this function
//    public static void createIslandHomes(Player p, int homes){
//        if (p == null){
//            return;
//        }
//        if(ASkyBlock.getInstance().cfg.getInt("MaxHomes") < homes){
//            p.sendMessage("maxedHome");
//            return;
//        }
//        
//        
//    }
    public static void handleIslandCommand(Player p) {
        handleIslandCommand(p, false, 0);
    }

    public static void handleIslandCommand(Player p, boolean reset, int homes) {
        if (!reset) {
            p.teleport(new Location(0, 90000, 0, ASkyBlock.get().getServer().getLevelByName("SkyBlock")));
            if (!Island.checkIsland(p)) {
                Island.createIsland(p);
            }
            IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName());
            if (pd == null || pd.owner == null) {
                p.sendMessage(TextFormat.RED + "No island found or could be created for you.");
                return;
            }
            
            GridManager.homeTeleport(p);

            p.setGamemode(Player.SURVIVAL);

        } else {
            Island.createIsland(p);
        }

    }

    public static void kickPlayerByName(final Player pOwner, final String victimName) {
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

    // #######################################################################
    // #                  Main Island Section [Step by Step]                 #
    // #######################################################################
    // Check if player got island
    public static boolean checkIsland(Player p) {
        return ASkyBlock.get().getDatabase().getIsland(p.getName()) != null;
    }

    // Create an island if they dont   
    @SuppressWarnings("deprecation")
    public static void createIsland(Player p) {
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
                locIsland = new Location(wx, wy, wz, 0, 0, world);
                //Island.create(world, wx - 13, wy, wz - 13);
                ASkyBlock.get().getSchematic("default").pasteSchematic(locIsland, p);
                Island.claim(p, locIsland, pd);                
                return;
            }
            ++i;
        }
    }

    // Then generate a random Island ID
    public static int generateIslandKey(Location loc) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        // 2512.75
        return Island.generateIslandKey(x, z);
    }

    public static int generateIslandKey(int x, int z) {
        return x / Settings.islandSize + z / Settings.islandSize * 10000;
    }

    // Create an island
    private static void create(Level world, int X, int Y, int Z) {
        int groundHeight = Y;
        // bedrock - ensures island are not overwritten
        for (int x = X + 13; x < X + 14; ++x) {
            for (int z = Z + 13; z < Z + 14; ++z) {
                world.setBlockIdAt(x, groundHeight, z, Block.BEDROCK);
            }
        }
        // Add some dirt and grass
        for (int x = X + 12; x < X + 15; ++x) {
            for (int z = X + 12; z < X + 15; ++z) {
                world.setBlockIdAt(x, groundHeight + 1, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 2, z, Block.DIRT);
            }
        }
        for (int x = X + 11; x < X + 16; ++x) {
            for (int z = Z + 11; z < Z + 16; ++z) {
                world.setBlockIdAt(x, groundHeight + 3, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 4, z, Block.DIRT);
            }
        }
        for (int x = X + 10; x < X + 17; ++x) {
            for (int z = Z + 10; z < Z + 17; ++z) {
                world.setBlockIdAt(x, groundHeight + 5, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 6, z, Block.DIRT);
                world.setBlockIdAt(x, groundHeight + 7, z, Block.GRASS);
            }
        }
        // Then cut off the corners to make it round-ish
        for (int x_space = X + 13 - 2; x_space <= X + 13 + 2; x_space += 4) {
            for (int z_space = Z + 13 - 2; z_space <= Z + 13 + 2; z_space += 4) {
                world.setBlockIdAt(x_space, groundHeight + 3, z_space, Block.AIR);
                world.setBlockIdAt(x_space, groundHeight + 4, z_space, Block.AIR);
            }
        }

        for (int y = groundHeight - 1; y < groundHeight + 8; ++y) {
            for (int x_space = X + 13 - 3; x_space <= X + 13 + 3; x_space += 6) {
                for (int z_space = Z + 13 - 3; z_space <= Z + 13 + 3; z_space += 6) {
                    world.setBlockIdAt(x_space, y, z_space, Block.AIR);
                }
            }
        }
        int Xt = X + 13;
        int Zt = X + 13;
        // First place
        world.setBlockIdAt(Xt - 1, groundHeight + 1, Zt + 1, Block.AIR);
        world.setBlockIdAt(Xt - 2, groundHeight + 1, Zt + 2, Block.AIR);
        world.setBlockIdAt(Xt - 1, groundHeight + 1, Zt - 1, Block.AIR);
        world.setBlockIdAt(Xt - 2, groundHeight + 1, Zt - 2, Block.AIR);
        // tree
        ObjectTree.growTree(world, X + 10, groundHeight + 8, Z + 11, new NukkitRandom(), BlockSapling.OAK);
    }

    // Claim island after creating island
    public static boolean claim(Player p, Location loc, IslandData pd) {
        int x = loc.getFloorX();
        x = x - x % Settings.islandSize + Settings.islandSize / 2;
        int z = loc.getFloorZ();
        z = z - z % Settings.islandSize + Settings.islandSize / 2;
        if (!Island.checkIslandAt(loc)) {
            return false;
        }
        int iKey = Island.generateIslandKey(loc);
        pd.id = iKey;
        pd.owner = p.getName();
        pd.name = "My Island";
        pd.members = new ArrayList<>();
        pd.X = x;
        pd.floor_y = loc.getFloorY();
        pd.Z = z;
        pd.biome = "PLAINS";
        pd.locked = "false";
        pd.team = "";
        pd.levelName = loc.getLevel().getName();
        p.sendMessage(getMsg("create"));
        ASkyBlock.get().getDatabase().saveIsland(pd);
        return true;
    }

    // #######################################################################
    // #                         Island reset                                #
    // #######################################################################
    public static void reset(Player p, boolean reset, int homes) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(p.getName());
        if (pd == null || pd.owner == null) {
            p.sendMessage(getMsg("no_island_error"));
            return;
        }
        Level world = Server.getInstance().getLevelByName("SkyBlock");
        Location loc = new Location(pd.X, pd.floor_y, pd.Z, 0, 0, world);
        Utils.ConsoleMsg(getMsg("att_reset").replace("[location]", Utils.LocStringShort(loc)).replace("[player]", p.getName()));
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
//        //hashNameToIsland.remove(p.getName());
//        //Integer key = Island.generateIslandKey(loc);
//        //if (hashIslandOwner.containsKey(key)) {
//        //    hashIslandOwner.remove(key);
//       // }
//        ArrayList<Entity> entRemove = new ArrayList<>();
//        for (Entity ent : world.getEntities()) {
//     //       if (ent == null || !Objects.equals(Island.generateIslandKey(ent.getLocation()), key)) {
//    //            continue;
//   //         }
//      //      entRemove.add(ent);
//        }
//        int i = entRemove.size() - 1;
//        while (i >= 0) {
//            Entity ent2 = entRemove.get(i);
//            ent2.kill();
//            --i;
//        }
        if (reset) {
            p.sendMessage(getPrefix() + getMsg("reset").replace("[min]", "30"));
            Island.handleIslandCommand(p, true, homes);
        } else {
            p.sendMessage(getPrefix() + getMsg("restart").replace("[min]", "30"));
        }
    }

    // #######################################################################
    // #                          Island GUI                                 #
    // #######################################################################
    public static boolean checkIslandAt(Location loc) {
        return loc.getLevel().getName().equalsIgnoreCase("skyblock");
    }

    public static boolean CanPlayerAccess(Player p, Location loc, String msg) {
        String pName = p.getName();
        if (p.isOp()) {
            return true;
        }
        if (!Island.checkIslandAt(loc)) {
            return true;
        }
        IslandData pd = Island.GetIslandAt(loc);
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
        if (msg != null) {
            p.sendMessage(msg);
        }
        return false;
    }

    public static IslandData GetIslandAt(Location loc) {
        if (!Island.checkIslandAt(loc)) {
            return null;
        }
        int iKey = Island.generateIslandKey(loc);
        IslandData res = ASkyBlock.get().getDatabase().getIslandById(iKey);
        if (res == null) {
            return null;
        }
        if (res.owner == null) {
            return null;
        }
        return res;
    }

    public static void islandInfo(Player p, Location loc) {
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        if (!checkIslandAt(loc)) {
            p.sendMessage(getMsg("level_error"));
            return;
        }
        final IslandData pd = GetIslandAt(loc);
        if (pd == null) {
            p.sendMessage(TextFormat.LIGHT_PURPLE + getMsg("no_claim"));
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

    public static String GetFlagString(final boolean b) {
        if (b) {
            return TextFormat.RED + "false";
        }
        return TextFormat.GREEN + "true";
    }

    private static void initChest(Level lvl, int x, int y, int z, Player p) {
        // It works!
        lvl.setBlockIdAt(x, y, z, Block.CHEST);
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<>("Items"))
                .putString("id", BlockEntity.CHEST)
                .putInt("x", x)
                .putInt("y", y)
                .putInt("z", z);

        BlockEntity.createBlockEntity(BlockEntity.CHEST, p.chunk, nbt);
        BlockEntityChest e = new BlockEntityChest(p.chunk, nbt);
        // Items
        Map<Integer, Item> items = new HashMap<>();
        items.put(0, Item.get(Item.ICE, 0, 2));
        items.put(1, Item.get(Item.BUCKET, 10, 1));
        items.put(2, Item.get(Item.BONE, 0, 2));
        items.put(3, Item.get(Item.SUGARCANE, 0, 1));
        items.put(4, Item.get(Item.RED_MUSHROOM, 0, 1));
        items.put(5, Item.get(Item.BROWN_MUSHROOM, 0, 2));
        items.put(6, Item.get(Item.PUMPKIN_SEEDS, 0, 2));
        items.put(7, Item.get(Item.MELON, 0, 1));
        items.put(8, Item.get(Item.SAPLING, 0, 1));
        items.put(9, Item.get(Item.STRING, 0, 12));
        items.put(10, Item.get(Item.POISONOUS_POTATO, 0, 32));
        e.getInventory().setContents(items);
    }

    private static void initChest(Location loc, Player p) {
        Level level = loc.getLevel();
        int x = loc.getFloorX();
        int y = loc.getFloorY();
        int z = loc.getFloorZ();
        Island.initChest(level, x + 1, y, z, p);
    }

    //todo Schematic 200x200 islands
    public static void kickPlayerByAdmin(CommandSender sender, String arg) {
        Player p = Server.getInstance().getPlayer(arg);
        if (p == null) {
            sender.sendMessage(getMsg("player_error").replace("[player]", arg));
            return;
        }
        Location locVict = p.getLocation();
        for (String lvl : ASkyBlock.get().level) {
            if (!locVict.getLevel().getName().equalsIgnoreCase(lvl)) {
                sender.sendMessage(getMsg("player_error").replace("[player]", arg));
                return;
            }
        }
        sender.sendMessage(getMsg("kick").replace("[player]", arg));
        p.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
    }

    private static String getMsg(String key) {
        return ASkyBlock.get().getMsg(key);
    }

    private static String getPrefix() {
        return ASkyBlock.get().getPrefix();
    }

    public static void teleportPlayer(Player p, String arg) {
        IslandData pd = ASkyBlock.get().getDatabase().getIsland(arg);
        if (pd.owner != null) {
            p.sendMessage(getMsg("player_error").replace("[player]", arg));
            return;
        }
        // TO-DO support homes!
        Level lvl = ASkyBlock.get().getServer().getLevelByName(pd.levelName);
        p.teleport(new Location(pd.X, pd.floor_y, pd.Z, lvl));
    }

    public static void setHomeLocation(Player p, Location lPlusOne, int number) {
        ASConnection db = ASkyBlock.get().getDatabase();
        Location loc = lPlusOne;
        db.setPosition(loc, number);
    }

}
