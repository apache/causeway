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
package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.List;
import java.util.Properties;

import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestCommonBase;
import org.apache.isis.runtimes.dflt.objectstores.sql.singleton.SqlIntegrationTestSingleton;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.SqlDataClassFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.EmptyInterface;
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

    private static final String IMPL_B_STRING = "Impl B String";

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

    public class EmptyInterfaceEx implements EmptyInterface {
        // {{ Special
        private String special;

        public String getSpecial() {
            return special;
        }

        public void setSpecial(final String special) {
            this.special = special;
        }

        // }}
    }

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
        // SqlIntegrationTestSingleton.drop("%");
    }

    public void testCreate() throws Exception {
        SqlIntegrationTestSingleton.drop("ISIS_POLYTESTCLASS");
        SqlIntegrationTestSingleton.drop("ISIS_POLYBASECLASS");
        SqlIntegrationTestSingleton.drop("ISIS_POLYINTERFACE");
        SqlIntegrationTestSingleton.drop("ISIS_POLYSUBCLASS");
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
        polyTestClass.getPolyInterfaces().add(polyIntImpA);

        // setup the polyTestClass
        PolySubClassOne polySubClassOne = factory.newPolySubClassOne();
        polySubClassOne.setStringBase("PolySubClassOne 1");
        polySubClassOne.setStringClassOne("Class 1");

        PolySubClassTwo polySubClassTwo = factory.newPolySubClassTwo();
        polySubClassTwo.setStringBase("PolySubClassTwo 1");
        polySubClassTwo.setStringClassTwo("Class 2");

        PolySubClassThree polySubClassThree = factory.newPolySubClassThree();
        polySubClassThree.setStringBase("PolySubClassThree 1");
        polySubClassThree.setStringClassThree("Another String");
        polySubClassThree.setStringClassTwo("Class 3");

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
        assertEquals(3, polyBaseClasses.size());

        PolyBaseClass polyClassBase = polyBaseClasses.get(0);
        assertTrue(polyClassBase instanceof PolySubClassOne);
        assertEquals("PolySubClassOne 1", polyClassBase.getStringBase());
        final PolySubClassOne polySubClassOne = (PolySubClassOne) polyClassBase;
        assertEquals("Class 1", polySubClassOne.getStringClassOne());

        polyClassBase = polyBaseClasses.get(1);
        assertTrue(polyClassBase instanceof PolySubClassTwo);
        final PolySubClassTwo polySubClassTwo = (PolySubClassTwo) polyClassBase;
        assertEquals("Class 2", polySubClassTwo.getStringClassTwo());

        polyClassBase = polyBaseClasses.get(2);
        assertTrue(polyClassBase instanceof PolySubClassThree);
        final PolySubClassThree polySubClassThree = (PolySubClassThree) polyClassBase;
        assertEquals("Class 3", polySubClassThree.getStringClassTwo());
        assertEquals("Another String", polySubClassThree.getStringClassThree());
    }

    public void testInterfaceLoad() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();

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

    public void testInterfaceLoadProperty() {
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();
        PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    public void testInterfaceLoadCollection() {
        final PolyTestClass polyTestClass = SqlIntegrationTestSingleton.getStaticPolyTestClass();
        List<PolyInterface> list = polyTestClass.getPolyInterfaces();

        assertEquals(1, list.size());
        PolyInterface loaded = list.get(0);

        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    public void testInterfaceEditSave() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        polyIntImpB = factory.newPolyInterfaceImplB();
        polyIntImpB.setString(IMPL_B_STRING);
        polyIntImpB.setSpecial("special");
        polyIntImpB.setInteger(1);

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

    public void testFindByMatchPartialEntity() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final EmptyInterface match = new EmptyInterfaceEx();
        final List<EmptyInterface> matches = factory.allEmptyInterfacesThatMatch(match);
        assertEquals(1, matches.size());

        EmptyInterface emptyInterface = matches.get(0);
        PolyInterfaceImplB imp = (PolyInterfaceImplB) emptyInterface;
        assertEquals(IMPL_B_STRING, imp.getString());
    }

    public void testCannotFindByMatchWithWrongValue() {
        final SqlDataClassFactory factory = SqlIntegrationTestSingleton.getSqlDataClassFactory();
        final PolyInterfaceImplB match = new PolyInterfaceImplB();
        match.setInteger(0);
        final List<EmptyInterface> matches = factory.allEmptyInterfacesThatMatch(match);
        assertEquals(0, matches.size());
    }

    // Last "test" - Set the Singleton state to 0 to invoke a clean shutdown.
    public void testZSetStateZero() {
        getSingletonInstance().setState(0);
    }

}
