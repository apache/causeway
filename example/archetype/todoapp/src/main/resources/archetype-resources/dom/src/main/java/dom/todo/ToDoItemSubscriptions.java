#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package dom.todo;

import java.util.EventObject;
import java.util.List;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyInteractionEvent;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

@DomainService
public class ToDoItemSubscriptions {

    //region > LOG
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ToDoItemSubscriptions.class);
    //endregion

    //region > on(Event)...

    public static enum Behaviour {
        AnyExecuteAccept,
        AnyExecuteVetoWithRecoverableException,
        AnyExecuteVetoWithNonRecoverableException,
        AnyExecuteVetoWithOtherException,
        UpdateCostActionHide,
        UpdateCostActionDisable,
        UpdateCostActionInvalidate,
        DescriptionPropertyHide,
        DescriptionPropertyDisable,
        DescriptionPropertyInvalidate,
        DependenciesCollectionHide,
        // not implemented in Wicket viewer, but supported in wrapped objects
        DependenciesCollectionDisable,
        // not implemented in Wicket viewer, but supported in wrapped objects
        DependenciesCollectionInvalidateAdd,
        DependenciesCollectionInvalidateRemove,
        SimilarToCollectionHide
    }
    private Behaviour behaviour = Behaviour.AnyExecuteAccept;

    /**
     * To demo/test what occurs if a subscriber that might veto an event.
     */
    @Prototype
    @MemberOrder(name = "Prototyping", sequence = "80")
    @Named("Set subscriber behaviour")
    @ActionSemantics(ActionSemantics.Of.IDEMPOTENT)
    public ToDoItem subscriberBehaviour(ToDoItem toDoItem, @Named("Behaviour") Behaviour behaviour) {
        this.behaviour = behaviour;
        container.informUser("Subscriber behaviour set to: " + behaviour);
        return toDoItem;
    }
    public Behaviour default1SubscriberBehaviour() {
        return this.behaviour;
    }

    @Programmatic
    public Behaviour getSubscriberBehaviour() {
        return behaviour;
    }
    private void onExecutedVetoIfRequired() {
        if(behaviour == Behaviour.AnyExecuteVetoWithRecoverableException) {
            throw new RecoverableException("Rejecting event (recoverable exception thrown)");
        }
        if(behaviour == Behaviour.AnyExecuteVetoWithNonRecoverableException) {
            throw new NonRecoverableException("Rejecting event (recoverable exception thrown)");
        }
        if(behaviour == Behaviour.AnyExecuteVetoWithOtherException) {
            throw new RuntimeException("Throwing some other exception");
        }
    }
    //endregion

    //region > on(Event) for ToDoItem-specific events
    @Programmatic
    @Subscribe
    public void on(final ToDoItem.CompletedEvent ev) {
        recordEvent(ev);
        switch(ev.getPhase()) {
            case HIDE:
                break;
            case DISABLE:
                break;
            case VALIDATE:
                break;
            case EXECUTING:
                break;
            case EXECUTED:
                LOG.info("Received ToDoItem.CompletedEvent for : " + container.titleOf(ev.getSource()));
                break;
        }
    }
    //endregion

    //region > on(Event) ... general purpose

    @Programmatic
    @Subscribe
    public void on(final ActionInteractionEvent<?> ev) {
        recordEvent(ev);
        switch(ev.getPhase()) {
            case HIDE:
                if(getSubscriberBehaviour() == Behaviour.UpdateCostActionHide) {
                    if(ev.getIdentifier().getMemberName().equals("updateCost")) {
                        ev.hide();
                    }
                }
                break;
            case DISABLE:
                if(getSubscriberBehaviour() == Behaviour.UpdateCostActionDisable) {
                    if(ev.getIdentifier().getMemberName().equals("updateCost")) {
                        ev.disable("ToDoItemSubscriptions says: updateCost action disabled!");
                    }
                }
                break;
            case VALIDATE:
                if(getSubscriberBehaviour() == Behaviour.UpdateCostActionInvalidate &&
                        ev.getIdentifier().getMemberName().equals("updateCost")) {
                    ev.disable("ToDoItemSubscriptions says: can't invoke updateCostaction with these args!");
                }
                break;
            case EXECUTING:
                break;
            case EXECUTED:
                LOG.info("Received ActionInteractionEvent, " + container.titleOf(ev.getSource()) + ", invoked " + ev.getIdentifier().getMemberName());
                onExecutedVetoIfRequired();
                break;
        }
    }

    @Programmatic
    @Subscribe
    public void on(PropertyInteractionEvent<?,?> ev) {
        recordEvent(ev);
        switch(ev.getPhase()) {
            case HIDE:
                if(getSubscriberBehaviour() == Behaviour.DescriptionPropertyHide &&
                    ev.getIdentifier().getMemberName().equals("description")) {
                    ev.hide();
                }
                break;
            case DISABLE:
                if(getSubscriberBehaviour() == Behaviour.DescriptionPropertyDisable &&
                    ev.getIdentifier().getMemberName().equals("description")) {
                    ev.disable("ToDoItemSubscriptions says: description property disabled!");
                }
                break;
            case VALIDATE:
                if(getSubscriberBehaviour() == Behaviour.DescriptionPropertyInvalidate &&
                    ev.getIdentifier().getMemberName().equals("description")) {
                    ev.disable("ToDoItemSubscriptions says: can't change description property to this value!");
                }
                break;
            case EXECUTING:
                break;
            case EXECUTED:
                LOG.info("Received PropertyInteractionEvent, " + container.titleOf(ev.getSource()) + ", changed " + ev.getIdentifier().getMemberName() + " : " + ev.getOldValue() + " -> " + ev.getNewValue());
                onExecutedVetoIfRequired();

                if(ev.getIdentifier().getMemberName().contains("description")) {
                    String newValue = (String) ev.getNewValue();
                    if(newValue.matches(".*demo veto.*")) {
                        throw new RecoverableException("oh no you don't! " + ev.getNewValue());
                    }
                }
                break;
        }
    }
    
    @Programmatic
    @Subscribe
    public void on(CollectionInteractionEvent<?,?> ev) {
        recordEvent(ev);
        switch (ev.getPhase()) {
            case HIDE:
                if(getSubscriberBehaviour() == Behaviour.DependenciesCollectionHide &&
                    ev.getIdentifier().getMemberName().equals("dependencies")) {
                    ev.hide();
                }
                if (getSubscriberBehaviour() == Behaviour.SimilarToCollectionHide &&
                    ev.getIdentifier().getMemberName().equals("similarTo")) {
                    ev.hide();
                }
                break;
            case DISABLE:
                if (getSubscriberBehaviour() == Behaviour.DependenciesCollectionDisable &&
                    ev.getIdentifier().getMemberName().equals("dependencies")) {
                    ev.disable("ToDoItemSubscriptions says: dependencies collection disabled!");
                }
                break;
            case VALIDATE:
                if(getSubscriberBehaviour() == Behaviour.DependenciesCollectionInvalidateAdd &&
                    ev.getIdentifier().getMemberName().equals("dependencies") &&
                    ev.getOf() == CollectionInteractionEvent.Of.ADD_TO ) {
                    ev.invalidate("ToDoItemSubscriptions says: can't add this object to dependencies collection!");
                }
                if(getSubscriberBehaviour() == Behaviour.DependenciesCollectionInvalidateRemove &&
                    ev.getIdentifier().getMemberName().equals("dependencies") &&
                    ev.getOf() == CollectionInteractionEvent.Of.REMOVE_FROM ) {
                    ev.invalidate("ToDoItemSubscriptions says: can't remove this object from dependencies collection!");
                }
                break;
            case EXECUTING:
                break;
            case EXECUTED:
                if(ev.getOf() == CollectionInteractionEvent.Of.ADD_TO) {
                    LOG.info("Received CollectionInteractionEvent, " + container.titleOf(ev.getSource()) + ", added to " + ev.getIdentifier().getMemberName() + " : " + ev.getValue());
                } else {
                    LOG.info("Received CollectionInteractionEvent, " + container.titleOf(ev.getSource()) + ", removed from " + ev.getIdentifier().getMemberName() + " : " + ev.getValue());
                }
                onExecutedVetoIfRequired();
                break;
        }

    }
    //endregion

    //region > receivedEvents
    private final List<java.util.EventObject> receivedEvents = Lists.newLinkedList();

    /**
     * Used in integration tests.
     */
    @Programmatic
    public List<java.util.EventObject> receivedEvents() {
        return receivedEvents;
    }

    /**
     * Used in integration tests.
     */
    @Programmatic
    public <T extends java.util.EventObject> List<T> receivedEvents(final Class<T> expectedType) {
        return newArrayList(
                    transform(
                        filter(receivedEvents, instanceOf(expectedType)),
                        castTo(expectedType)));
    }

    private static <T extends EventObject> Function<EventObject, T> castTo(Class<T> expectedType) {
        return new Function<EventObject, T>() {
                    @Override
                    public T apply(EventObject input) {
                        return (T) input;
                    }
                };
    }

    private static <T extends EventObject> Predicate<EventObject> instanceOf(final Class<T> expectedType) {
        return new Predicate<EventObject>() {
            @Override
            public boolean apply(EventObject input) {
                return expectedType.isInstance(input);
            }
        };
    }

    /**
     * Used in integration tests.
     */
    @Programmatic
    public <T extends java.util.EventObject> T mostRecentlyReceivedEvent(Class<T> expectedType) {
        final List<T> receivedEvents = receivedEvents(expectedType);
        return !receivedEvents.isEmpty() ? receivedEvents.get(0) : null;
    }
    private void recordEvent(final java.util.EventObject ev) {
        receivedEvents.add(0, ev);
    }
    /**
     * Used in integration tests.
     */
    @Programmatic
    public void reset() {
        receivedEvents.clear();
        subscriberBehaviour(null, ToDoItemSubscriptions.Behaviour.AnyExecuteAccept);
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private DomainObjectContainer container;

    @SuppressWarnings("unused")
    private EventBusService eventBusService;
    @Programmatic
    public final void injectEventBusService(EventBusService eventBusService) {
        eventBusService.register(this);
    }
    //endregion


}
