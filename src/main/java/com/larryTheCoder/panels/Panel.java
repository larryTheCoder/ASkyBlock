/*
 * Copyright (C) 2016-2018 Adam Matthew
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
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.schematic.SchematicHandler;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.IslandSettings;
import com.larryTheCoder.storage.SettingsFlag;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.utils.Settings;

import java.util.*;


/**
 * Plugin Panel controller class
 * <p>
 * Used to interface the player easier than before.
 * No getPrefix() Prefix used in this class. Interface made easy
 */
public class Panel implements Listener {

    private final ASkyBlock plugin;

    // Confirmation panels
    private final Map<Integer, PanelType> panelDataId = new HashMap<>();
    private final Map<Player, Integer> mapIslandId = new HashMap<>();
    private final Map<Player, Map<String, String>> challengeReorder = new HashMap<>();

    public Panel(ASkyBlock plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerRespondForm(PlayerFormRespondedEvent event) {
        // Check if the response was null
        if (event.getResponse() == null) {
            return;
        }
        Player p = event.getPlayer();
        int formId = event.getFormID();
        // Check if there is data in list
        // Otherwise there is another form running
        if (!panelDataId.containsKey(formId)) {
            return;
        }
        PanelType type = panelDataId.get(formId);

        switch (type) {
            // island features
            case TYPE_ISLAND:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowCustom windowCustom = (FormWindowCustom) event.getWindow();
                // Get the response form from player
                FormResponseCustom response = windowCustom.getResponse();

                // The input respond
                int responseId = 1;
                String islandName = response.getInputResponse(responseId++);

                String worldName = response.getDropdownResponse(responseId++).getElementContent(); // Dropdown respond

                // 6 - 5
                // The island schematic ID respond
                int id = 1;
                if (!ASkyBlock.get().getSchematics().isUseDefaultGeneration()) {
                    FormResponseData form = response.getDropdownResponse(responseId++); // Dropdown respond

                    String schematicType = form.getElementContent();

                    id = ASkyBlock.get().getSchematics().getSchemaId(schematicType);
                }
                // Nope it just a Label
                responseId++;
                boolean locked = response.getToggleResponse(responseId++);

                boolean teleport = response.getToggleResponse(responseId);

                plugin.getIsland().createIsland(p, id, worldName, islandName, locked, EnumBiome.PLAINS, teleport);
                break;
            // Challenges data
            case TYPE_CHALLENGES:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowSimple windowSimple = (FormWindowSimple) event.getWindow();

                FormResponseSimple responsesSimple = windowSimple.getResponse();

                String responseType = responsesSimple.getClickedButton().getText();
                Map<String, String> map = challengeReorder.get(p);
                String challenge = map.get(responseType);
                plugin.getServer().dispatchCommand(p, "c complete " + challenge);
                break;
            case TYPE_CHALLENGES_SEARCH:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                responsesSimple = windowSimple.getResponse();

                String role = TextFormat.clean(responsesSimple.getClickedButton().getText());
                if (plugin.getChallenges().isLevelAvailable(p, role)) {
                    showChallengeType(p, role);
                } else {
                    sendChallengeError(p);
                }
                break;
            case TYPE_HOMES:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                responsesSimple = windowSimple.getResponse();

                int responseHome = responsesSimple.getClickedButtonId();
                p.sendMessage(plugin.getLocale(p).hangInThere);
                plugin.getGrid().homeTeleport(p, responseHome + 1);
                break;
            case FIRST_TIME_PROTECTION:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                responsesSimple = windowSimple.getResponse();

                int islandIde = responsesSimple.getClickedButtonId();
                addProtectionOverlay(p, plugin.getDatabase().getIsland(p.getName(), islandIde + 1));
                break;
            case FIRST_TIME_SETTING:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                responsesSimple = windowSimple.getResponse();

                int islandId = responsesSimple.getClickedButtonId();
                addSettingFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), islandId + 1));
                break;
            case SECOND_TIME_SETTING:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }
                windowCustom = (FormWindowCustom) event.getWindow();
                // Get the response form from player
                response = windowCustom.getResponse();

                int idea = 1;
                IslandData pd = plugin.getDatabase().getIsland(p.getName(), mapIslandId.get(p));
                if (pd == null) {
                    p.sendMessage(plugin.getLocale(p).errorResponseUnknown);
                    break;
                }
                boolean lock = response.getToggleResponse(idea++);
                String nameIsland = response.getInputResponse(idea);
                if (pd.isLocked() != lock) {
                    pd.setLocked(lock);
                }
                if (!pd.getName().equalsIgnoreCase(nameIsland)) {
                    pd.setName(nameIsland);
                }
                plugin.getDatabase().saveIsland(pd);
                break;
            case FIRST_TIME_DELETE:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                FormResponseSimple delete = windowSimple.getResponse();

                String islandUID = delete.getClickedButton().getText();
                addDeleteFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), islandUID));
                break;
            case SECOND_TIME_DELETE:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
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
                break;
            case SECOND_TIME_PROTECTION:
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                IslandData pd3 = plugin.getDatabase().getIsland(p.getName(), mapIslandId.get(p));
                if (pd3 == null) {
                    return;
                }

                IslandSettings pd4 = pd3.getIgsSettings();

                windowCustom = (FormWindowCustom) event.getWindow();
                int idSc = 1;
                int settingsId = 1;
                for (Element element : windowCustom.getElements()) {
                    if (!(element instanceof ElementToggle)) {
                        continue;
                    }

                    SettingsFlag flag = SettingsFlag.getFlag(settingsId);
                    if (flag != null) {
                        boolean respond = windowCustom.getResponse().getToggleResponse(idSc);
                        pd4.setIgsFlag(flag, respond);
                        idSc++;
                        settingsId++;
                    }
                }

                plugin.getDatabase().saveIsland(pd3);
                break;
        }

        panelDataId.remove(formId);
    }

    private void sendChallengeError(Player player) {
        FormWindowModal confirm = new FormWindowModal("Error", "", "Okay", "Cancel");
        confirm.setContent("You haven't finished all the challenges for the first level yet, make sure your completed the first level to move on the other level.");

        player.showFormWindow(confirm);
    }

    public void addChallengesForm(Player player) {
        FormWindowSimple panelIsland = new FormWindowSimple("Quest Entries", "§aChoose your desired levels. Each levels contains a quests. Make sure you past all the quest in the level to go on into the next one!");

        for (String levels : Settings.challengeLevels) {
            if (plugin.getChallenges().isLevelAvailable(player, levels)) {
                panelIsland.addButton(new ElementButton(levels));
            } else {
                panelIsland.addButton(new ElementButton(TextFormat.RED + levels));
            }
        }

        int id = player.showFormWindow(panelIsland);
        panelDataId.put(id, PanelType.TYPE_CHALLENGES_SEARCH);
    }

    private void showChallengeType(Player player, String type) {
        FormWindowSimple panelIsland = new FormWindowSimple("Quest Menu for " + type, getLocale(player).panelChallengesHeader);

        HashMap<String, String> orders = new HashMap<>();
        for (Map.Entry<String, List<String>> list : plugin.getChallenges().getChallengeList().entrySet()) {
            if (!list.getKey().equalsIgnoreCase(type)) {
                continue;
            }

            Config cfg = plugin.getChallenges().getChallengeConfig();
            for (String challenge : list.getValue()) {
                String friendlyName = cfg.getString("challengeList." + challenge + ".friendlyname");
                String level = cfg.getString("challengeList." + challenge + ".type");
                if (!plugin.getChallenges().hasRequired(player, challenge, level, true)) {
                    friendlyName = TextFormat.RED + friendlyName;
                }
                panelIsland.addButton(new ElementButton(friendlyName));
                orders.put(friendlyName, challenge); // Sometimes this could fudge up
            }
        }

        int id = player.showFormWindow(panelIsland);
        panelDataId.put(id, PanelType.TYPE_CHALLENGES);
        challengeReorder.put(player, orders);
    }

    public void addIslandFormOverlay(Player player) {
        // First check the availability for worlds
        ArrayList<String> worldName = new ArrayList<>();
        List<IslandData> PlotPlayer = plugin.getDatabase().getIslands(player.getName());
        WorldSettings settings;
        for (String level : plugin.getLevels()) {
            List<IslandData> maxPlotsOfPlayers = plugin.getDatabase().getIslands(player.getName(), level);
            settings = plugin.getSettings(level);
            if (settings.getMaximumIsland() < 0 || maxPlotsOfPlayers.size() <= settings.getMaximumIsland()) {
                // Now players need this permission
                if (!player.hasPermission("is.home." + maxPlotsOfPlayers.size())) {
                    continue;
                }
                worldName.add(level);
            }
        }

        // Second. Check the player permission
        // Have no permission to create island at this location
        for (String level : worldName) {
            settings = plugin.getSettings(level);
            if (!player.hasPermission(settings.getPermission())) {
                worldName.remove(level);
            }
        }

        if (worldName.isEmpty()) {
            player.sendMessage(plugin.getLocale(player).errorMaxIsland.replace("[maxplot]", "" + PlotPlayer.size()));
            return;
        }

        int homes = plugin.getDatabase().getIslands(player.getName()).size();
        FormWindowCustom panelIsland = new FormWindowCustom("Island Menu");

        panelIsland.addElement(new ElementLabel(getLocale(player).panelIslandHeader));
        panelIsland.addElement(new ElementInput(getLocale(player).panelIslandHome, "", "Home #" + (homes + 1)));
        panelIsland.addElement(new ElementDropdown(getLocale(player).panelIslandWorld, worldName));

        SchematicHandler bindTo = ASkyBlock.get().getSchematics();
        if (!bindTo.isUseDefaultGeneration()) {
            panelIsland.addElement(new ElementDropdown(getLocale(player).panelIslandTemplate, bindTo.getSchemaList(), bindTo.getDefaultIsland() - 1));
        }

        panelIsland.addElement(new ElementLabel(getLocale(player).panelIslandDefault));
        panelIsland.addElement(new ElementToggle("Locked", false));
        panelIsland.addElement(new ElementToggle("Teleport to world", true));

        int id = player.showFormWindow(panelIsland);
        panelDataId.put(id, PanelType.TYPE_ISLAND);
    }

    public void addHomeFormOverlay(Player p) {
        ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());

        FormWindowSimple islandHome = new FormWindowSimple("Home list", getLocale(p).panelHomeHeader.replace("[function]", "§aTeleport to them"));
        for (IslandData pd : listHome) {
            islandHome.addButton(new ElementButton(pd.getName()));
        }
        int id = p.showFormWindow(islandHome);
        panelDataId.put(id, PanelType.TYPE_HOMES);
    }

    public void addDeleteFormOverlay(Player p) {
        this.addDeleteFormOverlay(p, null);
    }

    private void addDeleteFormOverlay(Player p, IslandData pd) {
        if (pd == null) {
            ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());
            // Automatically show default island setting
            if (listHome.size() == 1) {
                addDeleteFormOverlay(p, listHome.get(0));
                return;
            }

            FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "§aDelete your island."));
            for (IslandData pda : listHome) {
                islandHome.addButton(new ElementButton(pda.getName()));
            }

            int id = p.showFormWindow(islandHome);
            panelDataId.put(id, PanelType.FIRST_TIME_DELETE);
            return;
        }
        mapIslandId.put(p, pd.getId());

        FormWindowModal confirm = new FormWindowModal("Delete", getLocale(p).deleteIslandSure, "§cDelete my island", "Cancel");

        int id = p.showFormWindow(confirm);
        panelDataId.put(id, PanelType.SECOND_TIME_DELETE);
    }

    public void addProtectionOverlay(Player p) {
        this.addProtectionOverlay(p, null);
    }

    private void addProtectionOverlay(Player p, IslandData pd) {
        // This is the island Form
        if (pd == null) {
            ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());
            // Automatically show default island setting
            if (listHome.size() == 1) {
                addProtectionOverlay(p, plugin.getDatabase().getIsland(p.getName(), 1));
                return;
            }

            FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "§aSet your island settings."));
            for (IslandData pda : listHome) {
                islandHome.addButton(new ElementButton(pda.getName()));
            }

            int id = p.showFormWindow(islandHome);
            panelDataId.put(id, PanelType.FIRST_TIME_PROTECTION);
            return;
        }

        FormWindowCustom settingForm = new FormWindowCustom("" + pd.getName() + "'s Settings");

        settingForm.addElement(new ElementLabel(getLocale(p).panelProtectionHeader));

        HashMap<SettingsFlag, Boolean> settings = pd.getIgsSettings().getIgsValues();
        for (int i = 0; i < SettingsFlag.values().length; i++) {
            SettingsFlag[] set = SettingsFlag.values();
            SettingsFlag flag = set[i];
            Boolean value = settings.get(set[i]);
            settingForm.addElement(new ElementToggle(flag.getName(), value));
        }

        mapIslandId.put(p, pd.getId());
        int id = p.showFormWindow(settingForm);
        panelDataId.put(id, PanelType.SECOND_TIME_PROTECTION);
    }

    public void addSettingFormOverlay(Player p) {
        this.addSettingFormOverlay(p, null);
    }

    private void addSettingFormOverlay(Player p, IslandData pd) {
        // This is the island Form
        if (pd == null) {
            ArrayList<IslandData> listHome = plugin.getDatabase().getIslands(p.getName());
            // Automatically show default island setting
            if (listHome.size() == 1) {
                addSettingFormOverlay(p, plugin.getDatabase().getIsland(p.getName(), 1));
                return;
            }

            FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "§aSet your island settings."));
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
        settingForm.addElement(new ElementInput("Island Name", "", pd.getName())); // islandMaxNameLong
        mapIslandId.put(p, pd.getId());

        int id = p.showFormWindow(settingForm);
        panelDataId.put(id, PanelType.SECOND_TIME_SETTING);
    }

    private ASlocales getLocale(Player p) {
        return plugin.getLocale(p);
    }

    enum PanelType {
        TYPE_ISLAND,
        TYPE_CHALLENGES,
        TYPE_CHALLENGES_SEARCH,
        TYPE_HOMES,
        FIRST_TIME_SETTING,
        SECOND_TIME_SETTING,
        FIRST_TIME_DELETE,
        SECOND_TIME_DELETE,
        FIRST_TIME_PROTECTION,
        SECOND_TIME_PROTECTION
    }
}
