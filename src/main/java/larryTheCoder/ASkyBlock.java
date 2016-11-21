/**
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
/**
 * IMPOVEMENTS:
 *  - Add /is home [1 - %MAX_HOME%]
 *  - Add Schematic - will took a loooooong time to build
 *  - Player locales - /is locales [String:#]
 */
package larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.level.generator.Generator;
import static cn.nukkit.level.generator.Generator.addGenerator;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import static cn.nukkit.utils.Config.YAML;
import cn.nukkit.utils.LogLevel;
import static cn.nukkit.utils.LogLevel.INFO;
import static cn.nukkit.utils.LogLevel.WARNING;
import cn.nukkit.utils.TextFormat;
import static cn.nukkit.utils.TextFormat.GREEN;
import static cn.nukkit.utils.TextFormat.RED;
import static cn.nukkit.utils.TextFormat.YELLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static larryTheCoder.ConfigManager.load;
import static larryTheCoder.PlayerDiary.InitializeDiarySystem;
import static larryTheCoder.SkyBlockGenerator.TYPE_SKYBLOCK;
import static larryTheCoder.Utils.ConsoleMsg;
import static larryTheCoder.Utils.DIRECTORY;
import static larryTheCoder.Utils.EnsureDirectory;
import static larryTheCoder.Utils.LOCALES_DIRECTORY;

import larryTheCoder.chat.ChatFormatListener;
import larryTheCoder.chat.ChatHandler;
import larryTheCoder.command.HelpSubCommand;
import larryTheCoder.command.SubCommand;
import larryTheCoder.island.Island;
import static larryTheCoder.island.Island.LoadIslands;
import static larryTheCoder.island.Island.SaveIslands;
import larryTheCoder.island.IslandData;

/**
 * @author larryTheCoder
 */
public class ASkyBlock extends PluginBase {

    // This plugin
    public static ASkyBlock object;
    // Island level
    public ArrayList<String> level = new ArrayList<>();
    public Config cfg;
    public int[] version;
    public int gracePeriod = 200;
    private Config msg;
    private ChatHandler chatHandler;
    private HashMap<String, SubCommand> commands = new HashMap<>();

    @Override
    public void onLoad() {
        if (!(object instanceof ASkyBlock)) {
            object = this;
        }
    }

    @Override
    public void onEnable() {
        initConfig();
        getServer().getLogger().info(getPrefix() + getMsg("onLoad") + getPluginVersionString());
        getServer().getLogger().info(YELLOW + "------------------------------------------------------------");
        reloadLevel();
        setGenerators();
        generateLevel();
        setDefaults();
        initIslands();
        initCommand();
        getServer().getLogger().info(YELLOW + "------------------------------------------------------------");
        getServer().getLogger().info(getPrefix() + getMsg("onEnable"));
    }
    
    public void initCommand(){
        this.getServer().getCommandMap().register("SkyBlock", new Commands(this));
    }
    
    @Override
    public void onDisable() {
        getServer().getLogger().info(GREEN + "Saving islands framework");
        SaveIslands();
        saveLevel();
        getServer().getLogger().info(RED + "ASkyBlock ~ Disabled seccessfully");
    }

     /**
     * Get the current ASkyBlock version.
     * @return current version in config or null
     */
    public int[] getVersion() {     
        return this.version;
    }
    
    @SuppressWarnings("unchecked")
    public void reloadLevel() {
        try {
            File file = new File(valueOf(DIRECTORY) + "Worlds.dat");
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f);
            level = (ArrayList) s.readObject();
        } catch (IOException | ClassNotFoundException ez) {
            // Welcome meesage :D
            getServer().getLogger().log(INFO, "Welcome to your first ASkyBlock Plugin!");
        }
    }

    public void saveLevel() {
        try {
            File file = new File(valueOf(DIRECTORY) + "Worlds.dat");
            FileOutputStream f = new FileOutputStream(file);
            try (ObjectOutputStream s = new ObjectOutputStream(f)) {
                s.writeObject(level);
            }
        } catch (Throwable ex) {
            getServer().getLogger().log(WARNING, "Unable to save worlds...");
        }
    }

    public static ASkyBlock getInstance() {
        return object;
    }
    
    public String getPluginVersionString() {
        return getDescription().getVersion();
    }

    public int[] getPluginVersion() {
        String ver = getDescription().getVersion();
        if (ver.contains("-")) {
            ver = ver.split("-")[0];
        }
        String[] split = ver.split("\\.");
        return new int[]{parseInt(split[0]), parseInt(split[1]), parseInt(split[2])};
    }

    /**
     * Check if `version` is >= `version2`.
     * @param version
     * @param version2
     * @return true if `version` is >= `version2`
     */
    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1] || version[0] == version2[0]
                && version[1] == version2[1] && version[2] >= version2[2];
    }
    
    private void setDefaults() {
        Level world = getServer().getLevelByName("SkyBlock");
        world.setTime(1600);
        world.stopTime();
        if (cfg.getBoolean("chat.use_chat_formatting") == true) {
            getServer().getPluginManager().registerEvents(new ChatFormatListener(this), this);
        }
    }

    public ChatHandler getChatHandlers() {
        return chatHandler;
    }

    /**
     * Load every islands Components
     */
    private void initIslands() {
        getServer().getLogger().info(GREEN + "Preparing the Island Framework");
        InitializeDiarySystem();
        LoadIslands();
        PluginManager pm = getServer().getPluginManager();
//        chatHandler = new ChatHandler(this);
//        getServer().getPluginManager().registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        ServerScheduler s = this.getServer().getScheduler();
        s.scheduleDelayedRepeatingTask(new Runnable() {
            int iteration = 0;

            @Override
            public void run() {
                iteration++;
                if (iteration % 30 == 0) {
                    ConsoleMsg("-------- AutoSaving Custom Data Files --------");
                    SaveIslands();
                    ConsoleMsg("--------------------------------------------------");
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
        EnsureDirectory(DIRECTORY);
        EnsureDirectory(LOCALES_DIRECTORY);
        //initLocales();        
        if (this.getResource("config.yml") != null) {
            this.saveResource("config.yml");
        }
        cfg = new Config(new File(getDataFolder(), "config.yml"), YAML);
        if (this.getResource("English.yml") != null) {
            this.saveResource("English.yml");
        }
        msg = new Config(new File(getDataFolder(), "English.yml"), YAML);
        load();
    }

//    private void initLocales() {
//        // todo for this stuff
////        FileLister fl = new FileLister(this);
////        try {
////            int index = 1;
////            for (String code : fl.list()) {
////                availableLocales.put(code, new ASlocales(this, code, index++));
////            }
////        } catch (IOException e1) {
////            getLogger().emergency("Could not add locales! Reason:\n" + e1.toString());
////        }
////        // Default is locale.yml
////        availableLocales.put("locale", new ASlocales(this, "locale", 0));
//    }
    private void generateLevel() {

        if (getServer().isLevelGenerated("SkyBlock") == false) {
            getServer().generateLevel("SkyBlock", 0xe9bcdL, SkyBlockGenerator.class);
            getServer().getLogger().info(GREEN + "Loading the Island Framework");
        }
        if (getServer().isLevelLoaded("SkyBlock") == false) {
            getServer().loadLevel("SkyBlock");

        }
        level.stream().map((world) -> {
            if (getServer().isLevelGenerated(world) == false) {
                getServer().generateLevel(world, 0xe9bcdL, SkyBlockGenerator.class);
            }
            return world;
        }).filter((world) -> (getServer().isLevelLoaded(world) == false)).forEach((world) -> {
            getServer().loadLevel(world);
        });
        level.add("SkyBlock");
    }

    private void setGenerators() {
        addGenerator(SkyBlockGenerator.class, "island", TYPE_SKYBLOCK);
    }

//    @Override
//    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//        if (cmd.getName().equalsIgnoreCase("is")) {
//            if (args.length != 0) {
//                switch (args[0].toLowerCase()) {
//                    case "help":
//                        if (!sender.hasPermission("is.command.help")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        // Header
//                        sender.sendMessage(Utils.RainbowString(getName(), "b") + " " + TextFormat.RESET + TextFormat.GREEN + getDescription().getVersion() + " help:");
//                        // This is for a Player command only
//                        if (sender.isPlayer()) {
//                            Player p = getServer().getPlayer(sender.getName());
//                            // Create island functions
//                            if (sender.hasPermission("is.create")) {
//                                // Check if player has an island or not 
//                                if (Island.checkIsland(p)) {
//                                    // If the player does have an island, the help message will show teleport
//                                    sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getMsg("help_teleport"));
//                                } else {
//                                    // if not help message will show how to create an island
//                                    sender.sendMessage(TextFormat.GREEN + "/" + label + ": " + getMsg("help_island"));
//                                }
//                            }
//                            if (sender.hasPermission("is.command.leave") && Island.checkIsland(p)) {
//                                sender.sendMessage(TextFormat.GREEN + "/" + label + " leave: " + getMsg("help_leave"));
//                            }
//                            // Reset island
//                            if (sender.hasPermission("is.command.reset") && Island.checkIsland(p)) {
//                                sender.sendMessage(TextFormat.GREEN + "/" + label + " reset: " + getMsg("help_reset"));
//                            }
//                            // Remove island
//                            if (sender.hasPermission("is.command.remove") && Island.checkIsland(p)) {
//                                sender.sendMessage(TextFormat.GREEN + "/" + label + " reset: " + getMsg("help_reset"));
//                            }
//                            // Kick / expel functions...
//                            if (sender.hasPermission("is.command.kick") && Island.checkIsland(p)) {
//                                sender.sendMessage(TextFormat.GREEN + "/" + label + " expel: " + getMsg("help_kick"));
//                            }
//                        }
//                        // Kick a player without checking players island...
//                        // Full permission for admin
//                        if (sender.hasPermission("is.admin.kick")) {
//                            sender.sendMessage(TextFormat.GREEN + "/" + label + " kick: " + getMsg("help_end"));
//                        }
//                        // generate function
//                        if (sender.hasPermission("is.admin.generate")) {
//                            sender.sendMessage(TextFormat.GREEN + "/" + label + " generate: " + getMsg("help_generate"));
//                        }
//                        // This will not using any permission :D
//                        sender.sendMessage(TextFormat.GREEN + "/" + label + " about: " + getMsg("help_about"));
//                        break;
//                    case "kick":
//                        if (!sender.hasPermission("is.admin.kick")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        if (args.length != 0 && args.length != 2) {
//                            sender.sendMessage(getMsg("end_help"));
//                            break;
//                        }
//                        Island.kickPlayerByAdmin(sender, args[1]);
//                        break;
//                    case "expel":
//                        if (!sender.isPlayer()) {
//                            sender.sendMessage(getMsg("help"));
//                            break;
//                        }
//                        if (!sender.hasPermission("is.command.kick")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        Player p = getServer().getPlayer(sender.getName());
//                        if (!Island.checkIsland(p)) {
//                            sender.sendMessage(getPrefix() + getMsg("no_island_error"));
//                            break;
//                        }
//                        // do sanity checking
//                        if (args.length != 0 && args.length != 2) {
//                            sender.sendMessage(getMsg("expel_help"));
//                            break;
//                        }
//                        Island.kickPlayerByName(p, args[1]);
//                        break;
//                    case "home":
//                        if (!sender.isPlayer()) {
//                            sender.sendMessage(getMsg("help"));
//                            break;
//                        }
//                        if (!sender.hasPermission("is.command.home")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        // do sanity checking
//                        if (args.length != 0 && args.length != 2) {
//                            sender.sendMessage(getMsg("home_help").replace("%MAX_HOME%", cfg.getString("maxHome")));
//                            break;
//                        }
//                        ConcurrentHashMap<Integer, IslandData> availableHomes = new ConcurrentHashMap<>();
//                        int i = 0;
//                        while(i < cfg.getInt("maxHome")){
//                            IslandData pd = Island.hashNameToIsland.get(sender.getName() + i);
//                            if(pd.owner != null){
//                                availableHomes.put(i, pd);
//                            } else {
//                                break;
//                            }
//                        }
//                        if(!availableHomes.contains(args[1])){
//                            //%NO_HOME%
//                        }
//                        //%TELEPORT%
//                        i++;
//                        break;
//                    case "leave":
//                        if (!sender.isPlayer()) {
//                            sender.sendMessage(getMsg("help"));
//                            break;
//                        }
//                        if (!sender.hasPermission("is.command.leave")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        Player pt = getServer().getPlayer(sender.getName());
//                        if (!pt.getLevel().getName().equalsIgnoreCase("skyblock")) {
//                            sender.sendMessage(getMsg("leave_error"));
//                            break;
//                        }
//                        pt.teleport(getServer().getDefaultLevel().getSafeSpawn());
//                        break;
//                    case "reset":
//                        if (!sender.isPlayer()) {
//                            sender.sendMessage(getMsg("help"));
//                            break;
//                        }
//                        if (!sender.hasPermission("is.command.reset")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        Player pe = getServer().getPlayer(sender.getName());
//                        if (!Island.checkIsland(pe)) {
//                            pe.sendMessage(TextFormat.RED + "You don't have an island!");
//                            return true;
//                        }
//                        if (Utils.TooSoon(pe, "ResetIsland", ConfigManager.islandDeleteSeconds)) {
//                            return true;
//                        }
//                        Island.reset(pe, false);
//                        Island.handleIslandCommand(pe);
//                        break;
//                    case "delete":
//                    case "remove":
//                        if (!sender.isPlayer()) {
//                            sender.sendMessage(getMsg("help"));
//                            break;
//                        }
//                        if (!sender.hasPermission("is.command.delete")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        Player pp = getServer().getPlayer(sender.getName());
//                        if (!Island.checkIsland(pp)) {
//                            pp.sendMessage(getMsg("no_island_error"));
//                            break;
//                        }
//                        if (Utils.TooSoon(pp, "ResetIsland", ConfigManager.islandDeleteSeconds)) {
//                            break;
//                        }
//                        for (String lvl : level) {
//                            if (pp.getLocation().getLevel().getName().equalsIgnoreCase(lvl)) {
//                                PlayerData.SetDefaults(pp);
//                                pp.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
//                            } else {
//                                pp.sendMessage(getMsg("level_error"));
//                                break;
//                            }
//                        }
//                        Island.reset(pp, true);
//                        break;
//                    case "generate":
//                        if (!sender.hasPermission("is.admin.generate")) {
//                            sender.sendMessage(getMsg("permission_error"));
//                            break;
//                        }
//                        if (args.length != 0 && args.length != 2) {
//                            sender.sendMessage(getMsg("generate_help"));
//                            break;
//                        }
//                        if (!getServer().isLevelGenerated(args[1])) {
//                            getServer().generateLevel(args[1], System.currentTimeMillis(), SkyBlockGenerator.class);
//                            getServer().loadLevel(args[1]);
//                            level.add(args[1]);
//                            sender.sendMessage(getMsg("generate").replace("[level]", args[1]));
//                            break;
//                        }
//                        sender.sendMessage(getMsg("generate_error"));
//                        break;
//                    case "about":
//                        sender.sendMessage(TextFormat.GREEN + "ASkyBlock version " + getVersion() + " implementing API 2.0.0");
//                        sender.sendMessage(TextFormat.GREEN + "Substract version:" + TextFormat.RED + " BETA");
//                        break;
//                    default:
//                        sender.sendMessage(getMsg("help"));
//                        break;
//                }
//                return true;
//            }
//            if (sender instanceof Player) {
//                if (sender.hasPermission("is.create")) {
//                    Player pl = Server.getInstance().getPlayer(sender.getName());
//                    Island.handleIslandCommand(pl);
//                    return true;
//                }
//            } else {
//                sender.sendMessage(getMsg("help"));
//            }
//            return true;
//        }
//        return true;
//    }
}
