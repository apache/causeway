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
package org.apache.causeway.core.metamodel.facets;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.id.LogicalType;

class DomainEventHelperTest_newCollectionDomainEvent_forAdd {

    static class SomeDomainObject {
        public Set<SomeReferencedObject> getReferences() { return null; }
    }
    static class SomeReferencedObject {}

    public static class SomeDomainObjectCollectionDomainEvent extends CollectionDomainEvent<SomeDomainObject, SomeReferencedObject> {}

    @Test
    void defaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.collectionIdentifier(
                LogicalType.fqcn(SomeDomainObject.class), "references");

        final CollectionDomainEvent<Object, Object> ev = _Utils.domainEventHelper().newCollectionDomainEvent(
                CollectionDomainEvent.Default.class, null, identifier, sdo);
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
    }

    @Test
    void collectionAddedToDefaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.collectionIdentifier(
                LogicalType.fqcn(SomeDomainObject.class), "references");

        final CollectionDomainEvent<Object, Object> ev = _Utils.domainEventHelper().newCollectionDomainEvent(
                CollectionDomainEvent.Default.class, AbstractDomainEvent.Phase.EXECUTED, identifier, sdo);
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
    }

    @Test
    void customEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final Identifier identifier = Identifier.collectionIdentifier(
                LogicalType.fqcn(SomeDomainObject.class), "references");

        final CollectionDomainEvent<SomeDomainObject, SomeReferencedObject> ev = _Utils.domainEventHelper().newCollectionDomainEvent(
                SomeDomainObjectCollectionDomainEvent.class, AbstractDomainEvent.Phase.EXECUTED, identifier, sdo);
        assertThat(ev.getSource(), is(sdo));
        assertThat(ev.getIdentifier(), is(identifier));
    }

}
