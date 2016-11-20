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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author larryTheCoder
 */
@SuppressWarnings("serial")
public class IslandData implements Serializable {
    public String owner;
    public ArrayList<String> members;
    public long msClaimed;
    public boolean allowWaterFlow = false;
    public boolean allowPistons = false;
    public boolean allowTNT = false;
    public boolean allowPVP = false;
    public boolean allowTeleportIn = true;
    public int x;
    public int y;
    public int z;
}
