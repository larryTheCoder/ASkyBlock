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
package com.larryTheCoder.panels;

import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.generator.biome.Biome;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.schematic.SchematicHandler;

import java.util.ArrayList;

import static cn.nukkit.level.generator.biome.Biome.*;

/**
 * Panel class for MCPE 1.2
 */
public class IslandPanel extends FormWindowCustom implements Listener {

    private Player player;
    private SchematicHandler bindTo;

    public IslandPanel() {
        super("§aIsland Menu");
        this.bindTo = ASkyBlock.schematics;

        this.showInformation();
    }

    public void showInformation() {
        // Show the elements for Panel 1.2
        this.addElement(new ElementLabel("§aWelcome to the Island Panel. Please fill in these forms. " +
            "If you are done, close this form and you will be transfered into confirmation form."));
        this.addElement(new ElementInput("Your home name", "", "Home sweet Home"));

        if (!bindTo.isUseDefaultGeneration()) {
            this.addElement(new ElementDropdown("Island Templates", bindTo.getSchemaList(), bindTo.getDefaultIsland()));
        }

        this.addElement(new ElementLabel("§aThese are your island settings."));

        this.addElement(new ElementToggle("Locked", false));
        this.addElement(new ElementDropdown("Biome type", getBiomes(), 1));
        this.addElement(new ElementToggle("", true));

    }

    public ArrayList<String> getBiomes() {
        ArrayList<String> mojangFace = new ArrayList<>();

        mojangFace.add(Biome.getBiome(OCEAN).getName());
        mojangFace.add(Biome.getBiome(PLAINS).getName());
        mojangFace.add(Biome.getBiome(DESERT).getName());
        mojangFace.add(Biome.getBiome(MOUNTAINS).getName());
        mojangFace.add(Biome.getBiome(FOREST).getName());
        mojangFace.add(Biome.getBiome(TAIGA).getName());
        mojangFace.add(Biome.getBiome(SWAMP).getName());
        mojangFace.add(Biome.getBiome(RIVER).getName());
        mojangFace.add(Biome.getBiome(ICE_PLAINS).getName());
        mojangFace.add(Biome.getBiome(SMALL_MOUNTAINS).getName());
        mojangFace.add(Biome.getBiome(BIRCH_FOREST).getName());

        mojangFace.add(Biome.getBiome(JUNGLE).getName());
        mojangFace.add(Biome.getBiome(ROOFED_FOREST).getName());
        mojangFace.add(Biome.getBiome(ROOFED_FOREST_M).getName());
        mojangFace.add(Biome.getBiome(MUSHROOM_ISLAND).getName());
        mojangFace.add(Biome.getBiome(SAVANNA).getName());

        mojangFace.add(Biome.getBiome(BEACH).getName());

        return mojangFace;
    }
}
