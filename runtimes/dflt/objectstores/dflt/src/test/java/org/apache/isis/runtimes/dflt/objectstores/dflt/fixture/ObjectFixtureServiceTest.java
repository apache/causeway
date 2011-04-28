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

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.runtimes.dflt.runtime.fixturedomainservice.ObjectFixtureService;
import org.apache.isis.core.testsupport.testdomain.Movie;
import org.apache.isis.core.testsupport.testdomain.Person;
import org.apache.isis.core.testsupport.testdomain.Role;
import org.apache.isis.runtimes.dflt.objectstores.dflt.testsystem.TestProxySystemII;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * TODO: remove dependency on {@link TestProxySystemII} and move back to runtime.
 */
public class ObjectFixtureServiceTest {

    private ObjectFixtureService service;
    private TestProxySystemII system;

    @Before
    public void setup() {
        Logger.getRootLogger().setLevel(Level.OFF);

        Locale.setDefault(Locale.UK);
        system = new TestProxySystemII();
        system.init();

        service = new ObjectFixtureService();

        deleteFixtureData();
    }

    @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private boolean deleteFixtureData() {
        return new File("fixture-data").delete();
    }

    @Test
    public void loadNothingIfNoFileExists() throws Exception {
        service.loadFile();

        Set<Object> objects = service.allSavedObjects();
        Assert.assertEquals(0, objects.size());
    }

    @Test
    public void loadInstanceFromFile() throws Exception {
        String key = ConfigurationConstants.ROOT + "exploration-objects.file";
        String fileName = "test.data";
        system.addToConfiguration(key, fileName);
        service.loadFile();

        Set<Object> objects = service.allSavedObjects();
        Assert.assertEquals(1, objects.size());
        Object object = objects.toArray()[0];
        assertThat(object instanceof Person, is(true));
        Assert.assertEquals("Fred Smith", ((Person) object).getName());
        Assert.assertEquals(new Date(110, 2, 8, 13, 32), ((Person) object).getDate());
    }

    @Test
    public void saveObjectAddedToList() throws Exception {
        Person person = new Person();
        person.setName("Fred Smith");
        person.setDate(new Date(110, 2, 8, 13, 32));
        Movie movie = new Movie();
        movie.setDirector(person);
        movie.addToRoles(new Role());
        movie.addToRoles(new Role());
        service.save(movie);

        Set<Object> savedObjects = service.allSavedObjects();
        Assert.assertEquals(4, savedObjects.size());
    }

}
