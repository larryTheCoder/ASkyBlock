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
 */
package com.larryTheCoder.economy;

import cn.nukkit.Player;

/**
 * symbols
 */
public class EconomyAPI implements Economy {

    @Override
    public boolean reduceMoney(Player p, double amount) {
        double money = me.onebone.economyapi.EconomyAPI.getInstance().myMoney(p);
        if (money < amount) {
            int ret = me.onebone.economyapi.EconomyAPI.getInstance().reduceMoney(p, amount);
            if (ret == me.onebone.economyapi.EconomyAPI.RET_SUCCESS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addMoney(Player p, double amount) {
        return me.onebone.economyapi.EconomyAPI.getInstance().addMoney(p, amount, true) == me.onebone.economyapi.EconomyAPI.RET_SUCCESS;
    }

    @Override
    public double getMoney(Player p) {
        return me.onebone.economyapi.EconomyAPI.getInstance().myMoney(p);
    }
}
