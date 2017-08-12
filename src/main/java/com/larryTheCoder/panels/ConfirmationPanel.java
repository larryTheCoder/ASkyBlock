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
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;

public class ConfirmationPanel extends FormWindowSimple {

    private final Player player;
    public boolean choosen = false;
    public Panel.ConfirmationType type;

    public ConfirmationPanel(Player player, String content, Panel.ConfirmationType type) {
        super("§aConfirmation Panel", content);

        this.type = type;
        this.player = player;
        this.showInformation();
    }

    private void showInformation() {
        this.addButton(new ElementButton("§aConfirm"));
        this.addButton(new ElementButton("§cCancel"));
        this.addButton(new ElementButton("§eEdit again"));

        player.showFormWindow(this);
    }

    public Panel.ConfirmationType getType() {
        return type;
    }
}
