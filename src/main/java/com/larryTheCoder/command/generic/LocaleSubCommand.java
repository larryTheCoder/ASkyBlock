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
package com.larryTheCoder.command.generic;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.command.SubCommand;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.utils.Utils;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author larryTheCoder
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
        PlayerData pd = getPlugin().getPlayerInfo(p);
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
                        pd.setLocale(locale.getLocaleName());
                        pd.saveData();
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
