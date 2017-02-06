/*
 * Copyright (C) 2017 larryTheHarry 
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
package com.larryTheCoder.panels;

import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.larryTheCoder.schematic.Schematic;
import com.larryTheCoder.utils.Utils;

/**
 * Schematic Panel Item
 *
 * @author larryTheCoder
 */
public class SPItem {

    private Item item;
    private List<String> description = new ArrayList<>();
    private String heading;
    private String name;
    private String perm;
    private int slot;

    /**
     * This constructor is for the default schematic/island
     *
     * @param material
     * @param name
     * @param description
     * @param slot
     */
    public SPItem(Item material, String name, String description, int slot) {
        this.slot = slot;
        this.name = name;
        this.perm = "";
        this.heading = "";
        this.description.clear();
        item = material;
        this.description.addAll(Utils.chop(TextFormat.AQUA, description, 25));
        String descd = "";
        for (String st : this.description) {
            if (!name.equals("")) {
                descd += "\n";
            }
            descd += st;
        }
        item.setCustomName(descd);
    }

    /**
     * This constructor is for schematics that will do something if chosen
     *
     * @param schematic
     * @param slot
     */
    public SPItem(Schematic schematic, int slot) {
        this.slot = slot;
        this.name = schematic.getName();
        this.perm = schematic.getPerm();
        this.heading = schematic.getHeading();
        this.description.clear();
        //this.item = schematic.getIcon();
        this.item.setDamage(schematic.getDurability());

        // This neat bit of code makes a list out of the description split by new line character
        List<String> desc = new ArrayList<>(Arrays.asList(schematic.getDescription().split("\\|")));
        this.description.addAll(desc);
        String descd = "";
        for (String st : description) {
            if (!name.equals("")) {
                descd += "\n";
            }
            descd += st;
        }
        item.setCustomName(descd);
    }

    public Item getItem() {
        return item;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the perm
     */
    public String getPerm() {
        return perm;
    }

    /**
     * @return the heading
     */
    public String getHeading() {
        return heading;
    }
}
