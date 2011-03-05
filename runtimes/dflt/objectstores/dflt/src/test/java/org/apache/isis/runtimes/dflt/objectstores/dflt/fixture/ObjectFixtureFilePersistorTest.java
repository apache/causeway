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

package org.apache.isis.runtimes.dflt.objectstores.dflt.fixture;

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

import org.apache.isis.runtimes.dflt.runtime.fixture.ObjectFixtureFilePersistor;
import org.apache.isis.runtimes.dflt.runtime.fixturesinstaller.FixtureException;
import org.apache.isis.core.testsupport.testdomain.Movie;
import org.apache.isis.core.testsupport.testdomain.Person;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO: remove dependency on {@link TestProxySystemII} and move back to runtime.
 */
public class ObjectFixtureFilePersistorTest {

    private static final String PACKAGE_BASE = "org.apache.isis.core.testsupport";
    private ObjectFixtureFilePersistor persistor;
    private TestProxySystemII system;
    private Person person;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);

        Locale.setDefault(Locale.UK);
        system = new TestProxySystemII();
        system.init();

        persistor = new ObjectFixtureFilePersistor();

        person = new Person();
        person.setName("Fred Smith");
        person.setDate(new Date(110, 2, 8, 0, 0));

    }

    @Test
    public void loadInstance() throws Exception {
        StringReader reader =
            new StringReader(PACKAGE_BASE + ".testdomain.Person#1\n  name: Fred Smith\n  date: 08-Mar-2010 00:00");
        Set<Object> objects = persistor.loadData(reader);

        Assert.assertEquals(1, objects.size());
        Object object = objects.toArray()[0];
        assertThat(object instanceof Person, is(true));
        Assert.assertEquals("Fred Smith", ((Person) object).getName());
        Assert.assertEquals(new Date(110, 2, 8, 0, 0), ((Person) object).getDate());
    }

    @Test
    public void invalidFieldLine() throws Exception {
        try {
            StringReader reader = new StringReader(PACKAGE_BASE + ".testdomain.Person#1\n  name Fred Smith");
            persistor.loadData(reader);
            Assert.fail();
        } catch (FixtureException e) {
            Assert.assertEquals("failed to load data at line 2", e.getMessage());
            Assert.assertEquals("no colon (:) in: name Fred Smith", e.getCause().getMessage());
        }
    }

    @Test
    public void oldFieldNameSkipped() throws Exception {
        StringReader reader = new StringReader(PACKAGE_BASE + ".testdomain.Person#1\n  xname: Fred Smith");
        Set<Object> objects = persistor.loadData(reader);
        Object object = objects.toArray()[0];
        Assert.assertNull(((Person) object).getName());

    }

    @Test
    public void saveNoObjects() throws Exception {
        // Person person = new Person();
        Set<Object> objects = new HashSet<Object>();
        StringWriter out = new StringWriter();
        persistor.save(objects, out);
        Assert.assertEquals("", out.toString());
    }

    @Test
    public void saveOneObject() throws Exception {
        Set<Object> objects = new HashSet<Object>();
        objects.add(person);

        StringWriter out = new StringWriter();
        persistor.save(objects, out);
        String string1 = PACKAGE_BASE + ".testdomain.Person#2\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        String string2 = PACKAGE_BASE + ".testdomain.Person#2\r\n  date: 08-Mar-2010 00:00\r\n  name: Fred Smith\r\n";
        // *nix vs Windows?
        if ((out.toString().compareTo(string1) != 0) && (out.toString().compareTo(string2) != 0)) {
            Assert.assertEquals(string1, out.toString());
        }
    }

    @Test
    public void saveTwoObjects() throws Exception {
        Set<Object> objects = new AbstractSet<Object>() {
            @Override
            public Iterator<Object> iterator() {
                List<Object> list = new ArrayList<Object>();
                Person person = new Person();
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

        StringWriter out = new StringWriter();
        persistor.save(objects, out);

        // *nix vs Windows?
        String string1 =
            PACKAGE_BASE + ".testdomain.Person#2\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n" + PACKAGE_BASE
                + ".testdomain.Person#3\n  date: \n  name: \n";
        String string2 =
            PACKAGE_BASE + ".testdomain.Person#2\r\n  date: 08-Mar-2010 00:00\r\n  name: Fred Smith\r\n" + PACKAGE_BASE
                + ".testdomain.Person#3\r\n  date: \r\n  name: \r\n";
        if ((out.toString().compareTo(string1) != 0) && (out.toString().compareTo(string2) != 0)) {
            Assert.assertEquals(string1, out.toString());
        }
    }

    @Test
    public void saveAssociatedObjects() throws Exception {
        Set<Object> objects = new AbstractSet<Object>() {

            @Override
            public Iterator<Object> iterator() {
                List<Object> list = new ArrayList<Object>();
                Movie movie = new Movie();
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

        StringWriter out = new StringWriter();
        persistor.save(objects, out);
        String string1 =
            PACKAGE_BASE + ".testdomain.Movie#2\n  director: " + PACKAGE_BASE
                + ".testdomain.Person#3\n  name: The Blockbuster\n  roles: \n" + PACKAGE_BASE
                + ".testdomain.Person#3\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        String string2 =
            PACKAGE_BASE + ".testdomain.Movie#2\r\n  director: " + PACKAGE_BASE
                + ".testdomain.Person#3\r\n  name: The Blockbuster\r\n  roles: \r\n" + PACKAGE_BASE
                + ".testdomain.Person#3\r\n  date: 08-Mar-2010 00:00\r\n  name: Fred Smith\r\n";
        if ((out.toString().compareTo(string1) != 0) && (out.toString().compareTo(string2) != 0)) {
            Assert.assertEquals(string1, out.toString());
        }
    }

}
