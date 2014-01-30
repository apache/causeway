/**
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
package org.apache.isis.core.runtime.services.background;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.UUID;

import com.google.common.base.Objects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.applib.services.background.BackgroundTaskService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.InteractionContext;
import org.apache.isis.applib.services.interaction.InteractionDefault;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class BackgroundServiceDefaultTest_execute {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private BookmarkService mockBookmarkService;

    static class InteractionWithTransactionId extends InteractionDefault implements HasTransactionId {

        private UUID transactionId;
        
        @Override
        public UUID getTransactionId() {
            return transactionId;
        }

        @Override
        public void setTransactionId(UUID transactionId) {
            this.transactionId = transactionId;
        }
    }
    
    @Mock
    private SpecificationLoaderSpi mockSpecificationLoaderSpi;
    @Mock
    private ObjectSpecificationDefault mockSpec;
    @Mock
    private ObjectAction mockAction;
    private Identifier actionIdentifier;
    
    @Mock
    private InteractionContext mockInteractionContext;

    private InteractionWithTransactionId interactionWithTransactionId;
    private UUID transactionId = UUID.fromString("1231231231");
    
    @Mock
    private BackgroundTaskService mockBackgroundTaskService;

    

    static class Product {}
    static class Order {
        Product product;
        int quantity;

        public Order(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
    static class Customer {
        public Order placeOrder(Product product, int quantity, String comment) {
            return new Order(product, quantity);
        }
    }
    private Customer customer;
    private Product product;

    private BackgroundServiceDefault backgroundService;


    @Before
    public void setUp() throws Exception {
        backgroundService = new BackgroundServiceDefault() {
            @Override
            protected SpecificationLoaderSpi getSpecificationLoader() {
                return mockSpecificationLoaderSpi;
            }
        };
        backgroundService.injectBookmarkService(mockBookmarkService);
        backgroundService.injectInteractionContext(mockInteractionContext);
        backgroundService.injectBackgroundTaskService(mockBackgroundTaskService);

        interactionWithTransactionId = new InteractionWithTransactionId();
        interactionWithTransactionId.setTransactionId(transactionId);
        interactionWithTransactionId.setUser("fbloggs");
        
        context.checking(new Expectations() {
            {
                allowing(mockSpecificationLoaderSpi).loadSpecification(with(IsisMatchers.anySubclassOf(Customer.class)));
                will(returnValue(mockSpec));
                
                allowing(mockSpec).getFullIdentifier();
                will(returnValue(Customer.class.getName()));
                
                allowing(mockInteractionContext).getInteraction();
                will(returnValue(interactionWithTransactionId));
            }
        });
        customer = new Customer();
        product = new Product();
        actionIdentifier = Identifier.actionIdentifier(Customer.class, "placeOrder", Product.class, int.class);
    }
    
    @Test
    public void happy() {
        final Bookmark cusBookmark = new Bookmark("CUS", "456");
        final Bookmark prdBookmark = new Bookmark("PRD", "123");

        context.checking(new Expectations() {
            {
                allowing(mockSpec).getMember(with(any(Method.class)));
                will(returnValue(mockAction));
                
                allowing(mockAction).getIdentifier();
                will(returnValue(actionIdentifier));

                oneOf(mockBookmarkService).bookmarkFor(with(any(Customer.class))); // will be the proxy
                will(returnValue(cusBookmark));
                
                oneOf(mockBookmarkService).bookmarkFor(product);
                will(returnValue(prdBookmark));
                
                oneOf(mockBackgroundTaskService).execute(
                        with(x()), 
                        with(equalTo(transactionId)));
            }

            protected Matcher<ActionInvocationMemento> x() {
                return new TypeSafeMatcher<ActionInvocationMemento>() {

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("all sorted");
                    }

                    @Override
                    protected boolean matchesSafely(ActionInvocationMemento item) {
                        try {
                            return item.getActionId().equals("org.apache.isis.core.runtime.services.background.BackgroundServiceDefaultTest_execute.Customer#placeOrder(org.apache.isis.core.runtime.services.background.BackgroundServiceDefaultTest_execute$Product,int)")
                                    && item.getUser().equals("fbloggs")
                                    && item.getTarget().toString().equals("CUS:456")
                                    && item.getNumArgs() == 3
                                    && item.getArgType(0).equals(Bookmark.class)
                                    && item.getArg(0, Bookmark.class).toString().equals("PRD:123")
                                    && item.getArgType(1).equals(int.class)
                                    && item.getArg(1, int.class) == 3
                                    && item.getArgType(2).equals(String.class)
                                    && Objects.equal(item.getArg(2, String.class), null)
                                    ;
                        } catch (ClassNotFoundException e) {
                            return false;
                        }
                    }
                };
            }
        });
        backgroundService.execute(customer).placeOrder(product, 3, null);
    }
}
