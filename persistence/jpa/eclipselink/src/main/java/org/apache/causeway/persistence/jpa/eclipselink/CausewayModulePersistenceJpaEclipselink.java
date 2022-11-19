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
package org.apache.causeway.persistence.jpa.eclipselink;

import java.sql.SQLException;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.persistence.jpa.eclipselink.config.ElSettings;
import org.apache.causeway.persistence.jpa.integration.CausewayModulePersistenceJpaIntegration;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * EclipseLink integration.
 * <p>
 * Sets up EclipseLink as the implementation provider for Spring Data JPA.
 *
 * @implNote does not (yet) support weaving, explicitly disables it
 * @see <a href="https://www.baeldung.com/spring-eclipselink">baeldung.com</a>
 *
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    CausewayModulePersistenceJpaIntegration.class
})
@EnableConfigurationProperties(ElSettings.class)
@Log4j2
public class CausewayModulePersistenceJpaEclipselink extends JpaBaseConfiguration {

    @Inject private ElSettings elSettings;

    protected CausewayModulePersistenceJpaEclipselink(
            CausewayConfiguration causewayConfiguration,
            DataSource dataSource,
            JpaProperties properties,
            ObjectProvider<JtaTransactionManager> jtaTransactionManager) {

        super(
                autoCreateSchemas(dataSource, causewayConfiguration),
                addAdditionalOrmFiles(properties, causewayConfiguration),
                jtaTransactionManager);
    }

    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter() {

            private final EclipseLinkJpaDialect jpaDialect = eclipselinkJpaDialect();

            @Override
            public EclipseLinkJpaDialect getJpaDialect() {
                return jpaDialect;
            }
        };
    }

    @Override
    protected Map<String, Object> getVendorProperties() {
        return elSettings.asMap();
    }

    /**
     * integrates with settings from causeway.persistence.schema.*
     */
    @SneakyThrows
    protected static DataSource autoCreateSchemas(
            final DataSource dataSource,
            final CausewayConfiguration causewayConfiguration) {

        val persistenceSchemaConf = causewayConfiguration.getPersistence().getSchema();

        if(!persistenceSchemaConf.getAutoCreateSchemas().isEmpty()) {

            log.info("about to create db schema(s) {}", persistenceSchemaConf.getAutoCreateSchemas());

            try(val con = dataSource.getConnection()){

                val s = con.createStatement();

                for(val schema : persistenceSchemaConf.getAutoCreateSchemas()) {
                    s.execute(String.format(persistenceSchemaConf.getCreateSchemaSqlTemplate(), schema));
                }

            }
        }

        return dataSource;
    }

    /**
     * integrates with settings from causeway.persistence.schema.*
     */
    protected static JpaProperties addAdditionalOrmFiles(
            JpaProperties properties,
            CausewayConfiguration causewayConfiguration) {

        val persistenceSchemaConf = causewayConfiguration.getPersistence().getSchema();

        persistenceSchemaConf.getAdditionalOrmFiles()
        .forEach(schema->properties.getMappingResources()
                .add(String.format("META-INF/orm-%s.xml", schema)));

        if(!properties.getMappingResources().isEmpty()) {
            log.info("using mapping-resources {}", properties.getMappingResources());
        }

        return properties;
    }

    // --

    @SuppressWarnings("serial")
    private EclipseLinkJpaDialect eclipselinkJpaDialect() {

        val jdbcExceptionTranslator = newJdbcExceptionTranslator(getDataSource());

        return new EclipseLinkJpaDialect() {

            @Override
            public DataAccessException translateExceptionIfPossible(RuntimeException ex) {

                if(ex instanceof DataAccessException) {
                    return (DataAccessException)ex; // has already been translated to Spring's hierarchy
                }

                // if its eg. a DatabaseException, it might wrap a java.sql.SQLException
                if(getJdbcExceptionTranslator() != null
                        && ex.getCause() instanceof SQLException) {

                    //converts SQL exceptions to Spring's hierarchy
                    val translatedEx = getJdbcExceptionTranslator()
                            .translate(
                                    "JPA operation: " + ex.getMessage(),
                                    extractSqlStringFromException(ex),
                                    (SQLException) ex.getCause());

                    if(translatedEx!=null) {
                        return translatedEx;
                    }

                }

                // (null-able) converts javax.persistence exceptions to Spring's hierarchy
                val translatedEx = super.translateExceptionIfPossible(ex);

                if((translatedEx==null
                        // JpaSystemException is just a generic fallback, try to be smarter
                        || JpaSystemException.class.equals(translatedEx.getClass()))
                        && getJdbcExceptionTranslator() != null) {

                    val translatedSqlEx = _Exceptions.streamCausalChain(ex)
                    .filter(nextEx->nextEx instanceof SQLException)
                    .map(SQLException.class::cast)
                    //converts SQL exceptions to Spring's hierarchy
                    .map(nextEx->getJdbcExceptionTranslator()
                            .translate(
                                    "JPA operation: " + nextEx.getMessage(),
                                    extractSqlStringFromException(nextEx),
                                    nextEx))
                    .filter(_NullSafe::isPresent) //ISIS-3282
                    .findFirst()
                    .orElse(null);

                    if(translatedSqlEx!=null) {
                        return translatedSqlEx;
                    }

                }

                // (null-able)
                return translatedEx;

            }

            // -- HELPER

            /*
             * Template method for extracting a SQL String from the given exception.
             * <p>Default implementation always returns {@code null}. Can be overridden in
             * subclasses to extract SQL Strings for vendor-specific exception classes.
             * @param ex the JDOException, containing a SQLException
             * @return the SQL String, or {@code null} if none found
             */
            private String extractSqlStringFromException(Throwable ex) {
                return null;
            }

            private SQLExceptionTranslator getJdbcExceptionTranslator() {
                return jdbcExceptionTranslator;
            }


        };
    }

    /**
     * Create an appropriate SQLExceptionTranslator for the given PersistenceManagerFactory.
     * <p>If a DataSource is found, creates a SQLErrorCodeSQLExceptionTranslator for the
     * DataSource; else, falls back to a SQLStateSQLExceptionTranslator.
     * @param connectionFactory the connection factory of the EntityManagerFactory
     * (may be {@code null})
     * @return the SQLExceptionTranslator (never {@code null})
     * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
     * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
     */
    private static SQLExceptionTranslator newJdbcExceptionTranslator(Object connectionFactory) {
        // Check for PersistenceManagerFactory's DataSource.
        if (connectionFactory instanceof DataSource) {
            return new SQLErrorCodeSQLExceptionTranslator((DataSource) connectionFactory);
        }
        else {
            return new SQLStateSQLExceptionTranslator();
        }
    }

}
