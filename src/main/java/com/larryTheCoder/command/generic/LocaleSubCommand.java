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
package com.larryTheCoder.command.generic;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.utils.Utils;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Adam Matthew
 */
public class LocaleSubCommand extends SubCommand {

    public LocaleSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.lang") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "/is lang <#>";
    }

    @Override
    public String getName() {
        return "lang";
    }

    @Override
    public String getDescription() {
        return "Select your main language";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (args.length < 2) {
            sender.sendMessage("/is lang <#>:");
            displayLocales(p);
            return true;
        }
        if (!Utils.isNumeric(args[1])) {
            p.sendMessage(TextFormat.RED + "/is lang <#>");
            displayLocales(p);
            return true;
        } else {
            try {
                int index = Integer.valueOf(args[1]);
                if (index < 1 || index > getPlugin().getAvailableLocales().size()) {
                    p.sendMessage(TextFormat.RED + "/is lang <#>");
                    displayLocales(p);
                    return true;
                }
                for (ASlocales locale : getPlugin().getAvailableLocales().values()) {
                    if (locale.getIndex() == index) {
                        getPlugin().getPlayerInfo(p).setLocale(locale.getLocaleName());
                        p.sendMessage(TextFormat.GREEN + getLocale(p).generalSuccess);
                        return true;
                    }
                }
                // Not in the list
                p.sendMessage(TextFormat.RED + "/is lang <#>");
                displayLocales(p);
            } catch (Exception e) {
                p.sendMessage(TextFormat.RED + "/is lang <#>");
                displayLocales(p);
            }
        }
        return true;
    }

    /**
     * Shows available languages to the player
     *
     * @param player
     */
    private void displayLocales(Player player) {
        TreeMap<Integer, String> langs = new TreeMap<>();
        for (ASlocales locale : getPlugin().getAvailableLocales().values()) {
            if (!locale.getLocaleName().equalsIgnoreCase("locale")) {
                langs.put(locale.getIndex(), locale.getLanguageName() + " (" + locale.getCountryName() + ")");
            }
        }
        for (Map.Entry<Integer, String> entry : langs.entrySet()) {
            player.sendMessage(entry.getKey() + ": " + entry.getValue());
        }
    }

}
