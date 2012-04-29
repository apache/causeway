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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.junit.Test;

import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestCommonBase;
import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestFixtures;
import org.apache.isis.runtimes.dflt.objectstores.sql.common.SqlIntegrationTestFixtures.State;
import org.apache.isis.tck.dom.sqlos.poly.EmptyInterface;
import org.apache.isis.tck.dom.sqlos.poly.EmptyInterfaceEx;
import org.apache.isis.tck.dom.sqlos.poly.PolyBaseClass;
import org.apache.isis.tck.dom.sqlos.poly.PolyInterface;
import org.apache.isis.tck.dom.sqlos.poly.PolyInterfaceEx;
import org.apache.isis.tck.dom.sqlos.poly.PolyInterfaceImplA;
import org.apache.isis.tck.dom.sqlos.poly.PolyInterfaceImplB;
import org.apache.isis.tck.dom.sqlos.poly.PolySelfRefClass;
import org.apache.isis.tck.dom.sqlos.poly.PolySubClassOne;
import org.apache.isis.tck.dom.sqlos.poly.PolySubClassThree;
import org.apache.isis.tck.dom.sqlos.poly.PolySubClassTwo;
import org.apache.isis.tck.dom.sqlos.poly.PolyTestClass;

public class PolymorphismTest extends SqlIntegrationTestCommonBase {

    private static final String IMPL_B_STRING = "Impl B String";

    private static final String IMPL_A_STRING = "Impl A String";
    private static final String CHILD_1 = "Child 1";
    
    private static PolyInterfaceImplA polyIntImpA;
    private static PolyInterfaceImplB polyIntImpB;

    @Override
    public String getPropertiesFilename() {
        return "hsql-poly.properties";
    }

    @Override
    public void resetPersistenceStoreDirectlyIfRequired() {
        
        // Delete all HSQL Database files.
        deleteFiles("hsql-db", new FilenameFilter() {

            @Override
            public boolean accept(final File arg0, final String arg1) {
                return arg1.endsWith(".xml");
            }
        });
    }


    @Override
    public String getSqlTeardownString() {
        return "SHUTDOWN;";
    }


    @Test
    public void testAll() throws Exception {
        setupFixtures();
        
        setUpFactory();
        
        create();
        load();
        
        setUpFactory();
        
        polymorphicLoad();
        interfaceLoad();
        loadSelfReferencingCollection();
        interfaceLoadProperty();
        interfaceLoadCollection();
        interfaceEditSave();
        interfaceEditLoad();
        allInterfacesInstancesLoaded();
        interfacesLoadedByQuery();
        interfacesLoadedByQuerySpecial();
        findByMatchPartialEntity();
        cannotFindByMatchWithWrongValue();
        reinitializeFixtures();
    }

    private void setupFixtures() {
        resetPersistenceStoreDirectlyIfRequired();
        setFixtureInitializationState(State.INITIALIZE);
        
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYTESTCLASS");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYBASECLASS");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYINTERFACE");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYSUBCLASS");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYSUBCLASSONE");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYSUBCLASSTWO");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYSUBCLASSTHREE");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYINTERFACEIMPLA");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYINTERFACEIMPLB");
        getSqlIntegrationTestFixtures().dropTable("ISIS_POLYSELFREFCLASS");
    }

    private void create() throws Exception {

        final PolyTestClass polyTestClass = factory.newPolyTestClass();
        polyTestClass.setString("polyTestClassString");

        // Setup self-referencing collection
        final PolySelfRefClass polySelfRefClassParent = factory.newPolySelfRefClass();
        polySelfRefClassParent.setString("Parent");

        final PolySelfRefClass polySelfRefClassChild1 = factory.newPolySelfRefClass();
        polySelfRefClassChild1.setString(CHILD_1);
        polySelfRefClassParent.addToPolySelfRefClasses(polySelfRefClassChild1);

        final PolySelfRefClass polySelfRefClassChild2 = factory.newPolySelfRefClass();
        polySelfRefClassChild2.setString("Child 2");
        polySelfRefClassParent.addToPolySelfRefClasses(polySelfRefClassChild2);
        factory.save(polySelfRefClassChild2);

        final PolySelfRefClass polySelfRefClassChild3 = factory.newPolySelfRefClass();
        polySelfRefClassChild3.setString("Child 1 of Child 1");
        polySelfRefClassChild1.addToPolySelfRefClasses(polySelfRefClassChild3);

        factory.save(polySelfRefClassChild3);
        factory.save(polySelfRefClassChild1);

        factory.save(polySelfRefClassParent);
        polyTestClass.setPolySelfRefClass(polySelfRefClassParent);

        // polyTestClass.setPolyTestInterface(polyTestClass);

        polyIntImpA = factory.newPolyInterfaceImplA();
        polyIntImpA.setString(IMPL_A_STRING);
        polyIntImpA.setSpecial("special");
        factory.save(polyIntImpA);

        polyTestClass.setPolyInterfaceType(polyIntImpA);
        polyTestClass.getPolyInterfaces().add(polyIntImpA);

        // setup the polyTestClass
        final PolySubClassOne polySubClassOne = factory.newPolySubClassOne();
        polySubClassOne.setStringBase("PolySubClassOne 1");
        polySubClassOne.setStringClassOne("Class 1");

        final PolySubClassTwo polySubClassTwo = factory.newPolySubClassTwo();
        polySubClassTwo.setStringBase("PolySubClassTwo 1");
        polySubClassTwo.setStringClassTwo("Class 2");

        final PolySubClassThree polySubClassThree = factory.newPolySubClassThree();
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

        setFixtureInitializationState(State.DONT_INITIALIZE, "in-memory");
    }

    private void load() {
        final List<PolyTestClass> dataClasses = factory.allPolyTestClasses();
        assertEquals(1, dataClasses.size());
        final PolyTestClass polyTestClass = dataClasses.get(0);
        
        getSqlIntegrationTestFixtures().setPolyTestClass(polyTestClass);

        setFixtureInitializationState(State.DONT_INITIALIZE);
    }

    private void polymorphicLoad() {
        final List<PolyBaseClass> polyBaseClasses = polyTestClass.getPolyBaseClasses();
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

    private void interfaceLoad() {
        final PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        factory.resolve(loaded);
        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    private void loadSelfReferencingCollection() {
        final PolySelfRefClass polySelfRefParent = polyTestClass.getPolySelfRefClass();
        final List<PolySelfRefClass> list = polySelfRefParent.getPolySelfRefClasses();
        assertEquals(2, list.size());

        PolySelfRefClass polySelfRefChild1 = null;
        for (final PolySelfRefClass polySelfRefClass : list) {
            if (polySelfRefClass.getString().equals(CHILD_1)) {
                polySelfRefChild1 = polySelfRefClass;
            }
        }
        assertNotNull(polySelfRefChild1);

        assertEquals(CHILD_1, polySelfRefChild1.title());

        List<PolySelfRefClass> list2 = polySelfRefChild1.getPolySelfRefClasses();
        factory.resolve(polySelfRefChild1);
        list2 = polySelfRefChild1.getPolySelfRefClasses();
        assertEquals(1, list2.size());
    }

    private void interfaceLoadProperty() {
        final PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    private void interfaceLoadCollection() {
        final List<PolyInterface> list = polyTestClass.getPolyInterfaces();

        assertEquals(1, list.size());
        final PolyInterface loaded = list.get(0);

        assertEquals(polyIntImpA.getString(), loaded.getString());
    }

    private void interfaceEditSave() {
        polyIntImpB = factory.newPolyInterfaceImplB();
        polyIntImpB.setString(IMPL_B_STRING);
        polyIntImpB.setSpecial("special");
        polyIntImpB.setInteger(1);

        factory.save(polyIntImpB);

        polyTestClass.setPolyInterfaceType(polyIntImpB);

        setFixtureInitializationState(State.INITIALIZE);
    }

    private void interfaceEditLoad() {
        load(); // reload data

        final PolyInterface loaded = polyTestClass.getPolyInterfaceType();
        assertEquals(polyIntImpB.getString(), loaded.getString());
    }

    private void allInterfacesInstancesLoaded() {
        final List<PolyInterface> list = factory.allPolyInterfaces();
        assertEquals(2, list.size());
    }

    private void interfacesLoadedByQuery() {
        // PolyInterface query = polyIntImpA;

        final PolyInterfaceEx query = new PolyInterfaceEx();
        query.setString(IMPL_A_STRING);

        final List<PolyInterface> list = factory.queryPolyInterfaces(query);
        assertEquals(1, list.size());
    }

    private void interfacesLoadedByQuerySpecial() {

        final PolyInterfaceEx query = new PolyInterfaceEx();

        final List<PolyInterface> list = factory.queryPolyInterfaces(query);
        assertEquals(2, list.size());
    }

    private void findByMatchPartialEntity() {
        final EmptyInterface match = new EmptyInterfaceEx();
        final List<EmptyInterface> matches = factory.allEmptyInterfacesThatMatch(match);
        assertEquals(1, matches.size());

        final EmptyInterface emptyInterface = matches.get(0);
        final PolyInterfaceImplB imp = (PolyInterfaceImplB) emptyInterface;
        assertEquals(IMPL_B_STRING, imp.getString());
    }

    private void cannotFindByMatchWithWrongValue() {
        final PolyInterfaceImplB match = new PolyInterfaceImplB();
        match.setInteger(0);
        final List<EmptyInterface> matches = factory.allEmptyInterfacesThatMatch(match);
        assertEquals(0, matches.size());
    }

    private void reinitializeFixtures() {
        setFixtureInitializationState(State.INITIALIZE);
        SqlIntegrationTestFixtures.recreate();
    }

}
