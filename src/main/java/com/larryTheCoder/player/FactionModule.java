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
 *
 */
package com.larryTheCoder.player;

import cn.nukkit.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FactionModule {

    public UUID factionUUID;

    public int homeX = 0;
    public int homeY = 0;
    public int homeZ = 0;

    public String owner;
    public List<String> members;
    public List<UUID> factionAlly;

    public String name;
    public int factionLevel;

    public FactionModule(Player owner, ArrayList<String> members) {
        this.owner = owner.getName().toLowerCase();
        this.members = members;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<UUID> getFactionAlly() {
        return factionAlly;
    }

    // public
}
