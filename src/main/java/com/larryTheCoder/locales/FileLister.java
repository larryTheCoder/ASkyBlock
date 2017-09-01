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
package com.larryTheCoder.locales;

import cn.nukkit.plugin.PluginBase;
import com.larryTheCoder.ASkyBlock;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Adam Matthew
 */
public final class FileLister {

    private final static String FOLDERPATH = "locale";
    private final ASkyBlock plugin;

    public FileLister(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    public List<String> list() throws IOException {
        List<String> result = new ArrayList<>();

        // Check if the locale folder exists
        File localeDir = new File(plugin.getDataFolder(), FOLDERPATH);
        if (localeDir.exists()) {
            FilenameFilter ymlFilter = (File dir, String name) -> {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".yml");
            };
            for (String fileName : localeDir.list(ymlFilter)) {
                result.add(fileName.replace(".yml", ""));
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        // Else look in the JAR
        File jarfile = null;

        /**
         * Get the jar file from the plugin.
         */
        try {
            Method method = PluginBase.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            jarfile = (File) method.invoke(this.plugin);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IOException(e);
        }

        /**
         * Loop through all the entries.
         */
        try (JarFile jar = new JarFile(jarfile)) {
            /**
             * Loop through all the entries.
             */
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String path = entry.getName();

                /**
                 * Not in the folder.
                 */
                if (!path.startsWith(FOLDERPATH)) {
                    continue;
                }

                if (entry.getName().endsWith(".yml")) {
                    result.add((entry.getName().replace(".yml", "")).replace("locale/", ""));
                }

            }
        }
        return result;
    }
}
