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
package org.apache.causeway.core.metamodel.object;

import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmEventUtils {

    public <T> boolean eventTypeIsPostable(
            final Class<? extends T> eventType,
            final Class<? extends T> noopClass,
            final Class<? extends T> defaultClass,
            final boolean configurationPropertyValue) {

        if (noopClass.isAssignableFrom(eventType)) {
            return false;
        }
        if (defaultClass.isAssignableFrom(eventType)) {
            return configurationPropertyValue;
        }
        return true;
    }

    public boolean isDomainEventPostable(final CausewayConfiguration config, final Class<? extends AbstractDomainEvent<?>> eventType) {
        if(ActionDomainEvent.class.isAssignableFrom(eventType)) {
            return MmEventUtils.eventTypeIsPostable(
                    eventType,
                    ActionDomainEvent.Noop.class,
                    ActionDomainEvent.Default.class,
                    config.getApplib().getAnnotation().getAction().getDomainEvent().isPostForDefault());
        }
        if(PropertyDomainEvent.class.isAssignableFrom(eventType)) {
            return MmEventUtils.eventTypeIsPostable(
                    eventType,
                    PropertyDomainEvent.Noop.class,
                    PropertyDomainEvent.Default.class,
                    config.getApplib().getAnnotation().getProperty().getDomainEvent().isPostForDefault());
        }
        if(CollectionDomainEvent.class.isAssignableFrom(eventType)) {
            return MmEventUtils.eventTypeIsPostable(
                    eventType,
                    CollectionDomainEvent.Noop.class,
                    CollectionDomainEvent.Default.class,
                    config.getApplib().getAnnotation().getCollection().getDomainEvent().isPostForDefault());
        }
        return false;
    }

}
