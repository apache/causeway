/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.commandlog.applib.integtest.model;

import java.util.List;
import java.util.Objects;

import jakarta.inject.Inject;

import org.apache.causeway.applib.services.repository.RepositoryService;

public abstract class CounterRepository<X extends Counter> {

    private final Class<X> counterClass;

    public CounterRepository(final Class<X> counterClass) {
        this.counterClass = counterClass;
    }

    public List<X> find() {
        return repositoryService.allInstances(counterClass);
    }

    public X persist(final X counter) {
        return repositoryService.persistAndFlush(counter);
    }

    public void removeAll() {
        repositoryService.removeAll(counterClass);
    }

    @Inject RepositoryService repositoryService;

    public X findByName(final String name) {
        List<X> xes = find();
        return xes.stream().filter(x -> Objects.equals(x.getName(), name)).findFirst().orElseThrow();
    }
}
