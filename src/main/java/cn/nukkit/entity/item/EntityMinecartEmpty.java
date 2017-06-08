/*
 * EntityMaET: Massive Entity tracking system
 * Copyright (C) 2017 larryTheCoder
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
package cn.nukkit.entity.item;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.item.EntityVehicle;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMinecart;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;

import static cn.nukkit.item.Item.*;
import static cn.nukkit.entity.Entity.*;
/**
 * Complex and more complex code by larryTheCoder
 *
 * @author larryTheCoder
 */
public abstract class EntityMinecartEmpty extends EntityVehicle {

    private boolean a;
    private String b;
    private static final int[][][] matrix = new int[][][]{{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
    private int d;
    private double e;
    private double f;
    private double g;
    private double h;
    private double i;

    public EntityMinecartEmpty(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return 84;
    }

    @Override
    public float getHeight() {
        return 0.7F; // TRUE
    }

    @Override
    public float getWidth() {
        return 0.98F; // TRUE
    }

    @Override
    protected float getDrag() {
        return 0.1f;
    }

    @Override
    protected float getGravity() {
        return 0.5f;
    }

    @Override
    public String getName() {
        return "Minecart";
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;

        if (tickDiff <= 0) {
            return false;
        }
        this.lastUpdate = currentTick;

        super.timing.startTiming();

        if (this.y < -64.0D) {
            this.kill();
        }

        boolean hasUpdate = false;

        if (!this.isAlive()) {
            // SO COMPLEX!
            hasUpdate = doComplexCode();
        }

        this.updateMovement();

        super.timing.stopTiming();

        return hasUpdate;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (super.attack(source)) {
            if (source instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) source).getDamager();
                if (damager instanceof Player) {
                    if (((Player) damager).isCreative()) {
                        this.kill();
                    }
                    if (this.getHealth() <= 0) {
                        if (((Player) damager).isSurvival()) {
                            this.level.dropItem(this, new ItemMinecart());
                        }
                        this.close();
                    }
                }
            }

            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getId();
            pk.event = this.getHealth() <= 0 ? EntityEventPacket.DEATH_ANIMATION : EntityEventPacket.HURT_ANIMATION;
            Server.broadcastPacket(this.hasSpawned.values(), pk);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        super.close();

        if (this.linkedEntity instanceof Player) {
            this.linkedEntity.riding = null;
            this.linkedEntity = null;
        }

        // I am not wrong about this 
        SmokeParticle particle = new SmokeParticle(this);
        this.level.addParticle(particle);
    }
   @Override
    protected void initEntity() {
        super.initEntity();

        this.setMaxHealth(6);
        this.setHealth(getMaxHealth());
    }
    @Override
    public boolean onInteract(Player p, Item item) {
        if (this.linkedEntity != null) {
            return false;
        }

        SetEntityLinkPacket pk;

        pk = new SetEntityLinkPacket();
        pk.rider = this.getId(); //WTF
        pk.riding = p.getId();
        pk.type = 2;
        Server.broadcastPacket(this.hasSpawned.values(), pk);

        pk = new SetEntityLinkPacket();
        pk.rider = this.getId();
        pk.riding = 0;
        pk.type = 2;
        p.dataPacket(pk);

        p.riding = this;
        this.linkedEntity = p;

        p.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);
        p.sendPopup("Sneak to get off minecart");
        return true;
    }

    // START Minecart-Server built in java
    protected void b(double d0) {
        if (this.motionX < -d0) {
            this.motionX = -d0;
        }

        if (this.motionX > d0) {
            this.motionX = d0;
        }

        if (this.motionZ < -d0) {
            this.motionZ = -d0;
        }

        if (this.motionZ > d0) {
            this.motionZ = d0;
        }

        if (this.onGround) {
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }

        this.move(this.motionX, this.motionY, this.motionZ);
        if (!this.onGround) {
            this.motionX *= 0.949999988079071D;
            this.motionY *= 0.949999988079071D;
            this.motionZ *= 0.949999988079071D;
        }
    }

    protected void a(int i, int j, int k, double d0, double d1, Block block, int l) {
        this.fallDistance = 0.0F;
        Vector3 vec3d = this.a(this.x, this.y, this.z);

        this.y = (double) j;
        boolean flag = false;
        boolean flag1 = false;

        if (block.equals(Block.POWERED_RAIL)) {
            flag = (l & 8) != 0;
            flag1 = !flag;
        }

        if (l >= 2 && l <= 5) {
            this.y = (double) (j + 1);
        }

        if (l == 2) {
            this.motionX -= d1;
        }

        if (l == 3) {
            this.motionX += d1;
        }

        if (l == 4) {
            this.motionZ += d1;
        }

        if (l == 5) {
            this.motionZ -= d1;
        }

        int[][] aint = matrix[l];
        double d2 = (double) (aint[1][0] - aint[0][0]);
        double d3 = (double) (aint[1][2] - aint[0][2]);
        double d4 = Math.sqrt(d2 * d2 + d3 * d3);
        double d5 = this.motionX * d2 + this.motionZ * d3;

        if (d5 < 0.0D) {
            d2 = -d2;
            d3 = -d3;
        }

        double d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (d6 > 2.0D) {
            d6 = 2.0D;
        }

        this.motionX = d6 * d2 / d4;
        this.motionZ = d6 * d3 / d4;
        double d7;
        double d8;
        double d9;
        double d10;

        if (this.linkedEntity != null && this.linkedEntity instanceof EntityLiving) {
            d7 = (double) ((EntityLiving) this.linkedEntity).getMovementSpeed();
            if (d7 > 0.0D) {
                d8 = -Math.sin((double) (this.linkedEntity.yaw * 3.1415927F / 180.0F));
                d9 = Math.cos((double) (this.linkedEntity.yaw * 3.1415927F / 180.0F));
                d10 = this.motionX * this.motionX + this.motionZ * this.motionZ;
                if (d10 < 0.01D) {
                    this.motionX += d8 * 0.1D;
                    this.motionZ += d9 * 0.1D;
                    flag1 = false;
                }
            }
        }

        if (flag1) {
            d7 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d7 < 0.03D) {
                this.motionX *= 0.0D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.0D;
            } else {
                this.motionX *= 0.5D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.5D;
            }
        }

        d7 = 0.0D;
        d8 = (double) i + 0.5D + (double) aint[0][0] * 0.5D;
        d9 = (double) k + 0.5D + (double) aint[0][2] * 0.5D;
        d10 = (double) i + 0.5D + (double) aint[1][0] * 0.5D;
        double d11 = (double) k + 0.5D + (double) aint[1][2] * 0.5D;

        d2 = d10 - d8;
        d3 = d11 - d9;
        double d12;
        double d13;

        if (d2 == 0.0D) {
            this.x = (double) i + 0.5D;
            d7 = this.z - (double) k;
        } else if (d3 == 0.0D) {
            this.z = (double) k + 0.5D;
            d7 = this.x - (double) i;
        } else {
            d12 = this.x - d8;
            d13 = this.z - d9;
            d7 = (d12 * d2 + d13 * d3) * 2.0D;
        }

        this.x = d8 + d2 * d7;
        this.z = d9 + d3 * d7;
        this.setPosition(new Vector3(this.x, this.y + (double) this.getHeight(), this.z));
        d12 = this.motionX;
        d13 = this.motionZ;
        if (this.linkedEntity != null) {
            d12 *= 0.75D;
            d13 *= 0.75D;
        }

        if (d12 < -d0) {
            d12 = -d0;
        }

        if (d12 > d0) {
            d12 = d0;
        }

        if (d13 < -d0) {
            d13 = -d0;
        }

        if (d13 > d0) {
            d13 = d0;
        }

        this.move(d12, 0.0D, d13);
        if (aint[0][1] != 0 && MathHelper.floor(this.x) - i == aint[0][0] && MathHelper.floor(this.z) - k == aint[0][2]) {
            this.setPosition(new Vector3(this.x, this.y + (double) aint[0][1], this.z));
        } else if (aint[1][1] != 0 && MathHelper.floor(this.x) - i == aint[1][0] && MathHelper.floor(this.z) - k == aint[1][2]) {
            this.setPosition(new Vector3(this.x, this.y + (double) aint[1][1], this.z));
        }

        Vector3 vec3d1 = this.a(this.x, this.y, this.z);

        if (vec3d1 != null && vec3d != null) {
            double d14 = (vec3d.y - vec3d1.y) * 0.05D;

            d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d6 > 0.0D) {
                this.motionX = this.motionX / d6 * (d6 + d14);
                this.motionZ = this.motionZ / d6 * (d6 + d14);
            }

            this.setPosition(new Vector3(this.x, vec3d1.y, this.z));
        }

        int i1 = MathHelper.floor(this.x);
        int j1 = MathHelper.floor(this.z);

        if (i1 != i || j1 != k) {
            d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.motionX = d6 * (double) (i1 - i);
            this.motionZ = d6 * (double) (j1 - k);
        }

        if (flag) {
            double d15 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d15 > 0.01D) {
                double d16 = 0.06D;

                this.motionX += this.motionX / d15 * d16;
                this.motionZ += this.motionZ / d15 * d16;
            } else if (l == 1) {
                if (this.level.getBlock(new Vector3(i - 1, j, k)).isNormalBlock()) {
                    this.motionX = 0.02D;
                } else if (this.level.getBlock(new Vector3(i + 1, j, k)).isNormalBlock()) {
                    this.motionX = -0.02D;
                }
            } else if (l == 0) {
                if (this.level.getBlock(new Vector3(i, j, k - 1)).isNormalBlock()) {
                    this.motionZ = 0.02D;
                } else if (this.level.getBlock(new Vector3(i, j, k + 1)).isNormalBlock()) {
                    this.motionZ = -0.02D;
                }
            }
        }
    }

    public void a(int i, int j, int k, boolean flag) {
    }

    public Vector3 a(double d0, double d1, double d2) {
        int i = MathHelper.floor(d0);
        int j = MathHelper.floor(d1);
        int k = MathHelper.floor(d2);

        Block block = this.level.getBlock(new Vector3(i, j, k));

        if (isRail(block)) {
            int l = this.level.getBlock(new Vector3(i, j, k)).getDamage();

            d1 = (double) j;

            if (l >= 2 && l <= 5) {
                d1 = (double) (j + 1);
            }

            int[][] aint = matrix[l];
            double d3 = 0.0D;
            double d4 = (double) i + 0.5D + (double) aint[0][0] * 0.5D;
            double d5 = (double) j + 0.5D + (double) aint[0][1] * 0.5D;
            double d6 = (double) k + 0.5D + (double) aint[0][2] * 0.5D;
            double d7 = (double) i + 0.5D + (double) aint[1][0] * 0.5D;
            double d8 = (double) j + 0.5D + (double) aint[1][1] * 0.5D;
            double d9 = (double) k + 0.5D + (double) aint[1][2] * 0.5D;
            double d10 = d7 - d4;
            double d11 = (d8 - d5) * 2.0D;
            double d12 = d9 - d6;

            if (d10 == 0.0D) {
                d0 = (double) i + 0.5D;
                d3 = d2 - (double) k;
            } else if (d12 == 0.0D) {
                d2 = (double) k + 0.5D;
                d3 = d0 - (double) i;
            } else {
                double d13 = d0 - d4;
                double d14 = d2 - d6;

                d3 = (d13 * d10 + d14 * d12) * 2.0D;
            }

            d0 = d4 + d10 * d3;
            d1 = d5 + d11 * d3;
            d2 = d6 + d12 * d3;
            if (d11 < 0.0D) {
                ++d1;
            }

            if (d11 > 0.0D) {
                d1 += 0.5D;
            }

            return new Vector3(d0, d1, d2);
        } else {
            return null;
        }
    }

    private boolean isRail(Block block) {
        switch (block.getId()) {
            case RAIL:
            case POWERED_RAIL:
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
                return true;
            default:
                return false;
        }
    }

    public static double g(double f) {
        f %= 360.0;
        if (f >= 180.0) {
            f -= 360.0;
        }

        if (f < -180.0) {
            f += 360.0;
        }

        return f;
    }

    private boolean doComplexCode() {
        this.motionY -= 0.03999999910593033D;
        int j = MathHelper.floor(this.x);
        int i;
        i = MathHelper.floor(this.y);
        int k = MathHelper.floor(this.z);

        double d4 = 0.4D;
        double d5 = 0.0078125D;
        Block block = this.level.getBlock(new Vector3(j, i, k));

        if (isRail(block)) {
            int l = this.level.getBlock(new Vector3(j, i, k)).getDamage();

            this.a(j, i, k, d4, d5, block, l);
            if (block.equals(Block.ACTIVATOR_RAIL)) {
                this.a(j, i, k, (l & 8) != 0);
            }
        } else {
            this.b(d4);
        }

        this.pitch = 0.0F;
        double d6 = this.lastX - this.x;
        double d7 = this.lastZ - this.z;

        if (d6 * d6 + d7 * d7 > 0.001D) {
            this.yaw = (float) (Math.atan2(d7, d6) * 180.0D / 3.141592653589793D);
            if (this.a) {
                this.yaw += 180.0F;
            }
        }

        double d8 = this.g(this.yaw - this.lastYaw);

        if (d8 < -170.0D || d8 >= 170.0D) {
            this.yaw += 180.0F;
            this.a = !this.a;
        }

        this.setRotation(this.yaw % 360.0F, this.pitch % 360.0F);

        if (this.linkedEntity != null && !this.linkedEntity.isAlive()) {
            if (this.linkedEntity.riding == this) {
                this.linkedEntity.riding = null;
            }

            this.linkedEntity = null;
        }
        return true;
    }

    public void collide(Entity entity) {
        if (entity != this.riding) {
            if (entity instanceof EntityLiving && !(entity instanceof EntityHuman) && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D && this.linkedEntity == null && entity.riding == null) {
                entity.setLinkedEntity(null);
            }

            float d0 = (float) (entity.x - this.x);
            float d1 = (float) (entity.z - this.z);
            float d2 = d0 * d0 + d1 * d1;

            if (d2 >= 9.999999747378752E-5D) {
                d2 = MathHelper.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;

                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.10000000149011612D;
                d1 *= 0.10000000149011612D;
                d0 *= (double) (1.0F - this.y);
                d1 *= (double) (1.0F - this.y);
                d0 *= 0.5D;
                d1 *= 0.5D;
                if (entity instanceof EntityMinecartEmpty) {
                    double d4 = entity.x - this.x;
                    double d5 = entity.z - this.z;
                    Vector3 vec3d = new Vector3(d4, 0.0D, d5);
                    Vector3 vec3d1 = new Vector3((double) MathHelper.cos((float) this.yaw * 3.1415927F / 180.0F), 0.0D, (double) MathHelper.sin((float) this.yaw * 3.1415927F / 180.0F));
                    double d6 = Math.abs(vec3d.dot(vec3d1));

                    if (d6 < 0.800000011920929D) {
                        return;
                    }

                    double d7 = entity.motionX + this.motionX;
                    double d8 = entity.motionZ + this.motionZ;

                    if (((EntityMinecartEmpty) entity).m() == 2 && this.m() != 2) {
                        this.motionX *= 0.20000000298023224D;
                        this.motionZ *= 0.20000000298023224D;
                        this.g(entity.motionX - d0, 0.0D, entity.motionZ - d1);
                        entity.motionX *= 0.949999988079071D;
                        entity.motionZ *= 0.949999988079071D;
                    } else if (((EntityMinecartEmpty) entity).m() != 2 && this.m() == 2) {
                        entity.motionX *= 0.20000000298023224D;
                        entity.motionZ *= 0.20000000298023224D;
                        entity.move(this.motionX + d0, 0.0D, this.motionZ + d1);
                        this.motionX *= 0.949999988079071D;
                        this.motionZ *= 0.949999988079071D;
                    } else {
                        d7 /= 2.0D;
                        d8 /= 2.0D;
                        this.motionX *= 0.20000000298023224D;
                        this.motionZ *= 0.20000000298023224D;
                        this.g(d7 - d0, 0.0D, d8 - d1);
                        entity.motionX *= 0.20000000298023224D;
                        entity.motionZ *= 0.20000000298023224D;
                        gl(entity, d7 + d0, 0.0D, d8 + d1);
                    }
                } else {
                    this.g(-d0, 0.0D, -d1);
                    gl(entity, d0 / 4.0D, 0.0D, d1 / 4.0D);
                }
            }
        }

    }

    public void gl(Entity ent, double d0, double d1, double d2) {
        ent.motionX += d0;
        ent.motionY += d1;
        ent.motionZ += d2;
    }

    public void g(double d0, double d1, double d2) {
        this.motionX += d0;
        this.motionY += d1;
        this.motionZ += d2;
    }

    public abstract int m();
}
