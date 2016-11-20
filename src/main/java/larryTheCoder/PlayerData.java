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

package larryTheCoder;

import java.io.Serializable;
import cn.nukkit.Player;

/**
 * @author larryTheCoder
 */
@SuppressWarnings("serial")
public class PlayerData 
implements Serializable {
    public float health;
    public float saturation;
    public int exp;
    public int expTotal;
    public int level;
    public int toNextLevel;
    public int foodLevel;

    @SuppressWarnings("deprecation")
    public void SetPlayer(Player p) {
        if (this.health <= 0.0) {
            PlayerData.SetDefaults(p);
            return;
        }
        p.setHealth(health);
        p.getFoodData().setFoodSaturationLevel(saturation);
        p.getFoodData().setFoodLevel(this.foodLevel);
        p.getFoodData().setLevel(this.level);
        p.setExperience(this.exp,this.expTotal);
    }

    public void LoadPlayer(Player p) {
        this.health = p.getHealth();
        
        this.saturation = p.getFoodData().getFoodSaturationLevel();
        this.exp = p.getExperience();
        this.expTotal = p.getExperienceLevel();
        this.foodLevel = p.getFoodData().getMaxLevel();
        this.level = p.getFoodData().getLevel();
    }

    @SuppressWarnings("deprecation")
    public static void SetDefaults(Player p) {
        p.setHealth(p.getMaxHealth());
        p.getFoodData().setFoodSaturationLevel(20.0f);
        p.setExperience(0);
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setLevel(0);
    }
}
