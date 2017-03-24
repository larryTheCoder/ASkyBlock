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
package com.larryTheCoder.storage;

/**
 * @author larryTheCoder
 */
public class IslandData implements Cloneable {

    public int islandId;
    public int id;
    public String levelName;
    public int X = 0;
    public int Y = 0;
    public int Z = 0;
    public String name;
    public String owner;
    public String biome;
    public boolean locked;
    private int ProtectionSize = 0; //Unaccessable

    public IslandData(String levelName, int X, int Z, int PSize) {
        this.X = X;
        this.Z = Z;
        this.levelName = levelName;
        this.ProtectionSize = PSize;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    public IslandData(String levelName, int X, int Y, int Z, int PSize, String name, String owner, String biome, int id, int islandId, boolean locked) {
        if (biome.isEmpty()) {
            biome = "PLAINS";
        }
        this.levelName = levelName;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.ProtectionSize = PSize;
        this.name = name;
        this.owner = owner;
        this.biome = biome;
        this.id = id;
        this.islandId = islandId;
        this.locked = locked;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    public int getMinProtectedZ() {
        return (Z - ProtectionSize / 2);
    }

    public int getMinProtectedX() {
        return (X - ProtectionSize / 2);
    }

    public int getProtectionSize() {
        return ProtectionSize;
    }
}
