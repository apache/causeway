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
package org.apache.isis.persistence.jdo.datanucleus.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.enhancer.EnhancementNucleusContextImpl;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.store.ConnectionEncryptionProvider;

import org.apache.isis.commons.internal.base._Strings;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @implNote the methods in this class are <tt>protected</tt> to allow for easy subclassing.
 */
@Log4j2
public class CreateSchemaObjectFromClassMetadata
implements MetaDataListener, DataNucleusPropertiesAware {

    private Map<String, Object> properties;

    // -- loaded (API)

    @Override
    public void loaded(final AbstractClassMetaData cmd) {

        final String schemaName = cmd.getSchema();
        if(_Strings.isNullOrEmpty(schemaName)) {
            return;
        }

        Connection connection = null;
        Statement statement = null;

        final String driverName = getPropertyAsString("javax.jdo.option.ConnectionDriverName");
        final String url = getPropertyAsString("javax.jdo.option.ConnectionURL");
        final String userName = getPropertyAsString("javax.jdo.option.ConnectionUserName");
        final String password = getConnectionPassword();

        if(_Strings.isNullOrEmpty(driverName) || _Strings.isNullOrEmpty(url)) {
            log.warn("Unable to create schema due to missing configuration javax.jdo.option.Connection*");
            return;
        }

        try {

            connection = DriverManager.getConnection(url, userName, password);

            statement = connection.createStatement();
            if(skip(cmd, statement)) {
                return;
            }
            exec(cmd, statement);

        } catch (SQLException e) {
            log.warn("Unable to create schema", e);

        } finally {
            closeSafely(statement);
            closeSafely(connection);
        }

    }

    // -- skip, exec, schemaNameFor

    /**
     * Whether to skip creating this schema.
     */
    protected boolean skip(final AbstractClassMetaData cmd, final Statement statement) throws SQLException {
        final String schemaName = cmd.getSchema();
        if(_Strings.isNullOrEmpty(schemaName)) {
            return true;
        }
        final String sql = buildSqlToCheck(cmd);
        try (final ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            final int cnt = rs.getInt(1);
            return cnt > 0;
        }
    }

    protected String buildSqlToCheck(final AbstractClassMetaData cmd) {
        final String schemaName = schemaNameFor(cmd);
        return String.format("SELECT count(*) FROM INFORMATION_SCHEMA.SCHEMATA where SCHEMA_NAME = '%s'", schemaName);
    }

    /**
     * Create the schema
     */
    protected boolean exec(final AbstractClassMetaData cmd, final Statement statement) throws SQLException {
        final String sql = buildSqlToExec(cmd);
        return statement.execute(sql);
    }

    protected String buildSqlToExec(final AbstractClassMetaData cmd) {
        final String schemaName = schemaNameFor(cmd);
        final String schemaCreateSqlSyntax = schemaCreateSqlSyntaxFor(cmd);
        return String.format(schemaCreateSqlSyntax, schemaName);
    }

    /**
     * Determine the name of the schema.
     */
    protected String schemaNameFor(final AbstractClassMetaData cmd) {
        return cmd.getSchema();
    }

    /**
     * Determine the schema creation SQL syntax.
     */
    protected String schemaCreateSqlSyntaxFor(final AbstractClassMetaData cmd) {
        val jdbcVariant = JdbcVariant.detect(getPropertyAsString("javax.jdo.option.ConnectionURL"));
        switch (jdbcVariant) {
        case MYSQL:
            //XXX [ISIS-2439]
            return "CREATE SCHEMA `%s`";
        case POSTGRES:
        case HSQLDB:
        case SQLSERVER:
        case OTHER:
        default:
            return "CREATE SCHEMA \"%s\"";
        }
    }

    @RequiredArgsConstructor
    private static enum JdbcVariant {
        POSTGRES(url->url.startsWith("jdbc:postgres:")),
        HSQLDB(url->url.startsWith("jdbc:hsqldb:")),
        SQLSERVER(url->url.startsWith("jdbc:sqlserver:")),
        MYSQL(url->url.startsWith("jdbc:mysql:")
                || url.startsWith("jdbc:mariadb:")),
        OTHER(url->true)
        ;
        final Predicate<String> matcher;
        static JdbcVariant detect(String connectionUrl) {
            return Stream.of(JdbcVariant.values())
            .filter(variant->variant.matcher.test(connectionUrl))
            .findFirst()
            .orElse(OTHER);
        }
    }

    // -- helpers: closeSafely, getConnectionPassword
    protected void closeSafely(final AutoCloseable connection) {
        if(connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    // copied and adapted from org.datanucleus.store.AbstractStoreManager.getConnectionPassword()
    /**
     * Convenience accessor for the password to use for the connection.
     * Will perform decryption if the persistence property "datanucleus.ConnectionPasswordDecrypter" has
     * also been specified.
     * @return Password
     */
    private String getConnectionPassword() {
        String password = getPropertyAsString("javax.jdo.option.ConnectionPassword");
        if (password != null)
        {
            String decrypterName = getPropertyAsString("datanucleus.ConnectionPasswordDecrypter");
            if (decrypterName != null)
            {
                // Decrypt the password using the provided class
                ClassLoaderResolver clr = new EnhancementNucleusContextImpl("JDO", properties).getClassLoaderResolver(null);
                try
                {
                    Class<?> decrypterCls = clr.classForName(decrypterName);
                    ConnectionEncryptionProvider decrypter = (ConnectionEncryptionProvider) decrypterCls.newInstance();
                    password = decrypter.decrypt(password);
                }
                catch (Exception e)
                {
                    log.warn("Error invoking decrypter class {}", decrypterName, e);
                }
            }
        }
        return password;
    }


    // -- injected dependencies
    @Override
    public void setDataNucleusProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }


    private String getPropertyAsString(String key) {
        return (String) properties.get(key);
    }


}
