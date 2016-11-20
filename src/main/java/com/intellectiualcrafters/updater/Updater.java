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

package com.intellectiualcrafters.updater;

import com.intellectiualcrafters.json.JSONArray;
import com.intellectiualcrafters.json.JSONObject;
import com.intellectiualcrafters.util.HttpUtil;
import com.intellectiualcrafters.StringMan;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public class Updater {

    public static URL getUpdate() {
        String str = HttpUtil.readUrl("https://api.github.com/repos/larryTheCoder/ASkyBlock-Nukkit/releases/latest");
        JSONObject release = new JSONObject(str);
         JSONArray assets = (JSONArray) release.get("assets");
        String downloadURL = String.format(ASkyBlock.getInstance().getDescription().getFullName() + "-%s.jar");
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            String name = asset.getString("name");
            if (downloadURL.equals(name)) {
                try {
                    String[] split = release.getString("name").split("\\.");
                    int[] version;
                    if (split.length == 3) {
                        version = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
                    } else {
                        version = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0};
                    }
                    // If current version >= update
                    if (ASkyBlock.getInstance().checkVersion(ASkyBlock.getInstance().getPluginVersion(), version)) {
                        if (!ASkyBlock.getInstance().getPluginVersionString().contains("-SNAPSHOT") || !Arrays.equals(ASkyBlock.getInstance().getVersion(), version)) {
                            ASkyBlock.getInstance().getLogger().info("&7ASkyBlock is already up to date!");
                            return null;
                        }
                    }
                    ASkyBlock.getInstance().getLogger().warning("&6 ASkyBlock " + StringMan.join(split, ".") + " is available:");
                    ASkyBlock.getInstance().getLogger().warning("&8 - &3Download at: &7" + downloadURL);
                    return new URL(asset.getString("browser_download_url"));
                } catch (MalformedURLException e) {
                    ASkyBlock.getInstance().getLogger().info("&dCould not check for updates (1)");
                    ASkyBlock.getInstance().getLogger().info("&7 - Manually check for updates: https://github.com/IntellectualSites/PlotSquared/releases");
                }
            }
        }
        ASkyBlock.getInstance().getLogger().info("You are running the latest version of ASkyBlock!");
        return null;
    }
}
