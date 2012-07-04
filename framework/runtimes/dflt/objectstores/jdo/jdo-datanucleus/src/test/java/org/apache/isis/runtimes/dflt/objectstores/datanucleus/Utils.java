package org.apache.isis.runtimes.dflt.objectstores.datanucleus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.runtimes.dflt.objectstores.datanucleus.DataNucleusObjectStore;
import org.apache.isis.runtimes.dflt.objectstores.datanucleus.DataNucleusPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.objectstores.datanucleus.metamodel.specloader.progmodelfacets.DataNucleusProgrammingModelFacets;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.specloader.validator.JdoMetaModelValidator;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;

public class Utils {

    private Utils(){}

    public static IsisSystemWithFixtures.Builder systemBuilder() {
        return IsisSystemWithFixtures.builder()
        .with(configurationForDataNucleusOverHsqlDb())
        .with(new DataNucleusProgrammingModelFacets())
        .with(new JdoMetaModelValidator())
        .with(new DataNucleusPersistenceMechanismInstaller());
    }

    public static IsisSystemWithFixtures.Listener listenerToDeleteFrom(final String... tables) {
        return new IsisSystemWithFixtures.ListenerAdapter(){

            @Override
            public void postSetupSystem(boolean firstTime) throws Exception {
                final Statement statement = createStatement();
                for(String table: tables) {
                    statement.executeUpdate("DELETE FROM " + table);
                }
            }

            private Statement createStatement() throws SQLException {
                final DataNucleusObjectStore objectStore = (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
                Connection connection = objectStore.getJavaSqlConnection();
                return connection.createStatement();
            }
        };
    }

    public static IsisConfiguration configurationForDataNucleusOverHsqlDb() {
        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        Properties props = new Properties();
        props.put("isis.persistor.datanucleus.impl.openjpa.jdbc.SynchronizeMappings", "buildSchema");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
        //props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:hsqldb:file:hsql-db/test;hsqldb.write_delay=false;shutdown=true");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionUserName", "sa");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionPassword", "");
        props.put("isis.persistor.datanucleus.impl.datanucleus.autoCreateSchema", "true");
        props.put("isis.persistor.datanucleus.impl.datanucleus.validateTables", "false");
        props.put("isis.persistor.datanucleus.impl.datanucleus.validateConstraints", "false");
        configuration.add(props);
        return configuration;
    }

}
