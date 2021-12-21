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
package org.apache.isis.viewer.wicket.ui.app.registry;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;

/**
 * Defines an API for registering {@link ComponentFactory}s.
 *
 * <p>
 * As used by {@link org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistrarDefault}.
 */
public interface ComponentFactoryRegistrar {

    public static class ComponentFactoryList implements Iterable<ComponentFactory> {

        private final List<ComponentFactory> componentFactories = _Lists.newArrayList();

        public void add(final ComponentFactory componentFactory) {
            if(componentFactories.contains(componentFactory)) {
                return;
            }
            componentFactories.add(componentFactory);
        }

        public void replace(final ComponentFactory replacementComponentFactory) {
            removeExisting(matching(replacementComponentFactory.getComponentType()));
            add(replacementComponentFactory);
        }

        public void replace(final Class<? extends ComponentFactory> toReplace, final ComponentFactory replacementComponentFactory) {
            int indexOfOldFactory = removeExisting(matching(toReplace));
            insert(indexOfOldFactory, replacementComponentFactory);
        }

        private void insert(final int indexToInsertInto, final ComponentFactory replacementComponentFactory) {
            if (indexToInsertInto > -1 && indexToInsertInto < componentFactories.size()) {
                componentFactories.add(indexToInsertInto, replacementComponentFactory);
            } else {
                componentFactories.add(replacementComponentFactory);
            }
        }

        private int removeExisting(final Predicate<ComponentFactory> predicate) {
            int indexOfFirst = -1;
            for (int i = 0; i < componentFactories.size(); i++) {
                ComponentFactory factory = componentFactories.get(i);
                if (predicate.test(factory)) {
                    componentFactories.remove(i);
                    if (indexOfFirst == -1) {
                        indexOfFirst = i;
                    }
                    i--;
                }
            }

            return indexOfFirst;
        }

        private static Predicate<ComponentFactory> matching(final ComponentType componentType) {
            return new Predicate<ComponentFactory>() {
                @Override
                public boolean test(final ComponentFactory input) {
                    return input.getComponentType() == componentType;
                }
            };
        }

        private static Predicate<ComponentFactory> matching(final Class<? extends ComponentFactory> toReplace) {
            return new Predicate<ComponentFactory>() {
                @Override
                public boolean test(final ComponentFactory input) {
                    return toReplace.isAssignableFrom(input.getClass());
                }
            };
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
    }

    void addComponentFactories(ComponentFactoryList componentFactoryList);
}
