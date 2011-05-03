/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.runtimes.dflt.objectstores.sql.AbstractDatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlMetaData;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlObjectStore;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlObjectStoreException;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class JdbcConnector extends AbstractDatabaseConnector {
    private static final Logger LOG = Logger.getLogger(JdbcConnector.class);
    private Connection connection;

    @Override
    public void close() {
        try {
            if (connection != null) {
                LOG.info("close");
                connection.close();
            }
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Failed to close", e);
        }
    }

    @Override
    public int count(final String sql) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            final ResultSet result = statement.executeQuery();
            result.next();
            final int count = result.getInt(1);
            statement.close();
            return count;
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Failed count", e);
        }
    }

    @Override
    public void delete(final String sql) {
        update(sql);
    }

    public void open() {
        final String BASE = SqlObjectStore.BASE_NAME + ".jdbc.";
        final IsisConfiguration params = IsisContext.getConfiguration().getProperties(BASE);

        try {
            final String driver = params.getString(BASE + "driver");
            final String url = params.getString(BASE + "connection");
            final String user = params.getString(BASE + "user");
            final String password = params.getString(BASE + "password");

            if (connection != null) {
                throw new SqlObjectStoreException("Connection already established");
            }

            if (driver == null) {
                throw new SqlObjectStoreException("No driver specified for database connection");
            }
            if (url == null) {
                throw new SqlObjectStoreException("No connection URL specified to database");
            }
            if (user == null) {
                throw new SqlObjectStoreException("No user specified for database connection");
            }
            if (password == null) {
                throw new SqlObjectStoreException("No password specified for database connection");
            }

            Class.forName(driver);
            LOG.info("Connecting to " + url + " as " + user);
            connection = DriverManager.getConnection(url, user, password);
            if (connection == null) {
                throw new SqlObjectStoreException("No connection established to " + url);
            }
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Failed to start", e);
        } catch (final ClassNotFoundException e) {
            throw new SqlObjectStoreException("Could not find database driver", e);
        }

        final String BASE_DATATYPE = SqlObjectStore.BASE_NAME + ".datatypes.";
        final IsisConfiguration dataTypes = IsisContext.getConfiguration().getProperties(BASE_DATATYPE);
        populateSqlDataTypes(dataTypes, BASE_DATATYPE);
    }

    /*
     * public void executeStoredProcedure(final StoredProcedure storedProcedure) { Parameter[] parameters =
     * storedProcedure.getParameters(); StringBuffer sql = new StringBuffer("{call ");
     * sql.append(storedProcedure.getName()); sql.append(" ("); for (int i = 0, no = parameters.length; i < no; i++) {
     * sql.append(i == 0 ? "?" : ",?"); } sql.append(")}"); LOG.debug("SQL: " + sql);
     * 
     * CallableStatement statement; try { statement = connection.prepareCall(sql.toString());
     * 
     * for (int i = 0; i < parameters.length; i++) { LOG.debug(" setup param " + i + " " + parameters[i]);
     * parameters[i].setupParameter(i + 1, parameters[i].getName(), storedProcedure); } LOG.debug(" execute ");
     * statement.execute(); for (int i = 0; i < parameters.length; i++) { parameters[i].retrieve(i + 1,
     * parameters[i].getName(), storedProcedure); LOG.debug(" retrieve param " + i + " " + parameters[i]); } } catch
     * (SQLException e) { throw new ObjectAdapterRuntimeException(e); }
     * 
     * }
     * 
     * 
     * public MultipleResults executeStoredProcedure(final String name, final Parameter[] parameters) { StringBuffer sql
     * = new StringBuffer("{call "); sql.append(name); sql.append(" ("); for (int i = 0; i < parameters.length; i++) {
     * sql.append(i == 0 ? "?" : ",?"); } sql.append(")}"); LOG.debug("SQL: " + sql);
     * 
     * CallableStatement statement; try { statement = connection.prepareCall(sql.toString());
     * 
     * StoredProcedure storedProcedure = new JdbcStoredProcedure(statement);
     * 
     * for (int i = 0; i < parameters.length; i++) { LOG.debug(" setup param " + i + " " + parameters[i]);
     * parameters[i].setupParameter(i + 1, parameters[i].getName(), storedProcedure); } LOG.debug(" execute ");
     * statement.execute(); for (int i = 0; i < parameters.length; i++) { parameters[i].retrieve(i + 1,
     * parameters[i].getName(), storedProcedure); LOG.debug(" retrieve param " + i + " " + parameters[i]); }
     * 
     * return new JdbcResults(statement); } catch (SQLException e) { throw new ObjectAdapterRuntimeException(e); } }
     */

    static String TYPE_BOOLEAN;
    static String TYPE_TIMESTAMP;
    static String TYPE_DATETIME;
    static String TYPE_DATE;
    static String TYPE_TIME;
    static String TYPE_SHORT;
    static String TYPE_DOUBLE;
    static String TYPE_FLOAT;
    static String TYPE_LONG;
    static String TYPE_INT;
    static String TYPE_PK;
    static String TYPE_STRING;
    static String TYPE_PASSWORD;
    static String TYPE_DEFAULT;

    /**
     * Default SQL data types used to define the fields in the database. By providing this method, we allow the user an
     * opportunity to override these types by specifying alternatives in sql.properties (or which ever). For example,
     * Postgresql does not know about DATETIME, but can use TIMESTAMP instead.
     * 
     * @param dataTypes
     * @param baseName
     */
    private static void populateSqlDataTypes(final IsisConfiguration dataTypes, final String baseName) {
        TYPE_TIMESTAMP = dataTypes.getString(baseName + "timestamp", "DATETIME");
        TYPE_DATETIME = dataTypes.getString(baseName + "datetime", "DATETIME");
        TYPE_DATE = dataTypes.getString(baseName + "date", "DATE");
        TYPE_TIME = dataTypes.getString(baseName + "time", "TIME");
        TYPE_DOUBLE = dataTypes.getString(baseName + "double", "DOUBLE");
        TYPE_FLOAT = dataTypes.getString(baseName + "float", "FLOAT");
        TYPE_SHORT = dataTypes.getString(baseName + "short", "INT");
        TYPE_LONG = dataTypes.getString(baseName + "long", "BIGINT");
        TYPE_INT = dataTypes.getString(baseName + "int", "INT");
        TYPE_BOOLEAN = dataTypes.getString(baseName + "boolean", "BOOLEAN"); // CHAR(1)
        TYPE_PK = dataTypes.getString(baseName + "primarykey", "INTEGER");
        TYPE_STRING = dataTypes.getString(baseName + "string", "VARCHAR(65)");
        TYPE_PASSWORD = dataTypes.getString(baseName + "password", "VARCHAR(12)");
        TYPE_DEFAULT = dataTypes.getString(baseName + "default", "VARCHAR(65)");
    }

    public static String TYPE_TIMESTAMP() {
        return TYPE_TIMESTAMP;
    }

    public static String TYPE_SHORT() {
        return TYPE_SHORT;
    }

    public static String TYPE_INT() {
        return TYPE_INT;
    }

    public static String TYPE_LONG() {
        return TYPE_LONG;
    }

    public static String TYPE_FLOAT() {
        return TYPE_FLOAT;
    }

    public static String TYPE_DOUBLE() {
        return TYPE_DOUBLE;
    }

    public static String TYPE_BOOLEAN() {
        return TYPE_BOOLEAN;
    }

    public static String TYPE_PK() {
        return TYPE_PK;
    }

    public static String TYPE_STRING() {
        return TYPE_STRING;
    }

    public static String TYPE_PASSWORD() {
        return TYPE_PASSWORD;
    }

    public static String TYPE_DEFAULT() {
        return TYPE_DEFAULT;
    }

    @Override
    public Results select(final String sql) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            addPreparedValues(statement);
            return new JdbcResults(statement.executeQuery());
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        } finally {
            clearPreparedValues();
        }
    }

    @Override
    public int update(final String sql) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            addPreparedValues(statement);
            final int updateCount = statement.executeUpdate();
            statement.close();
            return updateCount;
        } catch (final SQLException e) {
            LOG.error("failed to execute " + sql, e);
            throw new SqlObjectStoreException("SQL error: " + e.toString(), e);
        } finally {
            clearPreparedValues();
        }
    }

    private void clearPreparedValues() {
        queryValues.clear();
    }

    private void addPreparedValues(final PreparedStatement statement) throws SQLException {
        if (queryValues.size() > 0) {
            int i = 1;
            try {
                for (final Object value : queryValues) {
                    if (value instanceof LocalDate) {
                        try {
                            statement.setObject(i, value, java.sql.Types.DATE);
                        } catch (final SQLException e) {
                            // This daft catch is required my MySQL, which also requires the TimeZone offset to be
                            // "undone"
                            final LocalDate localDate = (LocalDate) value;
                            final int millisOffset = -DateTimeZone.getDefault().getOffset(null);
                            final Date javaDate =
                                localDate.toDateTimeAtStartOfDay(DateTimeZone.forOffsetMillis(millisOffset)).toDate();

                            statement.setObject(i, javaDate, java.sql.Types.DATE);
                        }
                    } else {
                        statement.setObject(i, value);
                    }
                    i++;
                }
            } catch (final SQLException e) {
                LOG.error("Error adding prepared value " + i + " of type "
                    + queryValues.get(i - 1).getClass().getSimpleName(), e);
                throw e;
            }
        }
    }

    @Override
    public boolean hasTable(final String tableName) {
        try {
            final ResultSet set = connection.getMetaData().getTables(null, null, tableName, null);
            if (set.next()) {
                LOG.debug("Found " + set.getString("TABLE_NAME"));
                set.close();
                return true;
            } else {
                set.close();
                return false;
            }
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public boolean hasColumn(final String tableName, final String columnName) {
        try {
            final ResultSet set = connection.getMetaData().getColumns(null, null, tableName, columnName);
            if (set.next()) {
                LOG.debug("Found " + set.getString("COLUMN_NAME") + " in " + set.getString("TABLE_NAME"));
                set.close();
                return true;
            } else {
                set.close();
                return false;
            }
        } catch (final SQLException e) {
            throw new SqlObjectStoreException(e);
        }
    }

    @Override
    public void insert(final String sql) {
        update(sql);
    }

    @Override
    public void insert(final String sql, final Object oid) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            /*
             * require 3.0 ResultSet rs = statement.getGeneratedKeys(); if(rs.next()) { int id = rs.getInt(1); }
             */statement.close();
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("SQL error", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void commit() {
        try {
            LOG.debug("commit");
            connection.commit();
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Commit error", e);
        }
    }

    @Override
    public void begin() {
        try {
            LOG.debug("begin transaction");
            connection.setAutoCommit(false);
            clearPreparedValues();
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Rollback error", e);
        }

    }

    @Override
    public void rollback() {
        try {
            LOG.debug("rollback");
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Rollback error", e);
        }
    }

    @Override
    public SqlMetaData getMetaData() {
        try {
            final DatabaseMetaData metaData = connection.getMetaData();
            return new JdbcSqlMetaData(metaData);
        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Metadata error", e);
        }
    }

    @Override
    public void debug(final DebugBuilder debug) {
        try {
            final DatabaseMetaData metaData = connection.getMetaData();
            debug.appendln("Product", metaData.getDatabaseProductName() + "  " + metaData.getDatabaseProductVersion());
            try {
                debug.appendln("Product Version",
                    metaData.getDatabaseMajorVersion() + "." + metaData.getDatabaseMinorVersion());
            } catch (final AbstractMethodError ignore) {
            }
            debug.appendln("Drive", metaData.getDriverName() + "  " + metaData.getDriverVersion());
            debug.appendln("Driver Version", metaData.getDriverMajorVersion() + "." + metaData.getDriverMinorVersion());
            debug.appendln("Keywords", metaData.getSQLKeywords());
            debug.appendln("Date/Time functions", metaData.getTimeDateFunctions());
            debug.appendln("Mixed case identifiers", metaData.supportsMixedCaseIdentifiers());
            debug.appendln("Lower case identifiers", metaData.storesLowerCaseIdentifiers());
            debug.appendln("Lower case quoted", metaData.storesLowerCaseQuotedIdentifiers());
            debug.appendln("Mixed case identifiers", metaData.storesMixedCaseIdentifiers());
            debug.appendln("Mixed case quoted", metaData.storesMixedCaseQuotedIdentifiers());
            debug.appendln("Upper case identifiers", metaData.storesUpperCaseIdentifiers());
            debug.appendln("Upper case quoted", metaData.storesUpperCaseQuotedIdentifiers());
            debug.appendln("Max table name length", metaData.getMaxTableNameLength());
            debug.appendln("Max column name length", metaData.getMaxColumnNameLength());

        } catch (final SQLException e) {
            throw new SqlObjectStoreException("Metadata error", e);
        }
    }

    private final List<Object> queryValues = new ArrayList<Object>();

    @Override
    public String addToQueryValues(final int i) {
        queryValues.add(i);
        return "?";
    }

    @Override
    public String addToQueryValues(final String s) {
        queryValues.add(s);
        return "?";
    }

    @Override
    public String addToQueryValues(final Object o) {
        queryValues.add(o);
        return "?";
    }

}
