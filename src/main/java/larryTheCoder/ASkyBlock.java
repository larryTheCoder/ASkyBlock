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
 *  - Add Schematic - will took a long time to build
 *  - Player locales - /is locales [String:#]
 * SECURITY FIX:
 *  - Change the player name into UUID
 */
package larryTheCoder;

import larryTheCoder.utils.Utils;
import larryTheCoder.utils.ConfigManager;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.intellectiualcrafters.updater.Updater;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import larryTheCoder.database.purger.InventorySave;
import larryTheCoder.listener.IslandListener;
import larryTheCoder.listener.chat.ChatFormatListener;
import larryTheCoder.listener.chat.ChatHandler;
import larryTheCoder.database.ASConnection;
import larryTheCoder.database.helper.SQLiteDatabase;
import larryTheCoder.listener.invitation.InvitationHandler;
import larryTheCoder.island.GridManager;
import larryTheCoder.island.IslandFallback;
import larryTheCoder.island.IslandManager;
import larryTheCoder.player.TeamManager;
import larryTheCoder.player.TeleportLogic;
import larryTheCoder.schematic.Schematic;

/**
 * @author larryTheCoder
 */
public class ASkyBlock extends PluginBase {

    public Config cfg;
    public int[] version;
    public ArrayList<String> level = new ArrayList<>();
    public static HashMap<String, Schematic> schematics = new HashMap<>();

    // Managers
    private Config msg;
    private ASConnection db = null;
    private ChatHandler chatHandler;
    private static ASkyBlock object;
    private InvitationHandler invitationHandler;
    private IslandManager manager;
    private GridManager grid;
    private IslandFallback backup;
    private InventorySave inventory;
    private TeamManager managers;
    private TeleportLogic teleportLogic;

    /**
     * Try to register a schematic manually
     *
     * @api
     * @param schematic - File that contains mcedit .schematic
     * @param name - The name of that file e.g SkyBlock
     */
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

    /**
     * Get the Schematic
     *
     * @api
     * @param key - The key of that file e.g SkyBlock
     * @return Schematic
     */
    public Schematic getSchematic(String key) {
        if (ASkyBlock.schematics.containsKey(key)) {
            return ASkyBlock.schematics.get(key);
        }
        return null;
    }

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
     * Get the current ASkyBlock version.
     *
     * @api
     * @return current version in config or null
     */
    public int[] getVersion() {
        return this.version;
    }

    /**
     * Gets the Database Connection
     *
     * @api
     * @return ASConnection
     */
    public ASConnection getDatabase() {
        return db;
    }

    /**
     * Return of ASkyBlock plug-in
     *
     * @api
     * @return ASkyBlock
     */
    public static ASkyBlock get() {
        return object;
    }

    /**
     * get the plug-in version
     *
     * @api
     * @return String - Version of ASkyBlock
     */
    public String getPluginVersionString() {
        return getDescription().getVersion();
    }

    /**
     * Gets the current version
     *
     * @api
     * @return int[]
     */
    public int[] getPluginVersion() {
        String ver = getDescription().getVersion();
        if (ver.contains("-")) {
            ver = ver.split("-")[0];
        }
        String[] split = ver.split("\\.");
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }

    /**
     * Check if `version` is >= `version2`.
     *
     * @api
     * @param version
     * @param version2
     * @return true if `version` is >= `version2`
     */
    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1] || version[0] == version2[0]
                && version[1] == version2[1] && version[2] >= version2[2];
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
     * Get the Island backup section
     *
     * @api
     * @return GridManager
     */
    public IslandFallback getFallback() {
        return backup;
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
//  #################################### NON-API ####################################

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
        getServer().getLogger().info(getPrefix() + getMsg("onLoad") + getPluginVersionString());
        getServer().getLogger().info(TextFormat.YELLOW + "------------------------------------------------------------");
        initIslands();
        registerObject();
        getServer().getLogger().info(TextFormat.YELLOW + "------------------------------------------------------------");
        getServer().getLogger().info(getPrefix() + getMsg("onEnable"));
    }

    @Override
    public void onDisable() {
        Utils.ConsoleMsg(TextFormat.GREEN + "Saving islands framework");
        saveLevel();
        getDatabase().close();
        Utils.ConsoleMsg(TextFormat.RED + "ASkyBlock ~ Disabled seccessfully");
    }

    /**
     * Load every islands Components
     */
    private void initIslands() {
        PluginManager pm = getServer().getPluginManager();
        chatHandler = new ChatHandler(this);
        this.teleportLogic = new TeleportLogic(this);
        invitationHandler = new InvitationHandler(this);
        getServer().getPluginManager().registerEvents(chatHandler, this);
        pm.registerEvents(new IslandListener(this), this);
        ServerScheduler pd = getServer().getScheduler();
        pd.scheduleRepeatingTask(new PluginTask(this), 1200);
    }

    /**
     * Reload every level that had generated
     *
     * TO-DO: Support Multi-World
     */
    @SuppressWarnings("unchecked")
    private void reloadLevel() {
        this.getDatabase().getWorlds().stream().forEach((stmt) -> {
            level.add(stmt);
        });
    }

    /**
     * Reload every level that had generated
     *
     * TO-DO: Support Multi-World
     */
    private void saveLevel() {

    }

    private void registerObject() {
        loadSchematic();
        generateLevel();
        if (cfg.getBoolean("updater")) {
            Updater.getUpdate();
        }
        manager = new IslandManager(this);
        grid = new GridManager(this);
        managers = new TeamManager(this);
        inventory = new InventorySave(this);
        backup = new IslandFallback(this);
        backup.init();
        this.getServer().getCommandMap().register("SkyBlock", new Commands(this));
        try {
            this.db = new ASConnection(new SQLiteDatabase(new File(getDataFolder(), cfg.getString("database.SQLite.file-name") + ".db")), "null", false);
        } catch (SQLException | ClassNotFoundException ex) {
            if (ASkyBlock.get().isDebug()) {
                ex.printStackTrace();
            }
        }
        //reloadLevel();
        Level world = getServer().getLevelByName("SkyBlock");
        world.setTime(1600);
        world.stopTime();
        if (cfg.getBoolean("chat.UCFormatting") == true) {
            getServer().getPluginManager().registerEvents(new ChatFormatListener(this), this);
        }
    }

    public String getPrefix() {
        return cfg.getString("Prefix").replace("&", "§");
    }

    public String getMsg(String key) {
        String mssg = msg.getString(key).replace("&", "§");
        return mssg;
    }

    public final void initConfig() {
        Utils.EnsureDirectory(Utils.DIRECTORY);
        Utils.EnsureDirectory(Utils.LOCALES_DIRECTORY);
        //initLocales();        
        if (this.getResource("config.yml") != null) {
            this.saveResource("config.yml");
        }
        cfg = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        if (this.getResource("English.yml") != null) {
            this.saveResource("English.yml");
        }
        msg = new Config(new File(getDataFolder(), "English.yml"), Config.YAML);
        ConfigManager.load();
    }

    private void generateLevel() {
        Utils.ConsoleMsg(TextFormat.GREEN + "Loading the Island Framework");
        if (!Server.getInstance().isLevelGenerated("SkyBlock")) {
            Server.getInstance().generateLevel("SkyBlock", 0, SkyBlockGenerator.class);
        }
        if (!Server.getInstance().isLevelLoaded("SkyBlock")) {
            Server.getInstance().loadLevel("SkyBlock");
        }
//        level.stream().map((world) -> {
//            if (getServer().isLevelGenerated(world) == false) {
//                getServer().generateLevel(world, 0xe9bcdL, SkyBlockGenerator.class);
//            }
//            return world;
//        }).filter((world) -> (getServer().isLevelLoaded(world) == false)).forEach((world) -> {
//            getServer().loadLevel(world);
//        });
        level.add("SkyBlock");
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
        File schematicFile = new File(schematicFolder, "island.schematic");
        File netherFile = new File(schematicFolder, "nether.schematic");
        if (!schematicFile.exists()) {
            //plugin.getLogger().info("Default schematic does not exist...");
            // Only copy if the default exists
            if (getResource("schematics/island.schematic") != null) {
                getServer().getLogger().info("Default schematic does not exist, saving it...");
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
        String[] sting = cfg.getString("schematic.loaded").split(", ");
        for (String island : sting) {
            File random = new File(schematicFolder, island + ".schematic");
            try {
                if (!schematics.containsKey(island)) {
                    if (!island.equalsIgnoreCase("island")) {
                        Schematic netherIsland = new Schematic(this, random);
                        //netherIsland.setVisible(false);
                        schematics.put(island, netherIsland);
                    }

                }

            } catch (IOException e) {
                getServer().getLogger().error("Could not load " + island + " schematic!");
                if (ASkyBlock.get().isDebug()) {
                    e.printStackTrace();
                }
            }
        }

        getServer().getLogger().info("§7<§cSMC§7> §aSeccessfully loaded island Schematic");
    }

}
