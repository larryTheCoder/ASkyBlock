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
package com.larryTheCoder.db2.config;

import cn.nukkit.utils.Config;
import com.larryTheCoder.utils.Utils;
import org.sql2o.Sql2o;

import java.sql.SQLException;

/**
 * @author larryTheCoder
 */
public class MySQLConfig implements AbstractConfig {

    private final String user;
    private final String database;
    private final String password;
    private final int port;
    private final String hostname;
    private Sql2o connection;

    /**
     * Creates a new MySQL instance.
     *
     * @param hostname Name of the host
     * @param port     Port number
     * @param database Database name
     * @param username Username
     * @param password Password
     */
    public MySQLConfig(String hostname, int port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
        this.connection = null;
    }

    public MySQLConfig(Config config){
        this.hostname = config.getString("database.MySQL.host");
        this.port =  config.getInt("database.MySQL.port");
        this.database = config.getString("database.MySQL.database");
        this.user = config.getString("database.MySQL.username");
        this.password = config.getString("database.MySQL.password");
        this.connection = null;
    }

    @Override
    public Sql2o forceConnection() {
        return new Sql2o("jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database, this.user, this.password);
    }

    @Override
    public Sql2o openConnection() throws SQLException {
        if (checkConnection()) {
            return this.connection;
        }

        Utils.send("&aConnecting to: jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database);
        return new Sql2o("jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database, this.user, this.password);
    }

    @Override
    public boolean checkConnection() throws SQLException {
        if (connection == null) return false;

        try (java.sql.Connection con = connection.getConnectionSource().getConnection()) {
            return !con.isClosed();
        }
    }

    @Override
    public Sql2o getConnection() {
        return this.connection;
    }

    @Override
    public boolean closeConnection() throws SQLException {
        if (this.connection == null) {
            return false;
        }
        this.connection.getConnectionSource().getConnection().close();
        this.connection = null;
        return true;
    }

    @Override
    public String toString() {
        return "jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database;
    }
}
