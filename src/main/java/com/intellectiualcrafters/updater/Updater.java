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

package com.intellectiualcrafters.updater;

import com.intellectiualcrafters.json.JSONArray;
import com.intellectiualcrafters.json.JSONObject;
import com.intellectiualcrafters.util.HttpUtil;
import com.intellectiualcrafters.StringMan;
import com.intellectiualcrafters.json.JSONException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Utils;

/**
 * @author Adam Matthew
 */
public class Updater {

    public static URL getUpdate() {
        try{
        if(HttpUtil.readUrl("https://api.github.com/repos/larryTheCoder/ASkyBlock-Nukkit/releases/latest") == null){
            Utils.ConsoleMsg("&eUnable to check update! Are you offline?");
            return null;
        }
        String str = HttpUtil.readUrl("https://api.github.com/repos/larryTheCoder/ASkyBlock-Nukkit/releases/latest");
        JSONObject release = new JSONObject(str);
        JSONArray assets = (JSONArray) release.get("assets");
        String downloadURL = String.format(ASkyBlock.get().getDescription().getFullName() + "-%s.jar");
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
                    if (ASkyBlock.get().checkVersion(ASkyBlock.get().getPluginVersion(), version)) {
                        if (!ASkyBlock.get().getPluginVersionString().contains("-SNAPSHOT") || !Arrays.equals(ASkyBlock.get().getVersion(), version)) {
                            ASkyBlock.get().getLogger().info("&7ASkyBlock is already up to date!");
                            return null;
                        }
                    }
                    Utils.ConsoleMsg("&6 ASkyBlock " + StringMan.join(split, ".") + " is available:");
                    Utils.ConsoleMsg("&8 - &3Download at: &7" + downloadURL);
                    return new URL(asset.getString("browser_download_url"));
                } catch (MalformedURLException e) {
                    Utils.ConsoleMsg("&dCould not check for updates (1)");
                    Utils.ConsoleMsg("&7 - Manually check for updates: https://github.com/larryTheCoder/ASkyBlock-Nukkit/releases");
                }
            }
        }
        } catch(JSONException | NumberFormatException ex){
            Utils.ConsoleMsg("&aYou are running the latest version of ASkyBlock!");
        }        
        return null;
    }
}
