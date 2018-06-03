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
package com.larryTheCoder.updater;

import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.HttpUtil;
import com.larryTheCoder.utils.StringMan;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.json.JSONArray;
import com.larryTheCoder.utils.json.JSONException;
import com.larryTheCoder.utils.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

class UpdaterQuery {

    private final ASkyBlock plugin;
    private URL DownloadURL;

    public UpdaterQuery(ASkyBlock plugin) {
        this.plugin = plugin;
        this.tickUpdate();
    }

    /**
     * Retrieve the update from the GitHub database
     *
     * @return boolean (True if updated to latest or false that the URL cannot be retrieved)
     */
    private void tickUpdate() {
        // Slightly that this is an unofficial release
        if (HttpUtil.readUrl("https://api.github.com/repos/larryTheCoder/ASkyBlock-Nukkit/releases/latest") == null) {
            Utils.send("&eHead trace of the latest release object cannot be read (Unofficial release or Offline)");
            return;
        }
        // Well, I Should remove that notice above if there is an update from GitHub database
        try {
            String str = HttpUtil.readUrl("https://api.github.com/repos/larryTheCoder/ASkyBlock-Nukkit/releases/latest");
            JSONObject release = new JSONObject(str);
            JSONArray assets = (JSONArray) release.get("assets");
            String downloadURL = plugin.getDescription().getFullName() + "-%s.jar";
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
                        if (plugin.checkVersion(plugin.getPluginVersion(), version)) {
                            if (!plugin.getPluginVersionString().contains("-SNAPSHOT") || !Arrays.equals(plugin.getVersion(), version)) {
                                plugin.getLogger().info("&7ASkyBlock is already up to date!");
                                return;
                            }
                        }
                        Utils.send("&6 ASkyBlock " + StringMan.join(split, ".") + " is available:");
                        Utils.send("&8 - &3Download at: &7" + downloadURL);
                        DownloadURL = new URL(asset.getString("browser_download_url"));
                        return;
                    } catch (MalformedURLException e) {
                        Utils.send("&dCould not check for updates (1)");
                        Utils.send("&7 - Manually check for updates: https://github.com/larryTheCoder/ASkyBlock-Nukkit/releases");
                    }
                }
            }
        } catch (JSONException | NumberFormatException ex) {
            Utils.send("&aYou are running the latest version of ASkyBlock!");
        }
    }
}
