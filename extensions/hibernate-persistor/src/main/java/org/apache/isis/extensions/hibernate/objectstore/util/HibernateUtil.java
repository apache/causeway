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


package org.apache.isis.extensions.hibernate.objectstore.util;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.event.EventListeners;
import org.hibernate.event.InitializeCollectionEventListener;
import org.hibernate.event.LoadEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreLoadEventListener;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.def.DefaultInitializeCollectionEventListener;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.hibernate.event.def.DefaultPreLoadEventListener;
import org.hibernate.mapping.Table;
import org.hibernate.transaction.CMTTransactionFactory;
import org.hibernate.transaction.JTATransactionFactory;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.factory.InstanceFactory;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.hibernate.objectstore.HibernateConstants;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.interceptor.AdapterInterceptor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.CollectionAdapterInitializeEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterInsertPostEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterInsertPreEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterLoadEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterLoadPostEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterLoadPreEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterUpdatePostEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.listener.AdapterUpdatePreEventListener;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.session.SessionPlaceHolder;
import org.apache.isis.extensions.hibernate.objectstore.tools.HibernateTools;
import org.apache.isis.extensions.hibernate.objectstore.tools.internal.Nof2HbmXml;
import org.apache.isis.runtime.context.IsisContext;


/**
 * Basic Hibernate helper class for Hibernate configuration and startup.
 * <p>
 * Uses a static initializer to read startup options and initialize <tt>Configuration</tt> and
 * <tt>SessionFactory</tt>.
 * <p>
 * This class also tries to figure out if JNDI binding of the <tt>SessionFactory</tt> is used, otherwise it
 * falls back to a global static variable (Singleton). If you use this helper class to obtain a
 * <tt>SessionFactory</tt> in your code, you are shielded from these deployment differences.
 * <p>
 * Another advantage of this class is access to the <tt>Configuration</tt> object that was used to build the
 * current <tt>SessionFactory</tt>. You can access mapping metadata programmatically with this API, and
 * even change it and rebuild the <tt>SessionFactory</tt>.
 * <p>
 * If you want to assign a global interceptor, set its fully qualified class name with the system (or
 * hibernate.properties/hibernate.cfg.xml) property <tt>hibernate.util.interceptor_class</tt>. It will be
 * loaded and instantiated on static initialization of HibernateUtil; it has to have a no-argument
 * constructor. You can call <tt>HibernateUtil.getInterceptor()</tt> if you need to provide settings before
 * using the interceptor.
 * <p>
 * Note: This class supports annotations by default, hence needs JDK 5.0 and the Hibernate Annotations library
 * on the classpath. Change the single commented line in the source to make it compile and run on older JDKs
 * with XML mapping files only.
 * <p>
 * Note: This class supports only one data store. Support for several <tt>SessionFactory</tt> instances can
 * be easily added (through a static <tt>Map</tt>, for example). You could then lookup a
 * <tt>SessionFactory</tt> by its name.
 * 
 * @author christian@hibernate.org
 */
public class HibernateUtil {
    static Logger LOG = Logger.getLogger(HibernateUtil.class);
    
    private static boolean MANAGED_TRANSACTIONS = false;
    private static SessionFactory sessionFactory;
    private static Object sessionFactoryLock = new Object();
    private static boolean initRun = false;
    
    private static final String FILE_SEPERATOR = System.getProperty("file.separator");
    private static final String sql92keywords = "ABSOLUTE,ACTION,ADA,ADD,ALL,ALLOCATE,ALTER,AND,ANY,ARE,AS,ASC,ASSERTION,AT,AUTHORIZATION,AVG,BEGIN,BETWEEN,BIT,BIT_LENGTH,BOTH,BY,CASCADE,CASCADED,CASE,CAST,CATALOG,CHAR,CHARACTER,CHARACTER_LENGTH,CHAR_LENGTH,CHECK,CLOSE,COALESCE,COLLATE,COLLATION,COLUMN,COMMIT,CONNECT,CONNECTION,CONSTRAINT,CONSTRAINTS,CONTINUE,CONVERT,CORRESPONDING,COUNT,CREATE,CROSS,CURRENT,CURRENT_DATE,CURRENT_TIME,CURRENT_TIMESTAMP,CURRENT_USER,CURSOR,DATE,DAY,DEALLOCATE,DEC,DECIMAL,DECLARE,DEFAULT,DEFERRABLE,DEFERRED,DELETE,DESC,DESCRIBE,DESCRIPTOR,DIAGNOSTICS,DISCONNECT,DISTINCT,DOMAIN,DOUBLE,DROP,ELSE,END,END-EXEC,ESCAPE,EXCEPT,EXCEPTION,EXEC,EXECUTE,EXISTS,EXTERNAL,EXTRACT,FALSE,FETCH,FIRST,FLOAT,FOR,FOREIGN,FORTRAN,FOUND,FROM,FULL,GET,GLOBAL,GO,GOTO,GRANT,GROUP,HAVING,HOUR,IDENTITY,IMMEDIATE,IN,INCLUDE,INDEX,INDICATOR,INITIALLY,INNER,INPUT,INSENSITIVE,INSERT,INT,INTEGER,INTERSECT,INTERVAL,INTO,IS,ISOLATION,JOIN,KEY,LANGUAGE,LAST,LEADING,LEFT,LEVEL,LIKE,LOCAL,LOWER,MATCH,MAX,MIN,MINUTE,MODULE,MONTH,NAMES,NATIONAL,NATURAL,NCHAR,NEXT,NO,NONE,NOT,NULL,NULLIF,NUMERIC,OCTET_LENGTH,OF,ON,ONLY,OPEN,OPTION,OR,ORDER,OUTER,OUTPUT,OVERLAPS,PAD,PARTIAL,PASCAL,POSITION,PRECISION,PREPARE,PRESERVE,PRIMARY,PRIOR,PRIVILEGES,PROCEDURE,PUBLIC,READ,REAL,REFERENCES,RELATIVE,RESTRICT,REVOKE,RIGHT,ROLLBACK,ROWS,SCHEMA,SCROLL,SECOND,SECTION,SELECT,SESSION,SESSION_USER,SET,SIZE,SMALLINT,SOME,SPACE,SQL,SQLCA,SQLCODE,SQLERROR,SQLSTATE,SQLWARNING,SUBSTRING,SUM,SYSTEM_USER,TABLE,TEMPORARY,THEN,TIME,TIMESTAMP,TIMEZONE_HOUR,TIMEZONE_MINUTE,TO,TRAILING,TRANSACTION,TRANSLATE,TRANSLATION,TRIM,TRUE,UNION,UNIQUE,UNKNOWN,UPDATE,UPPER,USAGE,USER,USING,VALUE,VALUES,VARCHAR,VARYING,VIEW,WHEN,WHENEVER,WHERE,WITH,WORK,WRITE,YEAR,ZONE";
    private static Map<String, String> keywords = new HashMap<String, String>();

    public static final String MAPPING_DIR = "mappings";

    static final Session sessionPlaceholder = new SessionPlaceHolder();

    static boolean regenerate;
    static boolean auto;

    static {
        HibernateUtil.auto = isConfigured(HibernateConstants.HIB_AUTO_KEY, true);
        HibernateUtil.regenerate = isConfigured(HibernateConstants.HIB_REGENERATE_KEY, false);
    }


    public static void initialiseSessionFactory() {
        try {
            HibernateUtil.configuration = createConfiguration();
            HibernateUtil.createMapping();
            HibernateUtil.bindListeners();
            final String transactionStrategy = HibernateUtil.configuration.getProperty(Environment.TRANSACTION_STRATEGY);
            MANAGED_TRANSACTIONS = CMTTransactionFactory.class.getName().equals(transactionStrategy)
                    || JTATransactionFactory.class.getName().equals(transactionStrategy);

            if (HibernateUtil.configuration.getProperty(Environment.SESSION_FACTORY_NAME) != null) {
                // Let Hibernate bind the factory to JNDI
                HibernateUtil.configuration.buildSessionFactory();
            }
        } catch (final Throwable ex) {
            LOG.error("building SessionFactory failed.", ex);
            throw new IsisException(ex);
        }
    }

    private static List<File> readMappingFiles(final String path) {
        final List<File> files = new ArrayList<File>();
        final File dir = new File(path);
        if (dir.isDirectory()) {
            LOG.debug("searching for mapping file in " + dir.getAbsolutePath());
            files.addAll(Arrays.asList(dir.listFiles(new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    return name.endsWith(".hbm.xml");
                }
            })));

            for (final File subDir : dir.listFiles()) {
                if (subDir.isDirectory()) {
                    files.addAll(readMappingFiles(path + FILE_SEPERATOR + subDir.getName()));
                }
            }

        }
        return files;
    }

    static boolean addMappingFilesToConfiguration(final Configuration cfg) {
        boolean haveReadAMappingFile = false;
        final String path = getNofConfiguration().getString(
        		HibernateConstants.HBM_EXPORT_KEY, 
        		new File(".").getAbsolutePath() + FILE_SEPERATOR + MAPPING_DIR);
        final List<File> mappingFiles = readMappingFiles(path);
        for (final File file : mappingFiles) {
            cfg.addFile(file);
            haveReadAMappingFile = true;
            LOG.info("reading mapping file: " + file.getAbsolutePath());
        }
        return haveReadAMappingFile;
    }

    static Configuration createConfiguration() {
        Configuration cfg;
        // Detect if annotations (JDK 5.0) are required
        final boolean annotations = getNofConfiguration().getBoolean(HibernateConstants.HIB_ANNOTATIONS_KEY, false);
        if (annotations) {
            // remove dependency of this class on annotations jar
            cfg = (Configuration) InstanceFactory.createInstance("org.hibernate.cfg.AnnotationConfiguration");
        } else {
            cfg = new Configuration();
        }

        /*
         * This custom entity resolver supports entity placeholders in XML mapping files and tries to resolve
         * them on the classpath as a resource configuration.setEntityResolver(new
         * ImportFromClasspathEntityResolver());
         */

        // Read not only hibernate.properties, but also hibernate.cfg.xml
        cfg.configure();

        return cfg;
    }

    /**
     * Returns the original Hibernate configuration.
     * 
     * @return Configuration
     */
    public static Configuration getConfiguration() {
        return HibernateUtil.configuration;
    }

    /**
     * Returns the global SessionFactory.
     * 
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        SessionFactory sf = null;
        final String sfName = HibernateUtil.configuration.getProperty(Environment.SESSION_FACTORY_NAME);
        if (sfName != null) {
            LOG.debug("Looking up SessionFactory in JNDI.");
            try {
                sf = (SessionFactory) new InitialContext().lookup(sfName);
            } catch (final NamingException ex) {
                throw new IsisException(ex);
            }
        } else {
            if (sessionFactory == null) {
                synchronized (sessionFactoryLock) {
                    if (sessionFactory == null) {
                        sessionFactory = HibernateUtil.configuration.buildSessionFactory();
                    }
                }
            }
            sf = sessionFactory;
        }
        if (sf == null) {
            throw new IllegalStateException("SessionFactory not available.");
        }
        return sf;
    }

    /**
     * Returns the current Session, and starts a new transaction if one is not active
     * 
     * @return Session
     */
    public static Session getCurrentSession() {
        final Session session = getSessionFactory().getCurrentSession();
        if (!MANAGED_TRANSACTIONS) {
            Assert.assertTrue(session.getTransaction().isActive());
        }
        return session;
    }

    public static void startTransaction() {
        final Session session = getSessionFactory().getCurrentSession();
        if (!MANAGED_TRANSACTIONS) {
            Assert.assertFalse(session.getTransaction().isActive());
            session.beginTransaction();
        }
    }

    public static boolean inTransaction() {
        final Session session = getSessionFactory().getCurrentSession();
        if (!MANAGED_TRANSACTIONS) {
            return session.getTransaction().isActive();
        }
        return true;
    }


    /**
     * Commits the transaction on the current session if one is active. Also auto closes the current session.
     */
    public static void commitTransaction() {

        if (!MANAGED_TRANSACTIONS) {
            final Session session = getSessionFactory().getCurrentSession();
            final Transaction tx = session.getTransaction();
            if (tx.isActive()) {
                tx.commit();
            } else {
                session.close();
            }
        }
    }

    /**
     * Rolls back the transaction on the current session if one is active. Also auto closes the current
     * session.
     */
    public static void rollbackTransaction() {
        try {
            if (!MANAGED_TRANSACTIONS) {
                final Session session = getSessionFactory().getCurrentSession();
                final Transaction tx = session.getTransaction();
                if (tx.isActive()) {
                    tx.rollback();
                } else {
                    session.close();
                }
            }
        } catch (final Exception e) {
            // if rollback fails we've done our best. Log but continue
            LOG.warn("Rollback Failure: " + e.getMessage());
        }
    }

    /**
     * Rebuild the SessionFactory with the given Hibernate Configuration.
     * <p>
     * HibernateUtil does not configure() the given Configuration object, it directly calls
     * buildSessionFactory(). This method also closes the old SessionFactory before, if still open.
     * 
     * @param cfg
     */
    public static void rebuildSessionFactory() {
        LOG.debug("Rebuilding the SessionFactory from given Configuration.");
        synchronized (sessionFactoryLock) {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
            if (HibernateUtil.configuration.getProperty(Environment.SESSION_FACTORY_NAME) != null) {
                HibernateUtil.configuration.buildSessionFactory();
            } else {
                sessionFactory = HibernateUtil.configuration.buildSessionFactory();
            }

        }
    }

    public static String getRequiredClasses() {
        final StringBuffer sb = new StringBuffer(2048);
        for (final Iterator<String> iter = HibernateUtil.requiredToMap.iterator(); iter.hasNext();) {
            sb.append("\n").append(iter.next());
        }
        return sb.toString();
    }

    /**
     * Keep a track of when Hibernate is flushing, so we know not to resolve etc.
     */
    // TODO: need to look at handling of flushing. This is one way - but may be
    // better to mark all objects with a SERIALIZING state in
    // AdapterInterceptor.preFlush
    // and put them back in postFlush
    // private static class FlushState extends ThreadLocal {
    // protected Object initialValue() {
    // return new Boolean(false);
    // }
    // public boolean isFlushing() {
    // return ((Boolean) super.get()).booleanValue();
    // }
    // public void setFlushing(final boolean flushing) {
    // super.set(new Boolean(flushing));
    // }
    // }
    // private static FlushState commitPhase = new FlushState();
    //
    // protected static void setFlushing(final boolean flushing) {
    // commitPhase.setFlushing(flushing);
    // }
    // public static boolean isFlushing() {
    // return commitPhase.isFlushing();
    // }
    private static boolean tableExists(
            final DatabaseMetaData metaData,
            final String catalog,
            final String schema,
            final String table) throws SQLException {
        boolean exists = false;
        // check both upper and lower case names - different databases behave differently
        // mysql - doesn't care
        // hsqldb - match upper
        // postgresql - match lower
        final ResultSet tablesUC = metaData.getTables(catalog, schema, table.toUpperCase(), new String[] { "TABLE" });
        if (tablesUC.next()) {
            exists = true;
        } else {
            final ResultSet tablesLC = metaData.getTables(catalog, schema, table.toLowerCase(), new String[] { "TABLE" });
            if (tablesLC.next()) {
                exists = true;
            }
            tablesLC.close();
        }
        if (exists) {
            LOG.info("Table '" + table + "' already exists; isInitialized=true");
        }

        tablesUC.close();
        return exists;
    }

    //////////////////////////////////////////////////////////////////////
    // Initialization
    //////////////////////////////////////////////////////////////////////
    
    public static boolean hasInitRun() {
        return initRun;
    }

    public static synchronized boolean init() {
        if (initRun) {
            return true;
        }
        initialiseSessionFactory();
        if (MANAGED_TRANSACTIONS) {
            if (HibernateUtil.getNofConfiguration().getBoolean(HibernateConstants.HIB_SCHEMA_EXPORT_KEY)) {
                LOG.warn(HibernateConstants.HIB_SCHEMA_EXPORT_KEY + " is set, but cannot be run in this environment");
            }
            if (HibernateUtil.getNofConfiguration().getBoolean(HibernateConstants.HIB_SCHEMA_UPDATE_KEY)) {
                LOG.warn(HibernateConstants.HIB_SCHEMA_UPDATE_KEY + " is set, but cannot be run in this environment");
            }
            initRun = true;
            return true;
        }
        final boolean isInitialized = isInitialized();
        if (isInitialized) {
            if (HibernateUtil.getNofConfiguration().getBoolean(HibernateConstants.HIB_SCHEMA_UPDATE_KEY)) {
                HibernateTools.updateSchema(false, true);
            }
        } else {
            if (HibernateUtil.getNofConfiguration().getBoolean(HibernateConstants.HIB_SCHEMA_EXPORT_KEY, true)) {
                HibernateTools.exportSchema(false, true);
            }
        }
        // make sure the session factory is loaded
        HibernateUtil.getSessionFactory();
        // make sure we don't run the schema export/update again! for threaded apps this method will be called
        // multiple times (a new object store is created for each thread), and we don't want to go to the db
        // every time
        initRun = true;
        return isInitialized;
    }

    private static boolean isInitialized() {
        final IsisConfiguration config = HibernateUtil.getNofConfiguration();
        if (config.hasProperty(HibernateConstants.HIB_INITIALIZED_KEY)) {
            return config.getBoolean(HibernateConstants.HIB_INITIALIZED_KEY);
        }
        final Session session = getSessionFactory().openSession();
        Transaction tx = null;
        boolean isInitialized = false;
        try {
            tx = session.beginTransaction();
            final DatabaseMetaData metaData = session.connection().getMetaData();
            final String tableName = config.getString(HibernateConstants.PROPERTY_PREFIX + "initialized.table");
            if (tableName != null) {
                isInitialized = !tableExists(metaData, config.getString(HibernateConstants.PROPERTY_PREFIX + "initialized.catalog"), config
                        .getString(HibernateConstants.PROPERTY_PREFIX + "initialized.schema"), config.getString(HibernateConstants.PROPERTY_PREFIX
                        + "initialized.table"));
            } else {
                for (final Iterator<?> iter = HibernateUtil.configuration.getTableMappings(); iter.hasNext();) {
                    final Table t = (Table) iter.next();
                    if (t.isPhysicalTable()) {
                        isInitialized = tableExists(metaData, t.getCatalog(), t.getSchema(), t.getName());
                        break;
                    }
                }
            }
            tx.commit();
        } catch (final SQLException e) {
            tx.rollback();
            throw new IsisException(e);
        } finally {
            session.close();
        }
        return isInitialized;
    }


    //////////////////////////////////////////////////////////////////////
    // shutdown
    //////////////////////////////////////////////////////////////////////

    /**
     * Closes the current SessionFactory and releases all resources except the configuration.
     */
    public static void shutdown() {
        LOG.info("Shutting down Hibernate.");
        if (sessionFactory == null) {
            return;
        }
        final boolean shutdown = HibernateUtil.getNofConfiguration().getBoolean(HibernateConstants.PROPERTY_PREFIX + "shutdown", false);
        String url = null;
        if (shutdown) {
            // shutdown HSQL if requested
            url = getConfiguration().getProperty("connection.url");
            if (url != null && url.startsWith("jdbc:hsqldb:")) {
                LOG.info("Shutdown/compact hsqldb");
                Statement stmt = null;
                try {
                    final String command = "SHUTDOWN COMPACT";
                    startTransaction();
                    final Session session = getCurrentSession();
                    LOG.info("HSQLDB: " + command);
                    stmt = session.connection().createStatement();
                    stmt.execute(command);
                } catch (final Throwable ex) {
                    LOG.error("could not compact database", ex);
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (final SQLException ex) {
                            LOG.error("SQLException", ex);
                        }
                    }
                }
            }
        }

        rollbackTransaction();

        // Close caches and connection pools
        getSessionFactory().close();

        // Clear static variables
        HibernateUtil.configuration = null;
        sessionFactory = null;

        if (shutdown && url != null && url.startsWith("jdbc:derby:")) {
            // shutdown Derby database if running
            LOG.info("Shutdown Derby");
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (final SQLException e) {
                if (e.getSQLState().equals("XJ015")) {
                    LOG.info(e.getMessage());
                } else {
                    LOG.error("Exception in Derby shutdown", e);
                }
            }
        }
    }


    
    //////////////////////////////////////////////////////////////////////
    // Keywords
    //////////////////////////////////////////////////////////////////////

    public static boolean isDatabaseKeyword(final String potentialKeyword) {
        return keywords.containsKey(potentialKeyword.trim().toUpperCase());
    }

    private static void buildkeywordMap(final String keywordCSV) {
        final String allKeywords = sql92keywords + ", " + keywordCSV;
        final String[] keywordsTokens = allKeywords.split(",");
        for (final String keyword : keywordsTokens) {
            final String normalisedKeyword = keyword.trim().toUpperCase();
            if (normalisedKeyword.length() > 0) {
                keywords.put(normalisedKeyword, "");
            }
        }
    }

    static void getKeywords() {
        String databaseKeywords = "";
        synchronized (sessionFactoryLock) {
            final Session session = getSessionFactory().openSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                final DatabaseMetaData metaData = session.connection().getMetaData();
                databaseKeywords = metaData.getSQLKeywords();
                tx.commit();
            } catch (final SQLException e) {
                tx.rollback();
                throw new IsisException(e);
            } finally {
                session.close();
            }
            // throw away the sessionfactory as it's not complete
            sessionFactory = null;
        }
        buildkeywordMap(databaseKeywords);
    }


    private static boolean isConfigured(String key, boolean defaultValue) {
        return IsisContext.getConfiguration().getBoolean(key, defaultValue);
    }

    static IsisConfiguration getNofConfiguration() {
        return IsisContext.getConfiguration();
    }

    static void bindListeners() {
        getConfiguration().setInterceptor(new AdapterInterceptor());
        
        final EventListeners listeners = getConfiguration().getEventListeners();
        
    
        listeners.setInitializeCollectionEventListeners(
                new InitializeCollectionEventListener[] {
                        new DefaultInitializeCollectionEventListener(), 
                        new CollectionAdapterInitializeEventListener()
                });
    
        listeners.setPreLoadEventListeners(
                new PreLoadEventListener[] { 
                        new DefaultPreLoadEventListener(), 
                        new AdapterLoadPreEventListener()
                });
        listeners.setLoadEventListeners(
                new LoadEventListener[] { 
                        new AdapterLoadEventListener()
                });
        listeners.setPostLoadEventListeners(
                new PostLoadEventListener[] { 
                        new DefaultPostLoadEventListener(), 
                        new AdapterLoadPostEventListener() 
                });
    
        listeners.setPreInsertEventListeners(
                new PreInsertEventListener[] { 
                        new AdapterInsertPreEventListener()
                });
        listeners.setPostInsertEventListeners(
                new PostInsertEventListener[] { 
                        new AdapterInsertPostEventListener()
                });
        
        
        listeners.setPreUpdateEventListeners(
                new PreUpdateEventListener[] { 
                        new AdapterUpdatePreEventListener() 
                });
        listeners.setPostUpdateEventListeners(
                new PostUpdateEventListener[] { 
                        new AdapterUpdatePostEventListener() 
                });
    }

    @SuppressWarnings("unused")
    private static Session getSessionPlaceHolder() {
        return sessionPlaceholder;
    }

    static Configuration configuration;

    static List<String> requiredToMap = new ArrayList<String>();

    public static void ensureMapped(final ObjectSpecification specification) {
        if (getConfiguration().getClassMapping(specification.getFullName()) == null) {
            requiredToMap.add(specification.getFullName());
            if (getNofConfiguration().getBoolean(HibernateConstants.PROPERTY_PREFIX + "showremappings", false)) {
                LOG.info("remapping" + getRequiredClasses());
            }
            
            commitTransaction();
            
            // Rebuild the session factory but use any [[NAME]] persistent classes 
            // which have their specifications loaded.
            configuration = createConfiguration();
            auto = true;
            HibernateUtil.createMapping();
            bindListeners();
    
            if (getNofConfiguration().getBoolean(HibernateConstants.HIB_SCHEMA_UPDATE_KEY)) {
                HibernateTools.updateSchema(configuration, false, true);
            }
    
            rebuildSessionFactory();
        }
    }

    static void createMapping() {
        getKeywords();
        Nof2HbmXml nof2HbmXml = null;
    
        if (auto && regenerate) {
            LOG.info("(re)generating auto mapping files");
            nof2HbmXml = new Nof2HbmXml();
            nof2HbmXml.createMappingFiles();
        }
    
        final boolean mappingFilesReadIn = addMappingFilesToConfiguration(configuration);
        if (auto && !mappingFilesReadIn) {
            LOG.info("auto mapping DOM to Hibernate database");
            (nof2HbmXml == null ? new Nof2HbmXml() : nof2HbmXml).configure(configuration);
        }
    }



}
