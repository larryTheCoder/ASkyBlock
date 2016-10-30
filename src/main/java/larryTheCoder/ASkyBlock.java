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

import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import java.util.ArrayList;
import larryTheCoder.island.Island;

/**
 * @author larryTheCoder
 */
public class ASkyBlock extends PluginBase{
    
    // This plugin
    public static ASkyBlock object;
    // Island level
    public ArrayList<String> level;
    public Config cfg;
    public String version;
    
    @Override
    public void onLoad(){
        if(!(object instanceof ASkyBlock)){
            object = this;
        } 
    }
    
    @Override
    public void onEnable(){
        initConfig();
        setGenerators();
        generateLevel();
        setDefaults();
        initIslands();
    }
    
    public ASkyBlock getInstance(){
        return object;
    }
    
    public Object getVersion(){
        return version;
    }
    
    private void setDefaults() {
        Level world = getServer().getLevelByName("SkyBlock");
            world.setTime(1600);
            world.stopTime();
    }
    
    /**
     * Load every islands Components
     */
    private void initIslands() {
      //  _PlayerDiary.InitializeDiarySystem();
        Island.LoadIslands();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new IslandListener(this), this);
      //  BukkitScheduler s = this.getServer().getScheduler();
    }
    
    private void initConfig() {
        Utils.EnsureDirectory(Utils.Directory);
        version = getDescription().getVersion();
    }
    
    private void generateLevel() {
       if(getServer().isLevelGenerated("SkyBlock") == false){
           getServer().generateLevel("SkyBlock", 0xe9bcdL, SkyBlockGenerator.class);
       }
       if(getServer().isLevelLoaded("SkyBlock") == false){
           getServer().loadLevel("SkyBlock");
       }
       //level.;
    }
    
    private void setGenerators() {
        Generator.addGenerator(SkyBlockGenerator.class, "island", SkyBlockGenerator.TYPE_SKYBLOCK);
    }
}
