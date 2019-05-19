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

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

/**
 * Depends on {@link JdoBootstrappingTest} to succeed.
 */
class PublisherServiceTest  {
    
    @Inject FixtureScripts fixtureScripts;

    @BeforeEach
    void setUp() {
        
        // cleanup
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.PurgeAll.builder());
        
        // given
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.InventoryWith1Book.builder());
    }
    
    @Test @Disabled("service lookup fails")
    void publisherServiceShouldBeAwareOfInventoryChanges() {
        
        
        val serviceRegistry = IsisContext.getServiceRegistry();
        
        Map<Class<? extends PublisherService>, PublisherService> map =
                serviceRegistry.select(PublisherService.class)
                .stream()
                .collect(Collectors.toMap(ps->ps.getClass(), ps->ps));
        
        // TODO any instance type lookup (should be a test on its own)
        assertTrue(map.containsKey(PublisherServiceStub.class));
        
        // priority based service lookup (could be a test on its own)
        val publisherService = (PublisherServiceStub) map.get(PublisherServiceStub.class);
        
        // given
//        val publisherService = (PublisherServiceStub) publisherServiceAny; 
        val repository = IsisContext.getServiceRegistry()
                .lookupServiceElseFail(RepositoryService.class);
        val txMan = IsisContext.getPersistenceSession().get().getTransactionManager();
        val book = repository.allInstances(Book.class).listIterator().next();
        
        // when
        publisherService.clearHistory();
        book.setName("Book #2");
        repository.persist(book);
        txMan.endTransaction();
        
        // then TODO
//        assertEquals(
//                "...", 
//                publisherService.getHistory());
    }
    
    // -- HELPER
        
    @DomainService(menuOrder="1.0", nature=NatureOfService.DOMAIN)
    @DomainServiceLayout()
    public static class PublisherServiceStub implements PublisherService {

        private StringBuilder history = new StringBuilder();
        
        void clearHistory() {
            history = new StringBuilder();
        }
        
        String getHistory() {
            return history.toString();
        }

        @Override
        public void publish(Execution<?, ?> execution) {
            history.append("execution=").append(execution).append(",");
            System.out.println("h1: " + history);
        }

        @Override
        public void publish(PublishedObjects publishedObjects) {
            history.append("publishedObjects=").append(publishedObjects).append(",");
            System.out.println("h2: " + history);
        }

    }
    
}
