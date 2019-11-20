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

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import javax.inject.Inject;
import javax.jdo.JDODataStoreException;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.integtestsupport.ThrowableMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domainapp.modules.simple.dom.impl.SimpleObject;
import domainapp.modules.simple.dom.impl.SimpleObjects;
import domainapp.modules.simple.fixture.SimpleObject_persona;
import domainapp.modules.simple.integtests.SimpleModuleIntegTestAbstract;

@Transactional
public class SimpleObjects_IntegTest extends SimpleModuleIntegTestAbstract {

    @Inject
    SimpleObjects menu;

    public static class ListAll extends SimpleObjects_IntegTest {

        @Test
        public void happyCase() {

            // given
            fixtureScripts.run(new SimpleObject_persona.PersistAll());
            transactionService.flushTransaction();

            // when
            final List<SimpleObject> all = wrap(menu).listAll();

            // then
            assertThat(all).hasSize(SimpleObject_persona.values().length);
        }

        @Test
        public void whenNone() {

            // when
            final List<SimpleObject> all = wrap(menu).listAll();

            // then
            assertThat(all).hasSize(0);
        }
    }

    public static class Create extends SimpleObjects_IntegTest {

        @Test
        public void happyCase() {

            wrap(menu).create("Faz");

            // then
            final List<SimpleObject> all = wrap(menu).listAll();
            assertThat(all).hasSize(1);
        }

        @Test
        public void whenAlreadyExists() {

            // given
            fixtureScripts.runPersona(SimpleObject_persona.FIZZ);
            transactionService.flushTransaction();

            // expect
            Throwable cause = assertThrows(Throwable.class, ()->{

                // when
                wrap(menu).create("Fizz");
                transactionService.flushTransaction();

            });

            // also expect
            MatcherAssert.assertThat(cause, 
                    ThrowableMatchers.causedBy(JDODataStoreException.class));

        }

    }


}