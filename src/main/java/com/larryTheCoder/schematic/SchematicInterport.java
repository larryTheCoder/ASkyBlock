/*
 * Copyright (C) 2017 Adam Matthew
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

import cn.nukkit.Player;
import cn.nukkit.level.Position;

import java.util.List;

/**
 * Author: Adam Matthew
 * <p>
 * An abstract class represents a Schematic loader
 * You also can create an own Schematic loader, Better is greater!
 * This is only the basic stuff to generate island
 * Will be used in version: v0.5.3
 */
public abstract class SchematicInterport {

    /**
     * This method prepares to pastes a schematic.
     *
     * @param blocks The Blocks (same as data)
     * @param data   Data (damage or meta)
     */
    public abstract void handleSchematic(short[] blocks, byte[] data);

    /**
     * This method handling player island blocks-by-blocks
     *
     * @param p   The player
     * @param pos The position to pasting the blocks
     * @return True if the player island were generated|null
     */
    public abstract boolean pasteSchematic(Player p, Position pos);

    /**
     * Get the list of available islands
     *
     * @return An array of the listed blocks
     */
    public abstract List<IslandBlock> getIslandBlocks();
}
