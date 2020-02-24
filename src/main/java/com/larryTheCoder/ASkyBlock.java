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
import com.larryTheCoder.command.Admin;
import com.larryTheCoder.command.Quests;
import com.larryTheCoder.db2.DatabaseManager;
import com.larryTheCoder.db2.config.AbstractConfig;
import com.larryTheCoder.db2.config.MySQLConfig;
import com.larryTheCoder.db2.config.SQLiteConfig;
import com.larryTheCoder.integration.economy.Economy;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.island.TopTen;
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
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.ConfigManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.updater.Updater;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.data.Table;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.larryTheCoder.db2.TableSet.*;

/**
 * High quality SkyBlock mainframe
 * Fully documented for better looking
 * in your eyes
 *
 * @author larryTheCoder
 */
public class ASkyBlock extends PluginBase {

    public static Economy econ;
    private static ASkyBlock object;
    public final ArrayList<String> loadedLevel = new ArrayList<>();
    // Arrays
    public ArrayList<WorldSettings> level = new ArrayList<>();

    private SchematicHandler schematics;

    // Configs
    private Config cfg;
    private Config worldConfig;

    // Managers
    private DatabaseManager db = null;
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

    /**
     * Return of ASkyBlock plugin instance
     *
     * @return ASkyBlock
     */
    public static ASkyBlock get() {
        return object;
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
        // A Simple issue could cause a huge problem
        if (disabled || !initDatabase()) {
            return;
        }

        // Wohooo! Fast! Unique and Colorful!
        generateLevel(); // Regenerate The world
        getServer().getLogger().info(getPrefix() + "§7Loading ASkyBlock - Founders Edition (API 25)");

        // Only defaults
        initIslands();
        registerObject();

        getServer().getLogger().info(getPrefix() + "§aASkyBlock has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        Utils.send("&7Saving all island framework...");

        saveLevel(true);
        getDatabase().shutdownDB();
        getMessages().saveMessages();
        LavaCheck.clearStats();
        TopTen.topTenSave();
        getTManager().saveData();

        Utils.send("&cASkyBlock has been successfully disabled. Goodbye!");
    }

    @Override
    public Config getConfig() {
        return cfg;
    }

    private boolean initDatabase() {
        if (disabled) {
            return false;
        }
        String connectionType = cfg.getString("database.connection");
        AbstractConfig dbConfig;

        if (connectionType.equalsIgnoreCase("mysql")) {
            dbConfig = new MySQLConfig(cfg);
        } else {
            dbConfig = new SQLiteConfig(cfg);
        }

        try {
            db = new DatabaseManager(dbConfig);

            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
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
        // new LevelCalcTask(this);
        //TopTen.topTenLoad();

        pm.registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        pm.registerEvents(new LavaCheck(this), this);
        pm.registerEvents(new PlayerEvent(this), this);
        ServerScheduler pd = getServer().getScheduler();
        pd.scheduleRepeatingTask(new PluginTask(this), 20); // tick every 1 sec
    }

    private void registerObject() {
        Utils.send(TextFormat.GRAY + "Loading all island framework. Please wait...");
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
        //initGitCheckup();
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        Utils.EnsureDirectory(Utils.SCHEMATIC_DIRECTORY);

        // Use common sense on every damn thing
        saveResource("config.yml");
        saveResource("worlds.yml");
        saveResource("quests.yml");
        saveResource("blockvalues.yml", true);
        saveResource("schematics/island.schematic");
        saveResource("schematics/featured.schematic");
        saveResource("schematics/double.schematic");
        saveResource("schematics/harder.schematic");
        saveResource("schematics/nether.schematic");

        cfg = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        worldConfig = new Config(new File(getDataFolder(), "worlds.yml"), Config.YAML);
        recheck();
        ConfigManager.load();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void recheck() {
        File file = new File(ASkyBlock.get().getDataFolder(), "config.yml");
        Config config = new Config(file, Config.YAML);
        if (!Utils.isNumeric(config.get("version")) || config.getInt("version", 0) < 1) {
            file.renameTo(new File(ASkyBlock.get().getDataFolder(), "config.old"));
            ASkyBlock.get().saveResource("config.yml");
            Utils.send("&cYour configuration file is outdated! We are creating you new one, please wait...");
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

        db.pushQuery((connection) -> {
            List<String> levels = new ArrayList<>();
            try (Query query = connection.createQuery(FETCH_WORLDS.getQuery())) {
                Table table = query.executeAndFetchTable();

                table.rows().forEach(i -> levels.add(i.getString("worldName")));
            }

            if (!levels.contains("SkyBlock")) levels.add("SkyBlock");

            ArrayList<WorldSettings> settings = new ArrayList<>();
            for (String levelName : levels) {
                Utils.loadLevelSeed(levelName);

                Level level = getServer().getLevelByName(levelName);
                WorldSettings worldSettings;
                if (worldConfig.isSection(levelName)) {
                    ConfigSection section = worldConfig.getSection(levelName);
                    worldSettings = WorldSettings.builder()
                            .permission(section.getString("permission"))
                            .plotMax(section.getInt("maxHome"))
                            .plotSize(section.getInt("plotSize"))
                            .plotRange(section.getInt("protectionRange"))
                            .stopTime(section.getBoolean("stopTime"))
                            .useDefaultChest(section.getBoolean("useDefaultChest"))
                            .seaLevel(section.getInt("seaLevel"))
                            .level(level)
                            .build();

                    worldSettings.verifyWorldSettings();
                } else {
                    worldSettings = new WorldSettings(level);

                    worldSettings.saveConfig(cfg);
                }

                settings.add(worldSettings);
                loadedLevel.add(levelName);
            }
            this.level = settings;
        });
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
     * @param p Player|null
     * @return ASlocales class
     */
    public ASlocales getLocale(Player p) {
        if (p == null) {
            return getLocale("");
        }
        return getLocale(p.getName());
    }

    /**
     * Get the preferred locale for a player
     * If the player is null, default will be used
     *
     * @param p Player name
     * @return ASlocales class
     */
    public ASlocales getLocale(String p) {
        if (p == null || p.isEmpty()) {
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        PlayerData pd = getPlayerInfo(p);
        if (!this.getAvailableLocales().containsKey(pd.getLocale())) {
            Utils.send("&cUnknown locale: &e" + pd.getLocale());
            Utils.send("&cSwitching to default: &een_US");
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
        if (showEnd) Utils.send("&eSaving worlds...");

        db.pushQuery((connection) -> {
            try (Query queue = connection.createQuery(WORLDS_INSERT.getQuery())) {
                for (WorldSettings settings : this.level) {
                    queue.addParameter("levelName", settings.getLevel().getName());
                    queue.executeUpdate();
                }
            } catch (Exception err) {
                err.printStackTrace();

                Utils.send("&cUnable to save the world.");
            }
        });
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
     * Get the database handler for this plugin
     *
     * @return Database that associated with them
     */
    public DatabaseManager getDatabase() {
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
        int x = pos.getFloorX();
        int z = pos.getFloorZ();
        String levelName = pos.getLevel().getName();

        int id = getIsland().generateIslandKey(x, z, levelName);
        Connection conn = getDatabase().getConnection();

        Table levelPlot = conn.createQuery(FETCH_LEVEL_PLOT.getQuery())
                .addParameter("levelName", levelName)
                .addParameter("islandId", id)
                .executeAndFetchTable();

        if (levelPlot.rows().isEmpty()) {
            return new IslandData(levelName, x, z, getSettings(levelName).getProtectionRange());
        }

        return IslandData.fromRows(levelPlot.rows().get(0));
    }

    public IslandData getIslandInfo(String player) {
        return getIslandInfo(player, 1);
    }

    /**
     * Get the data for the player by
     * string
     *
     * @param player The player name
     * @return IslandData if the data is available otherwise null
     */
    public IslandData getIslandInfo(String player, int homeCount) {
        Connection conn = getDatabase().getConnection();

        Table levelPlot = conn.createQuery(FETCH_ISLAND_PLOT.getQuery())
                .addParameter("pName", player)
                .addParameter("islandId", homeCount)
                .executeAndFetchTable();

        if (levelPlot.rows().isEmpty()) {
            return null;
        }

        return IslandData.fromRows(levelPlot.rows().get(0));
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
        return getPlayerInfo(player.getName());
    }

    /**
     * Get the data for the player
     * Each data is being stored centrally
     *
     * @param player The player name
     * @return PlayerData class
     */
    public PlayerData getPlayerInfo(String player) {
        Connection conn = getDatabase().getConnection();

        Table data = conn.createQuery(FETCH_PLAYER_MAIN.getQuery())
                .addParameter("plotOwner", player)
                .executeAndFetchTable();

        if (data.rows().isEmpty()) {
            return null;
        }

        return PlayerData.fromRows(data.rows().get(0));
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
