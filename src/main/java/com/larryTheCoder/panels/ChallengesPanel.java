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
 *
 */
package com.larryTheCoder.panels;

import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;

import java.util.List;

public class ChallengesPanel extends FormWindowSimple {

    private List<String> challenges;

    public ChallengesPanel(List<String> challenges) {
        super("§aChallenges Menu", "§aChoose your toppings! All of these are your challenges to complete! You will be awarded with an amazing prize!");

        this.challenges = challenges;
        for (String toButton : challenges) {
            this.addButton(new ElementButton(toButton));
        }
    }

}
