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
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionRemovedFromEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;

public class ToDoItemSubscriptions {

    //region > LOG
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ToDoItemSubscriptions.class);
    //endregion


    //region > on(Event)...
    // //////////////////////////////////////
    public static enum Behaviour {
        AcceptEvents,
        RejectEventsWithRecoverableException,
        RejectEventsWithNonRecoverableException,
        ThrowOtherException
    }
    private Behaviour behaviour = Behaviour.AcceptEvents;

    /**
     * To demo/test what occurs if a subscriber that might veto an event.
     */
    @Prototype
    @MemberOrder(name = "Prototyping", sequence = "80")
    @Named("Set subscriber behaviour")
    @ActionSemantics(ActionSemantics.Of.IDEMPOTENT)
    public String subscriberBehaviour(@Named("Behaviour") Behaviour behaviour) {
        this.behaviour = behaviour;
        return "Subscriber behaviour set to: " + behaviour;
    }
    public Behaviour default0SubscriberBehaviour() {
        return this.behaviour;
    }
    @Programmatic
    public Behaviour getSubscriberBehaviour() {
        return behaviour;
    }
    private void rejectIfRequired() {
        if(behaviour == Behaviour.RejectEventsWithRecoverableException) {
            throw new RecoverableException("Rejecting event (recoverable exception thrown)");
        }
        if(behaviour == Behaviour.RejectEventsWithNonRecoverableException) {
            throw new NonRecoverableException("Rejecting event (recoverable exception thrown)");
        }
        if(behaviour == Behaviour.ThrowOtherException) {
            throw new RuntimeException("Throwing some other exception");
        }
    }
    //endregion

    //region > on(Event)...
    // //////////////////////////////////////

    @Programmatic
    @Subscribe
    public void on(ToDoItem.AbstractActionInvokedEvent ev) {
        rejectIfRequired();
        recordEvent(ev);
        LOG.info(ev.getEventDescription() + ": " + container.titleOf(ev.getSource()));
    }


    @Programmatic
    @Subscribe
    public void on(PropertyChangedEvent<?,?> ev) {
        rejectIfRequired();
        recordEvent(ev);
        if(ev.getIdentifier().getMemberName().contains("description")) {
            String newValue = (String) ev.getNewValue();
            if(newValue.matches(".*demo veto.*")) {
                throw new RecoverableException("oh no you don't! " + ev.getNewValue());
            }
        }
        LOG.info(container.titleOf(ev.getSource()) + ", changed " + ev.getIdentifier().getMemberName() + " : " + ev.getOldValue() + " -> " + ev.getNewValue());
    }
    
    @Programmatic
    @Subscribe
    public void on(CollectionAddedToEvent<?,?> ev) {
        rejectIfRequired();
        recordEvent(ev);
        LOG.info(container.titleOf(ev.getSource()) + ", added to " + ev.getIdentifier().getMemberName() + " : " + ev.getValue());
    }
    
    @Programmatic
    @Subscribe
    public void on(CollectionRemovedFromEvent<?,?> ev) {
        rejectIfRequired();
        recordEvent(ev);
        LOG.info(container.titleOf(ev.getSource()) + ", removed from " + ev.getIdentifier().getMemberName() + " : " + ev.getValue());
    }

    //endregion

    //region > receivedEvents
    // //////////////////////////////////////
    
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
    public <T extends java.util.EventObject> T mostRecentlyReceivedEvent(Class<T> expectedType) {
        if (receivedEvents.isEmpty()) {
            return null;
        } 
        final EventObject ev = receivedEvents.get(0);
        if(!expectedType.isAssignableFrom(ev.getClass())) {
            return null;
        } 
        return expectedType.cast(ev);
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
        subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AcceptEvents);
    }

    //endregion


    //region > injected services
    // //////////////////////////////////////
    
    @javax.inject.Inject
    private DomainObjectContainer container;

    @SuppressWarnings("unused")
    private EventBusService eventBusService;
    public final void injectEventBusService(EventBusService eventBusService) {
        eventBusService.register(this);
    }
    //endregion


}
