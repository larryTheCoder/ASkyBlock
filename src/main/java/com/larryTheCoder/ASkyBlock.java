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
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.cache.FastCache;
import com.larryTheCoder.cache.settings.WorldSettings;
import com.larryTheCoder.command.Commands;
import com.larryTheCoder.database.config.AbstractConfig;
import com.larryTheCoder.database.config.MySQLConfig;
import com.larryTheCoder.database.config.SQLiteConfig;
import com.larryTheCoder.database.Database;
import com.larryTheCoder.database.QueryDb;
import com.larryTheCoder.database.QueryInfo;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.island.TeleportLogic;
import com.larryTheCoder.island.TopTen;
import com.larryTheCoder.listener.ChatHandler;
import com.larryTheCoder.listener.IslandListener;
import com.larryTheCoder.listener.LavaCheck;
import com.larryTheCoder.listener.PlayerEvent;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.locales.LocaleInstance;
import com.larryTheCoder.locales.LocaleManager;
import com.larryTheCoder.schematic.SchematicHandler;
import com.larryTheCoder.task.LevelCalcTask;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.updater.Updater;
import com.larryTheCoder.utils.ConfigManager;
import com.larryTheCoder.utils.IslandAwaitStore;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.classpath.Autoloader;
import com.larryTheCoder.utils.integration.economy.Economy;
import com.larryTheCoder.utils.integration.luckperms.InternalPermission;
import com.larryTheCoder.utils.integration.luckperms.LuckPermsPermission;
import lombok.Getter;

import java.io.File;
import java.util.*;

/**
 * @author larryTheCoder
 */
public class ASkyBlock extends ASkyBlockAPI {

    public static Economy econ;
    private static ASkyBlock object;
    public final ArrayList<String> loadedLevel = new ArrayList<>();
    // Arrays
    @Getter
    private final ArrayList<WorldSettings> level = new ArrayList<>();

    // Configs
    private Config cfg;
    private Config worldConfig;

    private boolean disabled = false;
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
        // Register autoloader
        new Autoloader("lib");

        // Then we initialize database.
        disabled = !initDatabase();
    }

    @Override
    public void onEnable() {
        // A Simple issue could cause a huge problem
        if (disabled) {
            getServer().getPluginManager().disablePlugin(this);
            getServer().getLogger().info(getPrefix() + "§cRecent exceptions from database class disabled this plugin functionality.");
            return;
        }

        try {
            // Wohooo! Fast! Unique and Colorful!
            generateLevel();
            getServer().getLogger().info(getPrefix() + "§7Loading ASkyBlock - Bedrock Edition (API 31)");

            // Only defaults
            initIslands();
            registerObject();

            getServer().getLogger().info(getPrefix() + "§aASkyBlock has been successfully enabled!");
        } catch (Exception err) {
            err.printStackTrace();

            disabled = true;

            getServer().getPluginManager().disablePlugin(this);
            getServer().getLogger().info(getPrefix() + "§cRecent exceptions from SB-Core disabled this plugin functionality.");
        }
    }

    @Override
    public void onDisable() {
        if (!disabled) {
            Utils.send("&7Saving all island framework...");

            saveLevel(true);
            getDatabase().shutdown();
            getMessages().saveMessages();
            LavaCheck.clearStats();
            getLevelCalcThread().shutdown();
            TopTen.topTenSave();
        }

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
        int parallelThreads = cfg.getInt("database.parallels-threads", 1);
        String connectionType = cfg.getString("database.connection");
        AbstractConfig dbConfig;

        if (connectionType.equalsIgnoreCase("mysql")) {
            dbConfig = new MySQLConfig(cfg);
        } else {
            dbConfig = new SQLiteConfig(cfg);
        }

        try {
            database = new Database(dbConfig, parallelThreads);

            return true;
        } catch (Throwable e) {
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
        localeManager = new LocaleManager(this);
        chatHandler = new ChatHandler(this);
        teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        panel = new ServerPanel(this);
        fastCache = new FastCache(this);

        // This should be loaded first
        messages = new Messages(this);
        messages.loadMessages();
        levelCalcThread = new LevelCalcTask(this);
        loadPermissionNodes();
        //TopTen.topTenLoad();

        IslandAwaitStore.init();

        pm.registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        pm.registerEvents(new LavaCheck(this), this);
        pm.registerEvents(new PlayerEvent(this), this);
    }

    private void loadPermissionNodes() {
        Plugin plugin = Server.getInstance().getPluginManager().getPlugin("LuckPerms");
        if (plugin == null) {
            permissionHandler = new InternalPermission();
        } else {
            permissionHandler = new LuckPermsPermission();
        }
    }

    /**
     * Gathers an information about this plugin.
     */
    private void initGitCheckup() {
        Properties properties = new Properties();
        try {
            properties.load(getResource("git-sb.properties"));
        } catch (Throwable e) {
            getServer().getLogger().info("§cERROR! We cannot load the git loader for this ASkyBlock build!");
            // Wtf? Maybe this user is trying to using unofficial build of ASkyBlock?
            // Or they just wanna to create a PR to do a fix?
            // Hmm we will never know lol
            return;
        }
        // To developers: Don't remove this please.
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

        islandManager = new IslandManager(this);
        grid = new GridManager(this);

        if (Settings.checkUpdate) Updater.getUpdate();
    }

    private void initConfig() {
        initGitCheckup();
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        Utils.EnsureDirectory(Utils.SCHEMATIC_DIRECTORY);
        Utils.EnsureDirectory(Utils.UPDATES_DIRECTORY);
        Utils.EnsureDirectory(Utils.LIBRARY_DIRECTORY);

        // Use common sense on every damn thing
        saveResource("config.yml");
        saveResource("worlds.yml");
        //saveResource("quests.yml"); // TODO
        saveResource("blocks.yml");
        saveResource("schematics/island.schematic");
        saveResource("schematics/featured.schematic");
        saveResource("schematics/double.schematic");
        saveResource("schematics/harder.schematic");
        saveResource("schematics/nether.schematic");

        cfg = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        worldConfig = new Config(new File(getDataFolder(), "worlds.yml"), Config.YAML);

        ConfigManager.load();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void recheck() {
        File file = new File(ASkyBlock.get().getDataFolder(), "config.yml");
        Config config = new Config(file, Config.YAML);
        if (!Utils.isNumeric(config.get("version")) || config.getInt("version", 0) < 2) {
            file.renameTo(new File(ASkyBlock.get().getDataFolder(), "config.old"));
            ASkyBlock.get().saveResource("config.yml");
            Utils.send("&cYour configuration file is outdated! We are creating you new one, please wait...");
            Utils.send("&aYour old config will be renamed into config.old!");
        }
        ASkyBlock.get().cfg.reload(); // Reload the config
    }

    private void generateLevel() {
        database.fetchData(new QueryInfo("SELECT * FROM worldList"))
                .whenComplete((result, error) -> {
                    Map<Integer, String> levels = new HashMap<>();

                    result.rows().forEach(i -> levels.put(i.getInteger("levelId"), i.getString("worldName")));

                    if (!levels.containsValue("SkyBlock")) {
                        levels.put(1, "SkyBlock");
                        Utils.levelProvidedId.add(1);
                    }

                    ArrayList<WorldSettings> settings = new ArrayList<>();
                    for (Map.Entry<Integer, String> values : levels.entrySet()) {
                        String levelName = values.getValue();

                        String levelSafeName = levelName.replace(" ", "_");

                        Utils.loadLevelSeed(levelName);

                        Level level = getServer().getLevelByName(levelName);
                        WorldSettings worldSettings;
                        if (worldConfig.isSection(levelSafeName)) {
                            ConfigSection section = worldConfig.getSection(levelSafeName);
                            worldSettings = WorldSettings.builder()
                                    .setPermission(section.getString("permission"))
                                    .setPlotMax(section.getInt("maxHome"))
                                    .setPlotSize(section.getInt("plotSize"))
                                    .setPlotRange(section.getInt("protectionRange"))
                                    .isStopTime(section.getBoolean("stopTime"))
                                    .useDefaultChest(section.getBoolean("useDefaultChest"))
                                    .setSeaLevel(section.getInt("seaLevel"))
                                    .setLevel(level)
                                    .setSignSettings(section.getList("signConfig"))
                                    .setLevelId(values.getKey())
                                    .build();

                            worldSettings.verifyWorldSettings();
                        } else {
                            worldSettings = new WorldSettings(level);

                            worldSettings.saveConfig(cfg);
                        }

                        settings.add(worldSettings);
                        loadedLevel.add(levelName);
                        Utils.levelProvidedId.add(values.getKey());
                    }
                    level.addAll(settings);

                    saveLevel(false);
                });
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
                .filter(i -> i.getLevel().getName().equalsIgnoreCase(levelName))
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

    public LocaleInstance getLocale(CommandSender sender) {
        return sender.isPlayer() ? getLocale((Player) sender) : getLocale("");
    }

    public LocaleInstance getLocale(Player player) {
        return getLocaleManager().getLocaleFromPlayer(player);
    }

    public LocaleInstance getLocale(String playerName) {
        return getLocaleManager().getLocaleFromPlayer(playerName);
    }

    /**
     * Reload every level that had generated
     *
     * @param showEnd Check if there should be a stop message
     */
    public void saveLevel(boolean showEnd) {
        if (disabled) {
            return;
        }

        if (showEnd) Utils.send("&eSaving island worlds data...");

        List<QueryInfo> info = new ArrayList<>();
        for (WorldSettings settings : level) {
            info.add(new QueryInfo(QueryDb.getInstance().saveWorldData)
                    .addParameter("levelName", settings.getLevel().getName())
                    .addParameter("levelId", settings.getLevelId()));
        }

        database.processBulkUpdate(info.toArray(new QueryInfo[0]));
    }

    // ISLAND DATA STARTING LINE ---

    /**
     * Get a list of SkyBlock worlds/levels that are used for islands.
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
}