/*
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

package com.larryTheCoder.utils;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import com.larryTheCoder.ASkyBlock;
import java.io.File;

/**
 *
 * @author larryTheCoder
 */
public class ConfigUpdater {

    public static void recheck() {
        boolean update = false;
        File file;
        Config cfg = new Config(file = new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);
        if(!cfg.getString("version").equalsIgnoreCase(ConfigManager.CONFIG_VERSION)){
            Utils.ConsoleMsg("&cOutdated config! Creating new one");
            Utils.ConsoleMsg("&aYour old config will be renamed into config.old!");
            update = true;
        }
        if(update){
            file.renameTo(new File(ASkyBlock.get().getDataFolder(), "config.old"));
            ASkyBlock.get().saveResource("config.yml");
        }
    }

}
