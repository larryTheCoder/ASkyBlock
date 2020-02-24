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

import org.sql2o.Sql2o;
import org.sql2o.data.Table;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author larryTheCoder
 */
public interface AbstractConfig {

    Sql2o forceConnection();

    /**
     * Opens a connection with the database.
     *
     * @return Opened connection
     * @throws SQLException           if the connection can not be opened
     * @throws ClassNotFoundException if the driver cannot be found
     */
    Sql2o openConnection() throws SQLException, ClassNotFoundException;

    /**
     * Checks if a connection is open with the database.
     *
     * @return true if the connection is open
     * @throws SQLException if the connection cannot be checked
     */
    boolean checkConnection() throws SQLException;

    /**
     * Gets the connection with the database.
     *
     * @return Connection with the database, null if none
     */
    Sql2o getConnection();

    /**
     * Closes the connection with the database.
     *
     * @return true if successful
     * @throws SQLException if the connection cannot be closed
     */
    boolean closeConnection() throws SQLException;
}
