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

package org.apache.isis.alternatives.objectstore.sql.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.alternatives.objectstore.sql.AbstractDatabaseConnector;
import org.apache.isis.alternatives.objectstore.sql.Results;
import org.apache.isis.alternatives.objectstore.sql.SqlMetaData;
import org.apache.isis.alternatives.objectstore.sql.SqlObjectStore;
import org.apache.isis.alternatives.objectstore.sql.SqlObjectStoreException;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.log4j.Logger;

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
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Failed to close", e);
		}
	}

	@Override
	public int count(final String sql) {
		LOG.debug("SQL: " + sql);
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			result.next();
			int count = result.getInt(1);
			statement.close();
			return count;
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Failed count", e);
		}
	}

	@Override
	public void delete(final String sql) {
		update(sql);
	}

	public void open() {
		try {
			IsisConfiguration params = IsisContext.getConfiguration();
			String BASE = SqlObjectStore.BASE_NAME + ".jdbc.";
			String driver = params.getString(BASE + "driver");
			String url = params.getString(BASE + "connection");
			String user = params.getString(BASE + "user");
			String password = params.getString(BASE + "password");

			if (connection != null) {
				throw new SqlObjectStoreException(
						"Connection already established");
			}

			if (driver == null) {
				throw new SqlObjectStoreException(
						"No driver specified for database connection");
			}
			if (url == null) {
				throw new SqlObjectStoreException(
						"No connection URL specified to database");
			}
			if (user == null) {
				throw new SqlObjectStoreException(
						"No user specified for database connection");
			}
			if (password == null) {
				throw new SqlObjectStoreException(
						"No password specified for database connection");
			}

			Class.forName(driver);
			LOG.info("Connecting to " + url + " as " + user);
			connection = DriverManager.getConnection(url, user, password);
			if (connection == null) {
				throw new SqlObjectStoreException(
						"No connection established to " + url);
			}
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Failed to start", e);
		} catch (ClassNotFoundException e) {
			throw new SqlObjectStoreException("Could not find database driver",
					e);
		}
	}

	/*
	 * public void executeStoredProcedure(final StoredProcedure storedProcedure)
	 * { Parameter[] parameters = storedProcedure.getParameters(); StringBuffer
	 * sql = new StringBuffer("{call "); sql.append(storedProcedure.getName());
	 * sql.append(" ("); for (int i = 0, no = parameters.length; i < no; i++) {
	 * sql.append(i == 0 ? "?" : ",?"); } sql.append(")}"); LOG.debug("SQL: " +
	 * sql);
	 * 
	 * CallableStatement statement; try { statement =
	 * connection.prepareCall(sql.toString());
	 * 
	 * for (int i = 0; i < parameters.length; i++) { LOG.debug(" setup param " +
	 * i + " " + parameters[i]); parameters[i].setupParameter(i + 1,
	 * parameters[i].getName(), storedProcedure); } LOG.debug(" execute ");
	 * statement.execute(); for (int i = 0; i < parameters.length; i++) {
	 * parameters[i].retrieve(i + 1, parameters[i].getName(), storedProcedure);
	 * LOG.debug(" retrieve param " + i + " " + parameters[i]); } } catch
	 * (SQLException e) { throw new ObjectAdapterRuntimeException(e); }
	 * 
	 * }
	 * 
	 * 
	 * public MultipleResults executeStoredProcedure(final String name, final
	 * Parameter[] parameters) { StringBuffer sql = new StringBuffer("{call ");
	 * sql.append(name); sql.append(" ("); for (int i = 0; i <
	 * parameters.length; i++) { sql.append(i == 0 ? "?" : ",?"); }
	 * sql.append(")}"); LOG.debug("SQL: " + sql);
	 * 
	 * CallableStatement statement; try { statement =
	 * connection.prepareCall(sql.toString());
	 * 
	 * StoredProcedure storedProcedure = new JdbcStoredProcedure(statement);
	 * 
	 * for (int i = 0; i < parameters.length; i++) { LOG.debug(" setup param " +
	 * i + " " + parameters[i]); parameters[i].setupParameter(i + 1,
	 * parameters[i].getName(), storedProcedure); } LOG.debug(" execute ");
	 * statement.execute(); for (int i = 0; i < parameters.length; i++) {
	 * parameters[i].retrieve(i + 1, parameters[i].getName(), storedProcedure);
	 * LOG.debug(" retrieve param " + i + " " + parameters[i]); }
	 * 
	 * return new JdbcResults(statement); } catch (SQLException e) { throw new
	 * ObjectAdapterRuntimeException(e); } }
	 */

	@Override
	public Results select(final String sql) {
		LOG.debug("SQL: " + sql);
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			addPreparedValues(statement);
			return new JdbcResults(statement.executeQuery());
		} catch (SQLException e) {
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
			int updateCount = statement.executeUpdate();
			statement.close();
			return updateCount;
		} catch (SQLException e) {
			LOG.error("failed to execute " + sql, e);
			throw new SqlObjectStoreException("SQL error", e);
		} finally {
			clearPreparedValues();
		}
	}

	private void clearPreparedValues() {
		queryValues.clear();
	}

	private void addPreparedValues(PreparedStatement statement)
			throws SQLException {
		if (queryValues.size() > 0) {
			int i = 1;
			try {
				for (Object value : queryValues) {
					statement.setObject(i, value);
					i++;
				}
			} catch (SQLException e) {
				LOG.error("Error adding prepared value "+ i +" of type " + queryValues.get(i-1).getClass().getSimpleName(), e);
				throw e;
			}
		}
	}

	@Override
	public boolean hasTable(final String tableName) {
		try {
			ResultSet set = connection.getMetaData().getTables(null, null,
					tableName, null);
			if (set.next()) {
				LOG.debug("Found " + set.getString("TABLE_NAME"));
				set.close();
				return true;
			} else {
				set.close();
				return false;
			}
		} catch (SQLException e) {
			throw new SqlObjectStoreException(e);
		}
	}

	@Override
	public boolean hasColumn(final String tableName, final String columnName) {
		try {
			ResultSet set = connection.getMetaData().getColumns(null, null,
					tableName, columnName);
			if (set.next()) {
				LOG.debug("Found " + set.getString("COLUMN_NAME") + " in "
						+ set.getString("TABLE_NAME"));
				set.close();
				return true;
			} else {
				set.close();
				return false;
			}
		} catch (SQLException e) {
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
			 * require 3.0 ResultSet rs = statement.getGeneratedKeys();
			 * if(rs.next()) { int id = rs.getInt(1); }
			 */statement.close();
		} catch (SQLException e) {
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
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Commit error", e);
		}
	}

	@Override
	public void begin() {
		try {
			LOG.debug("begin transaction");
			connection.setAutoCommit(false);
			clearPreparedValues();
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Rollback error", e);
		}

	}

	@Override
	public void rollback() {
		try {
			LOG.debug("rollback");
			connection.rollback();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Rollback error", e);
		}
	}

	@Override
	public SqlMetaData getMetaData() {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			return new JdbcSqlMetaData(metaData);
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Metadata error", e);
		}
	}

	@Override
	public void debug(DebugString debug) {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			debug.appendln("Product", metaData.getDatabaseProductName() + "  "
					+ metaData.getDatabaseProductVersion());
			try {
				debug.appendln(
						"Product Version",
						metaData.getDatabaseMajorVersion() + "."
								+ metaData.getDatabaseMinorVersion());
			} catch (AbstractMethodError ignore) {
			}
			debug.appendln(
					"Drive",
					metaData.getDriverName() + "  "
							+ metaData.getDriverVersion());
			debug.appendln("Driver Version", metaData.getDriverMajorVersion()
					+ "." + metaData.getDriverMinorVersion());
			debug.appendln("Keywords", metaData.getSQLKeywords());
			debug.appendln("Date/Time functions",
					metaData.getTimeDateFunctions());
			debug.appendln("Date/Time functions",
					metaData.getTimeDateFunctions());
			debug.appendln("Mixed case identifiers",
					metaData.supportsMixedCaseIdentifiers());
			debug.appendln("Lower case identifiers",
					metaData.storesLowerCaseIdentifiers());
			debug.appendln("Lower case quoted",
					metaData.storesLowerCaseQuotedIdentifiers());
			debug.appendln("Mixed case identifiers",
					metaData.storesMixedCaseIdentifiers());
			debug.appendln("Mixed case quoted",
					metaData.storesMixedCaseQuotedIdentifiers());
			debug.appendln("Upper case identifiers",
					metaData.storesUpperCaseIdentifiers());
			debug.appendln("Upper case quoted",
					metaData.storesUpperCaseQuotedIdentifiers());
			debug.appendln("Max table name length",
					metaData.getMaxTableNameLength() );
			debug.appendln("Max column name length",
					metaData.getMaxColumnNameLength());
			
		} catch (SQLException e) {
			throw new SqlObjectStoreException("Metadata error", e);
		}
	}

	private final List<Object> queryValues = new ArrayList<Object>();

	@Override
	public String addToQueryValues(int i) {
		queryValues.add(i);
		return "?";
	}

	@Override
	public String addToQueryValues(String s) {
		queryValues.add(s);
		return "?";
	}

	@Override
	public String addToQueryValues(Object o) {
		queryValues.add(o);
		return "?";
	}
}
