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
package org.apache.isis.core.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Deprecated // no longer used, but could resurrected as a configuration bean if that makes sense
public class JdoDatanucleus {

    private String classMetadataLoadedListener = "org.apache.isis.persistence.jdo.datanucleus.config.CreateSchemaObjectFromClassMetadata";
    
    private final Datanucleus datanucleus = new Datanucleus();
    @Data
    public static class Datanucleus {


        /**
         * 	The JNDI name for a connection factory for transactional connections.
         *
         * 	<p>
         * 	    For RBDMS, it must be a JNDI name that points to a javax.sql.DataSource object.
         * 	</p>
         *
         * <p>
         *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
         * </p>
         *
         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
         */
        private String connectionFactoryName;

        /**
         * 	The JNDI name for a connection factory for non-transactional connections.
         *
         * 	<p>
         * 	    For RBDMS, it must be a JNDI name that points to a javax.sql.DataSource object.
         * 	</p>
         *
         * <p>
         *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
         * </p>
         *
         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
         */
        private String connectionFactory2Name;


        /**
         * Name of a class that implements <code>org.datanucleus.store.connection.DecryptionProvider</code>
         * and should only be specified if the password is encrypted in the persistence properties.
         *
         * <p>
         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
         * </p>
         *
         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
         */
        private String connectionPasswordDecrypter;


        /**
         * 	Used when we have specified the persistence-unit name for a PMF/EMF and where we want the
         * 	datastore "tables" for all classes of that persistence-unit loading up into the StoreManager.
         *
         * <p>
         *     Defaults to true, which is the opposite of DataNucleus' own default.
         *     (The reason that DN defaults to false is because some databases are slow so such an
         *     operation would slow down the startup process).
         * </p>
         *
         * <p>
         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
         * </p>
         *
         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
         */
        private boolean persistenceUnitLoadClasses = true;

        public enum TransactionTypeEnum {
            RESOURCE_LOCAL,
            JTA
        }

        /**
         * Type of transaction to use.
         *
         * <p>
         * If running under JavaSE the default is RESOURCE_LOCAL, and if running under JavaEE the default is JTA.
         * </p>
         *
         * <p>
         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
         * </p>
         *
         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
         */
        private Datanucleus.TransactionTypeEnum transactionType;

        private final Datanucleus.Cache cache = new Cache();
        @Data
        public static class Cache {
            private final Cache.Level2 level2 = new Level2();
            @Data
            public static class Level2 {
                /**
                 * Name of the type of Level 2 Cache to use.
                 *
                 * <p>
                 * Can be used to interface with external caching products.
                 * Use "none" to turn off L2 caching.
                 * </p>
                 *
                 * <p>
                 * See also Cache docs for JDO, and for JPA
                 * </p>
                 *
                 * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                 */
                @NotNull @NotEmpty
                private String type = "none";
            }
        }
        private final Datanucleus.Schema schema = new Schema();
        @Data
        public static class Schema {
            /**
             * Whether DN should automatically create the database schema on bootstrapping.
             *
             * <p>
             *     This should be set to <tt>true</tt> when running against an in-memory database, but
             *     set to <tt>false</tt> when running against a persistent database (use something like
             *     flyway instead to manage schema evolution).
             * </p>
             *
             * <p>
             *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
             * </p>
             *
             *
             * @implNote - this config property isn't used by the core framework, but is used by one the flyway extension.
             */
            private boolean autoCreateAll = false;

            /**
             * Previously we defaulted this property to "true", but that could cause the target database
             * to be modified
             *
             * <p>
             *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
             * </p>
             *
             * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
             */
            private boolean autoCreateDatabase = false;

            /**
             * <p>
             *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
             * </p>
             *
             * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
             */
            private boolean validateAll = true;
        }
    }
    
    private final Javax javax = new Javax();
    @Data
    public static class Javax {
        private final Jdo jdo = new Jdo();
        @Data
        public static class Jdo {

            /**
             * <p>
             *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
             * </p>
             *
             * @implNote - changing this property from its default is used to enable the flyway extension (in combination with {@link Datanucleus.Schema#isAutoCreateAll()}
             */
            @NotNull @NotEmpty
            private String persistenceManagerFactoryClass = "org.datanucleus.api.jdo.JDOPersistenceManagerFactory";

            private final Option option = new Option();
            @Data
            public static class Option {
                /**
                 * JDBC driver used by JDO/DataNucleus object store to connect.
                 *
                 * <p>
                 *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                 * </p>
                 *
                 * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete (and is mandatory if using JDO Datanucleus).
                 */
                private String connectionDriverName;
                /**
                 * URL used by JDO/DataNucleus object store to connect.
                 *
                 * <p>
                 *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                 * </p>
                 *
                 * @implNote - some extensions (H2Console, MsqlDbManager) peek at this URL to determine if they should be enabled.  Note that it is also mandatory if using JDO Datanucleus.
                 */
                private String connectionUrl;
                /**
                 * User account used by JDO/DataNucleus object store to connect.
                 *
                 * <p>
                 *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                 * </p>
                 *
                 * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete (and is mandatory if using JDO Datanucleus).
                 */
                private String connectionUserName;
                /**
                 * Password for the user account used by JDO/DataNucleus object store to connect.
                 *
                 * <p>
                 *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                 * </p>
                 *
                 * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete.  It is not necessarily mandatory, some databases accept an empty password.
                 */
                private String connectionPassword;
            }
        }
    }
}