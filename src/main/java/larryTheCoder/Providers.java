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

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import larryTheCoder.database.Database;
import larryTheCoder.database.DatabaseProvider;
import org.sql2o.Sql2o;
import ru.nukkit.dblib.DbLib;

/**
 * @author NOT larryTheCoder
 */
public enum Providers {
    
    DATABASE(DatabaseProvider.class);
    
    private Class<? extends Database> clazz;

    Providers(Class<? extends Database> clazz) {
        this.clazz = clazz;
    }
        
    public static Sql2o getSql2o() {
        if (Server.getInstance().getPluginManager().getPlugin("DbLib") == null) {
            ASkyBlock.object.getServer().getLogger().warning(TextFormat.RED + "Unable to find DbLib Database: NULL NULL NULL, DEBUG");
            return null;
        }
        Config cfg = new Config(new File(ASkyBlock.getInstance().getDataFolder(), "config.yml"), Config.YAML);
        switch (cfg.getString("database.connection").toLowerCase()) {
            case "dblib":
                return DbLib.getSql2o();
            case "sqlite":
                return DbLib.getSql2o(DbLib.getSqliteUrl(cfg.getString("database.SQLite.file-name")), "", "");
            //case "mysql":
            //    return DbLib.getSql2oMySql(cfg.dbMySqlHost, cfg.dbMySqlPort, cfg.dbMySqlDb, cfg.dbMySqlUser, cfg.dbMySqlPwd);
        }
        ASkyBlock.object.getServer().getLogger().warning(TextFormat.RED + "Unable to find a correct database... Using default: DATAFILE");
        return DbLib.getSql2o(DbLib.getSqliteUrl(cfg.getString("database.SQLite.file-name")), "", "");
    }
}
