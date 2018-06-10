/*
 * Copyright (C) 2016-2018 Adam Matthew
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
package com.larryTheCoder.database;

import com.larryTheCoder.ASkyBlock;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.sql.*;

/**
 * @author Adam Matthew
 */
public class JDBCUtilities {
    public static void getWarningsFromResultSet(ResultSet rs) throws SQLException {
        JDBCUtilities.printWarnings(rs.getWarnings());
    }

    public static void getWarningsFromStatement(Statement stmt) throws SQLException {
        JDBCUtilities.printWarnings(stmt.getWarnings());
    }

    private static void printWarnings(SQLWarning warning) {
        if (warning != null) {
            ASkyBlock.get().getServer().getLogger().notice("\n---Warning---\n");
            while (warning != null) {
                ASkyBlock.get().getServer().getLogger().notice("Message: " + warning.getMessage());
                ASkyBlock.get().getServer().getLogger().notice("SQLState: " + warning.getSQLState());
                System.out.print("Vendor error code: ");
                ASkyBlock.get().getServer().getLogger().notice(Integer.toString(warning.getErrorCode()));
                ASkyBlock.get().getServer().getLogger().notice("");
                warning = warning.getNextWarning();
            }
        }
    }

    private static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            ASkyBlock.get().getServer().getLogger().notice("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32")) {
            return true;
        }
        // 42Y55: Table already exists in schema
        return sqlState.equalsIgnoreCase("42Y55");
    }

    public static void printBatchUpdateException(BatchUpdateException b) {
        ASkyBlock.get().getServer().getLogger().notice("----BatchUpdateException----");
        ASkyBlock.get().getServer().getLogger().notice("SQLState:  " + b.getSQLState());
        ASkyBlock.get().getServer().getLogger().notice("Message:  " + b.getMessage());
        ASkyBlock.get().getServer().getLogger().notice("Vendor:  " + b.getErrorCode());
        System.err.print("Update counts:  ");
        int[] updateCounts = b.getUpdateCounts();
        for (int updateCount : updateCounts) {
            System.err.print(updateCount + "   ");
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(((SQLException) e).getSQLState())) {
                    e.printStackTrace(System.err);
                    ASkyBlock.get().getServer().getLogger().notice("SQLState: " + ((SQLException) e).getSQLState());
                    ASkyBlock.get().getServer().getLogger().notice("Error Code: " + ((SQLException) e).getErrorCode());
                    ASkyBlock.get().getServer().getLogger().notice("Message: " + e.getMessage());
                    Throwable t = ex.getCause();
                    while (t != null) {
                        ASkyBlock.get().getServer().getLogger().notice("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static void alternatePrintSQLException(SQLException ex) {
        while (ex != null) {
            ASkyBlock.get().getServer().getLogger().notice("SQLState: " + ex.getSQLState());
            ASkyBlock.get().getServer().getLogger().notice("Error Code: " + ex.getErrorCode());
            ASkyBlock.get().getServer().getLogger().notice("Message: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                ASkyBlock.get().getServer().getLogger().notice("Cause: " + t);
                t = t.getCause();
            }
            ex = ex.getNextException();
        }
    }

    public static String convertDocumentToString(Document doc) throws
            TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
//    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();

    }
}
