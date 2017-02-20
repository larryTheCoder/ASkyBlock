/*
 * Copyright (C) 2017 larryTheHarry
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
package com.larryTheCoder.economyHandler;

import cn.nukkit.Player;
import me.onebone.economyapi.EconomyAPI;

public class EconomyA implements Economy {

    @Override
    public boolean reduceMoney(Player p, double amount) {
        double money = EconomyAPI.getInstance().myMoney(p);
        if (money < amount) {
            int ret = EconomyAPI.getInstance().reduceMoney(p, amount);
            if (ret == EconomyAPI.RET_SUCCESS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addMoney(Player p, double amount) {
        EconomyAPI.getInstance().addMoney(p, amount, true);
    }
}
