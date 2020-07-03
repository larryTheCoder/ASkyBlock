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

package com.larryTheCoder.database;

import cn.nukkit.api.API;
import cn.nukkit.utils.TextFormat;
import com.larryTheCoder.database.config.AbstractConfig;
import com.larryTheCoder.database.config.MySQLConfig;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.IslandAwaitStore;
import com.larryTheCoder.utils.Settings;
import com.larryTheCoder.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.data.Row;
import org.sql2o.data.Table;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.larryTheCoder.database.TableSet.*;

/**
 * Improved version of Database class.
 * <p>
 * This version will be using {@code ConcurrentLinkedQueue} and every new
 * {@linkplain Consumer<Connection> queries} will be queued into a list for an async task
 * to execute the query using FIFO order. This method is more reliable as the process
 * will not be interrupted on main thread.
 */
@Log4j2
public class DatabaseManager {

    public static final String DB_VERSION = "0.1.9";
    public static String currentCacheId;

    private final AbstractConfig database;
    private final Connection connection;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<DatabaseImpl> requestQueue = new ConcurrentLinkedQueue<>();

    public static boolean isMysql = false;

    public DatabaseManager(AbstractConfig database) throws SQLException, ClassNotFoundException {
        this.database = database;
        this.connection = database.openConnection().open();

        isMysql = database instanceof MySQLConfig;

        this.startConnection();
    }

    /**
     * Shutdown this database and stops every database operations.
     * This method however will not stop the queries immediately until it has been fully finished.
     */
    public final void shutdownDB() {
        isClosed.compareAndSet(false, true);
    }

    /**
     * Get the correct uptime of the database. This method tries to fetch at least
     * 1 rows of a database is selected.
     *
     * @return Return the value (in milliseconds) of the database uptime.
     */
    public long pingDatabase() {
        long lastTime = System.currentTimeMillis();

        try (Query query = connection.createQuery("SELECT 1")) {
            query.executeAndFetchTable();
        }

        return System.currentTimeMillis() - lastTime;
    }

    private void startConnection() {
        TableSet[] tableSets = {
                METADATA_TABLE,
                WORLD_TABLE,
                PLAYER_TABLE,
                PLAYER_CHALLENGES,
                ISLAND_TABLE,
                ISLAND_DATA,
                ISLAND_RELATIONS,
                ISLAND_LIMIT_COUNT
        };

        if (isMysql) {
            connection.createQuery(FOR_TABLE_OPTIMIZE_A.getQuery()).executeUpdate();
            connection.createQuery(FOR_TABLE_OPTIMIZE_B.getQuery()).executeUpdate();
        } else {
            connection.createQuery(SQLITE_PRAGMA_ON.getQuery()).executeUpdate();
        }

        // Add all tables into our database.
        for (TableSet set : tableSets) {
            connection.createQuery(set.getQuery()).executeUpdate();
        }

        // Update database credentials.
        Table result = connection.createQuery(TABLE_FETCH_CACHE.getQuery()).executeAndFetchTable();

        if (result.rows().isEmpty()) {
            currentCacheId = UUID.randomUUID().toString();

            connection.createQuery(TABLE_INSERT_CACHE.getQuery())
                    .addParameter("cacheUniqueId", currentCacheId)
                    .addParameter("dbVersion", DB_VERSION)
                    .executeUpdate();
        } else {
            Row row1 = result.rows().get(0);

            currentCacheId = row1.getString("cacheUniqueId");

            String dbVersion = row1.getString("dbVersion");
            if (!dbVersion.equalsIgnoreCase(DB_VERSION)) {
                switch (dbVersion) {
                    case "0.1.5":
                        connection.createQuery("DROP TABLE islandRelations").executeUpdate();
                        connection.createQuery(ISLAND_RELATIONS.getQuery()).executeUpdate();
                    case "0.1.6":
                        connection.createQuery("ALTER TABLE islandRelations ADD COLUMN islandAdmins TEXT DEFAULT ''").executeUpdate();
                    case "0.1.7":
                        connection.createQuery("ALTER TABLE islandData ADD COLUMN islandLevel INTEGER DEFAULT 0").executeUpdate();
                    case "0.1.8":
                        connection.createQuery("CREATE TABLE player_dg_tmp" +
                                "(playerName VARCHAR(100) PRIMARY KEY," +
                                "playerUUID VARCHAR(36)," +
                                "locale TEXT NOT NULL," +
                                "banList TEXT NOT NULL," +
                                "resetAttempts INTEGER NOT NULL," +
                                "lastLogin DATETIME default CURRENT_TIMESTAMP)").executeUpdate();
                        //noinspection SqlResolve
                        connection.createQuery("INSERT INTO player_dg_tmp(playerName, playerUUID, locale, banList, resetAttempts) select playerName, playerUUID, locale, banList, resetAttempts from player");
                        connection.createQuery("DROP TABLE player").executeUpdate();
                        //noinspection SqlResolve
                        connection.createQuery("ALTER TABLE player_dg_tmp RENAME TO player").executeUpdate();
                    case "0.1.9":
                        break;
                }

                connection.createQuery(TABLE_CACHE_UPDATE.getQuery())
                        .addParameter("dbVersion", DB_VERSION)
                        .addParameter("cacheUniqueId", currentCacheId)
                        .executeUpdate();

                Utils.send(TextFormat.GOLD + "Updated database information to " + TextFormat.YELLOW + "v" + DB_VERSION);
            }
        }
        IslandAwaitStore.init(connection);

        // If the database is a mysql database, we can
        // apply as many connections we want since this database
        // structure provides pooling and etc.
        if (isMysql) {
            startAsyncPool(3);
        } else {
            startAsyncPool(1);
        }

        Utils.send(String.format("&6Connected to %s database,&e %sms",
                database instanceof MySQLConfig ? "mysql" : "sqlite",
                pingDatabase()));
    }

    /**
     * Pushes a query into the queue. This is a non thread-blocking operation.
     */
    @API(definition = API.Definition.UNIVERSAL, usage = API.Usage.MAINTAINED)
    public void pushQuery(DatabaseImpl callbacks) {
        requestQueue.offer(callbacks);
    }

    /**
     * Queries a connection immediately. This is a thread-blocking operation.
     */
    @API(definition = API.Definition.UNIVERSAL, usage = API.Usage.MAINTAINED)
    public void pushQuery(Consumer<Connection> conn) {
        conn.accept(connection);
    }

    /**
     * Get the access to the SkyBlock database.
     */
    @API(definition = API.Definition.INTERNAL, usage = API.Usage.INCUBATING)
    public Connection getConnection() {
        return connection;
    }

    private int currentPoolSize = 0;

    private void startAsyncPool(int maxPool) {
        // Use Thread instead of Async?
        Thread asyncThread = new Thread(() -> {
            while (!isClosed.get()) {
                try {
                    long lastTick = System.currentTimeMillis();
                    DatabaseImpl consumer;
                    while ((consumer = requestQueue.poll()) != null) {
                        Exception result = null;
                        try {
                            consumer.executeQuery(connection);
                        } catch (Exception ex) {
                            result = ex;
                        }

                        final Exception resultFinal = result;
                        final DatabaseImpl dbImpl = consumer;

                        TaskManager.runTask(() -> {
                            dbImpl.onCompletion(resultFinal);

                            if (Settings.verboseCode && resultFinal != null) {
                                resultFinal.printStackTrace();
                            }
                        });
                    }
                    IslandAwaitStore.databaseTick(connection);

                    long nowTick = System.currentTimeMillis() - lastTick;

                    // The execution took 50ms to execute, do not bother sleeping this thread.
                    // Continue execution.
                    if (nowTick >= 50) continue;

                    Thread.sleep(50 - nowTick);
                } catch (Throwable e) {
                    // Sync up with console.
                    log.throwing(e);
                }
            }
        });
        asyncThread.setName(String.format("Asynchronous SkyBlock-Database Pool #%s", currentPoolSize));
        asyncThread.start();

        currentPoolSize++;

        if (currentPoolSize < maxPool) startAsyncPool(maxPool);
    }

    // TODO: Use CompletableFuture<> class for better performance?
    public abstract static class DatabaseImpl {

        /**
         * Execute various queries for this database implication.
         * This executing will be running inside an async worker.
         *
         * @param connection The {@linkplain Connection connection} of sql2o database.
         */
        public abstract void executeQuery(Connection connection);

        /**
         * Fired when {@link DatabaseImpl#executeQuery} is executed during async collections.
         * This function will be running in the main thread.
         *
         * @param exception Will return {@link Exception} if the process failed to execute correctly.
         */
        public void onCompletion(Exception exception) {
            // Do nothing
        }
    }

}
