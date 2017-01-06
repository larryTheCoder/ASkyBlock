/*
 * Copyright (C) 2016 larryTheHarry 
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
 */
package larryTheCoder.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.passive.EntityCow;
import cn.nukkit.entity.passive.EntityOcelot;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.entity.passive.EntityWolf;
import cn.nukkit.level.Level;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetEntityMotionPacket;

/**
 * @author larryTheCoder
 */
public class EntityCowAI {

    public double width = 0.3;
    public int dif = 0;

    @SuppressWarnings("static-access")
    public EntityCowAI(BaseEntity ex) {
        // CowRandomWalkCalc
        ex.getServer().getScheduler().scheduleRepeatingTask(() -> {
            ex.plugin.level.stream().map((level) -> ex.getServer().getLevelByName(level)).forEach((Runnable) -> {
                for (Entity zo : Runnable.getEntities()) {
                    if ((zo.NETWORK_ID == EntityCow.NETWORK_ID) || (zo.NETWORK_ID == EntityPig.NETWORK_ID)
                            || (zo.NETWORK_ID == EntitySheep.NETWORK_ID) || (zo.NETWORK_ID == EntityOcelot.NETWORK_ID)
                            || (zo.NETWORK_ID == EntityWolf.NETWORK_ID)) {
                        if (ex.willMove(zo)) {
                            if (ex.getAI("Cow").get(zo.getId()).isEmpty()) {
                                CompoundTag nbt = new CompoundTag()
                                        .putLong("id", zo.getId())
                                        .putBoolean("isTarget", false)
                                        .putDouble("motionX", 0)
                                        .putDouble("motionY", 0)
                                        .putDouble("motionZ", 0)
                                        .putInt("heath", 10)
                                        .putInt("time", 10)
                                        .putDouble("x", 0)
                                        .putDouble("y", 0)
                                        .putDouble("z", 0)
                                        // instanceof 'oldv3' => $zo->getLocation(),
                                        .putIntArray("oldv3", new int[]{zo.getFloorX(), zo.getFloorY(), zo.getFloorZ()})
                                        .putInt("yup", 20)
                                        .putInt("up", 0)
                                        .putDouble("yup", zo.yaw)
                                        .putDouble("pitch", 0)
                                        .putString("level", zo.getLevel().getName())
                                        .putFloat("xxx", 0)
                                        .putFloat("zzz", 0)
                                        .putDouble("gotimer", 10)
                                        .putInt("swim", 0)
                                        .putDouble("jump", 0.01)
                                        .putBoolean("canJump", true)
                                        .putBoolean("drop", false)
                                        .putInt("canAttack", 0)
                                        .putBoolean("knockback", false);
                                ex.getAI("Cow").put(zo.getId(), nbt);
                                CompoundTag zom = ex.getAI("Cow").get(zo.getId());
                                zom.putDouble("x", zo.getX());
                                zom.putDouble("y", zo.getY());
                                zom.putDouble("z", zo.getZ());
                            }
                            CompoundTag zom = ex.getAI("Cow").get(zo.getId());
                            if (zom.getInt("gotimer") == 0 || zom.getInt("gotimer") == 10) {
                                NukkitRandom random = new NukkitRandom();
                                int newmx = random.nextRange(-5, 5) / 10;
                                while (Math.abs(newmx - zom.getDouble("motionX")) >= 0.7) {
                                    newmx = random.nextRange(-5, 5) / 10;
                                }
                                zom.putDouble("motionX", newmx);

                                int newmz = random.nextRange(-5, 5) / 10;
                                while (Math.abs(newmz - zom.getDouble("motionZ")) >= 0.7) {
                                    newmz = random.nextRange(-5, 5) / 10;
                                }
                                zom.putDouble("motionZ", newmz);
                            } else if (zom.getInt("gotimer") >= 20 && zom.getInt("gotimer") <= 24) {
                                zom.putDouble("motionX", 0);
                                zom.putDouble("motionZ", 0);
                            }
                            zom.putDouble("gotimer", 0.5);
                            if (zom.getDouble("gotimer") >= 22) {
                                zom.putDouble("gotimer", 0);
                            }
                        }
                    }
                }
            });
        }, 5);
        ex.getServer().getScheduler().scheduleRepeatingTask(new Runnable() {
            @Override
            public void run() {
                for (String level : ex.plugin.level) {
                    Level levelName = ex.getServer().getLevelByName(level);
                    for (Entity zo : levelName.getEntities()) {
                        if ((zo.NETWORK_ID == EntityCow.NETWORK_ID) || (zo.NETWORK_ID == EntityPig.NETWORK_ID)
                                || (zo.NETWORK_ID == EntitySheep.NETWORK_ID) || (zo.NETWORK_ID == EntityOcelot.NETWORK_ID)
                                || (zo.NETWORK_ID == EntityWolf.NETWORK_ID)) {
                            if (!ex.getAI("Cow").get(zo.getId()).isEmpty()) {
                                CompoundTag zom = ex.getAI("Cow").get(zo.getId());
                                if (zom.getInt("canAttack") != 0) {
                                    zom.putInt("canAttack", zom.getInt("canAttack") - 1);
                                }
                                boolean downly = zo.onGround;
                                // 0 = X, 1 = Y, 2 = Z
                                if (Math.abs(zo.getY() - zom.getIntArray("oldv3")[1]) == 1 && zom.getBoolean("canJump") == true) {
                                    zom.putBoolean("canJump", false);
                                    zom.putDouble("jump", 0.3);
                                } else if (zom.getDouble("jump") > 0.01) {
                                    zom.putDouble("jump", zom.getDouble("jump") - 0.1);
                                } else {
                                    zom.putDouble("jump", 0);
                                }
//                                SetEntityMotionPacket pk3 = new SetEntityMotionPacket();
//                                pk3.entities = zo.getId();
//                                pk3.motionX = zom.getFloat("xxx");
//                                pk3.motionY = ((float) (zom.getDouble("jump") - (downly ? 0.04 : 0)));
//                                pk3.motionZ = (float) zom.getDouble("zzz");
                            }
                        }
                    }
                }
            }

        }, 10);
    }
}
