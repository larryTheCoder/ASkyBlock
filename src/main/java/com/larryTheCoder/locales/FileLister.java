/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
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
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author larryTheCoder
 * @author tastybento
 */
public final class FileLister {

    private final static String FOLDER_PATH = "locale";
    private final ASkyBlock plugin;

    public FileLister(ASkyBlock plugin) {
        this.plugin = plugin;
    }

    public List<String> list() throws IOException {
        List<String> result = new ArrayList<>();

        // Check if the locale folder exists
        File localeDir = new File(plugin.getDataFolder(), FOLDER_PATH);
        if (localeDir.exists()) {
            FilenameFilter ymlFilter = (File dir, String name) -> {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".yml");
            };
            for (String fileName : Objects.requireNonNull(localeDir.list(ymlFilter))) {
                result.add(fileName.replace(".yml", ""));
            }
            if (!result.isEmpty()) {
                return result;
            }
        }
        // Else look in the JAR
        File jarfile;

        try {
            Method method = PluginBase.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            jarfile = (File) method.invoke(this.plugin);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IOException(e);
        }

        try (JarFile jar = new JarFile(jarfile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String path = entry.getName();

                if (!path.startsWith(FOLDER_PATH)) {
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
