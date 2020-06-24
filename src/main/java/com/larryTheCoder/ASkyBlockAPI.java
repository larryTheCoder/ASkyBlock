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

import cn.nukkit.plugin.PluginBase;
import com.larryTheCoder.cache.FastCache;
import com.larryTheCoder.cache.inventory.InventorySave;
import com.larryTheCoder.database.DatabaseManager;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.island.TeleportLogic;
import com.larryTheCoder.listener.ChatHandler;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.schematic.SchematicHandler;
import com.larryTheCoder.task.LevelCalcTask;
import com.larryTheCoder.utils.integration.luckperms.Permission;
import lombok.Getter;

public class ASkyBlockAPI extends PluginBase {

    @Getter
    protected DatabaseManager database = null;
    @Getter
    protected ChatHandler chatHandler;
    @Getter
    protected InvitationHandler invitationHandler;
    @Getter
    protected IslandManager islandManager;
    @Getter
    protected GridManager grid;
    @Getter
    protected InventorySave inventory;
    @Getter
    protected TeleportLogic teleportLogic;
    @Getter
    protected Messages messages;
    @Getter
    protected ServerPanel panel;
    @Getter
    protected FastCache fastCache;
    @Getter
    protected SchematicHandler schematics;
    @Getter
    protected LevelCalcTask levelCalcThread;
    @Getter
    protected Permission permissionHandler;
}
