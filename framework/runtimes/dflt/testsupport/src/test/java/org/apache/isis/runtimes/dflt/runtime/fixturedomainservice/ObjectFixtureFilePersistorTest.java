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

package org.apache.isis.runtimes.dflt.runtime.fixturedomainservice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.core.testsupport.testdomain.Movie;
import org.apache.isis.core.testsupport.testdomain.Person;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.fixturedomainservice.FixtureException;
import org.apache.isis.runtimes.dflt.runtime.fixturedomainservice.ObjectFixtureFilePersistor;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.testsupport.TestSystem;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.runtimes.dflt.testsupport.TestSystemWithObjectStoreTestAbstract;
import org.apache.isis.runtimes.embedded.PersistenceState;

public class ObjectFixtureFilePersistorTest extends TestSystemWithObjectStoreTestAbstract {

    private static final String PACKAGE_BASE = "org.apache.isis.core.testsupport";
    
    @Override
    protected PersistenceMechanismInstaller createPersistenceMechanismInstaller() {
        return new InMemoryPersistenceMechanismInstaller();
    }

    private ObjectFixtureFilePersistor persistor;
    private Person person;

    @Mock
    private AuthenticationSession mockAuthenticationSession;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);

        Locale.setDefault(Locale.UK);

        persistor = new ObjectFixtureFilePersistor();

        person = new Person();
        person.setName("Fred Smith");
        person.setDate(new Date(110, 2, 8, 0, 0));
        
        context.checking(new Expectations() {

            {
                allowing(mockEmbeddedContext).instantiate(Person.class);
                will(returnValue(new Person()));
                
                allowing(mockEmbeddedContext).getPersistenceState(with(any(String.class)));
                will(returnValue(PersistenceState.STANDALONE));
                
                allowing(mockEmbeddedContext).getAuthenticationSession();
                will(returnValue(mockAuthenticationSession));
            }
        });
    }

    @Test
    public void loadInstance() throws Exception {
        
        final StringReader reader = new StringReader(PACKAGE_BASE + ".testdomain.Person#1\n  name: Fred Smith\n  date: 08-Mar-2010 00:00");
        final Set<Object> objects = persistor.loadData(reader);

        Assert.assertEquals(1, objects.size());
        final Object object = objects.toArray()[0];
        assertThat(object instanceof Person, is(true));
        Assert.assertEquals("Fred Smith", ((Person) object).getName());
        Assert.assertEquals(new Date(110, 2, 8, 0, 0), ((Person) object).getDate());
    }

    @Test
    public void invalidFieldLine() throws Exception {
        try {
            final StringReader reader = new StringReader(PACKAGE_BASE + ".testdomain.Person#1\n  name Fred Smith");
            persistor.loadData(reader);
            Assert.fail();
        } catch (final FixtureException e) {
            Assert.assertEquals("failed to load data at line 2", e.getMessage());
            Assert.assertEquals("no colon (:) in: name Fred Smith", e.getCause().getMessage());
        }
    }

    @Test
    public void oldFieldNameSkipped() throws Exception {
        final StringReader reader = new StringReader(PACKAGE_BASE + ".testdomain.Person#1\n  xname: Fred Smith");
        final Set<Object> objects = persistor.loadData(reader);
        final Object object = objects.toArray()[0];
        Assert.assertNull(((Person) object).getName());

    }

    @Test
    public void saveNoObjects() throws Exception {
        // Person person = new Person();
        final Set<Object> objects = new HashSet<Object>();
        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void saveOneObject() throws Exception {
        final Set<Object> objects = new HashSet<Object>();
        objects.add(person);

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        final String string1 = PACKAGE_BASE + ".testdomain.Person#2\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        final String string2 = PACKAGE_BASE + ".testdomain.Person#2\r\n  date: 08-Mar-2010 00:00\r\n  name: Fred Smith\r\n";
        // *nix vs Windows?
        if ((out.toString().compareTo(string1) != 0) && (out.toString().compareTo(string2) != 0)) {
            Assert.assertEquals(string1, out.toString());
        }
    }

    @Test
    public void saveTwoObjects() throws Exception {
        final Set<Object> objects = new AbstractSet<Object>() {
            @Override
            public Iterator<Object> iterator() {
                final List<Object> list = new ArrayList<Object>();
                final Person person = new Person();
                person.setName("Fred Smith");
                person.setDate(new Date(110, 2, 8, 0, 0));
                list.add(person);
                list.add(new Person());
                return list.iterator();
            }

            @Override
            public int size() {
                return 2;
            }
        };

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);

        // *nix vs Windows?
        final String string1 = PACKAGE_BASE + ".testdomain.Person#2\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n" + PACKAGE_BASE + ".testdomain.Person#3\n  date: \n  name: \n";
        final String string2 = PACKAGE_BASE + ".testdomain.Person#2\r\n  date: 08-Mar-2010 00:00\r\n  name: Fred Smith\r\n" + PACKAGE_BASE + ".testdomain.Person#3\r\n  date: \r\n  name: \r\n";
        if ((out.toString().compareTo(string1) != 0) && (out.toString().compareTo(string2) != 0)) {
            Assert.assertEquals(string1, out.toString());
        }
    }

    @Test
    public void saveAssociatedObjects() throws Exception {
        final Set<Object> objects = new AbstractSet<Object>() {

            @Override
            public Iterator<Object> iterator() {
                final List<Object> list = new ArrayList<Object>();
                final Movie movie = new Movie();
                movie.setName("The Blockbuster");
                movie.setDirector(person);
                list.add(movie);
                list.add(person);
                return list.iterator();
            }

            @Override
            public int size() {
                return 2;
            }
        };

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        final String string1 = PACKAGE_BASE + ".testdomain.Movie#2\n  director: " + PACKAGE_BASE + ".testdomain.Person#3\n  name: The Blockbuster\n  roles: \n" + PACKAGE_BASE + ".testdomain.Person#3\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        final String string2 = PACKAGE_BASE + ".testdomain.Movie#2\r\n  director: " + PACKAGE_BASE + ".testdomain.Person#3\r\n  name: The Blockbuster\r\n  roles: \r\n" + PACKAGE_BASE + ".testdomain.Person#3\r\n  date: 08-Mar-2010 00:00\r\n  name: Fred Smith\r\n";
        if ((out.toString().compareTo(string1) != 0) && (out.toString().compareTo(string2) != 0)) {
            Assert.assertEquals(string1, out.toString());
        }
    }

}
