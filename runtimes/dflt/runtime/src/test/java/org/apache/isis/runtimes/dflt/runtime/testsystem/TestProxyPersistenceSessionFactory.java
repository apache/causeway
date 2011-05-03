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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;

public class TestProxyPersistenceSessionFactory implements PersistenceSessionFactory {

    private PersistenceSession persistenceSession;
    private List<Object> services;

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    public TestProxyPersistenceSessionFactory() {
        services = Collections.emptyList();
    }

    /**
     * Not API.
     */
    @Override
    public void setServices(final List<Object> services) {
        this.services = services;
    }

    /**
     * Not API.
     */
    public void setPersistenceSessionToCreate(final PersistenceSession persistenceSession) {
        this.persistenceSession = persistenceSession;
    }

    @Override
    public PersistenceSession createPersistenceSession() {
        return persistenceSession;
    }

    @Override
    public DeploymentType getDeploymentType() {
        throw new NotYetImplementedException();
    }

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        // does nothing
    }

    @Override
    public SpecificationLoader getSpecificationLoader() {
        throw new NotYetImplementedException();
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

}
