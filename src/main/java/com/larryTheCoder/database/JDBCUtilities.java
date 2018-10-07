/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2018 larryTheCoder and contributors
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
 * Safe error stack trace for the plugin,
 * Keeping the database to not interrupt the operation
 *
 * @author larryTheCoder
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

    public static String convertDocumentToString(Document doc) throws TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
//    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();

    }
}
