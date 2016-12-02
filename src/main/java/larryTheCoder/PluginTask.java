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

import com.intellectiualcrafters.updater.Updater;


/**
 * @author larryTheCoder
 */
public class PluginTask extends cn.nukkit.scheduler.PluginTask<ASkyBlock>{

    private int nextUpdate = 0;
    
    public PluginTask(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public void onRun(int currentTick) {
        nextUpdate++; 
        owner.getInvitationHandler().tick();
        // Get updates in 4 hours
        if(nextUpdate == 14400){
            nextUpdate = 0;
            Updater.getUpdate();
        }
    }

}
