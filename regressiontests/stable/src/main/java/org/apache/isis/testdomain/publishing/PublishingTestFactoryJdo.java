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
package org.apache.isis.testdomain.publishing;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Functions.CheckedConsumer;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.CommitListener;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Component
@Import({
    CommitListener.class
})
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PublishingTestFactoryJdo
extends PublishingTestFactoryAbstract {

    private final RepositoryService repository;
    private final WrapperFactory wrapper;
    private final ObjectManager objectManager;
    private final FixtureScripts fixtureScripts;
    private final CommitListener commitListener;
    private final FactoryService factoryService;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final InteractionService interactionService;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final TransactionService transactionService;

    @Named("transaction-aware-pmf-proxy")
    private final PersistenceManagerFactory pmf;

    // -- TEST SETUP

    @Override
    protected void setupEntity(final PublishingTestContext context) {
        switch(context.getScenario()) {
        case ENTITY_CREATION:

            // given - nothing to do
            break;

        case ENTITY_PERSISTING:

            // given
            fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
            break;

        case ENTITY_LOADING:
        case PROPERTY_UPDATE:
        case ACTION_INVOCATION:
        case ENTITY_REMOVAL:

            // given
            setupBookForJdo();
            break;
        default:
            throw _Exceptions.unmatchedCase(context.getScenario());
        }
    }

    // -- TESTS - PROGRAMMATIC

    @Override
    protected void programmaticExecution(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // This test does not trigger command or execution publishing, however it does trigger
        // entity-change-publishing.

        switch(context.getScenario()) {
        case ENTITY_CREATION:

            context.runGiven();
            //when
            factoryService.detachedEntity(JdoBook.fromDto(BookDto.sample())); // should trigger an ObjectCreatedEvent
            factoryService.detachedEntity(JdoBook.class); // should trigger a second ObjectCreatedEvent
            break;

        case ENTITY_PERSISTING:

            context.runGiven();
            //when
            setupBookForJdo();
            break;

        case ENTITY_LOADING:

            context.runGiven();
            //when
            withBookDo(book->{
                assertNotNull(book);
            });
            break;


        case PROPERTY_UPDATE:

            withBookDo(book->{

                context.runGiven();

                // when - direct change (circumventing the framework)
                context.changeProperty(()->book.setName("Book #2"));

                repository.persistAndFlush(book);

            });

            break;
        case ACTION_INVOCATION:

            withBookDo(book->{

                context.runGiven();

                // when - direct action method invocation (circumventing the framework)
                context.executeAction(()->book.doubleThePrice());

                repository.persistAndFlush(book);

            });

            break;
        case ENTITY_REMOVAL:

            withBookDo(book->{

                context.runGiven();
                //when
                repository.removeAndFlush(book);

            });

            break;
        default:
            throw _Exceptions.unmatchedCase(context.getScenario());
        }

    }

    // -- TESTS - INTERACTION API

    @Override
    protected void interactionApiExecution(
            final PublishingTestContext context) {

        context.bind(commitListener);

        switch(context.getScenario()) {

        case PROPERTY_UPDATE:

            withBookDo(book->{

                context.runGiven();

                // when
                context.changeProperty(()->{

                    val bookAdapter = objectManager.adapt(book);
                    val propertyInteraction = PropertyInteraction.start(bookAdapter, "name", Where.OBJECT_FORMS);
                    val managedProperty = propertyInteraction.getManagedPropertyElseThrow(__->_Exceptions.noSuchElement());
                    val propertyModel = managedProperty.startNegotiation();
                    val propertySpec = managedProperty.getElementType();
                    propertyModel.getValue().setValue(ManagedObject.of(propertySpec, "Book #2"));
                    propertyModel.submit();

                });

            });

            break;
        case ACTION_INVOCATION:

            withBookDo(book->{

                context.runGiven();

                // when
                context.executeAction(()->{

                    val bookAdapter = objectManager.adapt(book);

                    val actionInteraction = ActionInteraction.start(bookAdapter, "doubleThePrice", Where.OBJECT_FORMS);
                    val managedAction = actionInteraction.getManagedActionElseThrow(__->_Exceptions.noSuchElement());
                    // this test action is always disabled, so don't enforce rules here, just invoke
                    managedAction.invoke(Can.empty()); // no-arg action
                });

            });

            break;
        default:
            throw _Exceptions.unmatchedCase(context.getScenario());
        }

    }

    // -- TESTS - WRAPPER SYNC

    @Override
    protected void wrapperSyncExecutionNoRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        switch(context.getScenario()) {

        case PROPERTY_UPDATE:

            withBookDo(book->{

                context.runGiven();

                // when - running synchronous
                val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
                context.changeProperty(()->wrapper.wrap(book, syncControl).setName("Book #2"));

            });

            break;
        case ACTION_INVOCATION:

            withBookDo(book->{

                context.runGiven();

                // when - running synchronous
                val syncControl = SyncControl.control().withSkipRules(); // don't enforce rules
                context.executeAction(()->wrapper.wrap(book, syncControl).doubleThePrice());

            });

            break;
        default:
            throw _Exceptions.unmatchedCase(context.getScenario());
        }

    }

    @Override
    protected void wrapperSyncExecutionWithRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        switch(context.getScenario()) {

        case PROPERTY_UPDATE:

            withBookDo(book->{

                context.runGiven();

                // when - running synchronous
                val syncControl = SyncControl.control().withCheckRules(); // enforce rules

                //assertThrows(DisabledException.class, ()->{
                    wrapper.wrap(book, syncControl).setName("Book #2"); // should fail with DisabledException
                //});

            });

            break;
        case ACTION_INVOCATION:

            withBookDo(book->{

                context.runGiven();

                // when - running synchronous
                val syncControl = SyncControl.control().withCheckRules(); // enforce rules

                //assertThrows(DisabledException.class, ()->{
                    wrapper.wrap(book, syncControl).doubleThePrice(); // should fail with DisabledException
                //});

            });

            break;
        default:
            throw _Exceptions.unmatchedCase(context.getScenario());
        }

    }

    // -- TESTS - WRAPPER ASYNC

    @Override
    protected void wrapperAsyncExecutionNoRules(
            final PublishingTestContext context) throws InterruptedException, ExecutionException, TimeoutException {

        context.bind(commitListener);

        // given
        val asyncControl = returningVoid().withSkipRules(); // don't enforce rules

        withBookDo(book->{

            context.runGiven();

            // when - running asynchronous
            wrapper.asyncWrap(book, asyncControl)
            .setName("Book #2");

        });

        asyncControl.getFuture().get(10, TimeUnit.SECONDS);

    }

    @Override
    protected void wrapperAsyncExecutionWithRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        withBookDo(book->{

            context.runGiven();

            // when - running synchronous
            val asyncControl = returningVoid().withCheckRules(); // enforce rules

            //assertThrows(DisabledException.class, ()->{
                // should fail with DisabledException (synchronous) within the calling Thread
                wrapper.asyncWrap(book, asyncControl).setName("Book #2");

            //});

        });

    }

    // -- TEST SETUP

    private void setupBookForJdo() {

        val pm = pmf.getPersistenceManager();

        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given Inventory with 1 Book

        val products = new HashSet<JdoProduct>();

        val detachedNewBook = JdoBook.fromDto(BookDto.sample());

        products.add(detachedNewBook);

        val inventory = JdoInventory.of("Sample Inventory", products);
        pm.makePersistent(inventory);

        inventory.getProducts().forEach(product->{
            val prod = pm.makePersistent(product);

            _Probe.errOut("PROD ID: %s", JDOHelper.getObjectId(prod));

        });

        //fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);

        pm.flush();

    }

    @SneakyThrows
    private void withBookDo(final CheckedConsumer<JdoBook> transactionalBookConsumer) {
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        transactionalBookConsumer.accept(book);
    }

}
