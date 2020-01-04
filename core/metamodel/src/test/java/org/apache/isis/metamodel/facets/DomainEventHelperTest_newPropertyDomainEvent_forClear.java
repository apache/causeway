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

import org.joda.time.LocalDate;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;

import static junit.framework.Assert.assertEquals;

public class DomainEventHelperTest_newPropertyDomainEvent_forClear {

    public static class SomeDomainObject {}

    public static class SomeDatePropertyChangedDomainEvent extends PropertyDomainEvent<SomeDomainObject, LocalDate> {
    }

    @Test
    public void defaultEventType() throws Exception {

        SomeDomainObject sdo = new SomeDomainObject();
        Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "someDateProperty");
        LocalDate oldValue = new LocalDate(2013,4,1);
        LocalDate newValue = null;

        Utils.domainEventHelper();
        final PropertyDomainEvent<Object, Object> ev =
                DomainEventHelper.newPropertyDomainEvent(PropertyDomainEvent.Default.class, identifier, sdo, oldValue, newValue);
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
        assertEquals(ev.getOldValue(), oldValue);
        assertNull(ev.getNewValue());
    }


    @Test
    public void customEventType() throws Exception {

        SomeDomainObject sdo = new SomeDomainObject();
        Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "someDateProperty");
        LocalDate oldValue = new LocalDate(2013,4,1);
        LocalDate newValue = null;

        Utils.domainEventHelper();
        final PropertyDomainEvent<SomeDomainObject, LocalDate> ev =
                DomainEventHelper.newPropertyDomainEvent(SomeDatePropertyChangedDomainEvent.class, identifier, sdo, oldValue, newValue);
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getOldValue(), is(oldValue));
        assertNull(ev.getNewValue());
    }

}
