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
package com.larryTheCoder.locales;

import cn.nukkit.plugin.PluginBase;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import com.larryTheCoder.ASkyBlock;

/**
 * @author larryTheCoder
 */
public final class FileLister {

    private final ASkyBlock plugin;
    private final String FOLDERPATH;

    public FileLister(ASkyBlock plugin, String folderPath) {
        this.plugin = plugin;
        FOLDERPATH = folderPath;
    }

    public List<String> list() throws IOException {
        List<String> result = new ArrayList<>();

        // Check if the locale folder exists
        File localeDir = new File(plugin.getDataFolder(), FOLDERPATH);
        if (localeDir.exists()) {
            FilenameFilter ymlFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String lowercaseName = name.toLowerCase();
                    if (lowercaseName.endsWith(".yml")) {
                        return true;
                    } else {
                        return false;
                    }
                }
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
        } catch (Exception e) {
            throw new IOException(e);
        }

        JarFile jar = new JarFile(jarfile);

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
        jar.close();
        return result;
    }
}
