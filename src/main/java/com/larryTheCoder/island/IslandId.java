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

package com.larryTheCoder.island;

/**
 * @author larryTheCoder
 */
public class IslandId {
    /**
     * x value
     */
    public int x;
    /**
     * y value
     */
    public int y;
    private int hash;
    
    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    public IslandId(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     *
     * @return null if the string is invalid
     */
    public static IslandId fromString(String string) {
        if (string == null) {
            return null;
        }
        String[] parts = string.split(";");
        if (parts.length < 2) {
            return null;
        }
        int x;
        int y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ignored) {
            return null;
        }
        return new IslandId(x, y);
    }

    /**
     * Get the PlotId from the HashCode<br>
     * Note: Only accurate for small x,z values (short)
     * @param hash
     * @return
     */
    public static IslandId unpair(int hash) {
        return new IslandId(hash >> 16, hash & 0xFFFF);
    }

    /**
     * Get the PlotId in a relative direction
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * @param direction
     * @return PlotId
     */
    public IslandId getRelative(int direction) {
        switch (direction) {
            case 0:
                return new IslandId(this.x, this.y - 1);
            case 1:
                return new IslandId(this.x + 1, this.y);
            case 2:
                return new IslandId(this.x, this.y + 1);
            case 3:
                return new IslandId(this.x - 1, this.y);
        }
        return this;
    }

    /**
     * Get the PlotId in a relative location
     * @param x
     * @param y
     * @return PlotId
     */
    public IslandId getRelative(int x, int y) {
        return new IslandId(this.x + x, this.y + y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IslandId other = (IslandId) obj;
        return this.x == other.x && this.y == other.y;
    }

    /**
     * e.g.
     * 5;-6
     * @return
     */
    @Override
    public String toString() {
        return this.x + ";" + this.y;
    }

    /**
     * The PlotId object caches the hashcode for faster mapping/fetching/sorting<br>
     *     - Recalculation is required if the x/y values change
     * TODO maybe make x/y values private and add this to the mutators
     */
    public void recalculateHash() {
        this.hash = 0;
        hashCode();
    }
    
    @Override
    public int hashCode() {
        if (this.hash == 0) {
            this.hash = (this.x << 16) | (this.y & 0xFFFF);
        }
        return this.hash;
    }
}

