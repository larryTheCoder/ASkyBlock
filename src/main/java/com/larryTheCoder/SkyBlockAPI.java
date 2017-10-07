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
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import com.larryTheCoder.command.ChallangesCMD;
import com.larryTheCoder.database.ASConnection;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.listener.ChatHandler;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.player.TeamManager;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.storage.InventorySave;
import com.larryTheCoder.storage.IslandData;

import java.util.ArrayList;

/**
 * The public API of the ASkyBlock plugin.
 * <p>
 * This API will not be changed without prior notice - allowing other plugins a
 * way to access data from the ASkyBlock plugin.
 * <p>
 * To get hold of an API object, make sure your plugin depends on ASkyBlock, and
 * then do:
 * <pre>
 *     {@code
 *      if(Server.getInstance().getPluginManager().getPlugin("ASkyBlock") == null){
 *          return;
 *      }
 *     SkyBlockAPI api = ASkyBlock.get().registerAPI(YourPlugin::instance);
 *     }
 * }</pre>
 *
 * @since v0.3.7
 */
public class SkyBlockAPI {

    public static ArrayList<PluginBase> list = new ArrayList();
    public PluginBase executor;
    public ASkyBlock plugin = ASkyBlock.get();
    // Accessibility
    public ASConnection db = null;
    public ChatHandler chatHandler;
    public InvitationHandler invitationHandler;
    public IslandManager manager;
    public GridManager grid;
    public InventorySave inventory;
    public TeamManager managers;
    public TeleportLogic teleportLogic;
    public ChallangesCMD cmds;
    public Messages msgs;
    private boolean hasPermissionToAccessAll = false;

    public SkyBlockAPI(PluginBase plugin) {
        this.executor = plugin;
        SkyBlockAPI.list.add(plugin);
        if (plugin instanceof ASkyBlock) {
            hasPermissionToAccessAll = true;
        }
    }

}
