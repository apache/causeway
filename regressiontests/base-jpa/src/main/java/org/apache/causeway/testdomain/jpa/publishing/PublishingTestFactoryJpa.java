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
package org.apache.causeway.testdomain.jpa.publishing;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingFunction;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.functions._Functions.CheckedConsumer;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.testdomain.jpa.JpaTestDomainPersona;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.jpa.entities.JpaInventory;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract;
import org.apache.causeway.testdomain.publishing.PublishingTestFactoryAbstract.CommitListener;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@Import({
    CommitListener.class
})
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PublishingTestFactoryJpa
extends PublishingTestFactoryAbstract {

    private final RepositoryService repository;
    private final WrapperFactory wrapper;
    private final ObjectManager objectManager;
    private final FixtureScripts fixtureScripts;
    private final CommitListener commitListener;
    private final JpaSupportService jpaSupport;
    private final FactoryService factoryService;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final InteractionService interactionService;

    @Getter(onMethod_ = {@Override}, value = AccessLevel.PROTECTED)
    private final TransactionService transactionService;

    // -- TEST SETUP

    @Override
    protected void releaseContext(final PublishingTestContext context) {
    }

    @Override
    protected void setupEntity(final PublishingTestContext context) {
        switch(context.scenario()) {
        case ENTITY_CREATION:

            // given - nothing to do
            break;

        case ENTITY_PERSISTING:

            // given
            fixtureScripts.runPersona(JpaTestDomainPersona.InventoryPurgeAll);
            break;

        case ENTITY_LOADING:
        case PROPERTY_UPDATE:
        case ACTION_INVOCATION:
        case ENTITY_REMOVAL:

            // given
            setupBookForJpa();
            break;
        default:
            throw _Exceptions.unmatchedCase(context.scenario());
        }
    }

    // -- TESTS - PROGRAMMATIC

    @Override
    protected void programmaticExecution(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // This test does not trigger command or execution publishing, however it does trigger
        // entity-change-publishing.

        switch (context.scenario()) {
		case ENTITY_CREATION -> {
			context.runGiven();
			//when
            factoryService.detachedEntity(JpaBook.fromDto(BookDto.sample())); // should trigger an ObjectCreatedEvent
			factoryService.detachedEntity(JpaBook.class); // should trigger a second ObjectCreatedEvent
		}
		case ENTITY_PERSISTING -> {
			context.runGiven();
			//when
            setupBookForJpa();
		}
		case ENTITY_LOADING -> {
			context.runGiven();
			//when
            withBookDo(book->{
                assertNotNull(book);
            });
		}
		case PROPERTY_UPDATE -> withBookDo(book->{

		    context.runGiven();

		    // when - direct change (circumventing the framework)
		    context.changeProperty(()->book.setName("Book #2"));

		    repository.persistAndFlush(book);

		});
		case ACTION_INVOCATION -> withBookDo(book->{

		    context.runGiven();

		    // when - direct action method invocation (circumventing the framework)
		    context.executeAction(()->book.doubleThePrice());

		    repository.persistAndFlush(book);

		});
		case ENTITY_REMOVAL -> withBookDo(book->{

		    context.runGiven();
		    //when
		    repository.removeAndFlush(book);

		});
		default -> throw _Exceptions.unmatchedCase(context.scenario());
		}
		;

    }

    // -- TESTS - INTERACTION API

    @Override
    protected void interactionApiExecution(
            final PublishingTestContext context) {

        context.bind(commitListener);

        switch (context.scenario()) {
		case PROPERTY_UPDATE -> withBookDo(book->{

		    context.runGiven();

		    // when
		    context.changeProperty(()->{

		        var bookAdapter = objectManager.adapt(book);
		        var propertyInteraction = PropertyInteraction.start(bookAdapter, "name", VisibilityConstraint.invalid(Where.OBJECT_FORMS));
		        var managedProperty = propertyInteraction.getManagedPropertyElseThrow(__->_Exceptions.noSuchElement());
		        var propertyModel = managedProperty.startNegotiation();
		        var propertySpec = managedProperty.getElementType();
		        propertyModel.getValue().setValue(ManagedObject.value(propertySpec, "Book #2"));
		        propertyModel.submit();

		    });

		});
		case ACTION_INVOCATION -> withBookDo(book->{

		    context.runGiven();

		    // when
		    context.executeAction(()->{

		        var bookAdapter = objectManager.adapt(book);

		        var actionInteraction = ActionInteraction.start(bookAdapter, "doubleThePrice", VisibilityConstraint.invalid(Where.OBJECT_FORMS));
		        var managedAction = actionInteraction.getManagedActionElseThrow(__->_Exceptions.noSuchElement());
		        // this test action is always disabled, so don't enforce rules here, just invoke
		        managedAction.invoke(Can.empty()); // no-arg action
		    });

		});
		default -> throw _Exceptions.unmatchedCase(context.scenario());
		}
		;

    }

    // -- TESTS - WRAPPER SYNC

    @Override
    protected void wrapperSyncExecutionNoRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        switch (context.scenario()) {
		case PROPERTY_UPDATE -> withBookDo(book->{

		    context.runGiven();

		    // when - running synchronous
		    var syncControl = SyncControl.defaults().withSkipRules(); // don't enforce rules
		    context.changeProperty(()->wrapper.wrap(book, syncControl).setName("Book #2"));

		});
		case ACTION_INVOCATION -> withBookDo(book->{

		    context.runGiven();

		    // when - running synchronous
		    var syncControl = SyncControl.defaults().withSkipRules(); // don't enforce rules
		    context.executeAction(()->wrapper.wrap(book, syncControl).doubleThePrice());

		});
		default -> throw _Exceptions.unmatchedCase(context.scenario());
		}
		;

    }

    @Override
    protected void wrapperSyncExecutionWithRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        switch (context.scenario()) {
		case PROPERTY_UPDATE -> withBookDo(book->{

		    context.runGiven();

		    // when - running synchronous
		    var syncControl = SyncControl.defaults().withCheckRules(); // enforce rules

		    //assertThrows(DisabledException.class, ()->{
		        wrapper.wrap(book, syncControl).setName("Book #2"); // should throw DisabledException
		    //});

		});
		case ACTION_INVOCATION -> withBookDo(book->{

		    context.runGiven();

		    // when - running synchronous
		    var syncControl = SyncControl.defaults().withCheckRules(); // enforce rules

		    //assertThrows(DisabledException.class, ()->{
		        wrapper.wrap(book, syncControl).doubleThePrice(); // should throw DisabledException
		    //});

		});
		default -> throw _Exceptions.unmatchedCase(context.scenario());
		}
		;

    }

    // -- TESTS - WRAPPER ASYNC

    @Override
    protected void wrapperAsyncExecutionNoRules(
            final PublishingTestContext context) throws InterruptedException, ExecutionException, TimeoutException {

        context.bind(commitListener);

        // given
        var asyncControl = AsyncControl.defaults().withSkipRules(); // don't enforce rules

        var future = withBookCall(book->{
            context.runGiven();

            // when - running asynchronous
            return wrapper.asyncWrap(book, asyncControl)
                    .acceptAsync(bk->bk.setName("Book #2"));
        });

        future
            .tryGet(10, TimeUnit.SECONDS); // wait till done

    }

    @Override
    protected void wrapperAsyncExecutionWithRules(
            final PublishingTestContext context) {

        context.bind(commitListener);

        // when enforce rules
        withBookDo(book->{

            //assertThrows(DisabledException.class, ()->{
                // should fail with DisabledException (synchronous) within the calling Thread
            wrapper.asyncWrap(book)
                .acceptAsync(bk->bk.setName("Book #2"));

            //});

        });

    }

    // -- TEST SETUP

    private void setupBookForJpa() {

        var em = jpaSupport.getEntityManagerElseFail(JpaBook.class);

        // cleanup
        fixtureScripts.runPersona(JpaTestDomainPersona.InventoryPurgeAll);

        // given Inventory with 1 Book

        var products = new HashSet<JpaProduct>();

        var detachedNewBook = JpaBook.fromDto(BookDto.sample());

        products.add(detachedNewBook);

        var inventory = JpaInventory.of("Sample Inventory", products);
        em.persist(inventory);

        inventory.getProducts().forEach(product->{
            em.persist(product);

            _Probe.errOut("PROD ID: %s", product.getId());

        });

        //fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);

        em.flush();
    }

    // -- HELPER

    @SneakyThrows
    private void withBookDo(final CheckedConsumer<JpaBook> transactionalBookConsumer) {
        //var em = jpaSupport.getEntityManagerElseFail(JpaBook.class);
        var book = repository.allInstances(JpaBook.class).listIterator().next();
        transactionalBookConsumer.accept(book);
        //em.flush(); // in effect makes changes visible during PRE_COMMIT
    }

    @SneakyThrows
    private <T> T withBookCall(final ThrowingFunction<JpaBook, T> transactionalBookFunction) {
        var book = repository.allInstances(JpaBook.class).listIterator().next();
        return transactionalBookFunction.apply(book);
    }

}
