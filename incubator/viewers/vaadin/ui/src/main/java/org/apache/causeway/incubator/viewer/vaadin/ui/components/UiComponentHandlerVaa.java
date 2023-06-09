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
package org.apache.causeway.incubator.viewer.vaadin.ui.components;

import java.util.Optional;

import com.vaadin.flow.component.Component;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentFactory;

public interface UiComponentHandlerVaa
extends UiComponentFactory.Handler<Component> {

    default <T> Optional<T> getFeatureValue(final @Nullable Class<T> type, final ManagedProperty managedProperty) {
        //TODO do a type check before the cast, so we can throw a more detailed exception
        // that is, given type must be assignable from the actual pojo type
        return Optional.ofNullable(managedProperty.getPropertyValue())
                .filter(_Predicates.not(ManagedObjects::isNullOrUnspecifiedOrEmpty))
                .map(ManagedObject::getPojo)
                .map(type::cast);
    }

}
