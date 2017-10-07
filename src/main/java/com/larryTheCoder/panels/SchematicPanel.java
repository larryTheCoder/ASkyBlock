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
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.schematic.SchematicHandler;

/**
 * Panel class for MCPE 1.2
 */
public class SchematicPanel extends FormWindowCustom implements Listener {

    private Player player;
    private ASkyBlock plugin;
    private SchematicHandler bindTo;
    private boolean closed = false;

    public SchematicPanel(Player player, ASkyBlock plugin, SchematicHandler handling) {
        super("Schematic Menu");
        this.player = player;
        this.bindTo = handling;
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.showInformation();
    }

    public void showInformation() {
        // Show the elements for Panel 1.2
        this.addElement(new ElementLabel("Here you can pick which of the schematic templates. Pick a great ones"));
        this.addElement(new ElementDropdown("Choose one...", bindTo.getSchemaList(), bindTo.getDefaultIsland()));

        player.showFormWindow(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespondForm(PlayerFormRespondedEvent event) {
        if (event.wasClosed() || event.getResponse() == null) {
            closed = true;
            return;
        }

        // Sometimes, there a lot of players in server making its harder to decide which
        // Player are opening Panel. Using Player UUID will making this easier to decide
        if (event.getResponse().equals(this)) {
            FormResponseCustom response = getResponse();
            response.getStepSliderResponse(1); // Step slider
        }
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public int hashCode() {
        return player.getUniqueId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }
}
