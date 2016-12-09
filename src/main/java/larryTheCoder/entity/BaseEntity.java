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

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityCreeper;
import cn.nukkit.entity.passive.EntityChicken;
import cn.nukkit.entity.passive.EntityCow;
import cn.nukkit.entity.passive.EntityOcelot;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.entity.passive.EntityWolf;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.ListTag;
import java.util.HashMap;
import cn.nukkit.nbt.tag.FloatTag;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class BaseEntity {

    public EntityCowAI CowAI;
    public EntityCowAI PigAI;
    public EntityCowAI SheepAI;
    public EntityCowAI ChickenAI;
    public EntityCowAI PigZombieAI;

    public ASkyBlock plugin;

    // Initialize genisys AIHolder
    private HashMap<Long, CompoundTag> zombie = new HashMap<>();
    private HashMap<Long, CompoundTag> Creeper = new HashMap<>();
    private HashMap<Long, CompoundTag> Skeleton = new HashMap<>();
    private HashMap<Long, CompoundTag> Cow = new HashMap<>();
    private HashMap<Long, CompoundTag> Pig = new HashMap<>();
    private HashMap<Long, CompoundTag> Sheep = new HashMap<>();
    private HashMap<Long, CompoundTag> Chicken = new HashMap<>();
    private HashMap<Long, CompoundTag> irongolem = new HashMap<>();
    private HashMap<Long, CompoundTag> snowgolem = new HashMap<>();
    private HashMap<Long, CompoundTag> pigzombie = new HashMap<>();

    /**
     * Get the AIHolder of an Entity Default: Zombie, Creeper, Cow, Pig, Sheep,
     * Chicken
     *
     * @param key - The Entity
     * @return HashMap(Long, CompoundTag)
     */
    public HashMap<Long, CompoundTag> getAI(String key) {
        HashMap<Long, CompoundTag> target;
        switch (key.toUpperCase()) {
            case "ZOMBIE":
                target = zombie;
                break;
            case "CREEPER":
                target = Creeper;
                break;
            case "SKELETON":
                target = Skeleton;
                break;
            case "COW":
                target = Cow;
                break;
            case "PIG":
                target = Pig;
                break;
            case "SHEEP":
                target = Sheep;
                break;
            case "CHICKEN":
                target = Chicken;
                break;
            default:
                target = Chicken;
        }
        return target;
    }

    public BaseEntity(ASkyBlock plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleRepeatingTask(new Runnable() {
            @Override
            public void run() {
                HashMap<Long, CompoundTag> namedTag = new HashMap<>();
                for (Level Runnable : plugin.getServer().getLevels().values()) {
                    for (Entity entity : Runnable.getEntities()) {
                        // todo: Ask Nukkit Developer to add Moshroom Creeper Skeleton and Others Mobs
                        if (entity instanceof EntityCreeper
                                || entity instanceof EntityCow || entity instanceof EntityPig
                                || entity instanceof EntitySheep || entity instanceof EntityChicken
                                || entity instanceof EntityOcelot || entity instanceof EntityWolf) {
                            if (!entity.getViewers().isEmpty()) {
                                if (entity instanceof EntityCow) {
                                    namedTag = Cow;
                                } else if (entity instanceof EntityCreeper) {
                                    namedTag = Creeper;
                                } else if (entity instanceof EntityCow || entity instanceof EntityPig || entity instanceof EntitySheep || entity instanceof EntityOcelot) {
                                    namedTag = Cow;
                                } else if (entity instanceof EntityPig) {
                                    namedTag = Pig;
                                } else if (entity instanceof EntitySheep) {
                                    namedTag = Sheep;
                                } else if (entity instanceof EntityChicken) {
                                    namedTag = Chicken;
                                }
                                if (namedTag.get(entity.getId()) != null) {
                                    // Original Yaw
                                    double Oyaw = entity.getYaw();
                                    double yaw = namedTag.get(entity.getId()).getDouble("yaw");
                                    if (Math.abs(Oyaw - yaw) <= 180) {
                                        if (Oyaw <= yaw) {
                                            if (yaw - Oyaw <= 15) {
                                                Oyaw = yaw;
                                            } else {
                                                Oyaw += 15;
                                            }
                                        } else if (Oyaw - yaw <= 15) {
                                            Oyaw = yaw;
                                        } else {
                                            Oyaw -= 15;
                                        }
                                    } else if (Oyaw >= yaw) {
                                        if ((180 - Oyaw) + (yaw + 180) <= 15) {
                                            Oyaw = yaw;
                                        } else {
                                            Oyaw += 15;
                                            if (Oyaw >= 180) {
                                                Oyaw -= 360;
                                            }
                                        }
                                    } else if ((180 - yaw) - (Oyaw + 180) <= 15) {
                                        Oyaw = yaw;
                                    } else {
                                        Oyaw -= 15;
                                        if (Oyaw <= 180) {
                                            Oyaw += 360;
                                        }
                                    }

                                    double Opitch = entity.pitch;
                                    double pitch = namedTag.get(entity.getId()).getDouble("pitch");
                                    if (Math.abs(Opitch - pitch) <= 15) {
                                        Opitch = pitch;
                                    } else if (pitch > Opitch) {
                                        Opitch += 10;
                                    } else if (pitch < Opitch) {
                                        Opitch -= 10;
                                    }
                                    entity.setRotation(Oyaw, Opitch);
                                }
                            }

                        }
                    }
                }
            }
        }, 2);

    }

    /**
     * Create an Entity
     *
     * @param pos - Position of the spawned location
     * @param maxHealth - The maxHealth of an Entity
     * @param health - The health of current entity
     * @param p - The player
     */
    public void spawnCow(Position pos, int maxHealth, int health, Player p) {
        getCow(pos, maxHealth, health).spawnTo(p);
    }

    public Entity getCow(Position pos, int maxHealth, int health) {
        BaseFullChunk chunk = pos.level.getChunk((int) (pos.x) >> 4, (int) (pos.z) >> 4, false);
        CompoundTag nbt = this.getNBT();
        EntityCow zo = new EntityCow(chunk, nbt);
        zo.setPosition(pos);
        zo.setMaxHealth(maxHealth);
        zo.setHealth(health);
        return zo;
    }

    /**
     * @return ConpoundTag
     */
    public CompoundTag getNBT() {
        CompoundTag nbt = new CompoundTag()
                .put("pos", new ListTag<DoubleTag>("pos")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .put("Motion", new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .put("Rotation", new ListTag<FloatTag>("Motion")
                        .add(new FloatTag("", 0))
                        .add(new FloatTag("", 0))
                        .add(new FloatTag("", 0)));
        return nbt;
    }

    /**
     * @param entity The Player or the Entity
     * @return boolean
     */
    public boolean willMove(Entity entity) {
        return entity.getViewers().values().stream().anyMatch((viewers) -> (entity.distance(viewers.getLocation()) <= 32));
    }

    public Server getServer() {
        return plugin.getServer();
    }
}
