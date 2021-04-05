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

import org.eclipse.persistence.config.ProfilerType;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.tools.profiler.PerformanceMonitor;
import org.eclipse.persistence.tools.profiler.PerformanceProfiler;
import org.eclipse.persistence.tools.profiler.QueryMonitor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Selected configuration for Eclipselink, to provide IDE support.
 *
 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm">Eclipselink Config Property docs</a>
 */
@ConfigurationProperties("eclipselink")
@Data
@Validated
public class EclipselinkConfiguration {

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#weaving">eclipse.weaving</a> docs
     */
    private boolean weaving = false;

    @ConfigurationProperties("eclipselink.weaving")
    @Validated
    @Data
    public static class Weaving {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDCEEFC">eclipse.weaving.changetracking</a> docs
         */
        private boolean changetracking = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABGGCGC">eclipse.weaving.eager</a> docs
         */
        private boolean eager = false;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDDIFGE">eclipse.weaving.fetchgroups</a> docs
         */
        private boolean fetchgroups = false;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABJICDJ">eclipse.weaving.internal</a> docs
         */
        private boolean internal = false;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABDBIFE">eclipse.weaving.lazy</a> docs
         */
        private boolean lazy = false;
    }

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#delayonstartup">eclipselink.deploy-on-startup</a> docs
     */
    private boolean deployOnStartup = false;

    private final Descriptor descriptor = new Descriptor();
    @Data
    public static class Descriptor {
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CCHIEAIA">eclipselink.descriptor.customizer</a> docs
         */
        @IsisConfiguration.AssignableFrom("org.eclipse.persistence.descriptors.ClassDescriptor")
        Class<?> customizer = null;
    }

    private final Session session = new Session();
    @Data
    public static class Session {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#session.customizer">eclipselink.session.customizer</a> docs
         */
        @IsisConfiguration.AssignableFrom("org.eclipse.persistence.config.SessionCustomizer")
        Class<?> customizer = null;

        private final Include include = new Include();
        @Data
        public static class Include {
            private final Descriptor descriptor = new Descriptor();
            @Data
            public static class Descriptor {

                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDFDJJD">eclipselink.session.include.descriptor.queries</a> docs
                 */
                private boolean queries = false;
            }
        }
    }

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#session-event-listener">eclipselink.session-event-listener</a> docs
     */
    @IsisConfiguration.AssignableFrom("org.eclipse.persistence.sessions.SessionEventListener")
    Class<?> sessionEventListener = null;


    private final Logging logging = new Logging();
    @Data
    public static class Logging {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDJCBIF">eclipselink.logging.connection</a> docs
         */
        private boolean connection = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIFJBIE">eclipselink.logging.exceptions</a> docs
         */
        private boolean exceptions = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIHGBBC">eclipselink.logging.file</a> docs
         */
        private String file = null;

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
        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDGFDDH">eclipselink.logging.level</a> docs
         */
        private LoggingLevel level = LoggingLevel.INFO;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIHGGHH">eclipselink.logging.session</a>
         */
        private boolean session = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIFFGGB">eclipselink.logging.thread</a>
         */
        private boolean thread = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEIJCEBJ">eclipselink.logging.timestamp</a>
         */
        private boolean timestamp = true;
    }

    private final Cache cache = new Cache();
    @Data
    public static class Cache {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CCHEEFGA">eclipselink.cache.database-event-listener</a> docs
         */
        @IsisConfiguration.AssignableFrom("org.eclipse.persistence.platform.database.events.DatabaseEventListener")
        Class<?> databaseEventListener;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDEJCJJ">eclipselink.cache.shared</a> docs
         */
        boolean shared = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDIGIEC">eclipselink.cache.size</a> docs
         */
        int size = 100; // bytes


        /**
         * The CacheType enum is used with the Cache annotation for a persistent class.
         * It defines the type of cache (IdentityMap) used for the class. By default the
         * SOFT_WEAK cache type is used.
         *
         * @see org.eclipse.persistence.annotations.Cache
         * @author Guy Pelletier
         * @since Oracle TopLink 11.1.1.0.0
         */
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

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDGDIEI">eclipselink.cache.type</a> docs
         */
        CacheType type = CacheType.SoftWeak;
    }

    private final FlushClear flushClear = new FlushClear();
    @Data
    public static class FlushClear {


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

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CDEJGBEI">eclipselink.flush-clear.cache</a> docs
         */
        FlushClearCache cache = FlushClearCache.DropInvalidate;
    }


    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#createddljdbcfilename">eclipselink.create-ddl-jdbc-file-name</a> docs
     */
    String createDdlJdbcFileName = null;

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABFEBCG">eclipselink.drop-ddl-jdbc-file-name</a> docs
     */
    String dropDdlJdbcFileName = null;

    private final Ddl ddl = new Ddl();
    @Data
    public static class Ddl {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BEHBIEJG">eclipselink.ddl.table-creation-suffix</a> docs
         */
        String tableCreationSuffix = null;
    }

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABHEJJI">eclipselink.ddl-generation</a> docs
     */
    @IsisConfiguration.OneOf(value={"create-tables", "create-or-extend-tables", "drop-and-create-tables", "none"})
    String ddlGeneration = "none";

    @ConfigurationProperties("eclipselink.ddl-generation")
    @Validated
    @Data
    public static class DdlGeneration {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABCDHBB">eclipselink.ddl-generation.output-mode</a> docs
         */
        @IsisConfiguration.OneOf(value={"both", "database", "sql-script"})
        String outputMode = "database";

    }


    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CCHCJCDJ">eclipselink.exception-handler</a> docs
     */
    @IsisConfiguration.AssignableFrom("org.eclipse.persistence.exceptions.ExceptionHandler")
    Class<?> exceptionHandler = null;

    /**
     * The "<code>eclipselink.profiler</code>" property configures the type of
     * profiler used to capture runtime statistics.
     * <p>
     * <b>Allowed Values:</b>
     * <ul>
     * <li>"<code>NoProfiler</code>" (DEFAULT)
     * <li>"<code>PerformanceMonitor</code>" - use {@link PerformanceMonitor}
     * <li>"<code>PerformanceProfiler</code>" - use {@link PerformanceProfiler}
     * <li>"<code>QueryMonitor</code>" - use {@link QueryMonitor}
     * <li>"<code>DMSProfiler</code>" - use {@link org.eclipse.persistence.tools.profiler.oracle.DMSPerformanceProfiler}
     * <li>the fully qualified name for a class that implements {@link SessionProfiler} interface
     * </ul>
     *
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#profiler">eclipselink.profiler</a> docs
     */
    private String profiler = "NoProfiler";

    private final Jdbc jdbc = new Jdbc();
    @Data
    public static class Jdbc {

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CIHHJBFB">eclipselink.jdbc.allow-native-sql-queries</a> docs
         */
        private boolean allowNativeSqlQueries = true;

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CIHIAGAF">eclipselink.jdbc.batch-writing</a> docs
         */
        @IsisConfiguration.OneOf(value={"jdbc", "buffered", "oracle-jdbc", "none"})
        String batchWriting = "none";

        @ConfigurationProperties("eclipselink.jdbc.batch-writing")
        @Validated
        @Data
        public static class BatchWriting {

            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CIHJADHF">eclipse.jdbc.batchwriting.size</a> docs
             */
            private int size;
        }

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDHAFAA">eclipselink.jdbc.cache-statements</a> docs
         */
        private boolean cacheStatements = true;

        @ConfigurationProperties("eclipselink.jdbc.cache-statements")
        @Validated
        @Data
        public static class CacheStatements {

            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CACBICGG">eclipse.jdbc.cache-statements.size</a> docs
             */
            private int size;
        }

        /**
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm">eclipselink.jdbc.native-sql</a> docs
         */
        private boolean nativeSql = true;

    }

    private final Concurrency concurrency = new Concurrency();
    @Data
    public static class Concurrency {

        private final Manager manager = new Manager();
        @Data
        public static class Manager {

            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.waittime">eclipselink.concurrency.manager.waittime</a> docs
             */
            private long waittime = 0; // ms

            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.maxsleeptime">eclipselink.concurrency.manager.maxsleeptime</a> docs
             */
            private long maxsleeptime = 40000; // ms

            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.maxfrequencytodumptinymessage">eclipselink.concurrency.manager.maxfrequencytodumptinymessage</a> docs
             */
            private long maxfrequencytodumptinymessage = 40000; // ms

            /**
             * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.maxfrequencytodumptinymessage">eclipselink.concurrency.manager.maxfrequencytodumpmassivemessage</a> docs
             */
            private long maxfrequencytodumpmassivemessage = 60000; // ms

            private final Allow allow = new Allow();
            @Data
            public static class Allow {

                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.allow.interruptedexception">eclipselink.concurrency.manager.allow.interruptedexception</a> docs
                 */
                private boolean interruptedexception = true;

                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.allow.concurrencyexception">eclipselink.concurrency.manager.allow.concurrencyexception</a> docs
                 */
                private boolean concurrencyexception = true;

                /**
                 * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#concurrency.manager.allow.readlockstacktrace">eclipselink.concurrency.manager.allow.readlockstacktrace</a> docs
                 */
                private boolean readlockstacktrace = true;
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

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CACEEGCC">eclipselink.id-validation</a> docs
     */
    private IdValidation idValidation = IdValidation.None;

    private final Jpa jpa = new Jpa();
    @Data
    public static class Jpa {
        /**
         * Specify JPA processing to uppercase all column name definitions (simulating case insensitivity).
         *
         * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDJGCBD">eclipselink.jpa.uppercase-column-names</a> docs
         */
        private boolean upperCaseColumnNames = false;
    }

    private final Jpql jpql = new Jpql();
    @Data
    public static class Jpql {

        @IsisConfiguration.OneOf(value={"EclipseLink", "JPA 1.0", "JPA 2.0", "JPA 2.1", "None"})
        private String validation = "EclipseLink";
    }

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#CHDHBGIE">eclipselink.tenant-id</a> docs
     */
    private String tenantId;

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#validate-existence">eclipselink.validate-existence</a> docs
     */
    private boolean validateExistence = false;

    /**
     * @see <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#validation-only">eclipselink.validation-only</a> docs
     */
    private boolean validationOnly = false;

}
