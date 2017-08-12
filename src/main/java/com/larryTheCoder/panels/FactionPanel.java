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
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.player.FactionModule;

public class FactionPanel extends FormWindowCustom implements Listener {

    public static int PANEL_ID;

    public Player ownerFaction;
    public FactionModule module;

    public FactionPanel(Player owner, FactionModule module) {
        super("SkyBlock Team-Settings");
        this.ownerFaction = owner;
        this.module = module;

        ASkyBlock.get().getServer().getPluginManager().registerEvents(this, ASkyBlock.get());

        this.showInformation();
    }

    public void showInformation() {
        this.addElement(new ElementLabel("§aWelcome to the Schematic Panel. What can we do for help?"));

        this.addElement(new ElementInput("Rename your Team", module.name));
        this.addElement(new ElementInput("Add your ally Team"));
        this.addElement(new ElementInput("Add new members to your Team"));

        ownerFaction.showFormWindow(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespondForm(PlayerFormRespondedEvent event) {
        if ((event.wasClosed() || event.getResponse() == null) && event.getResponse().equals(this)) {
            closed = true;
            return;
        }

        // Sometimes, there a lot of players in server making its harder to decide which
        // Player are opening Panel. Using Player UUID will making this easier to decide
        if (event.getResponse().equals(this)) {
            // Get the response form from player
            FormResponseCustom response = getResponse();

            String renameTeam = response.getInputResponse(1);
            String addAlly = response.getInputResponse(2);
            String addMembers = response.getInputResponse(3);

            //float completed =

        }
    }


}
