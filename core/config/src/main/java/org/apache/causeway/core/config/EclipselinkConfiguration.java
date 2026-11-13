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
package org.apache.causeway.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Selected configuration for Eclipselink, to provide IDE support.
 *
 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm">Eclipselink Config Property docs</a>
 */
@ConfigurationProperties("eclipselink")
@Validated
public record EclipselinkConfiguration(
    /**
     * Options are
     * <ul>
     * <li>true: Weave the entity classes dynamically.</li>
     * <li>false: Do not weave the entity classes.</li>
     * <li>static: Weave the entity classes statically.</li>
     * </ul>
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#weaving">eclipse.weaving</a>
     */
    @DefaultValue("false")
    String weaving,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#delayonstartup">eclipselink.deploy-on-startup</a>
     */
    @DefaultValue("false")
    boolean deployOnStartup,
    @DefaultValue
    Descriptor descriptor,
    @DefaultValue
    Session session,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#session-event-listener">eclipselink.session-event-listener</a>
     */
    @CausewayConfiguration.AssignableFrom("org.eclipse.persistence.sessions.SessionEventListener")
    Class<?> sessionEventListener,
    @DefaultValue
    Logging logging,
    @DefaultValue
    Cache cache,
    @DefaultValue
    FlushClear flushClear,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#createddljdbcfilename">eclipselink.create-ddl-jdbc-file-name</a>
     */
    String createDdlJdbcFileName,

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABFEBCG">eclipselink.drop-ddl-jdbc-file-name</a>
     */
    String dropDdlJdbcFileName,
    @DefaultValue
    Ddl ddl,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABHEJJI">eclipselink.ddl-generation</a>
     */
    @CausewayConfiguration.OneOf(value={"create-tables", "create-or-extend-tables", "drop-and-create-tables", "none"})
    @DefaultValue("none")
    String ddlGeneration,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CCHCJCDJ">eclipselink.exception-handler</a>
     */
    @CausewayConfiguration.AssignableFrom("org.eclipse.persistence.exceptions.ExceptionHandler")
    Class<?> exceptionHandler,
    /**
     * The "<code>eclipselink.profiler</code>" property configures the type of
     * profiler used to capture runtime statistics.
     * <p>
     * <b>Allowed Values:</b>
     * <ul>
     * <li>"<code>NoProfiler</code>" (DEFAULT)
     * <li>"<code>PerformanceMonitor</code>" - use {@link org.eclipse.persistence.tools.profiler.PerformanceMonitor}
     * <li>"<code>PerformanceProfiler</code>" - use {@link org.eclipse.persistence.tools.profiler.PerformanceProfiler}
     * <li>"<code>QueryMonitor</code>" - use {@link org.eclipse.persistence.tools.profiler.QueryMonitor}
     * <li>"<code>DMSProfiler</code>" - use {@link org.eclipse.persistence.tools.profiler.oracle.DMSPerformanceProfiler}
     * <li>the fully qualified name for a class that implements {@link org.eclipse.persistence.sessions.SessionProfiler} interface
     * </ul>
     *
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#profiler">eclipselink.profiler</a>
     */
    @DefaultValue("NoProfiler")
    String profiler,
    @DefaultValue
    Jdbc jdbc,
    @DefaultValue
    Concurrency concurrency,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CACEEGCC">eclipselink.id-validation</a>
     */
    @DefaultValue("None")
    IdValidation idValidation,
    @DefaultValue
    Jpa jpa,
    @DefaultValue
    Jpql jpql,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDHBGIE">eclipselink.tenant-id</a>
     */
    String tenantId,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#validate-existence">eclipselink.validate-existence</a>
     */
    @DefaultValue("false")
    boolean validateExistence,
    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#validation-only">eclipselink.validation-only</a>
     */
    @DefaultValue("false")
    boolean validationOnly) {

    @ConfigurationProperties("eclipselink.weaving")
    @Validated
    public record Weaving(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDCEEFC">eclipse.weaving.changetracking</a>
         */
        @DefaultValue("true")
        boolean changetracking,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABGGCGC">eclipse.weaving.eager</a>
         */
        @DefaultValue("false")
        boolean eager,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDDIFGE">eclipse.weaving.fetchgroups</a>
         */
        @DefaultValue("true")
        boolean fetchgroups,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABJICDJ">eclipse.weaving.internal</a>
         */
        @DefaultValue("true")
        boolean internal,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABDBIFE">eclipse.weaving.lazy</a>
         */
        @DefaultValue("true")
        boolean lazy
        ) {
    }

    public record Descriptor(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CCHIEAIA">eclipselink.descriptor.customizer</a>
         */
        @CausewayConfiguration.AssignableFrom("org.eclipse.persistence.descriptors.ClassDescriptor")
        Class<?> customizer) {
    }

    public record Session(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#session.customizer">eclipselink.session.customizer</a>
         */
        @CausewayConfiguration.AssignableFrom("org.eclipse.persistence.config.SessionCustomizer")
        Class<?> customizer,
        @DefaultValue
        Include include) {

        public record Include(
            @DefaultValue
            Descriptor descriptor
            ) {

            public record Descriptor(
                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDFDJJD">eclipselink.session.include.descriptor.queries</a>
                 */
                @DefaultValue("false")
                boolean queries) {
            }
        }
    }

    public record Logging(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDJCBIF">eclipselink.logging.connection</a>
         */
        @DefaultValue("true")
        boolean connection,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIFJBIE">eclipselink.logging.exceptions</a>
         */
        @DefaultValue("true")
        boolean exceptions,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIHGBBC">eclipselink.logging.file</a>
         */
        String file,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDGFDDH">eclipselink.logging.level</a>
         */
        @DefaultValue("INFO")
        LoggingLevel level,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIHGGHH">eclipselink.logging.session</a>
         */
        @DefaultValue("true")
        boolean session,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIFFGGB">eclipselink.logging.thread</a>
         */
        @DefaultValue("true")
        boolean thread,

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIJCEBJ">eclipselink.logging.timestamp</a>
         */
        @DefaultValue("true")
        boolean timestamp) {

        public enum LoggingLevel {
            OFF,
            SEVERE,
            WARNING,
            INFO,
            CONFIG,
            FINE,
            FINER,
            FINEST,
            ALL
        }
    }

    public record Cache(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CCHEEFGA">eclipselink.cache.database-event-listener</a>
         */
        @CausewayConfiguration.AssignableFrom("org.eclipse.persistence.platform.database.events.DatabaseEventListener")
        Class<?> databaseEventListener,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDEJCJJ">eclipselink.cache.shared</a>
         */
        @DefaultValue("true")
        boolean shared,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDIGIEC">eclipselink.cache.size</a>
         */
        @DefaultValue("100") // bytes
        int size,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDGDIEI">eclipselink.cache.type</a>
         */
        @DefaultValue("SoftWeak")
        CacheType type) {

        /**
         * The CacheType enum is used with the Cache annotation for a persistent class.
         * It defines the type of cache (IdentityMap) used for the class. By default the
         * SOFT_WEAK cache type is used.
         *
         * @see org.eclipse.persistence.annotations.Cache
         * @author Guy Pelletier
         * @since Oracle TopLink 11.1.1.0.0
         */
        @SuppressWarnings("javadoc")
        public enum CacheType {
            /**
             * Provides full caching and guaranteed identity. Caches all objects
             * and does not remove them.
             * WARNING: This method may be memory intensive when many objects are
             * read.  If used on a large data set it will eventually causes an out of memory error.
             */
            Full,

            /**
             * Similar to the FULL identity map except that the map holds the
             * objects using weak references. This method allows full garbage
             * collection and guaranteed identity.  It will only hold objects
             * that are referenced by the application so may not provide a large caching benefit.
             */
            Weak,

            /**
             * Similar to the FULL identity map except that the map holds the
             * objects using soft references. This method allows full garbage
             * collection when memory is low and provides full caching and guaranteed identity.
             */
            Soft,

            /**
             * Similar to the WEAK identity map except that it maintains a
             * most-frequently-used sub-cache. The size of the sub-cache is
             * proportional to the size of the identity map as specified by
             * {@literal @}Cache size attribute. The sub-cache
             * uses soft references to ensure that these objects are
             * garbage-collected only if the system is low on memory.
             */
            SoftWeak,

            /**
             * Identical to the soft cache weak (SOFT_WEAK) identity map except
             * that it uses hard references in the sub-cache. Use this identity
             * map if soft references do not behave properly on your platform.
             */
            HardWeak,

            /**
             * WARNING: Does not preserve object identity and does not cache
             * objects.  This cache type is not recommend and should normally not be used.
             * This cache type should not be used to disable caching, to properly disable
             * caching set the @Cache isolation attribute to ISOLATED.
             */
            NONE
        }
    }

    public record FlushClear(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CDEJGBEI">eclipselink.flush-clear.cache</a>
         */
        @DefaultValue("DropInvalidate")
        FlushClearCache cache) {

        /**
         * FlushClearCache persistence property
         * defines modes of cache handling after em.flush call followed by em.clear call.
         * This property could be specified while creating either EntityManagerFactory
         * (createEntityManagerFactory or persistence.xml)
         * or EntityManager (createEntityManager); the latter overrides the former.
         *
         * <p>JPA persistence property Usage:
         *
         * <p><code>properties.add(PersistenceUnitProperties.FLUSH_CLEAR_CACHE, FlushClearCache.Drop);</code>
         *
         * <p>Values are case-insensitive.
         * "" could be used instead of default value FlushClearCache.DEFAULT.
         */
        public enum FlushClearCache {

            /**
             * Call to clear method causes to drop from EntityManager cache only the objects that haven't been flushed.
             * This is the most accurate mode: shared cache is perfect after commit;
             * but the least memory effective: smbd repeatedly using flush followed by clear
             * may eventually run out of memory in a huge transaction.
             */
            Merge,

            /**
             * Call to clear method causes to drop the whole EntityManager cache.
             * This is the fasteset and using the least memory mode -
             * but after commit the shared cache potentially contains stale data.
             */
            Drop,

            /** Call to clear method causes to drops the whole EntityManager cache,
             * on commit the classes that have at least one object updated or deleted
             * are invalidated in the shared cache.
             * This is a compromise mode: potentially a bit slower than drop,
             * but has virtually the same memory efficiency.
             * After commit all potentially stale data is invalidated in the shared cache.
             */
            DropInvalidate,
        }
    }

    public record Ddl(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEHBIEJG">eclipselink.ddl.table-creation-suffix</a>
         */
        String tableCreationSuffix) {
    }

    @ConfigurationProperties("eclipselink.ddl-generation")
    @Validated
    public record DdlGeneration(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABCDHBB">eclipselink.ddl-generation.output-mode</a>
         */
        @CausewayConfiguration.OneOf(value={"both", "database", "sql-script"})
        @DefaultValue("database")
        String outputMode) {
    }

    public record Jdbc(
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CIHHJBFB">eclipselink.jdbc.allow-native-sql-queries</a>
         */
        @DefaultValue("true")
        boolean allowNativeSqlQueries,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CIHIAGAF">eclipselink.jdbc.batch-writing</a>
         */
        @CausewayConfiguration.OneOf(value={"jdbc", "buffered", "oracle-jdbc", "none"})
        @DefaultValue("none")
        String batchWriting,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDHAFAA">eclipselink.jdbc.cache-statements</a>
         */
        @DefaultValue("true")
        boolean cacheStatements,
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm">eclipselink.jdbc.native-sql</a>
         */
        @DefaultValue("true")
        boolean nativeSql
        ) {

        @ConfigurationProperties("eclipselink.jdbc.batch-writing")
        @Validated
        public record BatchWriting(
            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CIHJADHF">eclipse.jdbc.batchwriting.size</a>
             */
            int size) {
        }

        @ConfigurationProperties("eclipselink.jdbc.cache-statements")
        @Validated
        public record CacheStatements(
            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CACBICGG">eclipse.jdbc.cache-statements.size</a>
             */
            int size) {
        }
    }

    public record Concurrency(
        @DefaultValue
        Manager manager) {

        public record Manager(
            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.waittime">eclipselink.concurrency.manager.waittime</a>
             */
            @DefaultValue("0") // ms
            long waittime,
            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.maxsleeptime">eclipselink.concurrency.manager.maxsleeptime</a>
             */
            @DefaultValue("40000") // ms
            long maxsleeptime,
            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.maxfrequencytodumptinymessage">eclipselink.concurrency.manager.maxfrequencytodumptinymessage</a>
             */
            @DefaultValue("40000") // ms
            long maxfrequencytodumptinymessage,
            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.maxfrequencytodumptinymessage">eclipselink.concurrency.manager.maxfrequencytodumpmassivemessage</a>
             */
            @DefaultValue("60000") // ms
            long maxfrequencytodumpmassivemessage,
            @DefaultValue
            Allow allow) {

            public record Allow(
                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.allow.interruptedexception">eclipselink.concurrency.manager.allow.interruptedexception</a>
                 */
                @DefaultValue("true")
                boolean interruptedexception,
                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.allow.concurrencyexception">eclipselink.concurrency.manager.allow.concurrencyexception</a>
                 */
                @DefaultValue("true")
                boolean concurrencyexception,
                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.allow.readlockstacktrace">eclipselink.concurrency.manager.allow.readlockstacktrace</a>
                 */
                @DefaultValue("true")
                boolean readlockstacktrace) {
            }
        }
    }

    public enum IdValidation {
        /**
         * Null, 0 and negative values are invalid for IDs extending Number and primitive int and long IDs.
         */
        Negative,
        /**
         * EclipseLink performs no ID validation.
         */
        None,
        /**
         * Null is invalid All other values are valid.
         */
        Null,
        /**
         * Null, 0 and negative values are invalid for primitive int and long IDs.
         */
        Zero
    }

    public record Jpa(
        /**
         * Specify JPA processing to uppercase all column name definitions (simulating case insensitivity).
         *
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDJGCBD">eclipselink.jpa.uppercase-column-names</a>
         */
        @DefaultValue("false")
        boolean upperCaseColumnNames) {
    }

    public record Jpql(
        @CausewayConfiguration.OneOf(value={"EclipseLink", "JPA 1.0", "JPA 2.0", "JPA 2.1", "None"})
        @DefaultValue("EclipseLink")
        String validation) {
    }

}
