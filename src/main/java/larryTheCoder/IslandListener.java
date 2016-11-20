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
package larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import java.util.ArrayList;
import larryTheCoder.island.Island;
import larryTheCoder.island.IslandData;

/**
 * @author larryTheCoder
 */
class IslandListener implements Listener {

    public final ASkyBlock plugin;

    public IslandListener(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (!Island.checkIslandAt(p.getLocation())) {
            return;
        }
        String msg = event.getMessage();
        String umsg = msg.toUpperCase();
        for (final String restrictedCmd : ConfigManager.whitelistedCommands) {
            if (umsg.startsWith(restrictedCmd.toUpperCase())) {
                if (!p.isOp()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getMsg("command_disabled"));
                    event.setCancelled(true);
                    return;
                }
                p.sendMessage(plugin.getPrefix() + plugin.getMsg("admin"));
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Block blk = e.getBlock();
        Location breakLoc = blk.getLocation();
        Player p = e.getPlayer();
        if (!Island.CanPlayerAccess(p, breakLoc, TextFormat.RED + " You can't break there!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Location placeLoc;
        Block blk;
        Player p = e.getPlayer();
        if (!Island.CanPlayerAccess(p, placeLoc = (blk = e.getBlock()).getLocation(), TextFormat.RED + "[SkyBlock] You can't place blocks there!")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent ev) {
        ArrayList<Block> blocksToSkip = new ArrayList<>();
        Location rootLoc = (Location) ev.getPosition();
        if (!Island.checkIslandAt(rootLoc)) {
            return;
        }
        ev.getBlockList().stream().forEach((b2) -> {
            Location loc = b2.getLocation();
            IslandData data = Island.GetIslandAt(loc);
            if (!(data == null || data.owner == null)) {
                blocksToSkip.add(b2);
            }
        });
        blocksToSkip.stream().forEach((b2) -> {
            ev.getBlockList().remove(b2);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent e) {
        Block b = e.getTo();
        Location loc = b.getLocation();
        if (!Island.checkIslandAt(loc)) {
            return;
        }
        int x = loc.getFloorX();
        int z = loc.getFloorZ();
        if (x % Island.islandSize == 0 || z % Island.islandSize == 0) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeld(PlayerItemHeldEvent e) {
        Item item = e.getItem();
        Player p = e.getPlayer();
        if (p == null) {
            return;
        }
        Location loc = p.getLocation();
        if (Island.checkIslandAt(loc)) {
            if ((item.getId()) == Item.BLAZE_ROD) {
                Island.islandInfo(p, loc);
            }
            if (!Island.CanPlayerAccess(p, loc, TextFormat.RED + "[SkyBlock] No permission here.")) {
                e.setCancelled(true);
            }
        }
    }
}
