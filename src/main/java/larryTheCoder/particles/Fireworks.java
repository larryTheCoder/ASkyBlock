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
package larryTheCoder.particles;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.sound.ClickSound;
import cn.nukkit.level.sound.FizzSound;
import cn.nukkit.level.sound.PopSound;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Its works!
 * 
 * @author larryTheCoder
 */
public class Fireworks {

    public static void addFireEffect(Player player) {
        int x = player.getLocation().getFloorX();
        int y = player.getLocation().getFloorY();
        int z = player.getLocation().getFloorZ();
        Level l = player.getLevel();
        int time = 10;
        while (time < 0) {
            switch (time) {
                case 9:
                    l.addParticle(new FlameParticle(new Vector3(x, y, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 1, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 2, z)));
                    break;
                case 8:
                    l.addParticle(new FlameParticle(new Vector3(x, y, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 1, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 2, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 3, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 4, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 5, z)));
                    break;
                case 7:
                    l.addParticle(new FlameParticle(new Vector3(x, y, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 1, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 2, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 3, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 4, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 5, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 6, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 7, z)));
                    l.addParticle(new FlameParticle(new Vector3(x, y + 8, z)));
                    ll(player);
                    l(player);
                    l.addSound(new FizzSound(new Vector3(x, y + 9, z)));
                    l.addSound(new PopSound(new Vector3(x, y + 9, z)));
                    l.addSound(new ClickSound(new Vector3(x, y + 9, z)));
                    break;
                case 0:
                    time = 10;
                    break;
            }
            time--;
        }
    }
    public static void l(Player p) {
        Level level = p.getLevel();
        int x = p.getFloorX();
        int yy = p.getFloorY();
        int z = p.getFloorZ();
       
        int y = yy + 10;
        double radius = 4.0;
        int count = 650;
        int r = MathHelper.getRandomNumberInRange(new Random(), 0, 300);
        int g = MathHelper.getRandomNumberInRange(new Random(), 0, 300);
        int b = MathHelper.getRandomNumberInRange(new Random(), 0, 300);
        Vector3 center = new Vector3(x, y + 1, z);
        DustParticle particle = new DustParticle(center, r, g, b);
        for (int i = 0; i < count; i++) {
            int pitch = (int) ((ThreadLocalRandom.current().nextFloat() + ThreadLocalRandom.current().nextFloat() - 0.5) * Math.PI);
            int yaw = (int) (ThreadLocalRandom.current().nextFloat() + ThreadLocalRandom.current().nextFloat() * 2 * Math.PI);
            y = (int) -Math.sin(pitch);
            double delta = Math.cos(pitch);
            x = (int) (-Math.sin(yaw) * delta);
            z = (int) (Math.cos(yaw) * delta);
            Vector3 v = new Vector3(x, y, z);
            Vector3 target = center.add(v.normalize().multiply(radius)); ///.add($v->normalize()->multiply($radius));
            particle.setComponents(target.x, target.y, target.z);
            level.addParticle(particle);
        }
    }
    
    public static void ll(Player p) {
        Level level = p.getLevel();
        int x = p.getFloorX();
        int yy = p.getFloorY();
        int z = p.getFloorZ();

        int y = yy + 10;
        double radius = 3.0;
        int count = 650;
        int r = MathHelper.getRandomNumberInRange(new Random(), 0, 200);
        int g = MathHelper.getRandomNumberInRange(new Random(), 0, 200);
        int b = MathHelper.getRandomNumberInRange(new Random(), 0, 200);
        Vector3 center = new Vector3(x, y + 1, z);
        DustParticle particle = new DustParticle(center, r, g, b);
        for (int i = 0; i < count; i++) {
            int pitch = (int) ((ThreadLocalRandom.current().nextFloat() + ThreadLocalRandom.current().nextFloat() - 0.5) * Math.PI);
            int yaw = (int) (ThreadLocalRandom.current().nextFloat() + ThreadLocalRandom.current().nextFloat() * 2 * Math.PI);
            y = (int) -Math.sin(pitch);
            double delta = Math.cos(pitch);
            x = (int) (-Math.sin(yaw) * delta);
            z = (int) (Math.cos(yaw) * delta);
            Vector3 v = new Vector3(x, y, z);
            Vector3 target = center.add(v.normalize().multiply(radius)); ///.add($v->normalize()->multiply($radius));
            particle.setComponents(target.x, target.y, target.z);
            level.addParticle(particle);
        }
    }
}
