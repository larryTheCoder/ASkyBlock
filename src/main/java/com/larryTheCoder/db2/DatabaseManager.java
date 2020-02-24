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

package com.larryTheCoder.db2;

import cn.nukkit.api.API;
import com.larryTheCoder.db2.config.AbstractConfig;
import com.larryTheCoder.db2.config.MySQLConfig;
import com.larryTheCoder.task.TaskManager;
import com.larryTheCoder.utils.Utils;
import org.sql2o.Connection;
import org.sql2o.Query;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.larryTheCoder.db2.TableSet.*;

/**
 * Improved version of Database class.
 * <p>
 * This version will be using {@code ConcurrentLinkedQueue} and every new
 * {@linkplain Consumer<Connection> queries} will be queued into a list for an async task
 * to execute the query using FIFO order. This method is more reliable as the process
 * will not be interrupted on main thread.
 */
public class DatabaseManager {

    // Notes: During SELECT, sqlite database can read its database easily without effecting the thread,
    //        But when we are using mysql as our database, it effects the thread uptime. So do we need to
    //        Optimize our code so it can be async-compatible?

    private AbstractConfig database;
    private Connection connection;

    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private ConcurrentLinkedQueue<DatabaseImpl> requestQueue = new ConcurrentLinkedQueue<>();

    public DatabaseManager(AbstractConfig database) throws SQLException, ClassNotFoundException {
        this.database = database;
        this.connection = database.openConnection().open();

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
        boolean isMysql = database instanceof MySQLConfig;

        TableSet[] tableSets = {
                ISLAND_DATA,
                PLAYER_TABLE,
                PLAYER_CHALLENGES,
                ISLAND_TABLE,
                ISLAND_DATA,
                ISLAND_RELATIONS
        };

        // Add all tables into our database.
        for (TableSet set : tableSets) {
            if (isMysql) {
                // InnoDB Special case
                connection.createQuery(set.getQuery() + FOR_INNODB_OPTIMIZE.getQuery()).executeUpdate();
            } else {
                connection.createQuery(set.getQuery()).executeUpdate();
            }
        }


        // If the database is a mysql database, we can
        // apply as many connections we want since this database
        // structure provides pooling and etc.
        if (database instanceof MySQLConfig) {
            connection.createQuery(FOR_TABLE_OPTIMIZE_A.getQuery()).executeUpdate();
            connection.createQuery(FOR_TABLE_OPTIMIZE_B.getQuery()).executeUpdate();

            startAsyncPool(3);
        } else {
            connection.createQuery(SQLITE_PRAGMA_ON.getQuery()).executeUpdate();

            startAsyncPool(1);
        }

        Utils.send(String.format("&6Connected to %s database.&e %s",
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
        TaskManager.runTaskAsync(() -> {
            while (!isClosed.get()) {
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

                    TaskManager.runTask(() -> dbImpl.onCompletion(resultFinal));
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        currentPoolSize++;

        if (currentPoolSize < maxPool) {
            startAsyncPool(maxPool);
        }
    }

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
