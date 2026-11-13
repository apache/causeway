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
package org.apache.causeway.viewer.wicket.ui.app.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;

public record ComponentFactoryList(
        Set<ComponentFactory> componentFactories) implements Iterable<ComponentFactory> {

    public ComponentFactoryList() {
        this(new LinkedHashSet<>());
    }

    public void add(final ComponentFactory componentFactory) {
        componentFactories.add(componentFactory);
    }

    @Override
    public Iterator<ComponentFactory> iterator() {
        return componentFactories.iterator();
    }

    public Stream<ComponentFactory> stream() {
        return componentFactories.stream();
    }

    public <T extends ComponentFactory> Stream<T> stream(final Class<T> requiredClass) {
        return stream()
                .filter(requiredClass::isInstance)
                .map(requiredClass::cast);
    }

    public ListMultimap<UiComponentType, ComponentFactory> asFactoriesByComponentType() {
        var map = _Multimaps.<UiComponentType, ComponentFactory>newListMultimap();
        stream().forEach(cf->map.putElement(cf.getComponentType(), cf));
        return map.asUnmodifiable();
    }

    public Map<Class<? extends ComponentFactory>, ComponentFactory> asFactoriesByType() {
        var map = new HashMap<Class<? extends ComponentFactory>, ComponentFactory>();
        stream().forEach(cf->map.put(cf.getClass(), cf));
        return Collections.unmodifiableMap(map);
    }

}