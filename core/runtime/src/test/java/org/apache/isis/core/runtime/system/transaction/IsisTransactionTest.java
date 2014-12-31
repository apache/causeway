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

package org.apache.isis.core.runtime.system.transaction;

import java.util.Collections;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.audit.AuditingService3;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.publish.EventSerializer;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PojoAdapterBuilder.Persistence;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.services.eventbus.EventBusServiceDefault;
import org.apache.isis.core.runtime.system.persistence.ObjectStore;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.equalTo;

public class IsisTransactionTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);
    
    private IsisTransaction transaction;

    private ObjectAdapter transientAdapter1;
    private ObjectAdapter transientAdapter2;

    private ObjectAdapter persistentAdapter1;
    private ObjectAdapter persistentAdapter2;

    @Mock
    private ObjectStore mockObjectStore;

    @Mock
    private IsisTransactionManager mockTransactionManager;
    @Mock
    private AuthenticationSession mockAuthenticationSession;
    @Mock
    private MessageBroker mockMessageBroker;
    @Mock
    private CommandContext mockCommandContext;
    @Mock
    private CommandService mockCommandService;
    @Mock
    private AuditingService3 mockAuditingService3;
    @Mock
    private PublishingService mockPublishingService;
    @Mock
    private EventSerializer mockEventSerializer;
    @Mock
    private ServicesInjector mockServicesInjector;
    @Mock
    private PublishedObject.PayloadFactory mockPublishedObjectPayloadFactory;
    @Mock
    private PublishedAction.PayloadFactory mockPublishedActionPayloadFactory;
    @Mock
    private ActionInvocationContext mockActionInvocationContext;
    @Mock
    private EventBusServiceDefault mockEventBusServiceDefault;

    private PersistenceCommand command;
    private PersistenceCommand command2;
    private PersistenceCommand command3;


    private CreateObjectCommand createCreateCommand(final ObjectAdapter object, final String name) {
        return new CreateObjectCommand() {

            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
            }

            @Override
            public ObjectAdapter onAdapter() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private DestroyObjectCommand createDestroyCommand(final ObjectAdapter object, final String name) {
        return new DestroyObjectCommand() {

            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
            }

            @Override
            public ObjectAdapter onAdapter() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private SaveObjectCommand createSaveCommand(final ObjectAdapter object, final String name) {
        return new SaveObjectCommand() {
            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
            }

            @Override
            public ObjectAdapter onAdapter() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private SaveObjectCommand createSaveCommandThatAborts(final ObjectAdapter object, final String name) {
        return new SaveObjectCommand() {
            @Override
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
                throw new ObjectPersistenceException();
            }

            @Override
            public ObjectAdapter onAdapter() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        context.ignoring(mockCommandContext);
        context.ignoring(mockCommandService);
        context.ignoring(mockAuditingService3);
        context.ignoring(mockEventBusServiceDefault);

        context.checking(new Expectations(){{
            allowing(mockServicesInjector).lookupService(CommandContext.class);
            will(returnValue(mockCommandContext));

            allowing(mockServicesInjector).lookupService(CommandService.class);
            will(returnValue(mockCommandService));

            allowing(mockServicesInjector).lookupService(AuditingService3.class);
            will(returnValue(mockAuditingService3));

            allowing(mockServicesInjector).lookupService(PublishingService.class);
            will(returnValue(mockPublishingService));
            
            allowing(mockServicesInjector).lookupService(EventSerializer.class);
            will(returnValue(mockEventSerializer));
            
            allowing(mockServicesInjector).lookupService(PublishedObject.PayloadFactory.class);
            will(returnValue(mockPublishedObjectPayloadFactory));
            
            allowing(mockServicesInjector).lookupService(PublishedAction.PayloadFactory.class);
            will(returnValue(mockPublishedActionPayloadFactory));
            
            allowing(mockServicesInjector).lookupService(ActionInvocationContext.class);
            will(returnValue(mockActionInvocationContext));
            
            allowing(mockServicesInjector).lookupService(EventBusServiceDefault.class);
            will(returnValue(mockEventBusServiceDefault));
            
            allowing(mockServicesInjector).getRegisteredServices();
            will(returnValue(Collections.emptyList())); // close enough...

        }});


        context.checking(new Expectations(){{
            allowing(mockTransactionManager).getAuthenticationSession();
            will(returnValue(mockAuthenticationSession));
        }});

        context.checking(new Expectations(){{
            allowing(mockAuthenticationSession).getUserName();
            will(returnValue("sven"));
        }});
        
        transaction = new IsisTransaction(mockTransactionManager, mockMessageBroker, mockObjectStore, mockServicesInjector);
        
        transientAdapter1 = PojoAdapterBuilder.create().with(Persistence.TRANSIENT).withIdentifier("1").build();
        transientAdapter2 = PojoAdapterBuilder.create().with(Persistence.TRANSIENT).withIdentifier("2").build();
        persistentAdapter1 = PojoAdapterBuilder.create().with(Persistence.PERSISTENT).withIdentifier("3").build();
        persistentAdapter2 = PojoAdapterBuilder.create().with(Persistence.PERSISTENT).withIdentifier("4").build();
    }
    
    @Test
    public void abort_neverDelegatesToObjectStore() throws Exception {

        command = createSaveCommand(transientAdapter1, "command 1");
        command2 = createSaveCommand(transientAdapter2, "command 2");

        context.checking(new Expectations() {
            {
                never(mockObjectStore);
            }
        });

        transaction.addCommand(command);
        transaction.addCommand(command2);
        transaction.markAsAborted();
    }


    @Test
    public void commit_delegatesToObjectStoreToExecutesAllCommands() throws Exception {

        command = createSaveCommand(transientAdapter1, "command 1");
        command2 = createSaveCommandThatAborts(transientAdapter2, "command 2");

        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(IsisMatchers.listContainingAll(command, command2)));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });
        
        transaction.addCommand(command);
        transaction.addCommand(command2);
        
        transaction.preCommit();
        transaction.commit();
    }

    @Test
    public void commit_disregardsSecondSaveCommandOnSameAdapter() throws Exception {

        command = createSaveCommand(persistentAdapter1, "command 1");
        command2 = createSaveCommand(persistentAdapter1, "command 2");

        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(IsisMatchers.listContainingAll(command)));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });
        
        transaction.addCommand(command);
        transaction.addCommand(command2);

        transaction.preCommit();
        transaction.commit();
    }


    @Test
    public void commit_disregardsSaveCommandsForObjectBeingCreated() throws Exception {

        command = createCreateCommand(transientAdapter1, "command 1");
        command2 = createSaveCommandThatAborts(transientAdapter1, "command 2");

        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(IsisMatchers.listContainingAll(command)));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });
        
        transaction.addCommand(command);
        transaction.addCommand(command2);

        transaction.preCommit();
        transaction.commit();
    }

    @Test
    public void commit_destroyCausesPrecedingSaveCommandsToBeDisregarded() throws Exception {

        command = createSaveCommand(persistentAdapter1, "command 1");
        command2 = createSaveCommand(persistentAdapter2, "command 2");
        command3 = createDestroyCommand(persistentAdapter1, "command 3");

        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(IsisMatchers.listContainingAll(command2, command3)));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });
        
        transaction.addCommand(command);
        transaction.addCommand(command2);
        transaction.addCommand(command3);

        transaction.preCommit();
        transaction.commit();
    }

    @Test
    public void commit_ignoresBothCreateAndDestroyCommandsWhenForSameObject() throws Exception {

        command = createSaveCommand(persistentAdapter1, "command 1");
        command2 = createSaveCommand(persistentAdapter2, "command 2");
        command3 = createDestroyCommand(persistentAdapter1, "command 3");

        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(IsisMatchers.listContainingAll(command2)));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });
        
        transaction.addCommand(command);
        transaction.addCommand(command2);
        transaction.addCommand(command3);

        transaction.preCommit();
        transaction.commit();
    }


    @Test
    public void commit_testNoCommands() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(Collections.<PersistenceCommand>emptyList()));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });


        transaction.preCommit();
        transaction.commit();
    }


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToAbortAnAlreadyAbortedTransaction() throws Exception {
        transaction.markAsAborted();

        transaction.markAsAborted();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToCommitAnAlreadyAbortedTransaction() throws Exception {
        transaction.markAsAborted();


        transaction.preCommit();
        transaction.commit();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToAbortAnAlreadyCommitedTransaction() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(Collections.<PersistenceCommand>emptyList()));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });


        transaction.preCommit();
        transaction.commit();

        transaction.markAsAborted();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToCommitAnAlreadyCommitedTransaction() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(mockObjectStore).execute(with(Collections.<PersistenceCommand>emptyList()));
                // second flush after publish
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                // and for command
                oneOf(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
            }
        });

        transaction.preCommit();
        transaction.commit();


        transaction.preCommit();
        transaction.commit();
    }
}
