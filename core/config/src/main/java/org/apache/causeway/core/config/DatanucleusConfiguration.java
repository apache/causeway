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
 * @see <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
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
     *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>.
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
     *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>.
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

        // declared only in additional-spring-configuration-metadata.json to rename to 'case'
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
             *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
             *     and the
             *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#cache_level2">DataNucleus Cache docs</a>.
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
             *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
             *     and the
             *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#cache_level2">DataNucleus Cache docs</a>.
             * </p>
             */
            private CacheMode mode = CacheMode.UNSPECIFIED;
        }
    }

    private final Schema schema = new Schema();
    @Data
    public static class Schema {

        /**
         * Whether to automatically (but lazily) generate any schema, tables, columns,
         * constraints that don’t exist.
         *
         * <p>
         *     For integration testing, it's generally better to use {@link GenerateDatabase#setMode(String) datanucleus.schema.generateDatabase.mode},
         *     which will eagerly create all tables on startup.
         * </p>
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         *
         * @see #setAutoCreateColumns(boolean)
         * @see #setAutoCreateConstraints(boolean)
         * @see #setAutoCreateTables(boolean)
         * @see GenerateDatabase#setMode(String)
         */
        private boolean autoCreateAll = false;

        /**
         * Whether to automatically generate any database (catalog/schema) that
         * doesn’t exist. This depends very much on whether the datastore in
         * question supports this operation.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean autoCreateDatabase = false;

        /**
         * Whether to automatically generate any tables that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         *
         * @see #setAutoCreateAll(boolean)
         * @see #setAutoCreateColumns(boolean)
         * @see #setAutoCreateConstraints(boolean)
         * @see GenerateDatabase#setMode(String)
         */
        private boolean autoCreateTables = false;

        /**
         * Whether to automatically generate any columns that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         *
         * @see #setAutoCreateAll(boolean)
         * @see #setAutoCreateConstraints(boolean)
         * @see #setAutoCreateTables(boolean)
         * @see GenerateDatabase#setMode(String)
         */
        private boolean autoCreateColumns = false;

        /**
         * Whether to automatically generate any constraints that don’t exist.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         *
         * @see #setAutoCreateAll(boolean)
         * @see #setAutoCreateColumns(boolean)
         * @see #setAutoCreateTables(boolean)
         * @see GenerateDatabase#setMode(String)
         */
        private boolean autoCreateConstraints = false;

        /**
         * Whether to validate tables against the persistence definition.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
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
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean validateColumns = false;

        /**
         * Whether to validate table constraints against the persistence definition.
         *
         * <p>
         *     For more details, see
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
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
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
         *     and the
         *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
         * </p>
         */
        private boolean validateAll = false;

        private final GenerateDatabase generateDatabase = new GenerateDatabase();
        @Data
        public static class GenerateDatabase {

            /**
             * Whether to eagerly create all tables at startup.
             *
             * <p>
             *     For integration testing, this is generally preferred to using {@link DatanucleusConfiguration.Schema#setAutoCreateAll(boolean) datanucleus.schema.autoCreateAll},
             *     because the <code>autoCreateAll</code> will only create tables lazily, when first persisted to.  While lazily initialization is potentially quicker,
             *     it can cause issues (eg with rollup mapping to super class tables).
             * </p>
             *
             * <p>
             *     Valid values: <code><b>none</b></code>, <code>create</code>, <code>drop-and-create</code>, <code>drop</code>.
             * </p>
             *
             * <p>
             *     For more details, see
             *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#pmf_properties">DataNucleus Config Property docs</a>
             *     and the
             *     <a href="https://www.datanucleus.org/products/accessplatform_6_0/jdo/persistence.html#schema">DataNucleus Schema Guide</a>.
             * </p>
             */
            private String mode = "none";
        }
    }
}

/*
As of 6.0.0-M5, these are all the config properties supported.
I'm pasting them here so that we might choose to surface some via application.properties, ie, define in this class.

Note: As it's not easy (impossible?) to define camelCase in Spring Boot, but because DN does case-insensitive checks, we can just make em lowercase.

datanucleus.allowattachoftransient
datanucleus.allowcallbacks
datanucleus.allowinstancecallbackannotations
datanucleus.allowlistenerupdateafterinit
datanucleus.attachsamedatastore
datanucleus.autostartclassnames
datanucleus.autostartmechanism
datanucleus.autostartmechanismmode
datanucleus.autostartmechanismxmlfile
datanucleus.autostartmetadatafiles
datanucleus.cache.collections
datanucleus.cache.collections.lazy
datanucleus.cache.level1.type
datanucleus.cache.level2.batchsize
datanucleus.cache.level2.cacheembedded
datanucleus.cache.level2.cachename
datanucleus.cache.level2.clearatclose
datanucleus.cache.level2.expirymillis
datanucleus.cache.level2.loadfields
datanucleus.cache.level2.maxsize
datanucleus.cache.level2.mode
datanucleus.cache.level2.readthrough
datanucleus.cache.level2.retrievemode
datanucleus.cache.level2.statisticsenabled
datanucleus.cache.level2.storebyvalue
datanucleus.cache.level2.storemode
datanucleus.cache.level2.type
datanucleus.cache.level2.updatemode
datanucleus.cache.level2.writethrough
datanucleus.cache.querycompilation.type
datanucleus.cache.querycompilationdatastore.type
datanucleus.cache.queryresults.cachename
datanucleus.cache.queryresults.clearatclose
datanucleus.cache.queryresults.expirymillis
datanucleus.cache.queryresults.maxsize
datanucleus.cache.queryresults.type
datanucleus.classloaderresolvername
datanucleus.connection.nontx.releaseafteruse
datanucleus.connection.resourcetype
datanucleus.connection.singleconnectionperexecutioncontext
datanucleus.connection2.resourcetype
datanucleus.connectiondrivername
datanucleus.connectionfactory
datanucleus.connectionfactory2
datanucleus.connectionfactory2name
datanucleus.connectionfactoryname
datanucleus.connectionpassword
datanucleus.connectionpassworddecrypter
datanucleus.connectionpool.driverprops
datanucleus.connectionpool.initialpoolsize
datanucleus.connectionpool.maxactive
datanucleus.connectionpool.maxconnections
datanucleus.connectionpool.maxidle
datanucleus.connectionpool.maxpoolsize
datanucleus.connectionpool.maxstatements
datanucleus.connectionpool.maxwait
datanucleus.connectionpool.minevictableidletimemillis
datanucleus.connectionpool.minidle
datanucleus.connectionpool.minpoolsize
datanucleus.connectionpool.testsql
datanucleus.connectionpool.timebetweenevictionrunsmillis
datanucleus.connectionpoolingtype
datanucleus.connectionpoolingtype.nontx
datanucleus.connectionurl
datanucleus.connectionusername
datanucleus.copyonattach
datanucleus.currentuser
datanucleus.currentuserprovider
datanucleus.datastoreidentitytype
datanucleus.datastorereadtimeout
datanucleus.datastorewritetimeout
datanucleus.deletionpolicy
datanucleus.detachalloncommit
datanucleus.detachallonrollback
datanucleus.detachaswrapped
datanucleus.detachedstate
datanucleus.detachmentfields
datanucleus.detachonclose
datanucleus.enablestatistics
datanucleus.executioncontext.closeactivetxaction
datanucleus.executioncontext.maxidle
datanucleus.executioncontext.reaperthread
datanucleus.findobject.typeconversion
datanucleus.findobject.validatewhencached
datanucleus.flush.auto.objectlimit
datanucleus.flush.mode
datanucleus.identifier.case
datanucleus.identifier.namingfactory
datanucleus.identifier.tableprefix
datanucleus.identifier.tablesuffix
datanucleus.identifier.wordseparator
datanucleus.identifierfactory
datanucleus.identitykeytranslatortype
datanucleus.identitystringtranslatortype
datanucleus.ignorecache
datanucleus.jmxtype
datanucleus.managerelationships
datanucleus.managerelationshipschecks
datanucleus.mapping
datanucleus.mapping.catalog
datanucleus.mapping.schema
datanucleus.maxfetchdepth
datanucleus.metadata.allowannotations
datanucleus.metadata.allowloadatruntime
datanucleus.metadata.allowxml
datanucleus.metadata.alwaysdetachable
datanucleus.metadata.autoregistration
datanucleus.metadata.defaultinheritancestrategy
datanucleus.metadata.defaultnullable
datanucleus.metadata.embedded.flat
datanucleus.metadata.ignoremetadataformissingclasses
datanucleus.metadata.javaxvalidationshortcuts
datanucleus.metadata.jdofileextension
datanucleus.metadata.jdoqueryfileextension
datanucleus.metadata.ormfileextension
datanucleus.metadata.supportorm
datanucleus.metadata.usediscriminatorclassnamebydefault
datanucleus.metadata.usediscriminatorforsingletable
datanucleus.metadata.xml.allowjdo1_0
datanucleus.metadata.xml.namespaceaware
datanucleus.metadata.xml.validate
datanucleus.multithreaded
datanucleus.name
datanucleus.optimistic
datanucleus.persistencebyreachabilityatcommit
datanucleus.persistenceunitloadclasses
datanucleus.persistenceunitname
datanucleus.persistencexmlfilename
datanucleus.plugin.allowuserbundles
datanucleus.plugin.pluginregistrybundlecheck
datanucleus.plugin.pluginregistryclassname
datanucleus.plugin.validateplugins
datanucleus.primaryclassloader
datanucleus.propertiesfile
datanucleus.query.checkunusedparameters
datanucleus.query.closeable
datanucleus.query.compilation.cached
datanucleus.query.compilenamedqueriesatstartup
datanucleus.query.compileoptimisevarthis
datanucleus.query.evaluateinmemory
datanucleus.query.flushbeforeexecution
datanucleus.query.jdoql.allowall
datanucleus.query.jpql.allowrange
datanucleus.query.loadresultsatcommit
datanucleus.query.resultcache.validateobjects
datanucleus.query.results.cached
datanucleus.query.resultsizemethod
datanucleus.query.sql.allowall
datanucleus.query.sql.syntaxchecks
datanucleus.query.usefetchplan
datanucleus.rdbms.allowcolumnreuse
datanucleus.rdbms.autofetchunloadedbasicfields
datanucleus.rdbms.autofetchunloadedfks
datanucleus.rdbms.checkexisttablesorviews
datanucleus.rdbms.classadditionmaxretries
datanucleus.rdbms.clonecalendarfordatetimezone
datanucleus.rdbms.constraintcreatemode
datanucleus.rdbms.datastoreadapterclassname
datanucleus.rdbms.discriminatorpersubclasstable
datanucleus.rdbms.dynamicschemaupdates
datanucleus.rdbms.informix.useserialforidentity
datanucleus.rdbms.initializecolumninfo
datanucleus.rdbms.mysql.characterset
datanucleus.rdbms.mysql.collation
datanucleus.rdbms.mysql.enginetype
datanucleus.rdbms.omitdatabasemetadatagetcolumns
datanucleus.rdbms.omitvaluegenerationgetcolumns
datanucleus.rdbms.oracle.nlssortorder
datanucleus.rdbms.persistemptystringasnull
datanucleus.rdbms.query.fetchdirection
datanucleus.rdbms.query.multivaluedfetch
datanucleus.rdbms.query.resultsetconcurrency
datanucleus.rdbms.query.resultsettype
datanucleus.rdbms.refreshalltablesonrefreshcolumns
datanucleus.rdbms.schematable.tablename
datanucleus.rdbms.sqltablenamingstrategy
datanucleus.rdbms.statementbatchlimit
datanucleus.rdbms.statementlogging
datanucleus.rdbms.stringdefaultlength
datanucleus.rdbms.stringlengthexceededaction
datanucleus.rdbms.tablecolumnorder
datanucleus.rdbms.uniqueconstraints.mapinverse
datanucleus.rdbms.usecolumndefaultwhennull
datanucleus.rdbms.usedefaultsqltype
datanucleus.rdbms.uselegacynativevaluestrategy
datanucleus.readonlydatastore
datanucleus.readonlydatastoreaction
datanucleus.relation.identitystoragemode
datanucleus.restorevalues
datanucleus.retainvalues
datanucleus.schema.autocreateall
datanucleus.schema.autocreatecolumns
datanucleus.schema.autocreateconstraints
datanucleus.schema.autocreatedatabase
datanucleus.schema.autocreatetables
datanucleus.schema.autocreatewarnonerror
datanucleus.schema.generatedatabase.create.order
datanucleus.schema.generatedatabase.createscript
datanucleus.schema.generatedatabase.drop.order
datanucleus.schema.generatedatabase.dropscript
datanucleus.schema.generatedatabase.mode
datanucleus.schema.generatescripts.create
datanucleus.schema.generatescripts.drop
datanucleus.schema.generatescripts.mode
datanucleus.schema.loadscript
datanucleus.schema.validateall
datanucleus.schema.validatecolumns
datanucleus.schema.validateconstraints
datanucleus.schema.validatetables
datanucleus.serializeread
datanucleus.servertimezoneid
datanucleus.singletonpmfforname
datanucleus.statemanager.classname
datanucleus.store.allowreferenceswithnoimplementations
datanucleus.tenantid
datanucleus.tenantprovider
datanucleus.tenantreadids
datanucleus.transaction.isolation
datanucleus.transaction.jta.transactionmanagerjndi
datanucleus.transaction.jta.transactionmanagerlocator
datanucleus.transaction.nontx.atomic
datanucleus.transaction.nontx.read
datanucleus.transaction.nontx.write
datanucleus.transaction.type
datanucleus.type.treatjavautildateasmutable
datanucleus.type.wrapper.basis
datanucleus.useimplementationcreator
datanucleus.validation.factory
datanucleus.validation.group.pre-persist
datanucleus.validation.group.pre-remove
datanucleus.validation.group.pre-update
datanucleus.validation.mode
datanucleus.valuegeneration.increment.allocationsize
datanucleus.valuegeneration.sequence.allocationsize
datanucleus.valuegeneration.transactionattribute
datanucleus.valuegeneration.transactionisolation
datanucleus.version.versionnumber.initialvalue
javax.jdo.mapping.catalog
javax.jdo.mapping.schema
javax.jdo.option.connectiondrivername
javax.jdo.option.connectionfactory
javax.jdo.option.connectionfactory2
javax.jdo.option.connectionfactory2name
javax.jdo.option.connectionfactoryname
javax.jdo.option.connectionpassword
javax.jdo.option.connectionurl
javax.jdo.option.connectionusername
javax.jdo.option.copyonattach
javax.jdo.option.datastorereadtimeoutmillis
javax.jdo.option.datastorewritetimeoutmillis
javax.jdo.option.detachalloncommit
javax.jdo.option.ignorecache
javax.jdo.option.mapping
javax.jdo.option.multitenancy
javax.jdo.option.multithreaded
javax.jdo.option.name
javax.jdo.option.nontransactionalread
javax.jdo.option.nontransactionalwrite
javax.jdo.option.optimistic
javax.jdo.option.persistenceunitname
javax.jdo.option.readonly
javax.jdo.option.restorevalues
javax.jdo.option.retainvalues
javax.jdo.option.servertimezoneid
javax.jdo.option.spi.resourcename
javax.jdo.option.transactionisolationlevel
javax.jdo.option.transactiontype
javax.jdo.persistencemanagerfactoryclass
 */
