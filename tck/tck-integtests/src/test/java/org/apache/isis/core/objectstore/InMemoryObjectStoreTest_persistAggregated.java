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

package org.apache.isis.core.objectstore;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.core.tck.dom.refs.ReferencingEntity;

public class InMemoryObjectStoreTest_persistAggregated {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(createPersistenceMechanismInstaller())
        .build();

    protected PersistenceMechanismInstaller createPersistenceMechanismInstaller() {
        return new InMemoryPersistenceMechanismInstaller();
    }

    @Test
    public void persistAggregatedEntityWithinCollection() throws Exception {
        final ReferencingEntity referencingEntity = iswf.fixtures.rfcg2;
        referencingEntity.addAggregatedEntityToCollection().setName("Aggregated Entity #1");
        iswf.persist(referencingEntity);
    }
    

    @Test
    public void persistAggregatedEntityWithinProperty() throws Exception {
        final ReferencingEntity referencingEntity = iswf.fixtures.rfcg2;
        referencingEntity.addAggregatedReference().setName("Aggregated Entity #1");
        iswf.persist(referencingEntity);
    }
    
}
