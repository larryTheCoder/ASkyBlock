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
import cn.nukkit.form.element.*;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseData;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.level.generator.biome.Biome;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.schematic.SchematicHandler;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.task.UpdateBiomeTask;
import com.larryTheCoder.utils.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.nukkit.level.generator.biome.Biome.*;

/**
 * Plugin Panel controller class
 * <p>
 * Used to interface the player easier than before
 */
public class Panel implements Listener {

    private final ASkyBlock plugin;

    // Confirmation panels
    private Map<Integer, PanelType> panelDataId = new HashMap<>();
    private Map<Player, Integer> mapIslandId = new HashMap<>();

    public Panel(ASkyBlock plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerRespondForm(PlayerFormRespondedEvent event) {
        Player p = event.getPlayer();
        int formId = event.getFormID();
        PanelType type = panelDataId.get(formId);

        switch (type) {
            // island features
            case TYPE_ISLAND:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowCustom panelIsland = (FormWindowCustom) event.getWindow();
                // Get the response form from player
                FormResponseCustom response = panelIsland.getResponse();

                // The input respond
                int responseId = 1;
                String islandName = response.getInputResponse(responseId++);

                String worldName = response.getDropdownResponse(responseId++).getElementContent(); // Dropdown respond

                // 6 - 5
                // The island schematic ID respond
                int id = 1; // Keep this 1 so they wont be inside of my UN-FINISHED island
                if (!ASkyBlock.schematics.isUseDefaultGeneration()) {
                    FormResponseData form = response.getDropdownResponse(responseId++); // Dropdown respond

                    String schematicType = form.getElementContent();

                    id = ASkyBlock.schematics.getSchemaId(schematicType);
                }
                // Nope it just a Label
                responseId++;
                boolean locked = response.getToggleResponse(responseId++);

                Biome biomeType = Biome.getBiome(response.getDropdownResponse(responseId++).getElementContent());

                plugin.getIsland().createIsland(p, id, worldName, islandName, locked, biomeType);
                panelDataId.remove(formId);
                break;
            // Challenges data
            case TYPE_CHALLENGES:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowSimple panelChallenges = (FormWindowSimple) event.getWindow();

                FormResponseSimple responses = panelChallenges.getResponse();

                String responseType = responses.getClickedButton().getText();
                plugin.getServer().dispatchCommand(p, "c complete " + responseType);
                break;
            case TYPE_HOMES:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowSimple homePanel = (FormWindowSimple) event.getWindow();

                FormResponseSimple homeResponse = homePanel.getResponse();

                int responseHome = homeResponse.getClickedButtonId();
                p.sendMessage(plugin.getLocale(p).hangInThere);
                plugin.getGrid().homeTeleport(p, responseHome);
                break;
            case FIRST_TIME_SETTING:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowSimple firstSettingPanel = (FormWindowSimple) event.getWindow();

                FormResponseSimple firstSettingResponse = firstSettingPanel.getResponse();

                int islandId = firstSettingResponse.getClickedButtonId();
                addSettingFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), islandId));
                break;
            case SECOND_TIME_SETTING:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }
                FormWindowCustom secondTime = (FormWindowCustom) event.getWindow();
                // Get the response form from player
                FormResponseCustom settingResponse = secondTime.getResponse();

                int idea = 1;
                IslandData pd = plugin.getDatabase().getIsland(p.getName(), mapIslandId.get(p));

                boolean lock = settingResponse.getToggleResponse(idea++);
                String nameIsland = settingResponse.getInputResponse(idea++);
                Biome biome = Biome.getBiome(settingResponse.getDropdownResponse(idea++).getElementContent());
                if (pd.isLocked() != lock) {
                    pd.setLocked(lock);
                }
                if (!pd.getName().equalsIgnoreCase(nameIsland)) {
                    pd.setName(nameIsland);
                }
                if (!pd.getBiome().equalsIgnoreCase(biome.getName())) {
                    pd.setBiome(biome.getName());
                    TaskManager.runTask(new UpdateBiomeTask(plugin, pd, p));
                }
                break;
            case FIRST_TIME_DELETE:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowSimple firstTimeDelta = (FormWindowSimple) event.getWindow();

                FormResponseSimple delete = firstTimeDelta.getResponse();

                int islandUID = delete.getClickedButtonId();
                addDeleteFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), islandUID));
                break;
            case SECOND_TIME_DELETE:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getPrefix() + plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowModal modalForm = (FormWindowModal) event.getWindow();

                int idButton = mapIslandId.get(p);

                int buttonId = modalForm.getResponse().getClickedButtonId();
                if (buttonId == 0) {
                    plugin.getIsland().deleteIsland(p, plugin.getDatabase().getIsland(p.getName(), idButton));
                } else {
                    p.sendMessage(plugin.getLocale(p).deleteIslandCancelled);
                }
        }
    }

    public void addChallengesFormOverlay(Player player) {
        FormWindowSimple panelIsland = new FormWindowSimple("Challenges Menu", getLocale(player).panelChallengesHeader);

        for (String toButton : plugin.getChallenges().getChallengeConfig().getSection("challenges.challengeList").getKeys(false)) {
            panelIsland.addButton(new ElementButton(toButton));
        }

        int id = player.showFormWindow(panelIsland);
        panelDataId.put(id, PanelType.TYPE_CHALLENGES);
    }

    public void addIslandFormOverlay(Player player) {
        // First check the availability for worlds
        ArrayList<String> worldName = new ArrayList<>();
        for (String level : plugin.getLevels()) {
            List<IslandData> maxPlotsOfPlayers = plugin.getDatabase().getIslands(player.getName(), level);
            if (!maxPlotsOfPlayers.isEmpty() || Settings.maxHome >= 0 && maxPlotsOfPlayers.size() >= Settings.maxHome) {
            } else {
                worldName.add(level);
            }
        }

        // Second. Check the player permission
        // Have no permission to create island at this location
        for (String level : worldName) {
            WorldSettings settings = plugin.getSettings(level);
            if (!player.hasPermission(settings.getPermission())) {
                worldName.remove(level);
            }
        }

        if (worldName.isEmpty()) {
            player.sendMessage(plugin.getPrefix() + plugin.getLocale(player).errorMaxIsland.replace("[maxplot]", "" + Settings.maxHome));
            return;
        }

        int homes = plugin.getDatabase().getIslands(player.getName()).size();
        FormWindowCustom panelIsland = new FormWindowCustom("Island Menu");

        panelIsland.addElement(new ElementLabel(getLocale(player).panelIslandHeader));
        panelIsland.addElement(new ElementInput(getLocale(player).panelIslandHome, "", "My " + (homes + 1) + " home"));
        panelIsland.addElement(new ElementDropdown(getLocale(player).panelIslandWorld, worldName));

        SchematicHandler bindTo = ASkyBlock.schematics;
        if (!bindTo.isUseDefaultGeneration()) {
            panelIsland.addElement(new ElementDropdown(getLocale(player).panelIslandTemplate, bindTo.getSchemaList(), bindTo.getDefaultIsland()));
        }

        panelIsland.addElement(new ElementLabel(getLocale(player).panelIslandDefault));
        panelIsland.addElement(new ElementToggle("Locked", false));
        panelIsland.addElement(new ElementDropdown("Biome type", getBiomes(), 1));

        int id = player.showFormWindow(panelIsland);
        panelDataId.put(id, PanelType.TYPE_ISLAND);
    }

    public void addHomeFormOverlay(Player p) {
        ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());

        FormWindowSimple islandHome = new FormWindowSimple("Home list", getLocale(p).panelHomeHeader.replace("[function]", "teleport"));
        for (IslandData pd : listHome) {
            islandHome.addButton(new ElementButton(pd.getName()));
        }
        int id = p.showFormWindow(islandHome);
        panelDataId.put(id, PanelType.TYPE_HOMES);
    }

    public void addDeleteFormOverlay(Player p) {
        this.addDeleteFormOverlay(p, null);
    }

    public void addDeleteFormOverlay(Player p, IslandData pd) {
        if (pd == null) {
            ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());
            // Automatically show default island setting
            if (listHome.size() == 1) {
                addDeleteFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), 1));
                return;
            }

            FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "set your island settings."));
            for (IslandData pda : listHome) {
                islandHome.addButton(new ElementButton(pda.getName()));
            }

            int id = p.showFormWindow(islandHome);
            panelDataId.put(id, PanelType.FIRST_TIME_DELETE);
            return;
        }
        mapIslandId.put(p, pd.getId());

        FormWindowModal confirm = new FormWindowModal("Delete", getLocale(p).deleteIslandSure, "Delete my island", "Cancel");

        int id = p.showFormWindow(confirm);
        panelDataId.put(id, PanelType.SECOND_TIME_DELETE);
    }

    public void addSettingFormOverlay(Player p) {
        this.addSettingFormOverlay(p, null);
    }

    public void addSettingFormOverlay(Player p, IslandData pd) {
        // This is the island Form
        if (pd == null) {
            ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());
            // Automatically show default island setting
            if (listHome.size() == 1) {
                addSettingFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), 1));
                return;
            }

            FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "set your island settings."));
            for (IslandData pda : listHome) {
                islandHome.addButton(new ElementButton(pda.getName()));
            }

            int id = p.showFormWindow(islandHome);
            panelDataId.put(id, PanelType.FIRST_TIME_SETTING);
            return;
        }

        FormWindowCustom settingForm = new FormWindowCustom("" + pd.getName() + "'s Settings");

        settingForm.addElement(new ElementLabel(getLocale(p).panelSettingHeader));
        settingForm.addElement(new ElementToggle("Locked", pd.isLocked()));
        settingForm.addElement(new ElementInput("Island Name", "", pd.getName()));
        settingForm.addElement(new ElementDropdown("Biome type", getBiomes(), 1));
        mapIslandId.put(p, pd.getId());

        int id = p.showFormWindow(settingForm);
        panelDataId.put(id, PanelType.SECOND_TIME_SETTING);
        return;
    }

    public ASlocales getLocale(Player p) {
        return plugin.getLocale(p);
    }

    private ArrayList<String> getBiomes() {
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

    enum PanelType {
        TYPE_ISLAND,
        TYPE_CHALLENGES,
        TYPE_HOMES,
        FIRST_TIME_SETTING,
        SECOND_TIME_SETTING,
        FIRST_TIME_DELETE,
        SECOND_TIME_DELETE
    }
}
