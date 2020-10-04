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
package demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class DomainObjectPublishingEnabledJdoEntities {

    public Optional<DomainObjectPublishingEnabledJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingEnabledJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingEnabledJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingEnabledJdo.class);
    }

    public Optional<DomainObjectPublishingEnabledJdo> first() {
        return all().stream().findFirst();
    }

    public DomainObjectPublishingEnabledJdo create(String newValue) {
        return repositoryService.persistAndFlush(new DomainObjectPublishingEnabledJdo(newValue));
    }

    public void remove(DomainObjectPublishingEnabledJdo enabledJdo) {
        repositoryService.removeAndFlush(enabledJdo);
    }

    @Inject
    RepositoryService repositoryService;

}
