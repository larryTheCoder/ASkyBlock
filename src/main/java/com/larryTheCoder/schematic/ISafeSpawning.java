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
package com.larryTheCoder.schematic;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;

/**
 * This is the class where all THE SAFE SPOT TELEPORT goes This will never
 * return null. might be lagging your server but for the first time use only
 *
 * @author larryTheCoder
 */
public class ISafeSpawning {

    public Schematic schema = null;
    public short blocks;
    public int x;
    public int y;
    public int z;
    public Level level;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ISafeSpawning(Schematic main, short blocks, int x, int y, int z, String level) {
        this.schema = main;
        this.blocks = blocks;
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = main.plugin.getServer().getLevelByName(level);
        this.calculateSafeSpot();
    }
    
    public void calculateSafeSpot() {
        // Best!
        if(isSafeSpot()){
            schema.spot.put(schema.spot.size() + 1, new Position(x, y, z, level));            
        }
    }
    
    public boolean isSafeSpot(){
        switch (blocks) {
            // Safe
            case Block.GRASS:
                return true;            
        }
        return false;
    }
}
