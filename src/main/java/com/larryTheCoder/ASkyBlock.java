/*
 * Copyright (C) 2017 Adam Matthew
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
import com.larryTheCoder.utils.Utils;
import com.larryTheCoder.utils.ConfigManager;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.biome.Biome;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.intellectiualcrafters.updater.Updater;
import com.larryTheCoder.command.AdminCMD;
import com.larryTheCoder.command.ChallangesCMD;
import com.larryTheCoder.database.ASConnection;
import com.larryTheCoder.database.JDBCUtilities;
import com.larryTheCoder.database.variables.MySQLDatabase;
import com.larryTheCoder.database.variables.SQLiteDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.larryTheCoder.storage.InventorySave;
import com.larryTheCoder.listener.IslandListener;
import com.larryTheCoder.listener.chat.ChatHandler;
import com.larryTheCoder.economy.Economy;
import com.larryTheCoder.listener.invitation.InvitationHandler;
import com.larryTheCoder.island.GridManager;
import com.larryTheCoder.island.IslandManager;
import com.larryTheCoder.locales.ASlocales;
import com.larryTheCoder.player.PlayerData;
import com.larryTheCoder.player.TeamManager;
import com.larryTheCoder.player.TeleportLogic;
import com.larryTheCoder.schematic.Schematic;
import com.larryTheCoder.storage.IslandData;
import com.larryTheCoder.utils.Settings;
import java.sql.SQLException;

/**
 * Author: Adam Matthew
 * <p>
 * Main class of SkyBlock Framework! Complete with API and Events. May contains
 * Nuts!
 */
public class ASkyBlock extends PluginBase implements ASkyBlockAPI {

    public Config cfg;
    public int[] version;
    public ArrayList<String> level = new ArrayList<>();
    public static HashMap<String, Schematic> schematics = new HashMap<>();

    // Managers
    private Config msg;
    public static Economy econ;
    private ASConnection db = null;
    private ChatHandler chatHandler;
    private static ASkyBlock object;
    private InvitationHandler invitationHandler;
    private IslandManager manager;
    private GridManager grid;
    private InventorySave inventory;
    private TeamManager managers;
    private TeleportLogic teleportLogic;
    private ChallangesCMD cmds;
    private Messages msgs;
    // Localization Strings
    private HashMap<String, ASlocales> availableLocales = new HashMap<>();

    @Override
    public void registerSchematic(File schematic, String name) {
        try {
            ASkyBlock.schematics.put("default", new Schematic(this, schematic));
        } catch (IOException ex) {
            Utils.ConsoleMsg("Unable to add " + schematic.getName() + " Schematic file.");
            if (isDebug()) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Schematic getSchematic(String key) {
        if (ASkyBlock.schematics.containsKey(key)) {
            return ASkyBlock.schematics.get(key);
        }
        return null;
    }

    @Override
    public String getPluginVersionString() {
        return getDescription().getVersion();
    }

    @Override
    public int[] getPluginVersion() {
        String ver = getDescription().getVersion();
        if (ver.contains("-")) {
            ver = ver.split("-")[0];
        }
        String[] split = ver.split("\\.");
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }

    @Override
    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1] || version[0] == version2[0]
                && version[1] == version2[1] && version[2] >= version2[2];
    }

    @Override
    public Integer getIslandLevel(Player player) {
        PlayerData pd = getPlayerInfo(player);
        return pd.getIslandLevel();
    }

    @Override
    public IslandData getIslandInfo(Player player) {
        return getIslandInfo(player.getName());
    }

    @Override
    public int[] getVersion() {
        return version;
    }

    @Override
    public ASConnection getDatabase() {
        return db;
    }

    @Override
    public IslandData getIslandInfo(Location location) {
        return getDatabase().getIslandLocation(location.getLevel().getName(), location.getFloorX(), location.getFloorZ());
    }

    @Override
    public PlayerData getPlayerInfo(Player player) {
        return getDatabase().getPlayerData(player.getName());
    }

//  #################################### NON-API ####################################
    public TeleportLogic getTeleportLogic() {
        return teleportLogic;
    }

    /**
     * Get if debugging enabled
     *
     * @return true if enabled
     */
    public boolean isDebug() {
        return ASkyBlock.object.cfg.getBoolean("debug");
    }

    /**
     * Return of ASkyBlock plug-in
     *
     * @return ASkyBlock
     */
    public static ASkyBlock get() {
        return object;
    }

    /**
     * Get The ChatHandler instance
     *
     * @api
     * @return ChatHandler
     */
    public ChatHandler getChatHandlers() {
        return chatHandler;
    }

    /**
     * Get The InvitationHandler instance
     *
     * @api
     * @return InvitationHandler
     */
    public InvitationHandler getInvitationHandler() {
        return invitationHandler;
    }

    /**
     * Get the island Manager section
     *
     * @api
     * @return IslandManager
     */
    public IslandManager getIsland() {
        return manager;
    }

    /**
     * Get the GridManager Manager section
     *
     * @api
     * @return GridManager
     */
    public GridManager getGrid() {
        return grid;
    }

    /**
     * Get the GridManager Manager section
     *
     * @api
     * @return InventorySave
     */
    public InventorySave getInventory() {
        return inventory;
    }

    /**
     * Get the TeamManager section
     *
     * @api
     * @return BaseEntity
     */
    public TeamManager getTManager() {
        return managers;
    }

    public IslandData getIslandInfo(Player player, int homes) {
        return getDatabase().getIsland(player.getName(), homes);
    }

    public IslandData getIslandInfo(String player) {
        return getDatabase().getIsland(player, 1);
    }

    public IslandData getIslandInfo(String player, int homes) {
        return getDatabase().getIsland(player, homes);
    }

    public ChallangesCMD getChallenges() {
        return cmds;
    }

    public boolean inIslandWorld(Player p) {
        return level.contains(p.getLevel().getName());
    }

    public String getDefaultWorld(Player p) {
        PlayerData pd = getPlayerInfo(p);
        // Sometimes default level are null
        if (level.contains(pd.defaultLevel)) {
            pd.defaultLevel = "SkyBlock";
            getDatabase().savePlayerData(pd);
            return "SkyBlock";
        }
        return pd.defaultLevel;
    }

    @Override
    public void onLoad() {
        if (!(object instanceof ASkyBlock)) {
            object = this;
        }
        // Register generator
        Generator.addGenerator(SkyBlockGenerator.class, "island", SkyBlockGenerator.TYPE_SKYBLOCK);
        // Register TaskManager        
        com.intellectiualcrafters.TaskManager.IMP = new com.intellectiualcrafters.TaskManager();
    }

    @Override
    public void onEnable() {
        initConfig();
        getServer().getLogger().info(getPrefix() + "§aLoading source §eASkyBlock §av" + getPluginVersionString());
        getServer().getLogger().info(TextFormat.YELLOW + "------------------------------------------------------------");
        initIslands();
        registerObject();
        getServer().getLogger().info(TextFormat.YELLOW + "------------------------------------------------------------");
        getServer().getLogger().notice(TextFormat.colorize('&', "&eYou are using BETA-Builds of ASkyBlock!"));
        getServer().getLogger().notice(TextFormat.colorize('&', "&eWarning! You might experience some crash and errors while using this plugin"));
        getServer().getLogger().notice(TextFormat.colorize('&', "&eIt is recommended to report any issues at: http://www.github.com/larryTheCoder/ASkyBlock-Nukkit/issues"));
        getServer().getLogger().info(getPrefix() + "§aASkyBlock has seccessfully enabled!");
    }

    @Override
    public void onDisable() {
        Utils.ConsoleMsg(TextFormat.GREEN + "Saving islands framework");
        saveLevel();
        getDatabase().close();
        msgs.saveMessages();
        Utils.ConsoleMsg(TextFormat.RED + "ASkyBlock ~ Disabled seccessfully");
    }

    /**
     * Load every islands Components
     */
    private void initIslands() {
        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        // This should be loaded first
        msgs = new Messages(this);
        msgs.loadMessages();
        getServer().getPluginManager().registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        ServerScheduler pd = getServer().getScheduler();
        pd.scheduleRepeatingTask(new PluginTask(this), 20); // tick every 1 sec
    }

    /**
     * Reload every level that had generated
     */
    private void saveLevel() {
        getDatabase().saveWorlds(level);
    }

    private void registerObject() {
        Utils.ConsoleMsg(TextFormat.GREEN + "Loading the Island Framework");
        loadSchematic();
        if (cfg.getBoolean("updater")) {
            Updater.getUpdate();
        }
        manager = new IslandManager(this);
        grid = new GridManager(this);
        managers = new TeamManager(this);
        inventory = new InventorySave(this);
        getServer().getCommandMap().register("ASkyBlock", new Commands(this));
        getServer().getCommandMap().register("ASkyBlock", this.cmds = new ChallangesCMD(this));
        getServer().getCommandMap().register("ASkyBlock", new AdminCMD(this));
        if (cfg.getString("database.connection").equalsIgnoreCase("mysql")) {
            try {
                db = new ASConnection(new MySQLDatabase(cfg.getString("database.MySQL.host"), cfg.getInt("database.MySQL.port"), cfg.getString("database.MySQL.database"), cfg.getString("database.MySQL.username"), cfg.getString("database.MySQL.password")), true);
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            } catch (ClassNotFoundException | InterruptedException ex) {
                Utils.ConsoleMsg("Unable to create MySql database");
            }
        } else {
            try {
                db = new ASConnection(new SQLiteDatabase(new File(getDataFolder(), cfg.getString("database.SQLite.file-name") + ".db")), true);
            } catch (SQLException ex) {
                JDBCUtilities.printSQLException(ex);
            } catch (ClassNotFoundException | InterruptedException ex) {
                Utils.ConsoleMsg("Unable to create MySql database");
            }
        }
        generateLevel();
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
            Utils.ConsoleMsg("Unknown locale: " + pd.pubLocale);
            Utils.ConsoleMsg("Using default: en-US");
            return getAvailableLocales().get(Settings.defaultLanguage);
        }
        return getAvailableLocales().get(pd.pubLocale);
    }

    public final void initConfig() {
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        if (getResource("config.yml") != null) {
            saveResource("config.yml");
        }
        if (getResource("challenges.yml") != null) {
            saveResource("challenges.yml");
        }
        cfg = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        if (getResource("English.yml") != null) {
            saveResource("English.yml");
        }
        msg = new Config(new File(getDataFolder(), "English.yml"), Config.YAML);
        recheck();
        ConfigManager.load();
    }

    public void recheck() {
        boolean update = false;
        File file;
        Config cfgg = new Config(file = new File(ASkyBlock.get().getDataFolder(), "config.yml"), Config.YAML);
        if (!cfgg.getString("version").equalsIgnoreCase(ConfigManager.CONFIG_VERSION)) {
            Utils.ConsoleMsg("&cOutdated config! Creating new one");
            Utils.ConsoleMsg("&aYour old config will be renamed into config.old!");
            update = true;
        }
        if (update) {
            file.renameTo(new File(ASkyBlock.get().getDataFolder(), "config.old"));
            ASkyBlock.get().saveResource("config.yml");
        }
    }

    /**
     * List schematics this player can access. If @param ignoreNoPermission is
     * true, then only schematics with a specific permission set will be
     * checked. I.e., no common schematics will be returned (including the
     * default one).
     *
     * @param player
     * @param ignoreNoPermission
     * @return List of schematics this player can use based on their permission
     * level
     */
    public List<Schematic> getSchematics(Player player, boolean ignoreNoPermission) {
        List<Schematic> result = new ArrayList<>();
        // Find out what schematics this player can choose from
        //Bukkit.getLogger().info("DEBUG: Checking schematics for " + player.getName());
        schematics.values().stream().filter((schematic) -> ((!ignoreNoPermission && schematic.getPerm().isEmpty()) || player.hasPermission(schematic.getPerm()))).filter((schematic) -> (schematic.isVisible())).forEach((schematic) -> {
            // Check if it's a nether island, but the nether is not enables
            if (schematic.getBiome().equals(Biome.HELL)) {

            } else {
                result.add(schematic);
            }
        }); //Bukkit.getLogger().info("DEBUG: schematic name is '"+ schematic.getName() + "'");
        //Bukkit.getLogger().info("DEBUG: perm is " + schematic.getPerm());
        //Bukkit.getLogger().info("DEBUG: player can use this schematic");
        // Only add if it's visible
        // Sort according to order
        Collections.sort(result, (Schematic o1, Schematic o2) -> ((o2.getOrder() < o1.getOrder()) ? 1 : -1));
        return result;
    }

    private void generateLevel() {
        if (!Server.getInstance().isLevelGenerated("SkyBlock")) {
            Server.getInstance().generateLevel("SkyBlock", 0, SkyBlockGenerator.class);
        }
        if (!Server.getInstance().isLevelLoaded("SkyBlock")) {
            Server.getInstance().loadLevel("SkyBlock");
        }
        level = getDatabase().getWorlds(); // should work
        level.stream().forEach((String world) -> {
            if (!Server.getInstance().isLevelGenerated(world)) {
                Server.getInstance().generateLevel(world, 0, SkyBlockGenerator.class);
            }
            if (!Server.getInstance().isLevelLoaded(world)) {
                Server.getInstance().loadLevel(world);
            }
            if (Settings.stopTime) {
                Level worldo = getServer().getLevelByName(world);
                worldo.setTime(1600);
                worldo.stopTime();
            }
        });
        if (!level.contains("SkyBlock")) {
            level.add("SkyBlock");
        }
    }

    private void loadSchematic() {
        // Check if there is a schematic folder and make it if it does not exist
        File schematicFolder = new File(getDataFolder(), "schematics");
        if (!schematicFolder.exists()) {
            schematicFolder.mkdir();
        }
        getServer().getLogger().info("§7<§cSMC§7> §eSchematic Post:");
        // Clear the schematic list that is kept in memory
        schematics.clear();
        // Load the default schematic if it exists
        // Set up the default schematic
        File schematicFile;
        // This part loads the schematic section
        ConfigSection schemSection = cfg.getSection("schematicsection");
        Settings.useSchematicPanel = schemSection.getBoolean("useschematicspanel", false);
        Settings.chooseIslandRandomly = schemSection.getBoolean("chooseislandrandomly", true);
        for (String key : schemSection.getSection("schematics").getKeys(false)) {
            try {
                Schematic newSchem = null;
                // Check the file exists
                String filename = schemSection.getString("schematics." + key + ".filename", "");
                if (!filename.isEmpty()) {
                    // Check if this file exists or if it is in the jar
                    schematicFile = new File(schematicFolder, filename);
                    // See if the file exists
                    if (schematicFile.exists()) {
                        newSchem = new Schematic(this, schematicFile);
                    } else if (getResource("schematics/" + filename) != null) {
                        saveResource("schematics/" + filename, false);
                        newSchem = new Schematic(this, schematicFile);
                    }
                } else if (key.equalsIgnoreCase("default")) {
                    newSchem = schematics.get("default");
                } else {
                    getServer().getLogger().info(TextFormat.YELLOW + "   - Schematic " + key + " does not have a filename. Skipping!");
                }
                if (newSchem != null) {
                    // Set the heading
                    //newSchem.setHeading(key);
                    // Order
                    newSchem.setOrder(schemSection.getInt("schematics." + key + ".order", 0));
                    // Load the rest of the settings
                    // Icon
                    try {

                        Item icon;
                        String iconString = schemSection.getString("schematics." + key + ".icon", "MAP").toUpperCase();
                        // Support damage values
                        String[] split = iconString.split(":");
                        if (Utils.isNumeric(split[0])) {
                            icon = Item.get(Integer.parseInt(iconString));
                        } else {
                            icon = Item.fromString(split[0]);
                        }
                        int damage = 0;
                        if (split.length == 2) {
                            if (Utils.isNumeric(split[1])) {
                                damage = Integer.parseInt(split[1]);
                            }
                        }
                        newSchem.setIcon(icon, damage);
                    } catch (Exception e) {
                        newSchem.setIcon(Item.MAP);
                    }
                    // Friendly name
                    String name = TextFormat.colorize('&', schemSection.getString("schematics." + key + ".name", ""));
                    newSchem.setName(name);
                    // Rating - Rating is not used right now
                    int rating = schemSection.getInt("schematics." + key + ".rating", 50);
                    if (rating < 1) {
                        rating = 1;
                    } else if (rating > 100) {
                        rating = 100;
                    }
                    newSchem.setRating(rating);
                    // Description
                    String description = TextFormat.colorize('&', schemSection.getString("schematics." + key + ".description", ""));
                    description = description.replace("[rating]", String.valueOf(rating));
                    newSchem.setDescription(description);
                    // Permission
                    String perm = schemSection.getString("schematics." + key + ".permission", "");
                    newSchem.setPerm(perm);
                    // Use default chest
                    newSchem.setUseDefaultChest(schemSection.getBoolean("schematics." + key + ".useDefaultChest", true));
                    // Biomes - overrides default if it exists
                    String biomeString = schemSection.getString("schematics." + key + ".biome", Settings.defaultBiome.toString());
                    Biome biome = null;
                    try {
                        biome = Biome.getBiome(biomeString);
                        newSchem.setBiome(biome);
                    } catch (Exception e) {
                        Utils.ConsoleMsg("Could not parse biome " + biomeString + " using default instead.");
                    }
                    // Use physics - overrides default if it exists
                    //newSchem.setUsePhysics(schemSection.getBoolean("schematics." + key + ".usephysics", Settings.usePhysics));
                    // Paste Entities or not
                    newSchem.setPasteEntities(schemSection.getBoolean("schematics." + key + ".pasteentities", false));
                    // Paste air or not. Default is false - huge performance savings!
                    //newSchem.setPasteAir(schemSection.getBoolean("schematics." + key + ".pasteair",false));	    
                    // Visible in GUI or not
                    newSchem.setVisible(schemSection.getBoolean("schematics." + key + ".show", true));
                    // Partner schematic
                    if (biome != null && biome.equals(Biome.HELL)) {
                        // Default for nether biomes is the default overworld island
                        newSchem.setPartnerName(schemSection.getString("schematics." + key + ".partnerSchematic", "default"));
                    } else {
                        // Default for overworld biomes is nether island
                        newSchem.setPartnerName(schemSection.getString("schematics." + key + ".partnerSchematic", "nether"));
                    }
                    // Get chest items
                    final List<String> chestItems = schemSection.getStringList("schematics." + key + ".chestItems");
                    if (!chestItems.isEmpty()) {
                        Item[] tempChest = new Item[chestItems.size()];
                        int i = 0;
                        for (String chestItemString : chestItems) {
                            try {
                                String[] amountdata = chestItemString.split(":");
                                Item mat;
                                if (Utils.isNumeric(amountdata[0])) {
                                    mat = Item.get(Integer.parseInt(amountdata[0]));
                                } else {
                                    mat = Item.fromString(amountdata[0].toUpperCase());
                                }
                                if (amountdata.length == 2) {
                                    tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountdata[1]));
                                    i++;
                                } else if (amountdata.length == 3) {
                                    tempChest[i] = new Item(mat.getId(), Integer.parseInt(amountdata[2]), Short.parseShort(amountdata[1]));
                                    i++;
                                }

                            } catch (Exception e) {
                                getServer().getLogger().info(TextFormat.YELLOW + "   - Problem loading chest item for schematic " + name + " so skipping it: " + chestItemString);
                            }
                        }

                        // Store it
                        newSchem.setDefaultChestItems(tempChest);
                    }
                    // Player spawn block
                    String spawnBlock = schemSection.getString("schematics." + key + ".spawnblock");
                    if (!spawnBlock.isEmpty()) {
                        // Check to see if this block is a valid material
                        try {
                            Item playerSpawnBlock;
                            if (Utils.isNumeric(spawnBlock)) {
                                playerSpawnBlock = Item.get(Integer.parseInt(spawnBlock));
                            } else {
                                playerSpawnBlock = Item.fromString(spawnBlock.toUpperCase());
                            }
                            if (newSchem.setPlayerSpawnBlock(Block.get(playerSpawnBlock.getId()))) {
                                getServer().getLogger().info(TextFormat.YELLOW + "   - Player will spawn at the " + playerSpawnBlock.toString());
                            } else {
                                getServer().getLogger().info(TextFormat.YELLOW + "   - Problem with schematic '" + name + "'. Spawn block '" + spawnBlock + "' not found in schematic or there is more than one. Skipping...");
                            }
                        } catch (Exception e) {
                            getServer().getLogger().info(TextFormat.YELLOW + "   - Problem with schematic '" + name + "'. Spawn block '" + spawnBlock + "' is unknown. Skipping...");
                        }
                    } else {
                        // plugin.getLogger().info("No spawn block found");
                    }
                    // Level handicap
                    newSchem.setLevelHandicap(schemSection.getInt("schematics." + key + ".levelHandicap", 0));

                    // Store it
                    schematics.put(key, newSchem);
                }
            } catch (Exception ex) {
                getServer().getLogger().info(TextFormat.YELLOW + "   - Error loading schematic in section " + key + ". Skipping...");
            }
        }
        // Try to load schematic the default schematic
        if (schematics.isEmpty()) {
            // Load the default schematic if it exists
            // Set up the default schematic
            schematicFile = new File(schematicFolder, "island.schematic");
            File netherFile = new File(schematicFolder, "nether.schematic");
            getServer().getLogger().info(TextFormat.YELLOW + " - " + schematicFile.getName().toUpperCase().replace(".SCHEMATIC", "") + " Info:");
            if (!schematicFile.exists()) {
                //plugin.getLogger().info("Default schematic does not exist...");
                // Only copy if the default exists
                if (getResource("schematics/island.schematic") != null) {
                    getServer().getLogger().info("§aDefault schematic does not exist, saving it...");
                    saveResource("schematics/island.schematic", false);
                    // Add it to schematics
                    try {
                        schematics.put("default", new Schematic(this, schematicFile));
                    } catch (IOException e) {
                        getServer().getLogger().info("Could not load default schematic!");
                        if (ASkyBlock.get().isDebug()) {
                            e.printStackTrace();
                        }
                    }
                    // If this is repeated later due to the schematic config, fine, it will only add info
                } else {
                    // No islands.schematic in the jar, so just make the default using 
                    // built-in island generation
                    schematics.put("default", new Schematic(this));
                }
            } else {
                // It exists, so load it
                try {
                    schematics.put("default", new Schematic(this, schematicFile));
                } catch (IOException e) {
                    getServer().getLogger().error("Could not load default schematic!");
                    if (ASkyBlock.get().isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
            getServer().getLogger().info(TextFormat.YELLOW + " - " + netherFile.getName().toUpperCase().replace(".SCHEMATIC", "") + " Info:");
            // Add the nether default too
            if (!netherFile.exists()) {
                if (getResource("schematics/nether.schematic") != null) {
                    saveResource("schematics/nether.schematic", false);
                    // Add it to schematics
                    try {
                        Schematic netherIsland = new Schematic(this, netherFile);
                        //netherIsland.setVisible(false);
                        schematics.put("nether", netherIsland);
                    } catch (IOException e) {
                        getServer().getLogger().error("Could not load default nether schematic!");
                        if (ASkyBlock.get().isDebug()) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    getServer().getLogger().error("Could not find default nether schematic!");
                }
            } else {
                // It exists, so load it
                try {
                    Schematic netherIsland = new Schematic(this, netherFile);
                    //netherIsland.setVisible(false);
                    schematics.put("nether", netherIsland);
                } catch (IOException e) {
                    getServer().getLogger().error("Could not load default nether schematic!");
                    if (ASkyBlock.get().isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
            // Set up some basic settings just in case the schematics section is missing
            if (schematics.containsKey("default")) {
                schematics.get("default").setName("Island");
                schematics.get("default").setDescription("");
                schematics.get("default").setPartnerName("nether");
                schematics.get("default").setBiome(Settings.defaultBiome);
                schematics.get("default").setIcon(Item.GRASS);
                if (Settings.chestItems.length == 0) {
                    schematics.get("default").setUseDefaultChest(false);
                }
                schematics.get("default").setOrder(0);
            }
            if (schematics.containsKey("nether")) {
                schematics.get("nether").setName("NetherBlock Island");
                schematics.get("nether").setDescription("Nether Island");
                schematics.get("nether").setPartnerName("default");
                schematics.get("nether").setBiome(Biome.HELL);
                schematics.get("nether").setIcon(Item.NETHERRACK);
                schematics.get("nether").setVisible(false);
                if (Settings.chestItems.length == 0) {
                    schematics.get("nether").setUseDefaultChest(false);
                }
            }
        }
        getServer().getLogger().info("§7<§cSMC§7> §aSeccessfully loaded island Schematic");

    }

    public Messages getMessages() {
        return msgs;
    }

    public void setAvailableLocales(HashMap<String, ASlocales> availableLocales) {
        this.availableLocales = availableLocales;
    }

    public HashMap<String, ASlocales> getAvailableLocales() {
        return availableLocales;
    }

    /**
     * Debug tools
     *
     * Where I test database and commands
     */
    private void test() {
        // getIsland() == seccess
        // deleteIsland() == seccess
        // createIsland() == seccess
        // getIslandLocation() == seccess
        // getIslands() == seccess
        // saveIsland() == seccess
        // getSpawn() == seccess
        // setSpawnPosition() == seccess
        // Island Data debug
        ASConnection con = getDatabase();
        String p = "PFrankDebug";
        IslandData pd = new IslandData(p, 50, 50, Settings.protectionrange);
        Utils.ConsoleMsg("&aAttempting to test Connection on Islands [Target]: " + p);
        int index = 1;
        // delete island
        if (con.getIsland(p, 1) != null) {
            Utils.ConsoleMsg("Case " + index + " [Island Delete]:");
            index++;
            pd = con.getIsland(p, 1);
            boolean result = con.deleteIsland(pd);
            Utils.ConsoleMsg("&aDeleted: " + result);
        }
        // create island
        if (con.getIsland(p, 1) == null) {
            Utils.ConsoleMsg("Case " + index + " [Island Create]:");
            pd.id = 1;
            pd.biome = Settings.defaultBiome.getName();
            pd.name = "WALK my PRANK!";
            pd.islandId = getIsland().generateIslandKey(50, 50);
            pd.owner = p;
            pd.setCenter(50, 50, 50);
            pd.levelName = "SkyBlock";
            pd.locked = false;
            boolean result = con.createIsland(pd);
            index++;
            Utils.ConsoleMsg("&aCreated: " + result);
            // Find island getIslandLocation()
            Utils.ConsoleMsg("Case " + index + " [Island cords XYZ]:");
            pd = con.getIslandLocation("SkyBlock", 50, 50);
            if (pd.biome.isEmpty()) {
                Utils.ConsoleMsg("&cFailed to find island");
            } else {
                Utils.ConsoleMsg("&aSeccessfully find my island!");
            }
            index++;
            Utils.ConsoleMsg("Case " + index + " [Island Find Array]:");
            ArrayList<IslandData> pd2 = con.getIslands(p);
            Utils.ConsoleMsg("&aSize of ArrayList: " + pd2.size());
            boolean isOwnerIsland = false;
            for (IslandData pda : pd2) {
                if (pda.owner.equalsIgnoreCase(p)) {
                    isOwnerIsland = true;
                    break;
                }
            }
            index++;
            Utils.ConsoleMsg("&aIsland Ownership?: " + isOwnerIsland);
            pd = con.getIsland(p, 1);
            // Test on spawns
            Utils.ConsoleMsg("Case " + index + " [Island Save]:");
            pd.setSpawn(true);
            result = con.saveIsland(pd);
            index++;
            Utils.ConsoleMsg("&aSeccess save island: " + result);
            Utils.ConsoleMsg("Case " + index + " [Island Spawn]:");
            pd = con.getSpawn();
            if (pd == null) {
                Utils.ConsoleMsg("&cFailed to detect island spawn!");
            } else {
                Utils.ConsoleMsg("&aSeccess! Found island spawn!");
            }
            index++;
            Utils.ConsoleMsg("Case " + index + " [Island setSpawn]:");
            Position pos = new Position(50, 50, 50, getServer().getLevelByName("SkyBlock"));
            if (con.setSpawnPosition(pos)) {
                Utils.ConsoleMsg("&aSeccess setSpawn on island spawn");
            } else {
                Utils.ConsoleMsg("&cFailed setSpawn on island spawn");
            }
        }
    }

}
