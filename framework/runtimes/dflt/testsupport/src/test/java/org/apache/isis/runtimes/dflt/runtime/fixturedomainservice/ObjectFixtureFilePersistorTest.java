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
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import com.google.common.collect.Sets;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.eg.ExamplePojoWithCollections;
import org.apache.isis.tck.dom.eg.ExamplePojoWithReferences;
import org.apache.isis.tck.dom.eg.ExamplePojoWithValues;

public class ObjectFixtureFilePersistorTest {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();
    
    private ObjectFixtureFilePersistor persistor;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);

        Locale.setDefault(Locale.UK);

        persistor = new ObjectFixtureFilePersistor();

        iswf.fixtures.epv1.setName("Fred Smith");
        iswf.fixtures.epv1.setDate(new Date(110, 2, 8, 0, 0));

        iswf.fixtures.epv2.setName("Joe Bloggs");
        iswf.fixtures.epv2.setDate(new Date(111, 3, 9, 1, 10));
    }
    

    @SuppressWarnings("deprecation")
    @Test
    public void loadInstance() throws Exception {
        
        final StringReader reader = new StringReader(ExamplePojoWithValues.class.getName() + "#1\n  name: Fred Smith\n  date: 08-Mar-2010 00:00");
        final Set<Object> objects = persistor.loadData(reader);

        Assert.assertEquals(1, objects.size());
        final Object object = objects.toArray()[0];
        assertThat(object instanceof ExamplePojoWithValues, is(true));
        final ExamplePojoWithValues epv = (ExamplePojoWithValues) object;
        Assert.assertEquals("Fred Smith", epv.getName());
        Assert.assertEquals(new Date(110, 2, 8, 0, 0), epv.getDate());
    }

    @Test
    public void invalidFieldLine() throws Exception {
        try {
            final StringReader reader = new StringReader(ExamplePojoWithValues.class.getName() + "#1\n  name Fred Smith");
            persistor.loadData(reader);
            Assert.fail();
        } catch (final FixtureException e) {
            Assert.assertEquals("failed to load data at line 2", e.getMessage());
            Assert.assertEquals("no colon (:) in: name Fred Smith", e.getCause().getMessage());
        }
    }

    @Test
    public void oldFieldNameSkipped() throws Exception {
        final StringReader reader = new StringReader(ExamplePojoWithValues.class.getName() + "#1\n  xname: Fred Smith");
        final Set<Object> objects = persistor.loadData(reader);
        final Object object = objects.toArray()[0];
        Assert.assertNull(((ExamplePojoWithValues) object).getName());

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
        final Set<Object> objects = Sets.newLinkedHashSet();
        objects.add(iswf.fixtures.epv1);

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        final String actual = out.toString().replaceAll("\r\n", "\n");
        
        final String expected = ExamplePojoWithValues.class.getName() + "#2\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        
        assertThat(actual, IsisMatchers.startsWith(expected));
    }

    @Test
    public void saveTwoObjects() throws Exception {
        final Set<Object> objects = Sets.newLinkedHashSet();
        objects.add(iswf.fixtures.epv1);
        objects.add(iswf.fixtures.epv3);

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        final String actual = out.toString().replaceAll("\r\n", "\n");

        final String expected1 = ExamplePojoWithValues.class.getName() + "#2\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        final String expected2 = ExamplePojoWithValues.class.getName() + "#3\n  date: \n  name: \n";
        assertThat(actual, IsisMatchers.contains(expected1));
        assertThat(actual, IsisMatchers.contains(expected2));
    }

    @Test
    public void saveReferencedObject() throws Exception {

        final Set<Object> objects = Sets.newLinkedHashSet();
        
        iswf.fixtures.epr1.setReference(iswf.fixtures.epv1);
        objects.add(iswf.fixtures.epr1);
        objects.add(iswf.fixtures.epv1);

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        final String actual = out.toString().replaceAll("\r\n", "\n");

        final String expected1 = ExamplePojoWithReferences.class.getName() + "#2\n  aggregatedReference: \n  reference: " + ExamplePojoWithValues.class.getName() + "#3";
        final String expected2 = ExamplePojoWithValues.class.getName() + "#3\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        assertThat(actual, IsisMatchers.contains(expected1));
        assertThat(actual, IsisMatchers.contains(expected2));
    }

    
    @Test
    public void saveObjectAndAssociatedCollection() throws Exception {

        final Set<Object> objects = Sets.newLinkedHashSet();
        
        iswf.fixtures.epc1.getHomogeneousCollection().add(iswf.fixtures.epv1);
        iswf.fixtures.epc1.getHomogeneousCollection().add(iswf.fixtures.epv2);
        objects.add(iswf.fixtures.epc1);

        objects.add(iswf.fixtures.epv1);
        objects.add(iswf.fixtures.epv2);

        final StringWriter out = new StringWriter();
        persistor.save(objects, out);
        final String actual = out.toString().replaceAll("\r\n", "\n");
        
        final String expected1 = ExamplePojoWithCollections.class.getName() + "#2\n  heterogeneousCollection: \n  homogeneousCollection: " + ExamplePojoWithValues.class.getName() + "#3 " + ExamplePojoWithValues.class.getName() + "#4 " + "\n";
        final String expected2 = ExamplePojoWithValues.class.getName() + "#3\n  date: 08-Mar-2010 00:00\n  name: Fred Smith\n";
        final String expected3 = ExamplePojoWithValues.class.getName() + "#4\n  date: 09-Apr-2011 01:10\n  name: Joe Bloggs\n";
        assertThat(actual.replaceAll("\n", "###"), IsisMatchers.contains(expected1.replaceAll("\n", "###")));
        assertThat(actual, IsisMatchers.contains(expected2));
        assertThat(actual, IsisMatchers.contains(expected3));
    }



}

