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
package domainapp.modules.simple.integtests.tests;

import java.sql.Timestamp;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.core.metamodel.services.jdosupport.Persistable_datanucleusIdLong;
import org.apache.isis.core.metamodel.services.jdosupport.Persistable_datanucleusVersionTimestamp;

import domainapp.modules.simple.dom.impl.SimpleObject;
import domainapp.modules.simple.fixture.SimpleObject_persona;
import domainapp.modules.simple.integtests.SimpleModuleIntegTestAbstract;
import static org.assertj.core.api.Assertions.assertThat;

public class SimpleObject_IntegTest extends SimpleModuleIntegTestAbstract {

    SimpleObject simpleObject;

    @Before
    public void setUp() {
        // given
        simpleObject = fixtureScripts.runBuilderScript(SimpleObject_persona.FOO.builder());
    }

    public static class Name extends SimpleObject_IntegTest {

        @Test
        public void accessible() {
            // when
            final String name = wrap(simpleObject).getName();

            // then
            assertThat(name).isEqualTo(simpleObject.getName());
        }

        @Test
        public void not_editable() {
            // expect
            expectedExceptions.expect(DisabledException.class);

            // when
            wrap(simpleObject).setName("new name");
        }

    }

    public static class UpdateName extends SimpleObject_IntegTest {

        @Test
        public void can_be_updated_directly() {

            // when
            wrap(simpleObject).updateName("new name");
            transactionService.nextTransaction();

            // then
            assertThat(wrap(simpleObject).getName()).isEqualTo("new name");
        }

        @Test
        public void failsValidation() {

            // expect
            expectedExceptions.expect(InvalidException.class);
            expectedExceptions.expectMessage("Exclamation mark is not allowed");

            // when
            wrap(simpleObject).updateName("new name!");
        }
    }


    public static class Title extends SimpleObject_IntegTest {

        @Inject
        TitleService titleService;

        @Test
        public void interpolatesName() {

            // given
            final String name = wrap(simpleObject).getName();

            // when
            final String title = titleService.titleOf(simpleObject);

            // then
            assertThat(title).isEqualTo("Object: " + name);
        }
    }

    public static class DataNucleusId extends SimpleObject_IntegTest {

        @Test
        public void should_be_populated() {
            // when
            final Long id = mixin(Persistable_datanucleusIdLong.class, simpleObject).prop();

            // then
            assertThat(id).isGreaterThanOrEqualTo(0);
        }
    }

    public static class DataNucleusVersionTimestamp extends SimpleObject_IntegTest {

        @Test
        public void should_be_populated() {
            // when
            final Timestamp timestamp = mixin(Persistable_datanucleusVersionTimestamp.class, simpleObject).prop();
            // then
            assertThat(timestamp).isNotNull();
        }
    }

}