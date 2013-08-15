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

package org.apache.isis.core.runtime.fixturedomainservice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.refs.SimpleEntity;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class ObjectFixtureServiceTest_loadFile {

    private static IsisConfiguration configuration() {
        IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        configuration.add(ConfigurationConstants.ROOT + "exploration-objects.file", "test.data");
        return configuration;
    }

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().with(configuration()).build();
    
    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private ObjectFixtureService service;

    
    @Before
    public void setup() {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        Locale.setDefault(Locale.UK);

        service = new ObjectFixtureService();
    }


    @Test
    public void loadFile() throws Exception {

        // when
        service.loadFile();

        // then
        final Set<Object> objects = service.allSavedObjects();
        Assert.assertEquals(1, objects.size());
        final Object object = objects.toArray()[0];
        assertThat(object instanceof SimpleEntity, is(true));
        Assert.assertEquals("Fred Smith", ((SimpleEntity) object).getName());
        Assert.assertEquals(new Date(110, 2, 8, 13, 32), ((SimpleEntity) object).getDate());
    }

}
