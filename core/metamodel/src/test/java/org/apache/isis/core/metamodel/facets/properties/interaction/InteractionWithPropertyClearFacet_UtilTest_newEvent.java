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
package org.apache.isis.core.metamodel.facets.properties.interaction;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.PropertyInteractionEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class InteractionWithPropertyClearFacet_UtilTest_newEvent {

    public static class SomeDomainObject {}
    
    public static class SomeDatePropertyChangedEvent extends PropertyInteractionEvent<SomeDomainObject, LocalDate> {
        private static final long serialVersionUID = 1L;
        public SomeDatePropertyChangedEvent(SomeDomainObject source, Identifier identifier, LocalDate oldValue, LocalDate newValue) {
            super(source, identifier, oldValue, newValue);
        }
    }

    @Test
    public void defaultEventType() throws Exception {
        
        SomeDomainObject sdo = new SomeDomainObject();
        Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "someDateProperty");
        LocalDate oldValue = new LocalDate(2013,4,1);
        LocalDate newValue = null;
        
        final PropertyInteractionEvent<Object, Object> ev =
                InteractionWithPropertyClearFacet.Util.newEvent(PropertyInteractionEvent.Default.class, sdo, identifier, oldValue, newValue);
        assertThat(ev.getSource(), is((Object)sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getOldValue(), is((Object)oldValue));
        assertThat(ev.getNewValue(), is((Object)newValue));
    }


    @Test
    public void customEventType() throws Exception {

        SomeDomainObject sdo = new SomeDomainObject();
        Identifier identifier = Identifier.propertyOrCollectionIdentifier(SomeDomainObject.class, "someDateProperty");
        LocalDate oldValue = new LocalDate(2013,4,1);
        LocalDate newValue = null;
        
        final PropertyInteractionEvent<SomeDomainObject, LocalDate> ev =
                InteractionWithPropertyClearFacet.Util.newEvent(SomeDatePropertyChangedEvent.class, sdo, identifier, oldValue, newValue);
        assertThat(ev.getSource(), is(sdo));
        assertThat(ev.getIdentifier(), is(identifier));
        assertThat(ev.getOldValue(), is(oldValue));
        assertThat(ev.getNewValue(), is(nullValue()));
    }
    
}
