package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.List;
import java.util.Properties;

import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestCommonBase;
import org.apache.isis.runtimes.dflt.objectstores.sql.singleton.SqlIntegrationTestSingleton;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.SqlDataClassFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyBaseClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyInterface;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyInterfaceImplA;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyInterfaceImplB;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySelfRefClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySubClassOne;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySubClassThree;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySubClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyTestClass;

public class PolymorphismTest extends SqlIntegrationTestCommonBase {

    public class PolyInterfaceEx implements PolyInterface {
        // {{ String
        private String string;

        @Override
        public String getString() {
            return string;
        }

        public void setString(final String string) {
            this.string = string;
        }

        // }}

        public String getSpecial() {
            return "special";
        }

    };

    private static final String IMPL_A_STRING = "Impl A String";
    private static final String CHILD_1 = "Child 1";
    private static PolyInterfaceImplA polyIntImpA;
    private static PolyInterfaceImplB polyIntImpB;

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

    public void testSetup() {
        initialiseTests();
        getSingletonInstance().setState(0);
        SqlIntegrationTestSingleton.drop("%");
    }

    public void testCreate() throws Exception {
        SqlIntegrationTestSingleton.drop("ISIS_POLYTESTCLASS");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSUBCLASSONE");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSUBCLASSTWO");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSUBCLASSTHREE");
        SqlIntegrationTestSingleton.drop("ISIS_POLYINTERFACEIMPLA");
        SqlIntegrationTestSingleton.drop("ISIS_POLYINTERFACEIMPLB");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSELFREFCLASS");

        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final PolyTestClass polyTestClass = factory.newPolyTestClass();
        polyTestClass.setString("polyTestClassString");

        // {{ Setup self-referencing collection
        PolySelfRefClass polySelfRefClassParent = factory.newPolySelfRefClass();
        polySelfRefClassParent.setString("Parent");

        PolySelfRefClass polySelfRefClassChild1 = factory.newPolySelfRefClass();
        polySelfRefClassChild1.setString(CHILD_1);
        polySelfRefClassParent.addToPolySelfRefClasses(polySelfRefClassChild1);

        PolySelfRefClass polySelfRefClassChild2 = factory.newPolySelfRefClass();
        polySelfRefClassChild2.setString("Child 2");
        polySelfRefClassParent.addToPolySelfRefClasses(polySelfRefClassChild2);
        factory.save(polySelfRefClassChild2);

        PolySelfRefClass polySelfRefClassChild3 = factory.newPolySelfRefClass();
        polySelfRefClassChild3.setString("Child 1 of Child 1");
        polySelfRefClassChild1.addToPolySelfRefClasses(polySelfRefClassChild3);

        factory.save(polySelfRefClassChild3);
        factory.save(polySelfRefClassChild1);

        factory.save(polySelfRefClassParent);
        polyTestClass.setPolySelfRefClass(polySelfRefClassParent);
        // }}

        // polyTestClass.setPolyTestInterface(polyTestClass);

        polyIntImpA = factory.newPolyInterfaceImplA();
        polyIntImpA.setString(IMPL_A_STRING);
        polyIntImpA.setSpecial("special");
        factory.save(polyIntImpA);

        polyTestClass.setPolyInterfaceType(polyIntImpA);

        // setup the polyTestClass
        PolySubClassOne polySubClassOne = factory.newPolySubClassOne();
        polySubClassOne.setString("PolySubClassOne 1");

        PolySubClassTwo polySubClassTwo = factory.newPolySubClassTwo();
        polySubClassTwo.setString("PolySubClassTwo 1");

        PolySubClassThree polySubClassThree = factory.newPolySubClassThree();
        polySubClassThree.setString("PolySubClassThree 1");
        polySubClassThree.setAnotherString("Another String");

        polyTestClass.getPolyBaseClasses().add(polySubClassOne);
        polyTestClass.getPolyBaseClasses().add(polySubClassTwo);
        polyTestClass.getPolyBaseClasses().add(polySubClassThree);

        factory.save(polySubClassOne);
        factory.save(polySubClassTwo);
        factory.save(polySubClassThree);

        // store it and step the state engine
        factory.save(polyTestClass);

        // For in-memory only!
        if (getProperties().getProperty("isis.persistor") == "in-memory") {
            getSingletonInstance().setState(1);
        }

    }

    // testLoad() Must be the first test defined and run
    public void testLoad() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final List<PolyTestClass> dataClasses = factory.allPolyTestClasses();
        assertEquals(1, dataClasses.size());
        final PolyTestClass polyTestClass = dataClasses.get(0);
        SqlIntegrationTestSingleton.setStaticPolyTestClass(polyTestClass);

        // Must set state to 1 to prevent re-initialisation
        getSingletonInstance().setState(1);
    }

    public void testPolymorphicLoad() {
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();

        List<PolyBaseClass> polyBaseClasses = polyTestClass.getPolyBaseClasses();
        // TODO Polymorphism test temporary disabled
        // assertEquals(3, polyBaseClasses.size());

    }

    public void testInterfaceLoad() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();
        // assertEquals(polyTestClass.getClass(), polyTestClass.getPolyTestInterface().getClass());

        PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        factory.resolve(loaded);
        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    public void testLoadSelfReferencingCollection() {
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();

        PolySelfRefClass polySelfRefParent = polyTestClass.getPolySelfRefClass();
        List<PolySelfRefClass> list = polySelfRefParent.getPolySelfRefClasses();
        assertEquals(2, list.size());

        PolySelfRefClass polySelfRefChild1 = null;
        for (PolySelfRefClass polySelfRefClass : list) {
            if (polySelfRefClass.getString().equals(CHILD_1)) {
                polySelfRefChild1 = polySelfRefClass;
            }
        }
        assertNotNull(polySelfRefChild1);

        assertEquals(CHILD_1, polySelfRefChild1.title());

        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        List<PolySelfRefClass> list2 = polySelfRefChild1.getPolySelfRefClasses();
        factory.resolve(polySelfRefChild1);
        list2 = polySelfRefChild1.getPolySelfRefClasses();
        assertEquals(1, list2.size());
    }

    public void testInterfaceLoadCollection() {
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();
        PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    public void testInterfaceEditSave() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        polyIntImpB = factory.newPolyInterfaceImplB();
        polyIntImpB.setString("Impl B String");
        polyIntImpB.setSpecial("special");

        factory.save(polyIntImpB);

        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();
        polyTestClass.setPolyInterfaceType(polyIntImpB);

        getSingletonInstance().setState(0); // ready for testInterfaceEditSave
    }

    public void testInterfaceEditLoad() {
        testLoad(); // reload data

        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();
        PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        assertEquals(polyIntImpB.getString(), loaded.getString());
    }

    public void testAllInterfacesInstancesLoaded() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        List<PolyInterface> list = factory.allPolyInterfaces();
        assertEquals(2, list.size());
    }

    public void testInterfacesLoadedByQuery() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        // PolyInterface query = polyIntImpA;

        PolyInterfaceEx query = new PolyInterfaceEx();
        query.setString(IMPL_A_STRING);

        List<PolyInterface> list = factory.queryPolyInterfaces(query);
        assertEquals(1, list.size());
    }

    public void testInterfacesLoadedByQuerySpecial() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();

        PolyInterfaceEx query = new PolyInterfaceEx();

        List<PolyInterface> list = factory.queryPolyInterfaces(query);
        assertEquals(2, list.size());
    }

    // Last "test" - Set the Singleton state to 0 to invoke a clean shutdown.
    public void testZSetStateZero() {
        getSingletonInstance().setState(0);
    }

}
