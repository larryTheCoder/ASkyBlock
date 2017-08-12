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
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseData;
import cn.nukkit.form.response.FormResponseSimple;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plugin Panel controller class
 * <p>
 * Used to interface the player easier than before
 */
public class Panel implements Listener {

    private final ASkyBlock plugin;

    // Confirmation panels
    private Map<Player, FactionPanel> factionPanelData = new HashMap<>();
    private Map<Player, SchematicPanelData> schematicPanelData = new HashMap<>();

    public Panel(ASkyBlock plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespondForm(PlayerFormRespondedEvent event) {
        Player p = event.getPlayer();
        if (event.getWindow() instanceof FactionPanel) {
            FactionPanel panel = (FactionPanel) event.getWindow();
            // Todo
        } else if (event.getWindow() instanceof ConfirmationPanel) {
            // Cancelled
            boolean cancelled = false;
            if (event.getWindow().wasClosed()) {
                cancelled = true;
            }
            switch (((ConfirmationPanel) event.getWindow()).type) {
                case TYPE_ISLAND:
                    if (cancelled && schematicPanelData.get(p) != null) {
                        schematicPanelData.remove(p);
                        p.sendMessage(plugin.getPrefix() + "§cYou just cancelled your create island confirmation.");
                        return;
                    }
                    // 0 = Yes, 1 = No, 2 = Retry
                    switch (((FormResponseSimple) event.getWindow().getResponse()).getClickedButtonId()) {
                        case 0:
                            SchematicPanelData data = schematicPanelData.get(p);
                            plugin.getIsland().createIsland(p, data.islandTemplate, data.islandName);
                            schematicPanelData.remove(p);
                        case 1:
                            p.sendMessage(plugin.getPrefix() + "§cYou just cancelled your create island confirmation.");
                            schematicPanelData.remove(p);
                        case 2:
                            this.addIslandFormOverlay(p);
                            break;
                        default:
                            Utils.send("&cPotential hacker detected " + p.getName() + " Reason: Unknown form of Button Element!");
                            break;
                    }
            }
        } else if (event.getWindow() instanceof IslandPanel) {
            // Check if the player closed this form
            if (event.getWindow().wasClosed()) {
                // Check if the form were filled or not
                if (schematicPanelData.get(p) != null) {
                    sendConfirmation(p, "Are your sure to create island with these changes? Close this form to cancel this confirmation", ConfirmationType.TYPE_ISLAND);
                }
                return;
            }
            // Panel Data : Used to save island Form
            SchematicPanelData panelData = new SchematicPanelData();

            IslandPanel panel = (IslandPanel) event.getWindow();
            // Get the response form from player
            FormResponseCustom response = panel.getResponse();

            // The input respond
            String islandName = response.getInputResponse(1);

            // The island schematic ID respond
            int id = -1;
            if (!ASkyBlock.schematics.isUseDefaultGeneration()) {
                FormResponseData form = response.getDropdownResponse(2); // Dropdown respond

                String schematicType = form.getElementContent();

                id = ASkyBlock.schematics.getSchemaId(schematicType);
            }

            panelData.islandTemplate = id;
            panelData.islandName = islandName;

            // If the data already exists then replace them (By using NO on confirmation Panel)
            if (schematicPanelData.get(p) != null) {
                schematicPanelData.replace(p, panelData);
            } else {
                schematicPanelData.put(p, panelData);
            }
        } else if (event.getWindow() instanceof ChallengesPanel) {
            ChallengesPanel panel = (ChallengesPanel) event.getWindow();
            if (panel.wasClosed()) {
                return;
            }

            FormResponseSimple response = panel.getResponse();
            String responseType = response.getClickedButton().text;
            plugin.getServer().dispatchCommand(p, "c complete " + responseType);

        }
    }

    public void addChallengesFormOverlay(Player player) {
        List<String> names = new ArrayList<>();
        for (String types : plugin.getChallenges().getChallengeConfig().getSection("challenges.challengeList").getKeys(false)) {
            names.add(types);
        }

        ChallengesPanel panel = new ChallengesPanel(names);
        player.showFormWindow(panel);
    }

    public void addIslandFormOverlay(Player player) {
        IslandPanel panel = new IslandPanel();
        player.showFormWindow(panel);
    }

    private void sendConfirmation(Player player, String content, ConfirmationType type) {
        player.showFormWindow(new ConfirmationPanel(player, content, type));
    }

    enum ConfirmationType {
        TYPE_FACTION,
        TYPE_ISLAND
    }

    class SchematicPanelData {
        public String islandName = "";
        public int islandTemplate = -1;
    }
}
