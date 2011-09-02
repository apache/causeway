package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.List;
import java.util.Properties;

import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestCommonBase;
import org.apache.isis.runtimes.dflt.objectstores.sql.singleton.SqlIntegrationTestSingleton;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.SqlDataClassFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.PolySubClassOne;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.PolySubClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.PolyTestClass;

public class PolymorphismTest extends SqlIntegrationTestCommonBase {

    // {{ Setup
    @Override
    public Properties getProperties() {
        Properties properties = super.getProperties();
        if (properties == null) { // Only used if properties file does not exist.
            properties = new Properties();
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.driver", "org.hsqldb.jdbcDriver");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.connection", "jdbc:hsqldb:file:hsql-db/polytests");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.user", "sa");
            properties.put(SqlObjectStore.BASE_NAME + ".jdbc.password", "");
            properties.put("isis.logging.objectstore", "on");
        }

        return properties;
    }

    @Override
    public String getPropertiesFilename() {
        return "hsql-polytest.properties";
    }

    @Override
    public String getSqlTeardownString() {
        return "SHUTDOWN;";
    }

    // }}

    public void testCreate() throws Exception {
        SqlIntegrationTestSingleton.drop("ISIS_POLYTESTCLASS");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSUBCLASSONE");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSUBCLASSTWO");

        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final PolyTestClass polyTestClass = factory.newPolyTestClass();
        polyTestClass.setString("polyTestClass");

        // setup the polyTestClass
        PolySubClassOne polySubClassOne = factory.newPolySubClassOne();
        polySubClassOne.setString("PolySubClassOne 1");

        PolySubClassTwo polySubClassTwo = factory.newPolySubClassTwo();
        polySubClassTwo.setString("PolySubClassTwo 1");

        polyTestClass.getPolyBaseClasses().add(polySubClassOne);
        polyTestClass.getPolyBaseClasses().add(polySubClassTwo);

        factory.save(polySubClassOne);
        factory.save(polySubClassTwo);

        // store it and step the state engine
        factory.save(polyTestClass);

        // For in-memory only!
        if (getProperties().getProperty("isis.persistor") == "in-memory") {
            getSingletonInstance().setState(1);
        }

    }

    // testLoad() Must be the first test defined and run
    public void testLoad() throws Exception {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final List<PolyTestClass> dataClasses = factory.allPolyTestClasses();
        assertEquals(1, dataClasses.size());
        final PolyTestClass polyTestClass = dataClasses.get(0);
        SqlIntegrationTestSingleton.setStaticPolyTestClass(polyTestClass);
        // Must set state to 1 to prevent re-initialisation
        getSingletonInstance().setState(1);
    }

    // Last "test" - Set the Singleton state to 0 to invoke a clean shutdown.
    public void testSetStateZero() {
        getSingletonInstance().setState(0);
    }

}
