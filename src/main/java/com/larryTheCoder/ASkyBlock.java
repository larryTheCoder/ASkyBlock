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
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginDescription;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.command.Admin;
import com.larryTheCoder.command.Quests;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.database.config.MySQLConfig;
import com.larryTheCoder.database.config.SQLiteConfig;
import com.larryTheCoder.database.database.MysqlConnection;
import com.larryTheCoder.database.database.SqlConnection;
import com.larryTheCoder.integration.economy.Economy;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.listener.ChatHandler;
import com.larryTheCoder.listener.IslandListener;
import com.larryTheCoder.listener.LavaCheck;
import com.larryTheCoder.listener.PlayerEvent;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.panels.Panel;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.player.TeamManager;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.schematic.SchematicHandler;
import com.larryTheCoder.storage.InventorySave;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.task.LevelCalcTask;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.ConfigManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.updater.Updater;
import ru.nukkit.dblib.nukkit.DbLibPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * High quality SkyBlock mainframe
 * Fully documented for better looking
 * in your eyes
 *
 * @author larryTheCoder
 */
public class ASkyBlock extends PluginBase {

    // This function is to keep track beta build
    // So I could barely see which is the thingy is used
    private boolean betaBuild;
    private String buildNumber;

    public static Economy econ;
    private static ASkyBlock object;
    // Arrays
    public ArrayList<WorldSettings> level = new ArrayList<>();
    public final ArrayList<String> loadedLevel = new ArrayList<>();
    private SchematicHandler schematics;
    // Configs
    private Config cfg;
    private Config worldConfig;
    // Managers
    private Database db = null;
    private ChatHandler chatHandler;
    private InvitationHandler invitationHandler;
    private IslandManager manager;
    private GridManager grid;
    private InventorySave inventory;
    private TeamManager managers;
    private TeleportLogic teleportLogic;
    private Quests challengesCommand;
    private Messages messageModule;
    private Panel panel;

    private boolean disabled = false;
    // Localization Strings
    private HashMap<String, ASlocales> availableLocales = new HashMap<>();

    @Override
    public void onLoad() {
        if (object == null) {
            object = this;
        }
        // Init this config
        initConfig();
        // Register generator
        Generator.addGenerator(SkyBlockGenerator.class, "island", SkyBlockGenerator.TYPE_SKYBLOCK);
        // Register TaskManager
        TaskManager.IMP = new TaskManager();
    }

    @Override
    public void onEnable() {
        // A simple problem while new plugin is placed on server
        initDependency();
        initDatabase();
        // A Simple issue could cause a huge problem
        if (disabled) {
            return;
        }

        // Wohooo! Fast! Unique and Colorful!
        generateLevel(); // Regenerate The world
        getServer().getLogger().info(getPrefix() + "§7Enabling ASkyBlock - Founders Edition (API 24)");
        if (cfg.getBoolean("fastLoad")) {
            TaskManager.runTaskLater(this::start, 100);
        } else {
            start();
        }
        // Beta build, we test things here, be safe
        if (isBetaBuild()) {
            test();
            getServer().getLogger().info(getPrefix() + "§cBETA Build detected, use with precautions.");
        }
        getServer().getLogger().info(getPrefix() + "§aASkyBlock has successfully enabled!");
    }

    @Override
    public void onDisable() {
        Utils.send("&7Saving islands framework");
        saveLevel(true);
        this.db.close();
        messageModule.saveMessages();
        LavaCheck.clearStats();
        Utils.send("&cASkyBlock has successfully disabled. Goodbye");
    }

    @Override
    public Config getConfig() {
        return cfg;
    }

    private void test() {
  
    }

    private void initDependency() {
        try {
            Plugin plugin = getServer().getPluginManager().getPlugin("DbLib");
            if (plugin instanceof DbLibPlugin) {
                DbLibPlugin dblib = (DbLibPlugin) plugin;
                PluginDescription description = dblib.getDescription();
                // TODO: compare them
            }
        } catch (ClassCastException ex) {
            Utils.send("&cNo valid DbLib plugin were found...");
            getServer().getPluginManager().disablePlugin(this);
            disabled = true;
        }
    }

    private void start() {
        initIslands();
        registerObject();
    }

    private void initDatabase() {
        if (disabled) {
            return;
        }
        // To be done: DbLib defined database, JSON, YML
        // Warning: MySQL Database may result an error while attempting to create a connection
        //          With the server because there is no error in it
        boolean fireSql = false;
        if (cfg.getString("database.connection").equalsIgnoreCase("mysql")) {
            try {
                MySQLConfig config = new MySQLConfig(cfg.getString("database.MySQL.host"), cfg.getInt("database.MySQL.port"), cfg.getString("database.MySQL.database"), cfg.getString("database.MySQL.username"), cfg.getString("database.MySQL.password"));
                db = new MysqlConnection(this, config, cfg.getString("prefix", ""));
                return;
            } catch (SQLException | ClassNotFoundException ex) {
                Utils.send("§cUnable to create a connection with MySQL Server... Please make sure that your server is up and running or check your config again.");
                fireSql = true;
            }
        } else {
            try {
                SQLiteConfig config = new SQLiteConfig(new File(getDataFolder(), cfg.getString("database.SQLite.file-name") + ".db"));
                db = new SqlConnection(this, config);
            } catch (SQLException | ClassNotFoundException ex) {
                Utils.send("§cUnable to create a connection with the database for SQLite, please check back your config!");
            }
        }

        if (fireSql) {
            try {
                SQLiteConfig config = new SQLiteConfig(new File(getDataFolder(), cfg.getString("database.SQLite.file-name") + ".db"));
                db = new SqlConnection(this, config);
            } catch (SQLException | ClassNotFoundException ex) {
                Utils.send("§cUnable to create a connection with the database for SQLite, please check back your config!");
                Utils.send("§cERROR: NO DATABASE PROVIDER WERE FOUND, PLEASE FIX THIS ISSUE, PLUGIN WILL DISABLE NOW.");
                getServer().getPluginManager().disablePlugin(this);
                disabled = true;
            }
        }
    }

    /**
     * Load every islands Components
     */
    private void initIslands() {
        getServer().getCommandMap().register("ASkyBlock", new Commands(this));
        getServer().getCommandMap().register("ASkyBlock", this.challengesCommand = new Quests(this));
        getServer().getCommandMap().register("ASkyBlock", new Admin(this));
        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        panel = new Panel(this);
        // This should be loaded first
        messageModule = new Messages(this);
        messageModule.loadMessages();
        //new LevelCalcTask(this);

        pm.registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        pm.registerEvents(new LavaCheck(this), this);
        pm.registerEvents(new PlayerEvent(this), this);
        ServerScheduler pd = getServer().getScheduler();
        pd.scheduleRepeatingTask(new PluginTask(this), 20); // tick every 1 sec
    }

    private void registerObject() {
        Utils.send(TextFormat.GRAY + "Loading the Island Framework");
        schematics = new SchematicHandler(this, new File(getDataFolder(), "schematics"));
        if (Settings.checkUpdate) {
            Updater.getUpdate();
        }
        manager = new IslandManager(this);
        grid = new GridManager(this);
        managers = new TeamManager(this);
        inventory = new InventorySave();
    }

    private void initConfig() {
        initGitCheckup();
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        Utils.EnsureDirectory(Utils.SCHEMATIC_DIRECTORY);
        if (getResource("config.yml") != null) {
            saveResource("config.yml");
        }
        if (getResource("worlds.yml") != null) {
            saveResource("worlds.yml");
        }
        if (getResource("quests.yml") != null) {
            saveResource("quests.yml");
        }

        saveResource("schematics/island.schematic", false);
        saveResource("schematics/featured.schematic", false);
        saveResource("schematics/double.schematic", false);
        saveResource("schematics/harder.schematic", false);
        saveResource("schematics/nether.schematic", false);

        cfg = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        worldConfig = new Config(new File(getDataFolder(), "worlds.yml"), Config.YAML);
        recheck();
        ConfigManager.load();
    }

    private void initGitCheckup() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
        } catch (IOException e) {
            getServer().getLogger().info("§cCannot load git loader for this ASkyBlock");
            // Wtf? Maybe this user is trying to using unofficial build of ASkyBlock?
            // Or they just wanna to create a PR to do a fix?
            // Hmm we will never know
            return;
        }
        // To developers: Don't remove this please.
        betaBuild = true;
        buildNumber = properties.getProperty("git.commit.id.abbrev", "");
        getServer().getLogger().info("§7ASkyBlock build-number: " + getBuildNumber());
        getServer().getLogger().info("§7ASkyBlock commit-number: " + properties.getProperty("git.commit.id"));
    }

    private void recheck() {
        File file = new File(ASkyBlock.get().getDataFolder(), "config.yml");
        Config config = new Config(file, Config.YAML);
        if (!config.getString("version").equalsIgnoreCase(ConfigManager.CONFIG_VERSION)) {
            file.renameTo(new File(ASkyBlock.get().getDataFolder(), "config.old"));
            ASkyBlock.get().saveResource("config.yml");
            Utils.send("&cOutdated config! Creating new one");
            Utils.send("&aYour old config will be renamed into config.old!");
        }
        cfg.reload(); // Reload the config
    }

    private void generateLevel() {
        if (!Server.getInstance().isLevelGenerated("SkyBlock")) {
            Server.getInstance().generateLevel("SkyBlock", 0, SkyBlockGenerator.class);
        }
        if (!Server.getInstance().isLevelLoaded("SkyBlock")) {
            Server.getInstance().loadLevel("SkyBlock");
        }
        List<String> levels = db.getWorlds();
        if (!levels.contains("SkyBlock")) {
            levels.add("SkyBlock");
        }

        ArrayList<WorldSettings> settings = new ArrayList<>();
        for (String levelName : levels) {
            if (!Server.getInstance().isLevelGenerated(levelName)) {
                Server.getInstance().generateLevel(levelName, 0, SkyBlockGenerator.class);
            }
            if (!Server.getInstance().isLevelLoaded(levelName)) {
                Server.getInstance().loadLevel(levelName);
            }

            Level level = getServer().getLevelByName(levelName);
            WorldSettings worldSettings;
            Config cfg = worldConfig;
            if (cfg.isSection(levelName)) {
                ConfigSection section = cfg.getSection(levelName);
                String permission = section.getString("permission");
                int maxPlot = section.getInt("maxHome");
                int plotSize = section.getInt("plotSize");
                int plotRange = section.getInt("protectionRange");
                boolean stopTime = section.getBoolean("stopTime");
                int seaLevel = section.getInt("seaLevel");
                if (stopTime) {
                    Level world = getServer().getLevelByName(levelName);
                    world.setTime(1600);
                    world.stopTime();
                }
                if (plotRange % 2 != 0) {
                    plotRange--;
                    Utils.send("Protection range must be even, using " + plotRange);
                }
                if (plotRange > plotSize) {
                    Utils.send("Protection range cannot be > island distance. Setting them to be half equal.");
                    plotRange = plotSize / 2; // Avoiding players from CANNOT break their island
                }
                if (plotRange < 0) {
                    plotRange = 0;
                }
                worldSettings = new WorldSettings(permission, level, maxPlot, plotSize, plotRange, stopTime, seaLevel);
            } else {
                // Default arguments
                String permission = "";
                int plotSize = 100;
                int plotRange = 200;
                int seaLevel = 3;
                worldSettings = new WorldSettings(permission, level, 5, plotSize, plotRange, false, seaLevel);
                cfg.set(levelName + ".permission", permission);
                cfg.set(levelName + ".maxHome", 5);
                cfg.set(levelName + ".plotSize", plotSize);
                cfg.set(levelName + ".protectionRange", plotRange);
                cfg.set(levelName + ".stopTime", false);
                cfg.set(levelName + ".seaLevel", seaLevel);
                cfg.set(levelName + ".USE_CONFIG_CHEST", true);
                cfg.save();
            }

            settings.add(worldSettings);
            loadedLevel.add(levelName);
        }
        this.level = settings;
    }

    /**
     * Get if debugging enabled,
     * maybe not worth it
     *
     * @return true if enabled
     */
    public boolean isDebug() {
        return cfg.getBoolean("debug");
    }

    /**
     * Get the list of levels that used
     * for SkyBlock worlds
     *
     * @return A list of string
     */
    public ArrayList<String> getLevels() {
        ArrayList<String> level = new ArrayList<>();
        for (WorldSettings settings : this.level) {
            level.add(settings.getLevel().getName());
        }
        return level;
    }

    /**
     * Get the level settings for the
     * SkyBlock world. Null if the world
     * isn't a skyblock world or the world
     * doesn't have a settings
     *
     * @param level String
     * @return WorldSettings
     */
    public WorldSettings getSettings(String level) {
        for (WorldSettings settings : this.level) {
            if (settings.getLevel().getName().equalsIgnoreCase(level)) {
                return settings;
            }
        }
        return null;
    }

    /**
     * Return of the plugin prefix
     * that made for consoles and chat messages
     *
     * @return String
     */
    public String getPrefix() {
        return cfg.getString("Prefix").replace("&", "§");
    }

    /**
     * Get the preferred locale for a player
     * If the player is null, default will be used
     *
     * @param p Player
     * @return ASlocales class
     */
    public ASlocales getLocale(Player p) {
        if (p == null) {
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        PlayerData pd = this.getPlayerInfo(p);
        if (!this.getAvailableLocales().containsKey(pd.getLocale())) {
            Utils.send("&cUnknown locale: &e" + pd.getLocale());
            Utils.send("&cUsing default: &een_US");
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        return getAvailableLocales().get(pd.getLocale());
    }

    /**
     * Reload every level that had generated
     *
     * @param showEnd Check if there should be a stop message
     */
    public void saveLevel(boolean showEnd) {
        if (showEnd) {
            Utils.send("&7Saving worlds...");
        }
        ArrayList<String> level = new ArrayList<>();
        for (WorldSettings settings : this.level) {
            level.add(settings.getLevel().getName());
        }
        boolean result = this.db.saveWorlds(level);
        if (!result) {
            Utils.send("&cUnable to save the world.");
        }
    }

    /**
     * Get the status of this plugin
     * Check if its a beta build or not
     *
     * @return bool
     */
    public boolean isBetaBuild() {
        return betaBuild;
    }

    /**
     * Check the build number for this plugin.
     * Every build number is different based on the
     * git commit file
     *
     * @return string
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * Return the messages module
     *
     * @return Messages class
     */
    public Messages getMessages() {
        return messageModule;
    }

    /**
     * Get all of the available locales
     * for the plugin
     *
     * @return HashMap that contains String and ASlocales
     */
    public HashMap<String, ASlocales> getAvailableLocales() {
        return availableLocales;
    }

    /**
     * Add a locale for this server
     * You could use this if you wanted to make it
     * private. Which its useless
     *
     * @param availableLocales HashMap that contains String and ASlocales
     */
    public void setAvailableLocales(HashMap<String, ASlocales> availableLocales) {
        this.availableLocales = availableLocales;
    }

    /**
     * Returns the version 2 schematics handler
     *
     * @return SchematicHandler
     */
    public SchematicHandler getSchematics() {
        return schematics;
    }

    /**
     * Return of ASkyBlock plugin instance
     *
     * @return ASkyBlock
     */
    public static ASkyBlock get() {
        return object;
    }

    /**
     * Get the database handler for this plugin
     *
     * @return Database that associated with them
     */
    public Database getDatabase() {
        return db;
    }

    /**
     * Get the chat handler for coop islands
     *
     * @return ChatHandler class
     */
    public ChatHandler getChatHandlers() {
        return chatHandler;
    }

    /**
     * Get the invitation handler for coop islands
     * This handles the timing for each users
     *
     * @return InvitationHandler class
     */
    public InvitationHandler getInvitationHandler() {
        return invitationHandler;
    }

    /**
     * Get the island manager that controls all
     * aspect of the island
     *
     * @return IslandManager class
     */
    public IslandManager getIsland() {
        return manager;
    }

    /**
     * Get the grid manager, this is used to
     * check if the island is orderly in grid or not.
     *
     * @return GridManager class
     */
    public GridManager getGrid() {
        return grid;
    }

    /**
     * Get the inventory save class which handles
     * all the transaction when player teleports to
     * their islands
     *
     * @return InventorySave
     */
    public InventorySave getInventory() {
        return inventory;
    }

    /**
     * Coop class for team management
     * I still writing script for this class and
     * it not mean to used in public server so do
     * not use it.
     *
     * @return TeamManager class
     */
    public TeamManager getTManager() {
        return managers;
    }

    /**
     * Get the panel class, this class
     * control the FormPanel for the plugin
     * and manage island by easy interface
     *
     * @return Panel
     */
    public Panel getPanel() {
        return panel;
    }

    /**
     * Get the island info by the player class
     * You can use this or use string instead.
     *
     * @param player The player class
     * @return IslandData class
     */
    public IslandData getIslandInfo(Player player) {
        return getIslandInfo(player.getName());
    }

    /**
     * Get the island info by the position
     * that available
     *
     * @param pos the position to be checked
     * @return Island data of the location
     */
    public IslandData getIslandInfo(Position pos) {
        return getDatabase().getIslandLocation(pos.getLevel().getName(), pos.getFloorX(), pos.getFloorZ());
    }

    /**
     * Get the data for the player by
     * string
     *
     * @param player The player name
     * @return IslandData if the data is available otherwise null
     */
    public IslandData getIslandInfo(String player) {
        return getDatabase().getIsland(player, 1);
    }

    /**
     * Get the challenges module for this
     * plugin. This class is still confirming its
     * credibility and being checked for any bugs
     * and error
     *
     * @return Quests class
     */
    public Quests getChallenges() {
        return challengesCommand;
    }

    /**
     * Check if the player is inside the skyblock
     * world.
     *
     * @param p The player class, not string
     * @return true if the player is inside the world
     */
    public boolean inIslandWorld(Player p) {
        return getIsland().checkIslandAt(p.getLevel());
    }

    /**
     * Get the data for the player
     * Each data is being stored centrally
     *
     * @param player The player class
     * @return PlayerData class
     */
    public PlayerData getPlayerInfo(Player player) {
        return getDatabase().getPlayerData(player.getName());
    }

    /**
     * The teleport logic for the teleportation between worlds
     * This ensure that player isn't fall into the doom.
     *
     * @return TeleportLogic class
     */
    public TeleportLogic getTeleportLogic() {
        return teleportLogic;
    }

    /**
     * Get the island level for the player
     * Coop function that still barely not working
     *
     * @param player The player of their island
     * @return The value of the island level
     */
    public Integer getIslandLevel(Player player) {
        PlayerData pd = getPlayerInfo(player);
        return pd == null ? 0 : pd.getIslandLevel();
    }

    /**
     * Get the default world that being used for this
     * plugin.
     *
     * @return String
     */
    public String getDefaultWorld() {
        return "SkyBlock";
    }
}
