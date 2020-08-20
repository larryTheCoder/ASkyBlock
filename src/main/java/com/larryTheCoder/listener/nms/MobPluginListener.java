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

package com.larryTheCoder.listener.nms;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.passive.EntityAnimal;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.listener.Action;
import com.larryTheCoder.utils.SettingsFlag;
import lombok.extern.log4j.Log4j2;
import nukkitcoders.mobplugin.entities.animal.Animal;
import nukkitcoders.mobplugin.entities.monster.Monster;

@Log4j2
public class MobPluginListener extends Action implements Listener {

    public MobPluginListener(ASkyBlock plugin) {
        super(plugin);

        log.debug("Using MobPlugin");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerHitEvent(EntityDamageEvent e) {
        log.debug("DEBUG: " + e.getEventName());
        log.debug("DEBUG: NMS MobPlugin notation.");

        Entity target = e.getEntity();

        if (notInWorld(target)) return;
        if (e instanceof EntityDamageByEntityEvent) {
            // Identifier for player mobs attack.
            if (!(target instanceof Player)) {
                log.debug("Target is not a player.");

                if (target instanceof Animal) {
                    log.debug("Target is not an animal.");

                    if (actionAllowed(target.getLocation(), SettingsFlag.HURT_MOBS)) return;
                } else if (target instanceof Monster) {
                    log.debug("Target is not a monster.");

                    if (actionAllowed(target.getLocation(), SettingsFlag.HURT_MONSTERS)) return;
                }
            } else {
                log.debug("Target is a player");
                if (actionAllowed(target.getLocation(), SettingsFlag.PVP)) return;
            }

            log.debug("Target cancelled.");

            e.setCancelled();
        }
    }
}
