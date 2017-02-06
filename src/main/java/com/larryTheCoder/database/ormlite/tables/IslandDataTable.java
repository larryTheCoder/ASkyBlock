/*
 * Copyright (C) 2017 larryTheCoder
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
package com.larryTheCoder.database.ormlite.tables;

import cn.nukkit.level.Position;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.larryTheCoder.storage.IslandData;

/**
 *
 * @author larryTheCoder
 */
@DatabaseTable(tableName = "island")
public class IslandDataTable {

    @DatabaseField(canBeNull = false, columnName = "id", generatedId = true)
    int id;
    @DatabaseField(canBeNull = false, columnName = "islandId")
    int islandId;
    @DatabaseField(canBeNull = false, columnName = "x")
    int x;
    @DatabaseField(canBeNull = false, columnName = "y")
    int y;
    @DatabaseField(canBeNull = false, columnName = "z")
    int z;
    @DatabaseField(canBeNull = false, columnName = "owner")
    String owner;
    @DatabaseField(canBeNull = false, columnName = "name")
    String name;
    @DatabaseField(canBeNull = false, columnName = "world")
    String world;
    @DatabaseField(canBeNull = false, columnName = "biome")
    String biome;
    @DatabaseField(canBeNull = false, columnName = "locked")
    boolean locked;

    IslandDataTable() {
    }

    public IslandDataTable(IslandData pd) {
        this.world = pd.levelName;
        this.x = pd.X;
        this.y = pd.floor_y;
        this.z = pd.Z;
        this.name = pd.name;
        this.owner = pd.owner;
        this.biome = pd.biome;
        this.id = pd.id;
        this.islandId = pd.islandId;
        this.locked = pd.locked;
    }

    public IslandDataTable(IslandData pd, Position pos) {
        this.world = pd.levelName;
        this.x = pos.getFloorX();
        this.y = pos.getFloorY();
        this.z = pos.getFloorZ();
        this.name = pd.name;
        this.owner = pd.owner;
        this.biome = pd.biome;
        this.id = pd.id;
        this.islandId = pd.islandId;
        this.locked = pd.locked;
    }

    public void saveIslandData(IslandData pd) {
        this.world = pd.levelName;
        this.x = pd.X;
        this.y = pd.floor_y;
        this.z = pd.Z;
        this.name = pd.name;
        this.owner = pd.owner;
        this.biome = pd.biome;
        this.id = pd.id;
        this.islandId = pd.islandId;
        this.locked = pd.locked;
    }

    public IslandData toIsland() {
        return new IslandData(world, x, y, z, name, owner, biome, id, islandId, locked);
    }
}
