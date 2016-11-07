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

package larryTheCoder.locales;

import cn.nukkit.utils.Config;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import larryTheCoder.ASkyBlock;

/**
 * All the text strings in the game sent to players
 * This version enables different players to have different locales.
 * 
 * @author larryTheHarry
 */
public class ASlocales {
    
    private final static Set<String> TITLE_COLORS = new HashSet<String>(Arrays.asList(
            "black",
            "dark_blue",
            "dark_green",
            "dark_aqua",
            "dark_red",
            "dark_purple",
            "gold",
            "gray",
            "dark_gray",
            "blue",
            "green",
            "aqua",
            "red",
            "light_purple",
            "yellow",
            "white"
            ));
    private Config locale = null;
    private File localeFile = null;
    private ASkyBlock plugin;
    private Locale localeObject;
    private String localeName;
    private int index;
    
    /**
     * Creates a locale object full of localized strings for a language
     * @param plugin
     * @param localeName - name of the yml file that will be used
     * @param index 
     */
    public ASlocales(ASkyBlock plugin, String localeName, int index) {
        this.plugin = plugin;
        this.index = index;
        this.localeName = localeName;
        getLocale(localeName);
        loadLocale();
        if (!localeName.equalsIgnoreCase("locale")) {
            localeObject = new Locale(localeName.substring(0, 2), localeName.substring(3, 5));
        }       
    }

    /**
     * @return locale Config object
     */
    public Config getLocale(String localeName) {
        if (this.locale == null) {
            reloadLocale(localeName);
        }
        return locale;
    }
    
    /**
     * Reloads the locale file
     */
    public void reloadLocale(String localeName) {
        // Make directory if it doesn't exist
        File localeDir = new File(plugin.getDataFolder() + File.separator + "locale");
        if (!localeDir.exists()) {
            localeDir.mkdir();
        }
        if (localeFile == null) {
            localeFile = new File(localeDir.getPath(), localeName + ".yml");
        }
        if (localeFile.exists()) {
            //plugin.getLogger().info("DEBUG: File exists!");
            locale = new Config(localeFile, Config.YAML);
        } else {
            // Look for defaults in the jar
            if (plugin.getResource("locale/" + localeName + ".yml") != null) {
                plugin.saveResource("locale/" + localeName + ".yml", true);
                localeFile = new File(plugin.getDataFolder() + File.separator + "locale", localeName + ".yml");
                locale = new Config(localeFile, Config.YAML);
                //locale.setDefaults(defLocale);
            } else {
                // Use the default file
                localeFile = new File(plugin.getDataFolder() + File.separator + "locale", "locale.yml");
                if (localeFile.exists()) {
                    locale = new Config(localeFile, Config.YAML);
                } else {
                    // Look for defaults in the jar                    
                    if (plugin.getResource("locale/locale.yml") != null) {
                        plugin.saveResource("locale/locale.yml", true);
                        localeFile = new File(plugin.getDataFolder() + File.separator + "locale", "locale.yml");
                        locale = new Config(localeFile, Config.YAML);
                    } else {
                        plugin.getLogger().emergency("Could not find any locale file!");
                    }
                }
            }
        }
    }
    
    private void loadLocale() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Checks that the color supplied is a valid color
     * @param string
     * @return color
     */
    private String colorCheck(String string) {
        string = string.toLowerCase();
        if (TITLE_COLORS.contains(string)) {
            return string;
        }
        plugin.getLogger().warning("Title color " + string + " is unknown. Use one from this list:");
        for (String color : TITLE_COLORS) {
            plugin.getLogger().warning(color);
        }
        return "white";
    }

    /**
     * @return the languageName
     */
    public String getLanguageName() {
        if (localeObject == null) {
            return "unknown";
        }
        return localeObject.getDisplayLanguage(localeObject);
    }

    public String getCountryName() {
        if (localeObject == null) {
            return "unknown";
        }
        return localeObject.getDisplayCountry(localeObject);
    }

    public String getLocaleName() {
        return this.localeName;        
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
}
