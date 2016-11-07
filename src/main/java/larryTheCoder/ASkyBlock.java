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
package larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import larryTheCoder.chat.ChatFormatListener;
import larryTheCoder.chat.ChatHandler;
import larryTheCoder.island.Island;
import larryTheCoder.locales.ASlocales;
import larryTheCoder.locales.FileLister;

/**
 * @author larryTheCoder
 */
public class ASkyBlock extends PluginBase {

    // This plugin
    public static ASkyBlock object;
    // Island level
    public ArrayList<String> level = new ArrayList<>();
    public Config cfg;
    public String version;
    public int gracePeriod = 200;
    private Config msg;
    private ChatHandler chatHandler;
    private HashMap<String, ASlocales> availableLocales = new HashMap<String, ASlocales>();

    @Override
    public void onLoad() {
        if (!(object instanceof ASkyBlock)) {
            object = this;
        }
    }

    @Override
    public void onEnable() {
        initConfig();
        getServer().getLogger().info(getPrefix() + getMsg("onLoad") + getVersion());
        getServer().getLogger().info(TextFormat.YELLOW + "------------------------------------------------------------");
        setGenerators();
        generateLevel();
        setDefaults();
        initIslands();
        getServer().getLogger().info(TextFormat.YELLOW + "------------------------------------------------------------");
        getServer().getLogger().info(getPrefix() + getMsg("onEnable"));
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info(TextFormat.GREEN + "Saving islands framework");
        Island.SaveIslands();
        getServer().getLogger().info(TextFormat.RED + "ASkyBlock - Disabled seccessfully");
    }

    public ASkyBlock getInstance() {
        return object;
    }

    public Object getVersion() {
        return version;
    }

    private void setDefaults() {
        Level world = getServer().getLevelByName("SkyBlock");
        world.setTime(1600);
        world.stopTime();
        if (cfg.getBoolean("useCFG") == true) {
            getServer().getPluginManager().registerEvents(new ChatFormatListener(this), this);
        }
    }

    public ChatHandler getChatHadlers() {
        return chatHandler;
    }

    /**
     * Load every islands Components
     */
    private void initIslands() {
        PlayerDiary.InitializeDiarySystem();
        Island.LoadIslands();
        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        pm.registerEvents(new IslandListener(this), this);
        ServerScheduler s = this.getServer().getScheduler();
        s.scheduleDelayedRepeatingTask(new Runnable() {
            int iteration = 0;

            @Override
            public void run() {
                iteration++;
                if (iteration % 30 == 0) {
                    Utils.ConsoleMsg("-------- AutoSaving Custom Data Files --------");
                    Island.SaveIslands();
                    Utils.ConsoleMsg("--------------------------------------------------");
                }
            }

        }, gracePeriod, 500);
    }

    public String getPrefix() {
        return cfg.getString("Prefix").replaceAll("&", "§");
    }

    public String getMsg(String key) {
        String mssg = msg.getString(key).replaceAll("&", "§");
        return mssg;
    }

    private void initConfig() {
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        //initLocales();
        version = getDescription().getVersion();
        if (this.getResource("config.yml") != null) {
            this.saveResource("config.yml");
        }
        cfg = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        if (this.getResource("English.yml") != null) {
            this.saveResource("English.yml");
        }
        msg = new Config(new File(getDataFolder(), "English.yml"), Config.YAML);
    }

    private void initLocales() {
        // todo for this stuff
        FileLister fl = new FileLister(this);
        try {
            int index = 1;
            for (String code : fl.list()) {
                availableLocales.put(code, new ASlocales(this, code, index++));
            }
        } catch (IOException e1) {
            getLogger().emergency("Could not add locales! Reason:\n" + e1.toString());
        }
        // Default is locale.yml
        availableLocales.put("locale", new ASlocales(this, "locale", 0));
    }

    private void generateLevel() {
        getServer().getLogger().info(TextFormat.GREEN + "Loading the Island Framework");
        if (getServer().isLevelGenerated("SkyBlock") == false) {
            getServer().generateLevel("SkyBlock", 0xe9bcdL, SkyBlockGenerator.class);

        }
        if (getServer().isLevelLoaded("SkyBlock") == false) {
            getServer().loadLevel("SkyBlock");
        }
        level.add("SkyBlock");
    }

    private void setGenerators() {
        Generator.addGenerator(SkyBlockGenerator.class, "island", SkyBlockGenerator.TYPE_SKYBLOCK);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("is")) {
            if (args.length != 0) {
                switch (args[0].toLowerCase()) {
                    case "help":
                        if (!sender.hasPermission("is.command.help")) {
                            sender.sendMessage(getMsg("permission_error"));
                            break;
                        }
                        // Header
                        sender.sendMessage(Utils.RainbowString(getName(), "b") + " " + TextFormat.RESET + TextFormat.GREEN + getDescription().getVersion() + " help:");
                        // This is for a Player command only
                        if (!sender.isPlayer()) {
                            Player p = getServer().getPlayer(sender.getName());
                            // Create island functions
                            if (sender.hasPermission("is.create")) {
                                // Check if player has an island or not 
                                if (Island.PlayerHasIsland(p)) {
                                    // If the player does have an island, the help message will show teleport
                                    sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getMsg("help_teleport"));
                                } else {
                                    // if not help message will show how to create an island
                                    sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getMsg("help_island"));
                                }
                            }
                            // Kick / expel functions...
                            if (sender.hasPermission("is.command.kick") && Island.PlayerHasIsland(p)) {
                                sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getMsg("help_kick"));
                            }

                        }
                        // generate function
                        if (sender.hasPermission("is.command.generate")) {
                            sender.sendMessage(TextFormat.GREEN + "/" + label + " generate: " + getMsg("help_generate"));
                        }
                        // This will not using any permission :D
                        sender.sendMessage(TextFormat.GREEN + "/" + label + " about: " + getMsg("help_about"));
                        break;
                    case "kick":
                    case "expel":
                        if (!sender.isPlayer()) {
                            sender.sendMessage(getMsg("help"));
                            break;
                        }
                        if (!sender.hasPermission("is.command.kick")) {
                            sender.sendMessage(getMsg("permission_error"));
                            break;
                        }
                        Player p = getServer().getPlayer(sender.getName());
                        if (!Island.PlayerHasIsland(p)) {
                            sender.sendMessage(getPrefix() + getMsg("no_island_error"));
                            break;
                        }
                        // do sanity checking
                        if (args.length != 0 && args.length != 2) {
                            sender.sendMessage(getMsg("generate_help"));
                            break;
                        }
                        Island.kickPlayerByName(p, args[1]);
                        break;
                    case "generate":
                        if (!sender.hasPermission("is.command.generate")) {
                            sender.sendMessage(getMsg("permission_error"));
                            break;
                        }
                        if (args.length != 0 && args.length != 2) {
                            sender.sendMessage(getMsg("generate_help"));
                            break;
                        }
                        if (!getServer().isLevelGenerated(args[1])) {
                            getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
                            getServer().loadLevel(args[1]);
                            level.add(args[1]);
                            sender.sendMessage(getMsg("generate").replace("[level]", args[1]));
                            break;
                        }
                        sender.sendMessage(getMsg("generate_error"));
                        break;
                    case "about":
                        sender.sendMessage(TextFormat.GOLD + "This plugin is free software: you can redistribute");
                        sender.sendMessage(TextFormat.GOLD + "it and/or modify it under the terms of the GNU");
                        sender.sendMessage(TextFormat.GOLD + "General Public License as published by the Free");
                        sender.sendMessage(TextFormat.GOLD + "Software Foundation, either version 3 of the License,");
                        sender.sendMessage(TextFormat.GOLD + "or (at your option) any later version.");
                        sender.sendMessage(TextFormat.GOLD + "This plugin is distributed in the hope that it");
                        sender.sendMessage(TextFormat.GOLD + "will be useful, but WITHOUT ANY WARRANTY; without");
                        sender.sendMessage(TextFormat.GOLD + "even the implied warranty of MERCHANTABILITY or");
                        sender.sendMessage(TextFormat.GOLD + "FITNESS FOR A PARTICULAR PURPOSE.  See the");
                        sender.sendMessage(TextFormat.GOLD + "GNU General Public License for more details.");
                        sender.sendMessage(TextFormat.GOLD + "You should have received a copy of the GNU");
                        sender.sendMessage(TextFormat.GOLD + "General Public License along with this plugin.");
                        sender.sendMessage(TextFormat.GOLD + "If not, see <http://www.gnu.org/licenses/>.");
                        sender.sendMessage(TextFormat.GOLD + "Souce code is available on GitHub.");
                        sender.sendMessage(TextFormat.GOLD + "(c) 2015 - 2016 by larryTheCoder");
                        break;
                    default:
                        sender.sendMessage(getMsg("help"));
                        break;
                }
                return true;
            }
            if (sender instanceof Player) {
                if (sender.hasPermission("is.create")) {
                    Player pl = Server.getInstance().getPlayer(sender.getName());
                    Island.GoToIsland(pl);
                    return true;
                }
            } else {
                sender.sendMessage(getMsg("help"));
            }
            return true;
        }
        return true;
    }

}
