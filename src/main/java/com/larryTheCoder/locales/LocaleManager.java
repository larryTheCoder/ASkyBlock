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

package com.larryTheCoder.locales;

import cn.nukkit.Player;
import cn.nukkit.api.API;
import cn.nukkit.utils.TextFormat;
import com.google.common.base.Preconditions;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * The LocaleManager instance, this class is responsible for handling Locales files,
 * in order to make sure that the locale provided is valid with the universal localization,
 * {@link Locale} class is used to confirm the locale validity.
 */
@Log4j2
public class LocaleManager {

    private static LocaleManager instance;

    private final ASkyBlock plugin;
    private final HashMap<Locale, LocaleInstance> locales = new HashMap<>();

    private LocaleInstance fallbackLocale;

    public LocaleManager(ASkyBlock plugin) {
        Preconditions.checkArgument(instance == null, "Locale manager has already been initialized.");
        instance = this;

        this.plugin = plugin;

        initLocale();
    }

    private void initLocale() {
        // Load languages
        FileLister fl = new FileLister(ASkyBlock.get());
        try {
            int index = 1;
            for (String code : fl.list()) addLocale(code, new LocaleInstance(plugin, code, index++), false);
        } catch (IOException e1) {
            log.error(TextFormat.RED + "Unable to insert localization config into plugin!");

            throw new RuntimeException(e1);
        }

        Locale defaultLocale = locales.keySet().stream()
                .filter(locale -> locale.toString().equalsIgnoreCase(Settings.defaultLanguage))
                .findFirst().orElse(null);

        if (defaultLocale == null) {
            log.warn(ASkyBlock.get().getPrefix() + TextFormat.YELLOW + "'" + Settings.defaultLanguage + ".yml' not found in /locale folder. Using /locale/en_US.yml");
            Settings.defaultLanguage = "en_US";

            addLocale("en_US", fallbackLocale = new LocaleInstance(plugin, "en_US", 0), true);
        } else {
            fallbackLocale = locales.get(defaultLocale);
        }
    }

    /**
     * Attempts to fetch the player locale information from the database, this method
     * If the player localization doesn't exists, the default locale will be chosen.
     *
     * @param player The player that needs be checked
     * @return The locale containing all translations.
     */
    @NonNull
    @API(definition = API.Definition.INTERNAL, usage = API.Usage.BLEEDING)
    public LocaleInstance getLocaleFromPlayer(Player player) {
        if (player == null) return fallbackLocale;

        return getLocaleFromPlayer(player.getName());
    }

    /**
     * Attempts to fetch a locale by the given name, if the name does not exists in the
     * database system, default locale will be chosen by default.
     *
     * @param playerName The player name that needs to be checked
     * @return The locale containing all translations.
     */
    @NonNull
    @API(definition = API.Definition.INTERNAL, usage = API.Usage.BLEEDING)
    public LocaleInstance getLocaleFromPlayer(String playerName) {
        if (playerName == null) return fallbackLocale;

        Locale locale = Arrays.stream(Locale.getAvailableLocales())
                .filter(lc -> lc.toString().equalsIgnoreCase(playerName))
                .findFirst().orElse(null);

        return locales.getOrDefault(locale, fallbackLocale);
    }

    /**
     * Returns a non-null locale instance, if the locale name given does not exists, it will always
     * return a default configured locale that is set in the server config.
     *
     * @param localeName The language + "_" + country code.
     * @return The locale containing all translations.
     */
    @NonNull
    @API(definition = API.Definition.UNIVERSAL, usage = API.Usage.MAINTAINED)
    public LocaleInstance getLocale(@NonNull String localeName) {
        Preconditions.checkArgument(localeName != null, "Locale name cannot be null");

        Locale locale = Arrays.stream(Locale.getAvailableLocales())
                .filter(lc -> lc.toString().equalsIgnoreCase(localeName))
                .findFirst().orElse(null);

        return locales.getOrDefault(locale, fallbackLocale);
    }

    /**
     * Appends a locale instance into the array list. You can create your own
     * custom locale if you did like to, but make sure that the locale are valid
     * locale based on ISO 3166 country codes.
     *
     * @param localeName The language + "_" + country code.
     * @param instance   The provided locale instance
     * @param force      Forces to add this locale even when the locale are already exists
     */
    @API(definition = API.Definition.UNIVERSAL, usage = API.Usage.MAINTAINED)
    public void addLocale(@NonNull String localeName, @NonNull LocaleInstance instance, boolean force) {
        Preconditions.checkArgument(localeName != null, "Locale name cannot be null");
        Preconditions.checkArgument(instance != null, "Locale instance class cannot be null");

        Locale locale = Arrays.stream(Locale.getAvailableLocales())
                .filter(lc -> lc.toString().equalsIgnoreCase(localeName))
                .findFirst().orElse(null);

        if (locale != null) {
            if (!force) {
                Preconditions.checkArgument(!locales.containsKey(locale), "Duplicated key for " + localeName + " is found. This condition is disgraced.");
            }

            locales.put(locale, instance);
            return;
        }

        Utils.send(TextFormat.RED + "Invalid localization name: " + localeName);
    }

    public HashMap<Locale, LocaleInstance> getRegisteredLocales() {
        return locales;
    }
}
