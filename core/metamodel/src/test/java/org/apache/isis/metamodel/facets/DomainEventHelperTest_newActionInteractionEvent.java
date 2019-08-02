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
package org.apache.isis.metamodel.facets;

import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.events.domain.ActionDomainEvent;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

public class DomainEventHelperTest_newActionInteractionEvent {

    public static class SomeDomainObject {
        public String foo(final int x, final String y) { return null; }
    }

    public static class SomeDomainObjectFooInvokedDomainEvent extends ActionDomainEvent<SomeDomainObject> {
        private static final long serialVersionUID = 1L;
    }

    @Test
    public void defaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.actionIdentifier(SomeDomainObject.class, "foo", new Class[]{int.class, String.class});

        Utils.domainEventHelper();
        final ActionDomainEvent<Object> ev = DomainEventHelper.newActionDomainEvent(
                ActionDomainEvent.Default.class, identifier, sdo, new Object[]{1, "bar"});
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getArguments(), is(not(nullValue())));
        assertEquals(ev.getArguments().get(0), Integer.valueOf(1));
        assertEquals(ev.getArguments().get(1), "bar");
    }

    @Test
    public void actionInvokedEventDefaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.actionIdentifier(SomeDomainObject.class, "foo", new Class[]{int.class, String.class});

        Utils.domainEventHelper();
        final ActionDomainEvent<Object> ev = DomainEventHelper.newActionDomainEvent(
                ActionDomainEvent.Default.class, identifier, sdo, new Object[]{1, "bar"});
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getArguments(), is(not(nullValue())));
        assertEquals(ev.getArguments().get(0), Integer.valueOf(1));
        assertEquals(ev.getArguments().get(1), "bar");
    }

    @Test
    public void customEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.actionIdentifier(SomeDomainObject.class, "foo", new Class[]{int.class, String.class});

        Utils.domainEventHelper();
        final ActionDomainEvent<SomeDomainObject> ev = DomainEventHelper.newActionDomainEvent(
                SomeDomainObjectFooInvokedDomainEvent.class, identifier, sdo, new Object[]{1, "bar"});
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getArguments(), is(not(nullValue())));
        assertEquals(ev.getArguments().get(0), Integer.valueOf(1));
        assertEquals(ev.getArguments().get(1), "bar");
    }

}
