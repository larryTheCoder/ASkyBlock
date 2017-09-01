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
package com.larryTheCoder.utils;

import cn.nukkit.block.Block;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Adam Matthew
 */
public enum BlockUtil {
    ;
    private static final Collection<Integer> FLUIDS = Arrays.asList(Block.STILL_WATER, Block.WATER, Block.LAVA, Block.STILL_LAVA);

    public static boolean isBreathable(Block block) {
        return !block.isSolid() && !isFluid(block);
    }

    public static boolean isFluid(Block block) {
        return FLUIDS.contains(block.getId());
    }

    public static boolean isFluid(int type) {
        return FLUIDS.contains(type);
    }
}
