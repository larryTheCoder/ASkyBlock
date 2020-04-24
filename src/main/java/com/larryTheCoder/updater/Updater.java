/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.larryTheCoder.updater;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;

/**
 * @author larryTheCoder
 */
public class Updater {

    public static final int UPDATE_FAILED = -1;
    public static final int UPDATE_NOT_FOUND = 0;
    public static final int NEW_UPDATE_FOUND = 1;
    public static final int NEW_UPDATE_DOWNLOAD = 2;

    @Getter
    private static int updateStatus = UPDATE_NOT_FOUND;
    private static String buildId;

    private static JSONObject updateLocation;

    public static void getUpdate() {
        buildId = ASkyBlock.get().getDescription().getVersion();

        Thread updateThread = new Thread(() -> {
            // Retrieve update from GitHub

            JSONObject config;
            try {
                URL link = new URL("https://api.github.com/repos/larryTheCoder/ASkyBlock/releases/latest");
                HttpsURLConnection conn = (HttpsURLConnection) link.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                config = new JSONObject(new JSONTokener(rd));

                rd.close();
            } catch (IOException e) {
                TaskManager.runTask(() -> {
                    updateStatus = UPDATE_FAILED;

                    Utils.send("&cFailed to check for an update.");
                });
                return;
            }

            JSONObject jsonData = null;
            String downloadFile = null;
            if (!config.getString("tag_name").equalsIgnoreCase(buildId)) {
                for (Object obj : config.getJSONArray("assets")) {
                    if (!(obj instanceof JSONObject)) {
                        continue;
                    }

                    jsonData = ((JSONObject) obj);
                    if (jsonData.getString("browser_download_url").endsWith(".jar")) {
                        downloadFile = jsonData.getString("browser_download_url");
                        break;
                    }
                }

                if (downloadFile == null) TaskManager.runTask(() -> {
                    updateStatus = UPDATE_FAILED;

                    Utils.send("&cError to check an update for ASkyBlock source, wrong link?");
                });
            }

            final JSONObject objectJson = jsonData;
            TaskManager.runTask(() -> setTargetUpdate(objectJson));
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    static void setTargetUpdate(JSONObject objectJson) {
        if (objectJson == null) {
            updateStatus = UPDATE_NOT_FOUND;
            return;
        }
        updateStatus = NEW_UPDATE_FOUND;
        updateLocation = objectJson;

        if (Settings.autoUpdate) {
            Utils.send("&aDownloading a new version of ASkyBlock now.");

            scheduleDownload(null);
        } else {
            Utils.send("&aAvailable update is ready to download, &6/is download &a to install the plugin automatically.");
        }
    }

    public static void scheduleDownload(CommandSender sender) {
        if (updateStatus != NEW_UPDATE_FOUND) {
            if (sender != null) sender.sendMessage(TextFormat.RED + "No new updates were found.");

            return;
        }

        long changeInTime = System.currentTimeMillis();
        Thread updateThread = new Thread(() -> {
            String updatePath = Utils.UPDATES_DIRECTORY + updateLocation.getString("name");
            try {
                URL website = new URL(updateLocation.getString("browser_download_url"));
                HttpsURLConnection conn = (HttpsURLConnection) website.openConnection();

                ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
                FileOutputStream fos = new FileOutputStream(updatePath);

                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                TaskManager.runTask(e::printStackTrace); // Sync the error/trace into the console.
                return;
            }

            TaskManager.runTask(() -> {
                String notice = String.format("Downloaded ASkyBlock plugin for %ss",
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - changeInTime));

                if (sender != null) sender.sendMessage(TextFormat.GREEN + notice);

                Utils.send(notice);
            });
        });
        updateThread.setDaemon(true);
        updateThread.start();

        updateStatus = NEW_UPDATE_DOWNLOAD;

        if (sender != null) sender.sendMessage(TextFormat.GREEN + "Downloading a new version of ASkyBlock now.");
    }
}
