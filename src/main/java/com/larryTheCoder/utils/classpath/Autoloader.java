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

package com.larryTheCoder.utils.classpath;

import cn.nukkit.utils.TextFormat;
import com.google.common.collect.Maps;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.locales.FileLister;
import com.larryTheCoder.utils.Utils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;

/**
 * Attempts to load the required libraries and inject its classpath
 * into the server/plugin.
 */
@Log4j2
public class Autoloader {

    private final Map<String, String> reqLib = Maps.newHashMap();
    private final String targetFolder;

    private List<String> files;

    public Autoloader(String targetFolder) {
        reqLib.put("sql2o-1.6.0.jar", "https://repo1.maven.org/maven2/org/sql2o/sql2o/1.6.0/sql2o-1.6.0.jar");
        reqLib.put("mysql-connector-java-8.0.16.jar", "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar");
        reqLib.put("sqlite-jdbc-3.27.2.1.jar", "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.27.2.1/sqlite-jdbc-3.27.2.1.jar");

        this.targetFolder = targetFolder;

        searchDirectory(targetFolder);
        attemptAutoloadMerge();
    }

    private void searchDirectory(String targetFolder) {
        try {
            files = new FileLister(targetFolder, false).list();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void attemptAutoloadMerge() {
        // Step 1: Attempts to check if database libraries are present.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("org.sqlite.JDBC");
            Class.forName("org.sql2o.Sql2o");

            log.debug("Found required libraries, no download needed.");
            return;
        } catch (ClassNotFoundException ignored) {
        }

        // Step 2: Attempt to download the required libraries from desired site.
        reqLib.keySet().stream().filter(lib -> files.stream().noneMatch(i -> i.equalsIgnoreCase(lib))).forEach(libs -> {
            log.info(ASkyBlock.get().getPrefix() + TextFormat.YELLOW + "Attempting to download library: " + libs);

            try {
                final URL website = new URL(reqLib.get(libs));

                @Cleanup final ReadableByteChannel rbc = Channels.newChannel(website.openStream());

                @Cleanup final FileOutputStream fos = new FileOutputStream(Utils.DIRECTORY + targetFolder + File.separator + libs);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException exception) {
                throw new RuntimeException("Please check your internet connection before installing...", exception);
            }
        });

        File mysqlDriver = new File(Utils.DIRECTORY + targetFolder + File.separator + "mysql-connector-java-8.0.16.jar");
        File sqliteDriver = new File(Utils.DIRECTORY + targetFolder + File.separator + "sqlite-jdbc-3.27.2.1.jar");
        File sql2oDriver = new File(Utils.DIRECTORY + targetFolder + File.separator + "sql2o-1.6.0.jar");

        try {
            // Step 3: Merge these libraries
            injectClass(mysqlDriver, "com.mysql.cj.jdbc.Driver");
            injectClass(sqliteDriver, "org.sqlite.JDBC");
            injectClass(sql2oDriver, "org.sql2o.Sql2o");

            log.debug("Injected database libraries.");
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }

    @SneakyThrows
    private void injectClass(File file, String classLoader) {
        log.debug("Attempting to reflect " + classLoader + " classpath");

        URLClassLoader autoload = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(autoload, file.toURI().toURL());
    }
}
