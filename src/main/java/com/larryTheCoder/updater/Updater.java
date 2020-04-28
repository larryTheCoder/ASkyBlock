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
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import lombok.Getter;
import net.lingala.zip4j.ZipFile;
import org.json.JSONArray;
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
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * @author larryTheCoder
 */
public class Updater {

    // Cannot directly use the download link, jar file name may be changed.
    public static final String JENKINS_DEFAULT_URL = "https://jenkins.potatohome.xyz/job/ASkyBlock/lastSuccessfulBuild/artifact/target/*zip*/target.zip";

    public static final int UPDATE_FAILED = -1;
    public static final int UPDATE_NOT_FOUND = 0;
    public static final int NEW_UPDATE_FOUND = 1;
    public static final int NEW_UPDATE_DOWNLOAD = 2;
    public static final int NEW_UPDATE_FOUND_NO_LINK = 3;

    @Getter
    private static int updateStatus = UPDATE_NOT_FOUND;
    private static String gitHashId;

    private static String updateUrl;
    private static Config updateManifest;

    public static void getUpdate() {
        gitHashId = ASkyBlock.get().getGitInfo().getProperty("git.commit.id");

        try {
            // Get the manifest file from this SB source first
            Enumeration<URL> resources = ASkyBlock.get().getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Config config = new Config(Config.YAML);
                config.load(resources.nextElement().openStream());

                // Simple sanity checks.
                if (config.exists("Implementation-Title") && config.getString("Implementation-Title").equalsIgnoreCase("ASkyBlock")) {
                    updateManifest = config;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();

            Utils.send("&cFailed to check for an update.");
            return;
        }

        // Update proposal:
        // When the program are starting to fetch an update, the main thing required are
        // - Github hash commit ID
        // - Jenkins build number
        // These two are required to ensure that the update capability are fulfilled.
        // The GitHub hash ID is needed to compare recent changes from GitHub.
        // However, if GitHub hash ID is presence and Jenkins build number is not presence,
        // We can notify the user to update the plugin via GitHub link instead.

        Thread updateThread = new Thread(() -> {
            // Check if this build is from jenkins, otherwise the automatic updates
            // Will notify the user about new commit from github
            if (updateManifest.getInt("jenkinsBuildId", -1) == -1) {
                Utils.send("&6You are not using a build from jenkins, use with caution.");
            }

            // Retrieve update from GitHub
            JSONArray gitCommit = new JSONArray();
            JSONObject config = null;
            try {
                // Get all of the commits available.
                URL link = new URL("https://api.github.com/repos/larryTheCoder/ASkyBlock/commits");
                HttpsURLConnection conn = (HttpsURLConnection) link.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                gitCommit = new JSONArray(new JSONTokener(rd));
                rd.close();

                // Now get the latest release from the source
                link = new URL("https://api.github.com/repos/larryTheCoder/releases/latest");
                conn = (HttpsURLConnection) link.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                config = new JSONObject(new JSONTokener(rd));

                rd.close();
            } catch (IOException e) {
                if (gitCommit.length() == 0) {
                    TaskManager.runTask(() -> {
                        updateStatus = UPDATE_FAILED;

                        Utils.send("&cFailed to check for an update.");
                    });

                    return;
                }
            }

            // Check for difference in git commits.
            int i = 0;
            for (; i < gitCommit.length(); i++) {
                JSONObject object = gitCommit.getJSONObject(i);

                if (object.getString("sha").equals(gitHashId)) {
                    break;
                }
            }

            // Now check if this build is a stable build, do not bother stable build with
            // Development builds.
            if (config != null) {
                // Stable builds
                // TODO: Figure out the right way to compare it.
            } else {
                // Development builds
                if (i > 0) {
                    TaskManager.runTask(() -> setTargetUpdate(JENKINS_DEFAULT_URL));
                } else {
                    TaskManager.runTask(() -> setTargetUpdate(null));
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    static void setTargetUpdate(String objectJson) {
        if (objectJson == null) {
            updateStatus = UPDATE_NOT_FOUND;

            Utils.send("&eThere is no new update found.");
            return;
        }
        updateStatus = NEW_UPDATE_FOUND;
        updateUrl = objectJson;

        if (Settings.autoUpdate) {
            Utils.send("&aDownloading a new version of ASkyBlock now.");

            scheduleDownload(null);
        } else {
            Utils.send("&aA new build of ASkyBlock is available to download.");
        }
    }

    public static void scheduleDownload(CommandSender sender) {
        if (updateStatus != NEW_UPDATE_FOUND) {
            if (sender != null) sender.sendMessage(TextFormat.RED + "No new updates were found.");

            return;
        }

        long changeInTime = System.currentTimeMillis();
        Thread updateThread = new Thread(() -> {
            String updatePath;
            if (updateUrl.equals(JENKINS_DEFAULT_URL)) {
                updatePath = Utils.UPDATES_DIRECTORY + "target.zip";
            } else {
                updatePath = Utils.UPDATES_DIRECTORY + "target.jar";
            }

            try {
                URL website = new URL(updateUrl);
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

                applyUpdateFor(updatePath);
            });
        });
        updateThread.setDaemon(true);
        updateThread.start();

        updateStatus = NEW_UPDATE_DOWNLOAD;

        if (sender != null) sender.sendMessage(TextFormat.GREEN + "Downloading a new version of ASkyBlock now.");
    }

    private static void applyUpdateFor(String updatePath) {
        // TODO
    }
}
