/*
 * Copyright (C) 2016-2018 Adam Matthew
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
 *
 */
package com.larryTheCoder;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.command.AdminCMD;
import com.larryTheCoder.command.ChallangesCMD;
import com.larryTheCoder.database.JDBCUtilities;
import com.larryTheCoder.database.SqliteConn;
import com.larryTheCoder.database.variables.MySQLDatabase;
import com.larryTheCoder.database.variables.SQLiteDatabase;
import com.larryTheCoder.economy.Economy;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.listener.ChatHandler;
import com.larryTheCoder.listener.IslandListener;
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
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.ConfigManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.updater.Updater;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Adam Matthew
 * <p>
 * High quality SkyBlock mainframe
 * Fully documented for better looking
 * in your eyes
 */
public class ASkyBlock extends PluginBase {

    // This function is to keep track beta build
    // So I could barely see which is the thingy is used
    private boolean betaBuild = true;
    private boolean betaInsider = false;

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
    private SqliteConn db = null;
    private ChatHandler chatHandler;
    private InvitationHandler invitationHandler;
    private IslandManager manager;
    private GridManager grid;
    private InventorySave inventory;
    private TeamManager managers;
    private TeleportLogic teleportLogic;
    private ChallangesCMD challengesCommand;
    private Messages messageModule;
    private Panel panel;

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
        initDatabase();
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
        Utils.send("&cASkyBlock has successfully disabled. Goodbye");
    }

    @Override
    public Config getConfig() {
        return cfg;
    }

    private void test() {
    }

    private void start() {
        initIslands();
        registerObject();
    }

    private void initDatabase() {
        // TODO: Varieties of database types
        if (cfg.getString("database.connection").equalsIgnoreCase("mysql")) {
            try {
                db = new SqliteConn(this, new MySQLDatabase(cfg.getString("database.MySQL.host"), cfg.getInt("database.MySQL.port"), cfg.getString("database.MySQL.database"), cfg.getString("database.MySQL.username"), cfg.getString("database.MySQL.password")));
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            } catch (ClassNotFoundException ex) {
                Utils.send("Unable to create MySql database");
            }
        } else {
            try {
                db = new SqliteConn(this, new SQLiteDatabase(new File(getDataFolder(), cfg.getString("database.SQLite.file-name") + ".db")));
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            } catch (ClassNotFoundException ex) {
                Utils.send("Unable to create Sqlite database");
            }
        }
    }

    /**
     * Load every islands Components
     */
    private void initIslands() {
        getServer().getCommandMap().register("ASkyBlock", new Commands(this));
        getServer().getCommandMap().register("ASkyBlock", this.challengesCommand = new ChallangesCMD(this));
        getServer().getCommandMap().register("ASkyBlock", new AdminCMD(this));
        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        panel = new Panel(this);
        // This should be loaded first
        messageModule = new Messages(this);
        messageModule.loadMessages();
        getServer().getPluginManager().registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
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
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        Utils.EnsureDirectory(Utils.SCHEMATIC_DIRECTORY);
        if (getResource("config.yml") != null) {
            saveResource("config.yml");
        }
        if (getResource("worlds.yml") != null) {
            saveResource("worlds.yml");
        }
        if (getResource("challenges.yml") != null) {
            saveResource("challenges.yml");
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
        if (!this.getAvailableLocales().containsKey(pd.pubLocale)) {
            Utils.send("&cUnknown locale: &e" + pd.pubLocale);
            Utils.send("&cUsing default: &een-US");
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        return getAvailableLocales().get(pd.pubLocale);
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
     * Check if the user is qualified to
     * Be an insider program, this only be used
     * For people who agree with the agreement
     * To use BETA build of ASkyBlock and get credit
     *
     * @return bool
     */
    public boolean isInsiderProgram() {
        return betaInsider;
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
    public SqliteConn getDatabase() {
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
     * @return Challenges class
     */
    public ChallangesCMD getChallenges() {
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
