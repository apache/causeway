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
import java.util.Locale;
import java.util.Map;
import javax.jdo.PersistenceManagerFactory;
import com.google.common.base.Strings;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.MetaDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSchemaObjectFromClassMetadata implements MetaDataListener, PersistenceManagerFactoryAware, DataNucleusPropertiesAware {

    private static final Logger LOG = LoggerFactory.getLogger(DataNucleusPersistenceMechanismInstaller.class);

    // unused
    protected PersistenceManagerFactory persistenceManagerFactory;
    protected Map<String, String> properties;

    protected String driverName;
    protected String url;

    @Override
    public void loaded(final AbstractClassMetaData cmd) {

        final String schemaName = cmd.getSchema();
        if(Strings.isNullOrEmpty(schemaName)) {
            return;
        }

        driverName = properties.get("javax.jdo.option.ConnectionDriverName");
        url = properties.get("javax.jdo.option.ConnectionURL");
        final String userName = properties.get("javax.jdo.option.ConnectionUserName");
        final String password = properties.get("javax.jdo.option.ConnectionPassword");

        Connection connection = null;
        Statement statement = null;
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

    protected void closeSafely(final AutoCloseable connection) {
        if(connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    //region > skip

    /**
     * Whether to skip creating this schema.
     */
    protected boolean skip(final AbstractClassMetaData cmd, final Statement statement) throws SQLException {
        final String schemaName = cmd.getSchema();
        if(Strings.isNullOrEmpty(schemaName)) {
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
    //endregion

    //region > exec

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
    //endregion

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

        if(url.contains("postgres")) {
            schemaName = schemaName.toLowerCase(Locale.ROOT);
        }
        if(url.contains("hsqldb")) {
            schemaName = schemaName.toUpperCase(Locale.ROOT);
        }
        if(url.contains("sqlserver")) {
            // unchanged
        }
        return schemaName;
    }


    // //////////////////////////////////////

    public void setPersistenceManagerFactory(final PersistenceManagerFactory persistenceManagerFactory) {
        this.persistenceManagerFactory = persistenceManagerFactory;
    }

    @Override
    public void setDataNucleusProperties(final Map<String, String> properties) {
        this.properties = properties;
    }
}
