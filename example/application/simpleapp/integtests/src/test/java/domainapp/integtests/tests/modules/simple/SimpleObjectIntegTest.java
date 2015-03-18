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
package domainapp.integtests.tests.modules.simple;

import domainapp.dom.modules.simple.SimpleObject;
import domainapp.dom.modules.simple.SimpleObjects;
import domainapp.fixture.scenarios.RecreateSimpleObjects;
import domainapp.integtests.tests.SimpleAppIntegTest;

import javax.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.InvalidException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class SimpleObjectIntegTest extends SimpleAppIntegTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    FixtureScripts fixtureScripts;
    @Inject
    SimpleObjects simpleObjects;
    @Inject
    TranslationService translationService;

    RecreateSimpleObjects fs;
    SimpleObject simpleObjectPojo;
    SimpleObject simpleObjectWrapped;

    @Before
    public void setUp() throws Exception {
        // given
        fs = new RecreateSimpleObjects().setNumber(1);
        fixtureScripts.runFixtureScript(fs, null);

        simpleObjectPojo = fs.getSimpleObjects().get(0);

        assertThat(simpleObjectPojo, is(not(nullValue())));
        simpleObjectWrapped = wrap(simpleObjectPojo);
    }

    public static class Name extends SimpleObjectIntegTest {

        @Test
        public void accessible() throws Exception {

            // when
            final String name = simpleObjectWrapped.getName();
            //
            // then
            assertThat(name, is(fs.NAMES.get(0)));
        }

        @Test
        public void cannotBeUpdatedDirectly() throws Exception {

            // expect
            expectedExceptions.expect(DisabledException.class);

            // when
            simpleObjectWrapped.setName("new name");
        }
    }

    public static class UpdateName extends SimpleObjectIntegTest {

        @Test
        public void happyCase() throws Exception {

            // when
            simpleObjectWrapped.updateName("new name");

            // then
            assertThat(simpleObjectWrapped.getName(), is("new name"));
        }

        @Test
        public void failsValidation() throws Exception {

            // expect
            expectedExceptions.expect(InvalidException.class);
            expectedExceptions.expectMessage("Exclamation mark is not allowed");

            // when
            simpleObjectWrapped.updateName("new name!");
        }
    }


    public static class Title extends SimpleObjectIntegTest {

        @Test
        public void interpolatesName() throws Exception {

            // given
            final String name = simpleObjectWrapped.getName();

            // when
            final String title = container().titleOf(simpleObjectWrapped);

            // then
            assertThat(title, is("Object: " + name));
        }
    }
}