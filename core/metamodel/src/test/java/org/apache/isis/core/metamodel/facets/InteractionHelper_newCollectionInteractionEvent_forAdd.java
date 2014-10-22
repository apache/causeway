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

import java.util.Set;
import org.junit.Test;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InteractionHelper_newCollectionInteractionEvent_forAdd {

    public static class SomeDomainObject {
        public Set<SomeReferencedObject> getReferences() { return null; }
    }
    public static class SomeReferencedObject {}
    
    public static class SomeDomainObjectCollectionEvent extends CollectionInteractionEvent<SomeDomainObject, SomeReferencedObject> {
        private static final long serialVersionUID = 1L;
        public SomeDomainObjectCollectionEvent(SomeDomainObject source, Identifier identifier, Of of, SomeReferencedObject value) {
            super(source, identifier, of, value);
        }
    }
    
    @Test
    public void defaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final SomeReferencedObject other = new SomeReferencedObject();
        final Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "references");

        final CollectionInteractionEvent<Object, Object> ev = new InteractionHelper(null).newCollectionInteractionEvent(
                CollectionInteractionEvent.Default.class, null, identifier, sdo, CollectionInteractionEvent.Of.ADD_TO, other);
        assertThat(ev.getSource(), is((Object)sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getOf(), is(CollectionInteractionEvent.Of.ADD_TO));
        assertThat(ev.getValue(), is((Object)other));
    }

    @Test
    public void collectionAddedToDefaultEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final SomeReferencedObject other = new SomeReferencedObject();
        final Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "references");

        final CollectionInteractionEvent<Object, Object> ev = new InteractionHelper(null).newCollectionInteractionEvent(
                CollectionAddedToEvent.Default.class, AbstractInteractionEvent.Phase.EXECUTED, identifier, sdo, CollectionInteractionEvent.Of.ADD_TO, other);
        assertThat(ev.getSource(), is((Object)sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getOf(), is(CollectionInteractionEvent.Of.ADD_TO));
        assertThat(ev.getValue(), is((Object)other));
    }

    @Test
    public void customEventType() throws Exception {
        final SomeDomainObject sdo = new SomeDomainObject();
        final SomeReferencedObject other = new SomeReferencedObject();
        final Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "references");
        
        final CollectionInteractionEvent<SomeDomainObject, SomeReferencedObject> ev = new InteractionHelper(null).newCollectionInteractionEvent(
                SomeDomainObjectCollectionEvent.class, AbstractInteractionEvent.Phase.EXECUTED, identifier, sdo, CollectionInteractionEvent.Of.ADD_TO, other);
        assertThat(ev.getSource(), is(sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getOf(), is(CollectionInteractionEvent.Of.ADD_TO));
        assertThat(ev.getValue(), is(other));
    }
    
}
