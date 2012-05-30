package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;

public class Utils {

    private Utils(){}

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
                final OpenJpaObjectStore objectStore = (OpenJpaObjectStore) IsisContext.getPersistenceSession().getObjectStore();
                Connection connection = objectStore.getConnection();
                return connection.createStatement();
            }
        };
    }

    public static IsisConfiguration configurationForOpenJpaOverHsqlDb() {
        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        Properties props = new Properties();
        props.put("isis.persistor.openjpa.impl.openjpa.jdbc.SynchronizeMappings", "buildSchema");
        //props.put("isis.persistor.openjpa.impl.openjpa.ConnectionURL", "jdbc:hsqldb:file:hsql-db/test;hsqldb.write_delay=false;shutdown=true");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionURL", "jdbc:hsqldb:mem:test");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionUserName", "sa");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionPassword", "");
        props.put("isis.persistor.openjpa.impl.openjpa.Log", "DefaultLevel=ERROR, Tool=INFO");
        props.put("isis.persistor.openjpa.impl.openjpa.RuntimeUnenhancedClasses", "supported"); // in production, should always pre-enhance using the maven openjpa plugin
        configuration.add(props);
        return configuration;
    }

}
