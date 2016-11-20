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

package larryTheCoder.database;

import cn.nukkit.level.Location;
import larryTheCoder.island.IslandData;

/**
 * @author larryTheCoder
 */
public interface Database {
    
    /**
     * Save the player island 
     *     
     * @param pd        - The Island Database
     * @return boolean
     */    
    boolean saveIsland(IslandData pd);
    
    /**
     * Get the player island information 
     *     
     * @param pd        - The Island Database
     * @return boolean
     */    
    IslandData getIsland(String pd);
    
    /**
     * Get the player island information by Owner
     *     
     * @param owner        - The Island Database
     * @param levelName    - The Island level
     * @return boolean
     */     
    IslandData getIslandByOwner(String owner, String levelName);
    
    /**
     * Teleport the Owner island
     *     
     * @param owner        - The Island Database
     * @param levelName    - The Island level
     * @return boolean
     */
    Location teleportToIsland(String owner, String levelName);
    
    boolean isEnabled();
    
    /**
     * ZZZZZZZzzz...
     */
    void close();
    
}
