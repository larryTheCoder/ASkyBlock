/*
 * Copyright (C) 2017 Amir Muazzam
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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.larryTheCoder.utils.Utils;
import java.util.List;

/**
 *
 * @author larryTheHarry
 */
@DatabaseTable(tableName = "worlds")
public class WorldDataTable {

    @DatabaseField(canBeNull = true, columnName = "world")
    String world;

    WorldDataTable() {
    }

    public WorldDataTable(List nb) {
        world = Utils.arrayToString(nb);
    }
    
    public List<String> toArray(){
        return Utils.stringToArray(world, ", ");
    }

}