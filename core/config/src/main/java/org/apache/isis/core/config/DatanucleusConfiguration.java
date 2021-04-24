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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Selected configuration for Datanucleus, to provide IDE support.
 *
 * <p>
 *     Note that Datanucleus configuration properties must be specified using
 *     camelCase rather than kebab-casing.   This is enforced by explicit
 *     entries in <tt>additional-spring-configuration-metadata.json</tt>.
 * </p>
 *
 * @see <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
 */
@ConfigurationProperties("datanucleus")
@Data
@Validated
public class DatanucleusConfiguration {

    /**
     * Whether to run the "persistence-by-reachability" algorithm at commit
     * time.
     *
     * <p>
     *     This means that objects that were reachable at a call to
     *     makePersistent() but that are no longer persistent will be
     *     removed from persistence. For performance improvements,
     *     consider turning this off.
     * </p>
     *
     * <p>
     *     For more details, see
     *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>.
     * </p>
     */
    private boolean persistenceByReachabilityAtCommit = true;

    /**
     * Whether DataNucleus will try to manage bidirectional relations,
     * correcting the input objects so that all relations are consistent.
     *
     * <p>
     * This process runs when flush()/commit() is called. You can set it to
     * false if you always set both sides of a relation when persisting/updating.
     * </p>
     *
     * <p>
     *     For more details, see
     *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>.
     * </p>
     */
    private boolean manageRelationships = true;

    private final Identifier identifier = new Identifier();
    @Data
    public static class Identifier {

        public enum Case {
            UPPERCASE,
            LowerCase,
            MixedCase
        }

        // declared only in additional-spring-configuratoin-metadata.json to rename to 'case'
        // private Case identifierCase = Case.UPPERCASE;
    }

    private final Cache cache = new Cache();
    @Data
    public static class Cache {
        private final Level2 level2 = new Level2();
        @Data
        public static class Level2 {
            /**
             * Name of the type of Level 2 Cache to use.
             *
             * <p>
             * Can be used to interface with external caching products.
             * Use "none" to turn off L2 caching; other values include "soft", "weak", "javax.cache".
             * </p>
             *
             * <p>
             *     For more details, see
             *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
             *     and the
             *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#cache_level2">DataNucleus Cache docs</a>.
             * </p>
             */
            @NotNull @NotEmpty
            private String type = "soft";

            public enum CacheMode {
                NONE,
                ALL,
                ENABLE_SELECTIVE,
                DISABLE_SELECTIVE,
                UNSPECIFIED
            }

            /**
             * The mode of operation of the L2 cache, deciding which
             * entities are cached.
             *
             * <p>
             * The default (UNSPECIFIED) is the same as DISABLE_SELECTIVE.
             * </p>
             *
             * <p>
             *     For more details, see
             *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
             *     and the
             *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#cache_level2">DataNucleus Cache docs</a>.
             * </p>
             */
            private CacheMode mode = CacheMode.UNSPECIFIED;
        }
    }

    private final Schema schema = new Schema();
    @Data
    public static class Schema {

        /**
         * Whether to automatically generate any schema, tables, columns,
         * constraints that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean autoCreateAll = false;

        /**
         * Whether to automatically generate any database (catalog/schema) that
         * doesn’t exist. This depends very much on whether the datastore in
         * question supports this operation.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean autoCreateDatabase = false;

        /**
         * Whether to automatically generate any tables that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean autoCreateTables = false;

        /**
         * Whether to automatically generate any columns that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean autoCreateColumns = false;

        /**
         * Whether to automatically generate any constraints that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean autoCreateConstraints = false;

        /**
         * Whether to validate tables against the persistence definition.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean validateTables = false;

        /**
         * Whether to validate columns against the persistence definition.
         * This refers to the column detail structure and NOT to whether the
         * column exists or not.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean validateColumns = false;

        /**
         * Whether to validate table constraints against the persistence definition.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean validateConstraints = false;

        /**
         * Alias for defining <code>datanucleus.schema.validateTables</code>,
         * <code>datanucleus.schema.validateColumns</code> and
         * <code>datanucleus.schema.validateConstraints</code> as true.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_5_2/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean validateAll = false;
    }
}
