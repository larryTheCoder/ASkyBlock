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
package com.larryTheCoder.database.config;

import cn.nukkit.utils.Config;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.Utils;
import org.sql2o.Sql2o;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author larryTheCoder
 */
public class SQLiteConfig implements AbstractConfig {

    private final String dbLocation;
    private final File file;
    private Sql2o connection;

    public SQLiteConfig(Config data) {
        this.file = new File(ASkyBlock.get().getDataFolder(), data.getString("database.SQLite.file-name"));
        this.dbLocation = file.getAbsolutePath();
    }

    public String getAbsolutePath() {
        return dbLocation;
    }

    @Override
    public Sql2o forceConnection() {
        return new Sql2o("jdbc:sqlite:" + this.dbLocation, null, null);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Sql2o openConnection() throws SQLException {
        if (checkConnection()) return this.connection;

        File file = new File(this.dbLocation);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
                Utils.send("&cUnable to create database!");
            }
        }

        return new Sql2o("jdbc:sqlite:" + this.dbLocation, null, null);
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
        return "SQLite, " + file.getName();
    }
}
