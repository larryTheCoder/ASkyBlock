/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.larryTheCoder;

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
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.cache.settings.IslandSettings;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.schematic.SchematicHandler;
import com.larryTheCoder.utils.SettingsFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Plugin Panel controller class
 * <p>
 * Used to interface the player easier than before.
 * No getPrefix() Prefix used in this class. Interface made easy
 *
 * @author larryTheCoder
 */
public class ServerPanel implements Listener {

    private final ASkyBlock plugin;

    // Confirmation panels
    private final Map<Integer, PanelType> panelDataId = new HashMap<>();
    private final Map<Player, String> defaultLevel = new HashMap<>();
    private final Map<Player, IslandData> mapIslandId = new HashMap<>();
    private final Map<Player, List<IslandData>> islandCache = new HashMap<>();

    public ServerPanel(ASkyBlock plugin) {
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
        PanelType type = panelDataId.remove(formId);

        switch (type) {
            // island features
            case TYPE_ISLAND:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                // MAX: 6
                // MIN: 4
                FormWindowCustom windowCustom = (FormWindowCustom) event.getWindow();
                // Get the response form from player
                FormResponseCustom response = windowCustom.getResponse();

                // The input respond
                int responseId = 1;

                // Value 1:
                String islandName = response.getInputResponse(responseId++);
                if (islandName == null || islandName.isEmpty()) {
                    Element rd = windowCustom.getElements().get(1);
                    if (rd instanceof ElementInput) islandName = ((ElementInput) rd).getDefaultText();
                }
                String worldName;

                // Value 2:
                FormResponseData temp;
                if ((temp = response.getDropdownResponse(responseId++)) == null || !plugin.getLevels().contains(temp.getElementContent())) {
                    responseId--;

                    worldName = defaultLevel.remove(p);
                } else {
                    worldName = temp.getElementContent();
                }

                // Value 3:

                int id = 1;
                if (!ASkyBlock.get().getSchematics().isUseDefaultGeneration()) {
                    FormResponseData form = response.getDropdownResponse(responseId++); // Dropdown respond

                    String schematicType = form.getElementContent();

                    id = ASkyBlock.get().getSchematics().getSchemaId(schematicType);
                }

                // Value 4:
                responseId++;

                boolean locked = response.getToggleResponse(responseId++);
                boolean teleport = response.getToggleResponse(responseId);

                plugin.getIslandManager().createIsland(p, id, worldName, islandName, locked, EnumBiome.PLAINS, teleport);
                break;
            case TYPE_HOMES:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowSimple windowSimple = (FormWindowSimple) event.getWindow();

                FormResponseSimple responsesSimple = windowSimple.getResponse();

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

                IslandData resultDelete;
                List<IslandData> data = islandCache.remove(p);
                if ((resultDelete = data.get(responsesSimple.getClickedButtonId())) != null) {
                    addProtectionOverlay(p, resultDelete);
                } else {
                    p.sendMessage(plugin.getLocale(p).errorFailedCritical);
                }
                break;
            case FIRST_TIME_SETTING:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                responsesSimple = windowSimple.getResponse();

                data = islandCache.remove(p);
                if ((resultDelete = data.get(responsesSimple.getClickedButtonId())) != null) {
                    addSettingFormOverlay(p, resultDelete);
                } else {
                    p.sendMessage(plugin.getLocale(p).errorFailedCritical);
                }
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
                IslandData pd = mapIslandId.remove(p);
                if (pd == null) {
                    p.sendMessage(plugin.getLocale(p).errorResponseUnknown);
                    break;
                }
                String nameIsland = response.getInputResponse(idea++);
                boolean lock = response.getToggleResponse(idea);
                if (pd.isLocked() != lock) {
                    pd.setLocked(lock);
                }

                if (!pd.getIslandName().equalsIgnoreCase(nameIsland) && !nameIsland.isEmpty()) {
                    pd.setIslandName(nameIsland);
                }

                pd.saveIslandData();
                break;
            case FIRST_TIME_DELETE:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                windowSimple = (FormWindowSimple) event.getWindow();

                FormResponseSimple delete = windowSimple.getResponse();

                data = islandCache.remove(p);
                if ((resultDelete = data.get(delete.getClickedButtonId())) != null) {
                    addDeleteFormOverlay(p, resultDelete);
                } else {
                    p.sendMessage(plugin.getLocale(p).errorFailedCritical);
                }
                break;
            case SECOND_TIME_DELETE:
                // Check if the player closed this form
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                FormWindowModal modalForm = (FormWindowModal) event.getWindow();

                IslandData idButton = mapIslandId.remove(p);

                int buttonId = modalForm.getResponse().getClickedButtonId();
                if (buttonId == 0) {
                    plugin.getIslandManager().deleteIsland(p, idButton);
                } else {
                    p.sendMessage(plugin.getLocale(p).deleteIslandCancelled);
                }
                break;
            case SECOND_TIME_PROTECTION:
                if (event.getWindow().wasClosed()) {
                    p.sendMessage(plugin.getLocale(p).panelCancelled);
                    break;
                }

                IslandData pd3 = mapIslandId.remove(p);
                if (pd3 == null) {
                    break;
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

                pd3.saveIslandData();
                break;
        }
    }

    public void addIslandFormOverlay(Player player) {
        // First check the availability for worlds
        ArrayList<String> worldName = plugin.getLevels();

        plugin.getFastCache().getRelations(player.getName(), relations -> {
            if (relations != null) {
                player.sendMessage(getLocale(player).errorIslandRelation);
                return;
            }

            plugin.getFastCache().getIslandsFrom(player.getName(), result -> {
                if (result == null) {
                    player.sendMessage(getLocale(player).errorFailedCritical);

                    return;
                }
                int homes = result.size(); // TODO: Check player homes

                FormWindowCustom panelIsland = new FormWindowCustom("Island Menu");

                panelIsland.addElement(new ElementLabel(getLocale(player).panelIslandHeader));
                panelIsland.addElement(new ElementInput(getLocale(player).panelIslandHome, "", "Home #" + (homes + 1)));
                if (worldName.size() > 1) {
                    panelIsland.addElement(new ElementDropdown(getLocale(player).panelIslandWorld, worldName));
                } else {
                    defaultLevel.put(player, worldName.remove(0));
                }

                SchematicHandler bindTo = ASkyBlock.get().getSchematics();
                if (!bindTo.isUseDefaultGeneration()) {
                    panelIsland.addElement(new ElementDropdown(getLocale(player).panelIslandTemplate, bindTo.getSchemaList(), bindTo.getDefaultIsland() - 1));
                }

                panelIsland.addElement(new ElementLabel(getLocale(player).panelIslandDefault));
                panelIsland.addElement(new ElementToggle("Locked", false));
                panelIsland.addElement(new ElementToggle("Teleport to world", true));

                int id = player.showFormWindow(panelIsland);
                panelDataId.put(id, PanelType.TYPE_ISLAND);
            });
        });
    }

    public void addHomeFormOverlay(Player p, List<IslandData> listHome) {
        FormWindowSimple islandHome = new FormWindowSimple("Home list", getLocale(p).panelHomeHeader.replace("[function]", "§aTeleport to them"));
        for (IslandData pd : listHome) {
            islandHome.addButton(new ElementButton(pd.getIslandName()));
        }

        int id = p.showFormWindow(islandHome);
        panelDataId.put(id, PanelType.TYPE_HOMES);
    }

    public void addDeleteFormOverlay(Player p) {
        this.addDeleteFormOverlay(p, null);
    }

    private void addDeleteFormOverlay(Player p, IslandData pd) {
        if (pd == null) {
            plugin.getFastCache().getIslandsFrom(p.getName(), listHome -> {
                if (listHome == null) {
                    p.sendMessage(plugin.getLocale(p).errorFailedCritical);
                    return;
                }

                // Automatically show default island setting
                if (listHome.size() == 1) {
                    addDeleteFormOverlay(p, listHome.get(0));
                    return;
                }

                FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "§aDelete your island."));
                for (IslandData pda : listHome) {
                    islandHome.addButton(new ElementButton(pda.getIslandName()));
                }

                islandCache.put(p, listHome);

                int id = p.showFormWindow(islandHome);
                panelDataId.put(id, PanelType.FIRST_TIME_DELETE);
            });
            return;
        }
        mapIslandId.put(p, pd);

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
            plugin.getFastCache().getIslandsFrom(p.getName(), listHome -> {
                if (listHome == null) {
                    p.sendMessage(plugin.getLocale(p).errorFailedCritical);
                    return;
                }

                // Automatically show default island setting
                if (listHome.size() == 1) {
                    addProtectionOverlay(p, listHome.get(0));
                    return;
                }

                FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "§aSet your island settings."));
                for (IslandData pda : listHome) {
                    islandHome.addButton(new ElementButton(pda.getIslandName()));
                }

                islandCache.put(p, listHome);

                int id = p.showFormWindow(islandHome);
                panelDataId.put(id, PanelType.FIRST_TIME_PROTECTION);
            });
            return;
        }

        FormWindowCustom settingForm = new FormWindowCustom("" + pd.getIslandName() + "'s Settings");

        settingForm.addElement(new ElementLabel(getLocale(p).panelProtectionHeader));

        HashMap<SettingsFlag, Boolean> settings = pd.getIgsSettings().getIgsValues();
        for (int i = 0; i < SettingsFlag.values().length; i++) {
            SettingsFlag[] set = SettingsFlag.values();
            SettingsFlag flag = set[i];
            Boolean value = settings.get(set[i]);
            settingForm.addElement(new ElementToggle(flag.getName(), value));
        }

        mapIslandId.put(p, pd);
        int id = p.showFormWindow(settingForm);
        panelDataId.put(id, PanelType.SECOND_TIME_PROTECTION);
    }

    public void addSettingFormOverlay(Player p) {
        this.addSettingFormOverlay(p, null);
    }

    private void addSettingFormOverlay(Player p, IslandData pd) {
        // This is the island Form
        if (pd == null) {
            plugin.getFastCache().getIslandsFrom(p.getName(), listHome -> {
                if (listHome == null) {
                    p.sendMessage(plugin.getLocale(p).errorFailedCritical);
                    return;
                }
                // Automatically show default island setting
                if (listHome.size() == 1) {
                    addSettingFormOverlay(p, listHome.get(0));
                    return;
                }

                FormWindowSimple islandHome = new FormWindowSimple("Choose your home", getLocale(p).panelHomeHeader.replace("[function]", "§aSet your island settings."));
                for (IslandData pda : listHome) {
                    islandHome.addButton(new ElementButton(pda.getIslandName()));
                }

                int id = p.showFormWindow(islandHome);
                panelDataId.put(id, PanelType.FIRST_TIME_SETTING);
            });
            return;
        }

        FormWindowCustom settingForm = new FormWindowCustom("" + pd.getIslandName() + "'s Settings");

        settingForm.addElement(new ElementLabel(getLocale(p).panelSettingHeader));
        settingForm.addElement(new ElementInput("Island Name", "", pd.getIslandName())); // islandMaxNameLong
        settingForm.addElement(new ElementToggle("Locked", pd.isLocked()));
        mapIslandId.put(p, pd);

        int id = p.showFormWindow(settingForm);
        panelDataId.put(id, PanelType.SECOND_TIME_SETTING);
    }

    private ASlocales getLocale(Player p) {
        return plugin.getLocale(p);
    }

    enum PanelType {
        TYPE_ISLAND,
        TYPE_HOMES,
        FIRST_TIME_SETTING,
        SECOND_TIME_SETTING,
        FIRST_TIME_DELETE,
        SECOND_TIME_DELETE,
        FIRST_TIME_PROTECTION,
        SECOND_TIME_PROTECTION
    }
}
