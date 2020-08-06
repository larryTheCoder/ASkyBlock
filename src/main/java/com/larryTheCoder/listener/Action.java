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

package com.larryTheCoder.listener;

import cn.nukkit.Player;
import cn.nukkit.api.API;
import cn.nukkit.level.Location;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.cache.CoopData;
import com.larryTheCoder.cache.IslandData;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.SettingsFlag;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * A simple utility abstract class that determines if the action done
 * by the player is allowed or not.
 */
@Log4j2
@API(definition = API.Definition.UNIVERSAL, usage = API.Usage.STABLE)
public abstract class Action {

    private final ASkyBlock plugin;

    public Action(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    /**
     * Determines if a location is in the island world or not or in the new
     * nether if it is activated
     *
     * @param loc Location of the entity to be checked
     * @return true if in the island world
     */
    protected boolean notInWorld(@NonNull Location loc) {
        return !ASkyBlock.get().getLevels().contains(loc.getLevel().getName());
    }

    /**
     * Action allowed in this location
     *
     * @param location The location to be checked
     * @param flag     Kind of flag to be checked
     * @return true if allowed
     */
    protected boolean actionAllowed(Location location, SettingsFlag flag) {
        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        if (island != null) {
            log.debug("DEBUG: Action is determined by settings");
            return island.getIgsSettings().getIgsFlag(flag);
        }
        log.debug("DEBUG: Action is defined by settings");
        return Settings.defaultWorldSettings.get(flag);
    }

    /**
     * Checks if action is allowed for player in location for flag
     *
     * @param player   The player or entity
     * @param location The location to be checked
     * @return true if allowed
     */
    protected boolean actionAllowed(Player player, Location location, SettingsFlag flag) {
        if (player == null) return actionAllowed(location, flag);

        // This permission bypasses protection
        if (player.isOp() || hasPermission(player, "is.mod.bypassprotect")) {
            return true;
        }

        IslandData island = plugin.getGrid().getProtectedIslandAt(location);
        CoopData pd = plugin.getFastCache().getRelations(location);
        if (island != null && (island.getIgsSettings().getIgsFlag(flag) || (pd != null && pd.isMember(player.getName())))) {
            return true;
        }

        if (island == null || island.getPlotOwner() == null) return false;
        if (island.getPlotOwner().equalsIgnoreCase(player.getName())) return true;

        return Settings.defaultWorldSettings.get(flag);
    }

    protected boolean hasPermission(Player player, String permission) {
        return plugin.getPermissionHandler().hasPermission(player, permission);
    }
}
