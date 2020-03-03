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
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.cmd2.Commands;
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
import com.larryTheCoder.storage.FastCache;
import com.larryTheCoder.storage.InventorySave;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.storage.WorldSettings;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.ConfigManager;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.updater.Updater;
import lombok.Getter;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.data.Table;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static com.larryTheCoder.db2.TableSet.*;

/**
 * High quality SkyBlock mainframe
 * Fully documented for better looking
 * in your eyes
 *
 * @author larryTheCoder
 */
public class ASkyBlock extends ASkyBlockAPI {

    public static Economy econ;
    private static ASkyBlock object;
    public final ArrayList<String> loadedLevel = new ArrayList<>();
    // Arrays
    @Getter
    private ArrayList<WorldSettings> level = new ArrayList<>();

    // Configs
    private Config cfg;
    private Config worldConfig;

    private boolean disabled = false;
    // Localization Strings
    private HashMap<String, ASlocales> availableLocales = new HashMap<>();
    private Properties pluginGit;

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
        getServer().getLogger().info(getPrefix() + "§7Loading ASkyBlock - Bedrock Edition (API 30)");

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
            database = new DatabaseManager(dbConfig);

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

        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        panel = new Panel(this);
        fastCache = new FastCache(this);

        // This should be loaded first
        messages = new Messages(this);
        messages.loadMessages();
        // new LevelCalcTask(this);
        //TopTen.topTenLoad();

        pm.registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        pm.registerEvents(new LavaCheck(this), this);
        pm.registerEvents(new PlayerEvent(this), this);
        ServerScheduler pd = getServer().getScheduler();
        pd.scheduleRepeatingTask(new PluginTask(this), 20); // tick every 1 sec
    }

    /**
     * Gathers an information about this plugin.
     */
    private void initGitCheckup() {
        Properties properties = new Properties();
        try {
            properties.load(getResource("git-sb.properties"));
        } catch (IOException e) {
            getServer().getLogger().info("§cERROR! We cannot load the git loader for this ASkyBlock build!");
            // Wtf? Maybe this user is trying to using unofficial build of ASkyBlock?
            // Or they just wanna to create a PR to do a fix?
            // Hmm we will never know lol
            return;
        }
        // To developers: Don't remove this please.\
        Utils.sendDebug("§7ASkyBlock Git Information:");
        Utils.sendDebug("§7Build number: " + properties.getProperty("git.commit.id.describe", ""));
        Utils.sendDebug("§7Commit number: " + properties.getProperty("git.commit.id"));

        pluginGit = properties;
    }

    public Properties getGitInfo() {
        return pluginGit;
    }

    private void registerObject() {
        Utils.send(TextFormat.GRAY + "Loading all island framework. Please wait...");
        schematics = new SchematicHandler(this, new File(getDataFolder(), "schematics"));
        if (Settings.checkUpdate) {
            Updater.getUpdate();
        }

        islandManager = new IslandManager(this);
        grid = new GridManager(this);
        tManager = new TeamManager(this);
        inventory = new InventorySave();
    }

    private void initConfig() {
        initGitCheckup();
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

        database.pushQuery((connection) -> {
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
                            .setPermission(section.getString("permission"))
                            .setPlotMax(section.getInt("maxHome"))
                            .setPlotSize(section.getInt("plotSize"))
                            .setPlotRange(section.getInt("protectionRange"))
                            .isStopTime(section.getBoolean("stopTime"))
                            .useDefaultChest(section.getBoolean("useDefaultChest"))
                            .setSeaLevel(section.getInt("seaLevel"))
                            .setLevel(level)
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
     * Get the settings of a level provided. The rightful owner of this
     * server will set the level settings.
     *
     * @param levelName String
     * @return WorldSettings
     */
    public WorldSettings getSettings(String levelName) {
        return level.stream()
                .filter(i -> i.getLevelName().equalsIgnoreCase(levelName))
                .findFirst()
                .orElse(null);
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

    public ASlocales getLocale(CommandSender sender) {
        return sender.isPlayer() ? getLocale((Player) sender) : getLocale("");
    }

    /**
     * Get the preferred locale for a player
     * If the player is null, default will be used
     *
     * @param p Player|null
     * @return ASlocales class
     */
    public ASlocales getLocale(Player p) {
        return p == null ? getLocale("") : getLocale(p.getName());
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

        database.pushQuery((connection) -> {
            try (Query queue = connection.createQuery(WORLDS_INSERT.getQuery())) {
                for (WorldSettings settings : this.level) {
                    queue.addParameter("levelName", settings.getLevelName());
                    queue.executeUpdate();
                }
            } catch (Exception err) {
                err.printStackTrace();

                Utils.send("&cUnable to save the world.");
            }
        });
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

    // DEPRECATED DUE TO OUR ASYNC TARGET VIOLATION.

    // ISLAND DATA STARTING LINE ---

    @Deprecated
    public List<IslandData> getIslandsInfo(String playerName) {
        Connection conn = getDatabase().getConnection();

        Table levelPlot = conn.createQuery(FETCH_ISLAND_PLOT.getQuery())
                .addParameter("pName", playerName)
                .executeAndFetchTable();

        List<IslandData> islandList = new ArrayList<>();
        levelPlot.rows().forEach(o -> islandList.add(IslandData.fromRows(o)));

        return islandList;
    }

    /**
     * Get an island information from a player's name
     *
     * @param player The player name itself.
     * @return The IslandData class
     */
    @Deprecated
    public IslandData getIslandInfo(String player) {
        return getIslandInfo(player, 1);
    }

    /**
     * Get a list of SkyBlock worlds/levels that are used for islands.
     *
     * @return A list of string
     */
    public ArrayList<String> getLevels() {
        ArrayList<String> level = new ArrayList<>();
        for (WorldSettings settings : this.level) {
            level.add(settings.getLevelName());
        }
        return level;
    }

    /**
     * Get an island information from a player name.
     *
     * @param player The player name
     * @return IslandData if the data is available otherwise null
     */
    @Deprecated
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

    @Deprecated
    public IslandData getIslandInfo(String player, String homeName) {
        Connection conn = getDatabase().getConnection();

        Table levelPlot = conn.createQuery(FETCH_ISLAND_PLOT.getQuery())
                .addParameter("pName", player)
                .addParameter("islandName", homeName)
                .executeAndFetchTable();

        if (levelPlot.rows().isEmpty()) {
            return null;
        }

        return IslandData.fromRows(levelPlot.rows().get(0));
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

    // PLAYER DATA STARTING LINE ---

    /**
     * Check if the player is inside the skyblock
     * world.
     *
     * @param p The player class, not string
     * @return true if the player is inside the world
     */
    public boolean inIslandWorld(Player p) {
        return getIslandManager().checkIslandAt(p.getLevel());
    }

    /**
     * Get the data for the player
     * Each data is being stored centrally
     *
     * @param player The player class
     * @return PlayerData class
     */
    @Deprecated
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
    @Deprecated
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
}
