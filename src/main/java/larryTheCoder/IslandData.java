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

import cn.nukkit.level.generator.biome.Biome;
import java.util.ArrayList;

/**
 * Taken from PlotMe
 *
 * @author larryTheCoder
 */
public class IslandData {

    public int id;
    public String levelName;
    public int X;
    public int Z;
    public String name;
    public String owner;
    public ArrayList<String> members;
    public String biome;
    public String locked;    
    public String team;
    public int floor_y;

    public IslandData(String levelName, int X, int Z) {
        this.X = X;
        this.Z = Z;
        this.levelName = levelName;

    }

    @SuppressWarnings("AssignmentToMethodParameter")
    public IslandData(String levelName, int X, int Y, int Z, String name, String owner, String team, ArrayList<String> helpers, String biome, int id, String locked) {
        if (biome.isEmpty()) {
            biome = "PLAINS";
        }
        this.team = team;
        this.levelName = levelName;
        this.X = X;
        this.floor_y = Y;
        this.Z = Z;
        this.name = name;
        this.owner = owner;
        this.members = helpers;
        this.biome = biome;
        this.id = id;
        this.locked = locked;
    }
    
    public boolean isMember(String p){
        return members.contains(p);
    }
}
