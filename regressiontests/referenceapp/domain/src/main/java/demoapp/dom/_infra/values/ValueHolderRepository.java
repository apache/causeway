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
package demoapp.dom._infra.values;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.apache.causeway.applib.services.repository.RepositoryService;

import demoapp.dom.types.Samples;
import lombok.AccessLevel;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ValueHolderRepository<T, E extends ValueHolder<T>> {

    @Inject protected RepositoryService repositoryService;

    private final Class<E> entityType;

    // -- REPOSITORY BASICS

    public Optional<E> find(final T readOnlyProperty) {
        return repositoryService
                .firstMatch(
                        entityType,
                        x -> Objects.equals(x.value(), readOnlyProperty));
    }

    public List<E> all() {
        return repositoryService.allInstances(entityType);
    }

    protected abstract E newDetachedEntity(T value);

    public Optional<E> first() {
        return all().stream().findFirst();
    }

    public List<E> firstAsList() {
        List<E> all = all();
        return all.isEmpty() ? Collections.emptyList() : Collections.singletonList(all.get(0));
    }
    public List<E> lastAsList() {
        List<E> all = all();
        return all.isEmpty() ? Collections.emptyList() : Collections.singletonList(all.get(all.size()-1));
    }

    public void remove(final Object entity) {
        repositoryService.removeAndFlush(entity);
    }

    public E create(final T value) {
        // emits 'created' life-cycle event
        var detachedEntity = repositoryService.detachedEntity(newDetachedEntity(value));
        // persist
        return repositoryService.persistAndFlush(detachedEntity);
    }

    // -- SEEDING SUPPORT

    @Inject protected Samples<T> samples;

    public void seedSamples(
            final @NonNull Consumer<E> onSamplePersisted) {

        samples.stream()
                .map(this::newDetachedEntity)
                .peek(repositoryService::persist)
                .forEach(onSamplePersisted);
    }

}
