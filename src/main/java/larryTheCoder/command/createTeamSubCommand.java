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
package larryTheCoder.command;

import cn.nukkit.command.CommandSender;
import larryTheCoder.ASkyBlock;
import larryTheCoder.database.purger.IslandData;
import larryTheCoder.database.purger.TeamData;

/**
 * @author larryTheCoder
 */
public class createTeamSubCommand extends SubCommand {

    public createTeamSubCommand(ASkyBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("is.command.create") && sender.isPlayer();
    }

    @Override
    public String getUsage() {
        return "<homeID> <name> OR <name>";
    }

    @Override
    public String getName() {
        return "tcreate";
    }

    @Override
    public String getDescription() {
        return "Create your team";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"createTeam", "tc", "ct", "tcre"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }
        IslandData pd = getPlugin().getDatabase().getIsland(sender.getName());
        if (pd.members != null) {
            sender.sendMessage(getMsg("error_have_team"));
            return true;
        }
        if (!pd.team.isEmpty()) {
            sender.sendMessage(getMsg("error_in_team"));
            return true;
        }
        pd.members = new TeamData(args[1], pd.owner, "");
        sender.sendMessage(getPrefix() + getMsg("seccess_team"));
        return true;
    }

}
