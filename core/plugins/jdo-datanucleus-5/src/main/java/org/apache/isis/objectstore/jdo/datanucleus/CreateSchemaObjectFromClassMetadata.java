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
package org.apache.isis.objectstore.jdo.datanucleus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.isis.commons.internal.base._Strings;

import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPropertiesAware;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.enhancer.EnhancementNucleusContextImpl;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.store.ConnectionEncryptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation note: the methods in this class are <tt>protected</tt> to allow for easy subclassing.
 */
public class CreateSchemaObjectFromClassMetadata implements MetaDataListener, DataNucleusPropertiesAware {

    private static final Logger LOG = LoggerFactory.getLogger(CreateSchemaObjectFromClassMetadata.class);

    // -- persistenceManagerFactory, properties

    private Map<String, String> properties;
    protected Map<String, String> getProperties() {
        return properties;
    }


    // -- loaded (API)

    @Override
    public void loaded(final AbstractClassMetaData cmd) {

        final String schemaName = cmd.getSchema();
        if(_Strings.isNullOrEmpty(schemaName)) {
            return;
        }

        Connection connection = null;
        Statement statement = null;

        final String driverName = properties.get("javax.jdo.option.ConnectionDriverName");
        final String url = properties.get("javax.jdo.option.ConnectionURL");
        final String userName = properties.get("javax.jdo.option.ConnectionUserName");
        final String password = getConnectionPassword();

        if(_Strings.isNullOrEmpty(driverName) || _Strings.isNullOrEmpty(url)) {
            LOG.warn("Unable to create schema due to missing configuration javax.jdo.option.Connection*");
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
            LOG.warn("Unable to create schema", e);

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
        return String.format("CREATE SCHEMA \"%s\"", schemaName);
    }

    /**
     * Determine the name of the schema.
     */
    protected String schemaNameFor(final AbstractClassMetaData cmd) {
        String schemaName = cmd.getSchema();

        // DN uses different casing for identifiers.
        //
        // http://www.datanucleus.org/products/accessplatform_3_2/jdo/orm/datastore_identifiers.html
        // http://www.datanucleus.org/products/accessplatform_4_0/jdo/orm/datastore_identifiers.html
        //
        // the following attempts to accommodate heuristically for the "out-of-the-box" behaviour for three common
        // db vendors without requiring lots of complex configuration of DataNucleus
        //

        String url = getProperties().get("javax.jdo.option.ConnectionURL");

        if(url.contains("postgres")) {
            // in DN 4.0, was forcing lower case:
            // schemaName = schemaName.toLowerCase(Locale.ROOT);

            // in DN 4.1, am guessing that may be ok to leave unchaged (quoted identifiers?)
        }
        if(url.contains("hsqldb")) {
            // in DN 4.0, was forcing upper case:
            // schemaName = schemaName.toUpperCase(Locale.ROOT);

            // in DN 4.1, seems to be ok to leave as unchanged (is quoted identifiers what makes this work?)
        }
        if(url.contains("sqlserver")) {
            // unchanged
        }
        return schemaName;
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
        String password = properties.get("javax.jdo.option.ConnectionPassword");
        if (password != null)
        {
            String decrypterName = properties.get("datanucleus.ConnectionPasswordDecrypter");
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
                    LOG.warn("Error invoking decrypter class {}", decrypterName, e);
                }
            }
        }
        return password;
    }


    // -- injected dependencies
    @Override
    public void setDataNucleusProperties(final Map<String, String> properties) {
        this.properties = properties;
    }



}
