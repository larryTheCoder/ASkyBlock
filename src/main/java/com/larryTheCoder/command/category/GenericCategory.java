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

package com.larryTheCoder.command.category;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.island.TopTen;
import com.larryTheCoder.locales.LocaleInstance;
import com.larryTheCoder.locales.LocaleManager;
import com.larryTheCoder.updater.Updater;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;

import java.util.*;

public class GenericCategory extends SubCategory {

    public GenericCategory(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public List<String> getCommands() {
        return Arrays.asList("expel", "kick", "lobby", "leave", "spawn", "locale", "protection", "settings", "top", "about", "test", "clear");
    }

    @Override
    public boolean canUse(CommandSender sender, String command) {
        switch (command.toLowerCase()) {
            case "test":
            case "clear":
                return sender.isOp();
            case "expel":
            case "kick":
                return hasPermission(sender, "is.command.expel") && sender.isPlayer();
            case "lobby":
            case "spawn":
            case "leave":
                return hasPermission(sender, "is.command.leave") && sender.isPlayer();
            case "locale":
                return hasPermission(sender, "is.command.lang") && sender.isPlayer();
            case "protection":
                return hasPermission(sender, "is.panel.protection") && sender.isPlayer();
            case "settings":
                return hasPermission(sender, "is.panel.setting") && sender.isPlayer();
            case "top":
                return hasPermission(sender, "is.topten");
            case "about":
                return true;
            case "download":
                return hasPermission(sender, "is.command.download");
            default:
                return false;
        }
    }

    @Override
    public String getDescription(String commandName) {
        switch (commandName.toLowerCase()) {
            case "kick":
                return "Kick out a member from your island.";
            case "lobby":
                return "Leave your island and teleport to server lobby.";
            case "locale":
                return "Change your preferred locale.";
            case "protection":
                return "Change your island protection settings.";
            case "settings":
                return "Change your island preferred settings.";
            case "top":
                return "Shows top ten islands with highest points.";
            case "about":
                return "About this plugin and its version.";
            default:
                return null;
        }
    }

    @Override
    public String getParameters(String commandName) {
        switch (commandName.toLowerCase()) {
            case "expel":
            case "kick":
                return "[Player Name]";
            case "locale":
                return "[Locale]";
            default:
                return "";
        }
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String[] args) {
        Player p = Server.getInstance().getPlayer(sender.getName());

        switch (args[0].toLowerCase()) {
            case "clear":
                getPlugin().getFastCache().clearSavedCaches();
                break;
            case "test":
                break;
            case "expel":
            case "kick":
                if (getPlugin().getIslandManager().checkIsland(p)) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorNoIsland);
                    break;
                } else if (args.length != 2) {
                    break;
                }
                if (getPlugin().getServer().getPlayer(args[1]) == null) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorOfflinePlayer);
                    break;
                }

                getPlugin().getIslandManager().kickPlayerByName(p, args[1]);
                break;
            case "lobby":
            case "spawn":
            case "leave":
                if (getPlugin().getLevel().stream()
                        .noneMatch(i -> i.getLevel().getName().equalsIgnoreCase(p.getLevel().getName()))) {
                    sender.sendMessage(getPrefix() + getLocale(p).errorWrongWorld);
                    break;
                }

                if (!p.isOp() && p.getGamemode() != 0) p.setGamemode(0);
                //if (Settings.saveInventory) getPlugin().getInventory().loadPlayerInventory(p);

                p.teleport(Location.fromObject(getPlugin().getServer().getDefaultLevel().getSafeSpawn()));
                break;
            case "locale":
                if (args.length < 2) {
                    displayLocales(p);
                    break;
                }

                HashMap<Locale, LocaleInstance> registeredLocale = getPlugin().getLocaleManager().getRegisteredLocales();
                if (!Utils.isNumeric(args[1])) {
                    displayLocales(p);
                    break;
                } else {
                    final int index = Integer.parseInt(args[1]);
                    if (index < 1 || index > registeredLocale.size()) {
                        displayLocales(p);
                        break;
                    }

                    LocaleInstance locale = registeredLocale
                            .values().stream()
                            .filter(i -> i.getIndex() == index)
                            .findAny().orElse(null);

                    // Recheck again if there the index is not in the list.
                    if (locale == null) {
                        displayLocales(p);
                        return;
                    }

                    // Now we update them into the list.
                    getPlugin().getFastCache().getPlayerData(p.getName(), pd -> {
                        if (pd == null) {
                            p.sendMessage("An error just occurred while interpreting the command.");
                            return;
                        }

                        pd.setLocale(locale.getLocaleName());
                        pd.saveData();

                        p.sendMessage(String.format("Successfully marked %s as your default locale", locale.getLocaleName()));
                    });
                }
                break;
            case "protection":
                getPlugin().getPanel().addProtectionOverlay(p);
                break;
            case "settings":
                getPlugin().getPanel().addSettingFormOverlay(p);
                break;
            case "top":
                TopTen.topTenShow(sender);
                break;
            case "about":
                Properties prep = getPlugin().getGitInfo();
                sender.sendMessage("§aASkyBlock, §eUnparalleled Innocent, §7Quality > Quantity.");
                sender.sendMessage("§7Version: §6v" + getPlugin().getDescription().getVersion());
                sender.sendMessage("§7Build date: §6" + prep.getProperty("git.build.time", "§cUnverified"));
                sender.sendMessage("§7GitHub link: §6" + prep.getProperty("git.remote.origin.url", "§cUnverified"));
                sender.sendMessage("§7Last commit by: §6" + prep.getProperty("git.commit.user.name", "Unknown"));
                sender.sendMessage("-- EOL");
                break;
            case "download":
                if (Updater.getUpdateStatus() == Updater.NEW_UPDATE_FOUND) {
                    sender.sendMessage(TextFormat.RED + "No new updates were found.");

                    break;
                }

                Updater.scheduleDownload(sender);
                break;
            case "status":
                // TODO: SkyBlock status and stats.
        }
    }

    private void displayLocales(Player player) {
        LocaleManager localeManager = getPlugin().getLocaleManager();
        player.sendMessage(TextFormat.GREEN + "Your default locale: " + TextFormat.YELLOW + getPlugin().getLocale(player).getLocaleName());
        player.sendMessage(TextFormat.RED + "/is lang <#>");

        TreeMap<Integer, String> locales = new TreeMap<>();
        for (LocaleInstance locale : localeManager.getRegisteredLocales().values()) {
            if (!locale.getLocaleName().equalsIgnoreCase("locale")) {
                locales.put(locale.getIndex(), locale.getLanguageName() + " (" + locale.getCountryName() + ")");
            }
        }

        for (Map.Entry<Integer, String> entry : locales.entrySet()) {
            player.sendMessage(entry.getKey() + ": " + entry.getValue());
        }
    }
}
