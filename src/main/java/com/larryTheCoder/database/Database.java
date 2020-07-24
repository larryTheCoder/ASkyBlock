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

import cn.nukkit.utils.TextFormat;
import com.google.common.base.Preconditions;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.database.config.AbstractConfig;
import com.larryTheCoder.database.config.MySQLConfig;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.data.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * A high quality database functionalities that is now relying on {@link ForkJoinPool}
 * as it provides a better async capabilities with a FIFO execution model.
 *
 * <p> In other hand, all database operations are considered thread-safe and can be
 * used within all thread concurrently with the help of {@link CompletableFuture<>}.
 *
 * @author larryTheCoder
 * @since 0.5.3-BETA
 */
@Log4j2
public class Database {

    @Getter
    private static Database instance;

    private final Sql2o sql2oConn;

    // Provide FIFO asynchronous model to our fork join pool class.
    private final ForkJoinPool workerTask;

    @SneakyThrows
    public Database(@NonNull AbstractConfig database, int parallelThreads) {
        Preconditions.checkState(instance == null, "Database already initialized");

        workerTask = new ForkJoinPool((parallelThreads > 0 && database instanceof MySQLConfig) ? parallelThreads : 1,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null, true);

        // Sql2o credentials. TODO: Add SSL support (MITM attack)
        sql2oConn = database.openConnection();

        try (java.sql.Connection con = sql2oConn.getConnectionSource().getConnection()) {
            if (con.isClosed()) throw new RuntimeException("Connection has unexpectedly closed.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        instance = this;

        new QueryDb(database instanceof MySQLConfig);

        startConnection();

        log.info(ASkyBlock.get().getPrefix() + TextFormat.GREEN + "Successfully connected to {} database, {}ms uptime.",
                TextFormat.YELLOW + (database instanceof MySQLConfig ? "mysql" : "sqlite") + TextFormat.GREEN,
                (TextFormat.GOLD + "" + getDatabasePing().join()));
    }

    private void startConnection() {
        executeUpdate(new QueryInfo(QueryDb.getInstance().worldTable)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().playerTable)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().playerChallenges)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().islandTable)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().islandData)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().islandRelations)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().islandLimitCount)).join();
        executeUpdate(new QueryInfo(QueryDb.getInstance().metadata)).join();
    }

    public CompletableFuture<Long> getDatabasePing() {
        CompletableFuture<Long> savedFuture = new CompletableFuture<>();

        workerTask.execute(() -> {
            @Cleanup Connection connection = sql2oConn.open();
            try {
                long lastTime = System.currentTimeMillis();

                @Cleanup Query query = connection.createQuery("SELECT 1");
                query.executeAndFetchTable();

                savedFuture.complete(System.currentTimeMillis() - lastTime);
            } catch (Throwable err) {
                log.throwing(err);

                savedFuture.completeExceptionally(err);
            }
        });

        return savedFuture;
    }

    /**
     * Fetches data from a query with a custom return data class. Useful if
     * you want to return data types in a class. This function is special as it allow
     * you to execute the query as bulking or one-by-one.
     *
     * @param returnData The class that will be returned.
     * @param query      The queries that will be executed.
     * @return The return types of the data.
     */
    public <A> CompletableFuture<List<A>> fetchClassData(Class<A> returnData, QueryInfo... query) {
        CompletableFuture<List<A>> savedFuture = new CompletableFuture<>();

        workerTask.execute(() -> {
            List<A> rows = new ArrayList<>();

            @Cleanup Connection connection = sql2oConn.open();
            try {
                for (QueryInfo queryInfo : query) {
                    @Cleanup Query conn = connection.createQuery(queryInfo.getQuery());
                    queryInfo.getParameter().forEach((param) -> conn.addParameter(param.getParamName(), param.getValue()));

                    rows.addAll(conn.executeAndFetch(returnData));
                }

                savedFuture.complete(rows);
            } catch (Throwable err) {
                log.throwing(err);

                savedFuture.completeExceptionally(err);
            }
        });

        return savedFuture;
    }

    /**
     * Fetches data from a query concurrently. Another addition if you want to
     * have a better way of receiving the same row concurrently.
     *
     * @param query The custom implementation of Query database
     * @return The future that is expected for class Table
     * @see Database#fetchBulkData(QueryInfo...)
     */
    public CompletableFuture<List<Table>> fetchBulkData(@NonNull QueryInfo... query) {
        CompletableFuture<List<Table>> savedFuture = new CompletableFuture<>();

        workerTask.execute(() -> {
            @Cleanup Connection connection = sql2oConn.open();

            List<Table> rows = new ArrayList<>();
            try {
                for (QueryInfo queryInfo : query) {
                    @Cleanup Query conn = connection.createQuery(queryInfo.getQuery());
                    queryInfo.getParameter().forEach((param) -> conn.addParameter(param.getParamName(), param.getValue()));

                    rows.add(conn.executeAndFetchTable());
                }

                savedFuture.complete(rows);
            } catch (Throwable err) {
                log.throwing(err);

                savedFuture.completeExceptionally(err);
            }
        });

        return savedFuture;
    }

    /**
     * Fetch a data from the mysql dataset with a provided query
     * information.
     *
     * @param queryInfo The custom implementation of Query database
     * @return The future that is expected for class Table
     * @see Database#executeUpdate(QueryInfo)
     */
    public CompletableFuture<Table> fetchData(@NonNull QueryInfo queryInfo) {
        CompletableFuture<Table> savedFuture = new CompletableFuture<>();

        workerTask.execute(() -> {
            @Cleanup Connection connection = sql2oConn.open();
            try {
                @Cleanup Query conn = connection.createQuery(queryInfo.getQuery());
                queryInfo.getParameter().forEach((param) -> conn.addParameter(param.getParamName(), param.getValue()));

                savedFuture.complete(conn.executeAndFetchTable());
            } catch (Throwable err) {
                log.throwing(err);

                savedFuture.completeExceptionally(err);
            }
        });

        return savedFuture;
    }

    /**
     * Attempts to update a query based on the provided parameters
     * As always, this will carry a {@link CompletableFuture<Void>} class
     * in which the class will be executed inside an async FIFO model pool.
     *
     * <p> For bulking processing, please use {@link Database#processBulkUpdate(QueryInfo...)}
     * as it allows multiple queries to be executed in a single batch.
     *
     * @param query The query that needs to be executed
     * @return A CompletableFuture class marking the execution is completed.
     */
    public CompletableFuture<Void> executeUpdate(@NonNull QueryInfo query) {
        CompletableFuture<Void> savedFuture = new CompletableFuture<>();

        workerTask.execute(() -> {
            @Cleanup Connection connection = sql2oConn.open();
            try {
                @Cleanup Query conn = connection.createQuery(query.getQuery());
                query.getParameter().forEach((param) -> conn.addParameter(param.getParamName(), param.getValue()));

                conn.executeUpdate();

                savedFuture.complete(null);
            } catch (Throwable err) {
                log.throwing(err);

                savedFuture.completeExceptionally(err);
            }
        });

        return savedFuture;
    }

    /**
     * Process bulk changes of a QueryInfo class. This function is to cut down
     * the amount of bandwidth needed to upload data each bulk updates.
     *
     * <p> Unlike {@link Database#executeUpdate(QueryInfo)}, this function only executes an update
     * once per execution, which is bad for bulking as it will add more queues into database
     * queries pool.
     *
     * @param queryInfo The custom implementation of Query database
     * @return The future that is successfully ran.
     */
    public CompletableFuture<Void> processBulkUpdate(@NonNull QueryInfo... queryInfo) {
        CompletableFuture<Void> savedFuture = new CompletableFuture<>();

        workerTask.execute(() -> {
            @Cleanup Connection connection = sql2oConn.open();
            try {
                connection.getJdbcConnection().setAutoCommit(false);
                for (QueryInfo queue : queryInfo) {
                    @Cleanup Query conn = connection.createQuery(queue.getQuery());
                    queue.getParameter().forEach((param) -> conn.addParameter(param.getParamName(), param.getValue()));

                    conn.executeUpdate();
                }
                connection.getJdbcConnection().commit();

                savedFuture.complete(null);
            } catch (Throwable err) {
                log.throwing(err);

                savedFuture.completeExceptionally(err);
            }
        });

        return savedFuture;
    }

    @SneakyThrows
    public void shutdown() {
        workerTask.shutdown();
        workerTask.awaitTermination(150000, TimeUnit.SECONDS);

        log.info(ASkyBlock.get().getPrefix() + TextFormat.RED + "Database operations are now disabled.");
    }
}
