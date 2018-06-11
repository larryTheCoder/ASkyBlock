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
import cn.nukkit.level.Location;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.command.AdminCMD;
import com.larryTheCoder.command.ChallangesCMD;
import com.larryTheCoder.database.SqliteConn;
import com.larryTheCoder.database.JDBCUtilities;
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
 * Main class of SkyBlock Framework! Complete with API and Events. May contains
 * Nuts!
 */
public class ASkyBlock extends PluginBase {

    public static Economy econ;
    private static ASkyBlock object;
    // Arrays
    private int[] version;
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
    private ChallangesCMD cmds;
    private Messages msgs;
    private Panel panel;

    // Localization Strings
    private HashMap<String, ASlocales> availableLocales = new HashMap<>();

    /**
     * Return of ASkyBlock plug-in
     *
     * @return ASkyBlock
     */
    public static ASkyBlock get() {
        return object;
    }

    public SqliteConn getDatabase() {
        return db;
    }

    public ChatHandler getChatHandlers() {
        return chatHandler;
    }

    public InvitationHandler getInvitationHandler() {
        return invitationHandler;
    }

    public IslandManager getIsland() {
        return manager;
    }

    public GridManager getGrid() {
        return grid;
    }

    public InventorySave getInventory() {
        return inventory;
    }

    public TeamManager getTManager() {
        return managers;
    }

    public Panel getPanel() {
        return panel;
    }

    public IslandData getIslandInfo(String player) {
        return getDatabase().getIsland(player, 1);
    }

    public ChallangesCMD getChallenges() {
        return cmds;
    }

    public boolean inIslandWorld(Player p) {
        return getIsland().checkIslandAt(p.getLevel());
    }

    public PlayerData getPlayerInfo(Player player) {
        return getDatabase().getPlayerData(player.getName());
    }

    public IslandData getIslandInfo(Location location) {
        return getDatabase().getIslandLocation(location.getLevel().getName(), location.getFloorX(), location.getFloorZ());
    }

    public TeleportLogic getTeleportLogic() {
        return teleportLogic;
    }

    public Integer getIslandLevel(Player player) {
        PlayerData pd = getPlayerInfo(player);
        return pd == null ? 0 : pd.getIslandLevel();
    }

    public String getDefaultWorld() {
        return "SkyBlock";
    }

    public IslandData getIslandInfo(Player player) {
        return getIslandInfo(player.getName());
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
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }

    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1] || version[0] == version2[0]
                && version[1] == version2[1] && version[2] >= version2[2];
    }

    public int[] getVersion() {
        return version;
    }

    /**
     * Get if debugging enabled
     *
     * @return true if enabled
     */
    public boolean isDebug() {
        return ASkyBlock.object.cfg.getBoolean("debug");
    }

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
        getServer().getLogger().info(getPrefix() + "§7Enabling ASkyBlock - Founders Edition (API 22)");
        if (cfg.getBoolean("fastLoad")) {
            TaskManager.runTaskLater(this::start, 100);
        } else {
            start();
        }
        getServer().getLogger().info(getPrefix() + "§cBETA Build detected, use with precautions.");
        getServer().getLogger().info(getPrefix() + "§aASkyBlock has successfully enabled!");
    }

    public ArrayList<String> getLevels() {
        ArrayList<String> level = new ArrayList<>();
        for (WorldSettings settings : this.level) {
            level.add(settings.getLevel().getName());
        }
        return level;
    }

    private void start() {
        initIslands();
        registerObject();
    }

    public WorldSettings getSettings(String level) {
        for (WorldSettings settings : this.level) {
            if (settings.getLevel().getName().equalsIgnoreCase(level)) {
                return settings;
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        Utils.send("&7Saving islands framework");
        saveLevel(true);
        this.db.close();
        msgs.saveMessages();
        Utils.send("&cASkyBlock has successfully disabled. Goodbye");
    }

    @Override
    public Config getConfig() {
        return cfg;
    }

    private void initDatabase() {
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
        getServer().getCommandMap().register("ASkyBlock", this.cmds = new ChallangesCMD(this));
        getServer().getCommandMap().register("ASkyBlock", new AdminCMD(this));
        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        panel = new Panel(this);
        // This should be loaded first
        msgs = new Messages(this);
        msgs.loadMessages();
        getServer().getPluginManager().registerEvents(chatHandler, this);
        //pm.registerEvents(new IslandGuard(this), this);
        pm.registerEvents(new IslandListener(this), this);
        ServerScheduler pd = getServer().getScheduler();
        pd.scheduleRepeatingTask(new PluginTask(this), 20); // tick every 1 sec
    }

    /**
     * Reload every level that had generated
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

    private void registerObject() {
        Utils.send(TextFormat.GRAY + "Loading the Island Framework");
        loadV2Schematic();
        if (Settings.checkUpdate) {
            Updater.getUpdate();
        }
        manager = new IslandManager(this);
        grid = new GridManager(this);
        managers = new TeamManager(this);
        inventory = new InventorySave(this);
    }

    public String getPrefix() {
        return cfg.getString("Prefix").replace("&", "§");
    }

    public ASlocales getLocale(Player p) {
        if (p == null) {
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        PlayerData pd = this.getPlayerInfo(p);
        if (!this.getAvailableLocales().containsKey(pd.pubLocale)) {
            Utils.send("Unknown locale: " + pd.pubLocale);
            Utils.send("Using default: en-US");
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        return getAvailableLocales().get(pd.pubLocale);
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
        boolean update = false;
        File file = new File(ASkyBlock.get().getDataFolder(), "config.yml");
        Config cfgg = new Config(file, Config.YAML);
        if (!cfgg.getString("version").equalsIgnoreCase(ConfigManager.CONFIG_VERSION)) {
            Utils.send("&cOutdated config! Creating new one");
            Utils.send("&aYour old config will be renamed into config.old!");
            update = true;
        }
        if (update) {
            file.renameTo(new File(ASkyBlock.get().getDataFolder(), "config.old"));
            ASkyBlock.get().saveResource("config.yml");
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
                cfg.save();
            }

            settings.add(worldSettings);
            loadedLevel.add(levelName);
        }
        this.level = settings;
    }

    private void loadV2Schematic() {
        File schematicFolder = new File(getDataFolder(), "schematics");

        // Works well
        schematics = new SchematicHandler(this, schematicFolder);
    }

    public Messages getMessages() {
        return msgs;
    }

    public HashMap<String, ASlocales> getAvailableLocales() {
        return availableLocales;
    }

    public void setAvailableLocales(HashMap<String, ASlocales> availableLocales) {
        this.availableLocales = availableLocales;
    }

    public SchematicHandler getSchematics() {
        return schematics;
    }
}
