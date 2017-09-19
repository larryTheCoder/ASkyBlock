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
 */

package com.larryTheCoder.database.variables;

import com.larryTheCoder.utils.Utils;

import java.sql.*;

/**
 * @author Adam Matthew
 */
public class MySQLDatabase implements AbstractDatabase {


    private final String user;
    private final String database;
    private final String password;
    private final int port;
    private final String hostname;
    private Connection connection;

    /**
     * Creates a new MySQL instance.
     *
     * @param hostname Name of the host
     * @param port     Port number
     * @param database Database name
     * @param username Username
     * @param password Password
     */
    public MySQLDatabase(String hostname, int port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
        this.connection = null;
    }

    @Override
    public Connection forceConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        this.connection =
            DriverManager.getConnection("jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database, this.user, this.password);
        return this.connection;
    }

    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return this.connection;
        }
        Class.forName("com.mysql.jdbc.Driver");
        Utils.send("jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database);
        this.connection =
            DriverManager.getConnection("jdbc:mysql://" + this.hostname + ':' + this.port + '/' + this.database, this.user, this.password);
        return this.connection;
    }

    @Override
    public boolean checkConnection() throws SQLException {
        return (this.connection != null) && !this.connection.isClosed();
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public boolean closeConnection() throws SQLException {
        if (this.connection == null) {
            return false;
        }
        this.connection.close();
        this.connection = null;
        return true;
    }

    @Override
    public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement statement = this.connection.createStatement()) {
            return statement.executeQuery(query);
        }
    }

    @Override
    public int updateSQL(String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement statement = this.connection.createStatement()) {
            return statement.executeUpdate(query);
        }
    }
}
