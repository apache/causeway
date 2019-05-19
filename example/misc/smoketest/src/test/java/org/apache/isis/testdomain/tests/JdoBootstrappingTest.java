/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.tests;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.fixturespec.FixtureScriptsDefault;
import org.apache.isis.core.integtestsupport.components.HeadlessTransactionSupportDefault;
import org.apache.isis.core.runtime.headless.HeadlessTransactionSupport;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.testdomain.jdo.Inventory;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {
                HeadlessTransactionSupportDefault.class,
                IsisBoot.class,
                FixtureScriptsDefault.class,
                JdoTestDomainModule.class,
                },
        properties = {
                //"isis.reflector.introspector.parallelize=false",
                //"logging.level.org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
                }
        )
class JdoBootstrappingTest {

    @Inject IsisSessionFactory isisSessionFactory;
    @Inject HeadlessTransactionSupport transactions;
    @Inject FixtureScripts fixtureScripts;
    private Inventory inventory;
    
    @BeforeEach
    void setUp() {
        
        isisSessionFactory.openSession(new InitialisationSession());
        
        transactions.beginTransaction();
        
        // cleanup
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.PurgeAll.builder());

        // given
        inventory = fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.InventoryWith1Book.builder());
        
        transactions.endTransaction();
        
        isisSessionFactory.closeSession();

    }
    
    @Test
    void sampleInventoryShouldBeSetUp() {

        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(1, inventory.getProducts().size());
        
        val product = inventory.getProducts().iterator().next();
        assertEquals("Sample Book", product.getName());
        
    }

        
}
