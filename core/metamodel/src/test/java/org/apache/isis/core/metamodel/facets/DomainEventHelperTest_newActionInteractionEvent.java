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
package org.apache.isis.core.metamodel.facets;

import org.junit.Test;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionInvokedEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DomainEventHelperTest_newActionInteractionEvent {

    public static class SomeDomainObject {
        public String foo(final int x, final String y) { return null; }
    }
    
    public static class SomeDomainObjectFooInvokedEvent extends ActionDomainEvent<SomeDomainObject> {
        private static final long serialVersionUID = 1L;
        public SomeDomainObjectFooInvokedEvent(final SomeDomainObject source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }
    
    @Test
    public void defaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.actionIdentifier(SomeDomainObject.class, "foo", new Class[]{int.class, String.class});

        final ActionDomainEvent<Object> ev = new DomainEventHelper(null).newActionDomainEvent(
                ActionDomainEvent.Default.class, identifier, sdo, new Object[]{1, "bar"});
        assertThat(ev.getSource(), is((Object)sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getArguments(), is(not(nullValue())));
        assertThat(ev.getArguments().get(0), is((Object)Integer.valueOf(1)));
        assertThat(ev.getArguments().get(1), is((Object)"bar"));
    }

    @Test
    public void actionInvokedEventDefaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.actionIdentifier(SomeDomainObject.class, "foo", new Class[]{int.class, String.class});

        final ActionDomainEvent<Object> ev = new DomainEventHelper(null).newActionDomainEvent(
                ActionInvokedEvent.Default.class, identifier, sdo, new Object[]{1, "bar"});
        assertThat(ev.getSource(), is((Object)sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getArguments(), is(not(nullValue())));
        assertThat(ev.getArguments().get(0), is((Object)Integer.valueOf(1)));
        assertThat(ev.getArguments().get(1), is((Object)"bar"));
    }

    @Test
    public void customEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.actionIdentifier(SomeDomainObject.class, "foo", new Class[]{int.class, String.class});
        
        final ActionDomainEvent<SomeDomainObject> ev = new DomainEventHelper(null).newActionDomainEvent(
                SomeDomainObjectFooInvokedEvent.class, identifier, sdo, new Object[]{1, "bar"});
        assertThat((SomeDomainObject)ev.getSource(), is(sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getArguments(), is(not(nullValue())));
        assertThat(ev.getArguments().get(0), is((Object)Integer.valueOf(1)));
        assertThat(ev.getArguments().get(1), is((Object)"bar"));
    }
    
}
