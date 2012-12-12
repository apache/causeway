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

package org.apache.isis.security.sql.authentication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.authentication.standard.PasswordRequestAuthenticatorAbstract;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class SqlAuthenticator extends PasswordRequestAuthenticatorAbstract {
    private static final Logger LOG = Logger.getLogger(SqlAuthenticator.class);

    static SqlAuthenticator instance;

    public static SqlAuthenticator getInstance() {
        return instance;
    }

    // Override this method if dbPasswords are encoded.
    protected boolean verifyPasswordsAreEqual(final String loginPassword, final String dbPassword) {
        return dbPassword.equals(loginPassword);
    }

    // Override this method
    protected ResultSet postProcessLogin(final String user, final String password, final ResultSet results) {
        return results;
    }

    // override this method
    protected String getRoles(final ResultSet results, final String roles) {
        try {
            if (userRoleField != null) {
                final String dbRoles = results.getString(userRoleField);
                return roles + "|" + dbRoles;
            }
        } catch (final SQLException e) {
            LOG.warn("Error fetching role", e);
        }
        return roles;
    }

    // Override this method, if you want to create data.
    public void initialise() {
    }

    static final String PROPERTY_BASE = "isis.authentication.sql";
    static final String USER_TABLE = PROPERTY_BASE + ".userTable";
    static final String USER_TABLE_NAME_FIELD = PROPERTY_BASE + ".userNameField";
    static final String USER_TABLE_PASSWORD_FIELD = PROPERTY_BASE + ".passwordField";
    static final String USER_TABLE_ROLE_FIELD = PROPERTY_BASE + ".roleField";

    private Connection connection;

    final String passwordField;
    final String userTable;
    final String userNameField;
    final String userRoleField;

    public static String getPropertyBase() {
        return PROPERTY_BASE;
    }

    public static String getPropertyUserTable() {
        return USER_TABLE;
    }

    public static String getPropertyUserTableNameField() {
        return USER_TABLE_NAME_FIELD;
    }

    public static String getPropertyUserTablePasswordField() {
        return USER_TABLE_PASSWORD_FIELD;
    }

    public static String getPropertyUserTableRoleField() {
        return USER_TABLE_ROLE_FIELD;
    }

    @Inject
    public SqlAuthenticator(final IsisConfiguration configuration) {
        super(configuration);

        userTable = configuration.getString(USER_TABLE);
        userNameField = configuration.getString(USER_TABLE_NAME_FIELD);
        passwordField = configuration.getString(USER_TABLE_PASSWORD_FIELD);
        userRoleField = configuration.getString(USER_TABLE_ROLE_FIELD);

        instance = this;
    }

    @Override
    public void init() {
        if (connection != null) {
            LOG.info("close");
        }

        final String BASE = PROPERTY_BASE + ".jdbc.";
        final IsisConfiguration params = IsisContext.getConfiguration().getProperties(BASE);

        try {
            final String driver = params.getString(BASE + "driver");
            final String url = params.getString(BASE + "connection");
            final String user = params.getString(BASE + "user");
            final String password = params.getString(BASE + "password");

            if (connection != null) {
                throw new ApplicationException("Connection already established");
            }

            if (driver == null) {
                throw new ApplicationException("No driver specified for database connection");
            }
            if (url == null) {
                throw new ApplicationException("No connection URL specified to database");
            }
            if (user == null) {
                throw new ApplicationException("No user specified for database connection");
            }
            if (password == null) {
                throw new ApplicationException("No password specified for database connection");
            }

            Class.forName(driver);
            LOG.info("Connecting to " + url + " as " + user);
            connection = DriverManager.getConnection(url, user, password);
            if (connection == null) {
                throw new ApplicationException("No connection established to " + url);
            }
        } catch (final SQLException e) {
            throw new ApplicationException("Failed to start", e);
        } catch (final ClassNotFoundException e) {
            throw new ApplicationException("Could not find database driver", e);
        }

    }

    @Override
    public void shutdown() {
        String sql;
        sql = "SHUTDOWN";
        update(sql);
        closeConnection();
    }

    private void closeConnection() {
        try {
            connection.close();
            connection = null;
        } catch (final SQLException e) {
            LOG.warn("Failed to close connection:" + e);
        }
    }

    @Override
    public final boolean isValid(final AuthenticationRequest request) {
        final AuthenticationRequestPassword passwordRequest = (AuthenticationRequestPassword) request;
        final String username = passwordRequest.getName();
        if (Strings.isNullOrEmpty(username)) {
            return false;
        }
        final String password = passwordRequest.getPassword();
        Assert.assertNotNull(password);

        return isPasswordValidForUser(passwordRequest, username, password);

    }

    private boolean isPasswordValidForUser(final AuthenticationRequest request, final String user, final String password) {
        final ResultSet results = loadUserDetails(user, password);
        if (results != null) {
            final String roles = getRoles(results, "org.apache.isis.viewer.wicket.roles.USER|org.starobjects.wicket.roles.USER");
            setRoles(request, roles);

            return true;
        }
        return false;
    }

    protected ResultSet loadUserDetails(final String user, final String password) {
        final String sql = "SELECT * FROM " + userTable + " WHERE UPPER(" + userNameField + ") = ?";
        addToQueryValues(user.toUpperCase());
        final ResultSet results = select(sql);
        try {
            while (results.next()) {
                final String dbPassword = results.getString(passwordField);
                if (verifyPasswordsAreEqual(password, dbPassword)) {
                    return postProcessLogin(user, password, results);
                }
            }
        } catch (final SQLException e) {
            LOG.error("Error loading user details: " + sql);
            throw new ApplicationException("Error loading user details", e);
        }

        return null;
    }

    private final void setRoles(final AuthenticationRequest request, final String line) {
        final StringTokenizer tokens = new StringTokenizer(line, "|", false);
        final String[] roles = new String[tokens.countTokens()];
        for (int i = 0; tokens.hasMoreTokens(); i++) {
            roles[i] = tokens.nextToken();
        }
        request.setRoles(Arrays.asList(roles));
    }

    public boolean isSetup() {
        return hasTable(userTable);
    }

    // {{ JDBC Connection SQL helpers
    protected int update(final String sql) {
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
            throw new ApplicationException("Error executing update", e);
        } finally {
            clearPreparedValues();
        }

    }

    private ResultSet select(final String sql) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            addPreparedValues(statement);
            return (statement.executeQuery());
        } catch (final SQLException e) {
            LOG.error("failed to execte select: " + sql, e);
            throw new ApplicationException("Error executing select", e);
        } finally {
            clearPreparedValues();
        }
    }

    private final List<Object> queryValues = new ArrayList<Object>();

    public String addToQueryValues(final Object o) {
        queryValues.add(o);
        return "?";
    }

    private void clearPreparedValues() {
        queryValues.clear();
    }

    private void addPreparedValues(final PreparedStatement statement) throws SQLException {
        if (queryValues.size() > 0) {
            int i = 1;
            try {
                for (final Object value : queryValues) {
                    statement.setObject(i, value);
                    i++;
                }
            } catch (final SQLException e) {
                LOG.error("Error adding prepared value " + i + " of type " + queryValues.get(i - 1).getClass().getSimpleName(), e);
                throw e;
            }
        }
    }

    private boolean hasTable(final String tableName) {
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
            LOG.error("failed to find table: " + tableName, e);
            throw new ApplicationException("Error checking for table: " + tableName, e);
        }
    }

    // }}

}
