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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is for a control panel button that has an icon, a command to run
 * if pressed or a link to another control panel.
 *
 * @author larryTheCoder
 */
public class CPItem {

    private Item item;
    private String command;
    private String nextSection;

    /**
     * A Control Panel item
     *
     * @param material
     * @param name
     * @param command
     * @param nextSection
     */
    public CPItem(Item material, String name, String command, String nextSection) {
        this.command = command;
        this.nextSection = nextSection;
        item = material;
        Item meta = item;
        // Handle multi line names (split by |)
        List<String> desc = new ArrayList<>(Arrays.asList(name.split("\\|")));
        meta.setCustomName(desc.get(0));
        if (desc.size() > 1) {
            desc.remove(0); // Remove the name
            String descd = "";
            for (String st : desc) {
                if (!command.equals("")) {
                    descd += "\n";
                }
                descd += st;
            }
            meta.setCustomName(descd);
        }
    }

    // For warps
    public CPItem(Item itemStack, String command) {
        this.command = command;
        this.nextSection = "";
        this.item = itemStack;
    }

    public void setLore(List<String> lore) {
        Item meta = item;
        String descd = "";
        for (String st : lore) {
            if (!command.equals("")) {
                descd += "\n";
            }
            descd += st;
        }
        meta.setCustomName(descd);
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return the nextSection
     */
    public String getNextSection() {
        return nextSection;
    }

    /**
     * @param nextSection the nextSection to set
     */
    public void setNextSection(String nextSection) {
        this.nextSection = nextSection;
    }

    public Item getItem() {
        return item;
    }
}
