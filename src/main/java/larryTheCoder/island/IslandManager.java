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
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import java.util.ArrayList;
import larryTheCoder.ASkyBlock;
import larryTheCoder.IslandData;
import larryTheCoder.Settings;

/**
 * @author larryTheCoder
 */
public class IslandManager {

    private ASkyBlock plugin;

    public IslandManager(ASkyBlock plugin){
        this.plugin = plugin;
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
                locIsland = new Location(wx, wy, wz, 0, 0, world);
                ASkyBlock.get().getSchematic("default").pasteSchematic(locIsland, p);
                this.claim(p, locIsland);
                ASkyBlock.get().getDatabase().saveIsland(pd);
                return;
            }
            ++i;
        }
    }
    
    public boolean claim(Player p, Location loc) {
        int x = loc.getFloorX();
        x = x - x % Settings.islandSize + Settings.islandSize / 2;
        int z = loc.getFloorZ();
        z = z - z % Settings.islandSize + Settings.islandSize / 2;
        if (!Island.checkIslandAt(loc)) {
            return false;
        }
        int iKey = Island.generateIslandKey(loc);
        IslandData pd = ASkyBlock.get().getDatabase().getIslandById(iKey);
        pd.owner = p.getName();
        pd.members = new ArrayList<>();
        pd.X = x;
        pd.floor_y = loc.getFloorY();
        pd.Z = z;
        pd.levelName = loc.getLevel().getName();
        p.sendMessage(plugin.getMsg("create"));
        return true;
    }
    
}
