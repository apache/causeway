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
package org.apache.causeway.core.metamodel.facets;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

/**
 * Introduced when {@link ManagedObject} was sealed.
 * (Works around cannot mock sealed interfaces.)
 */
public record Mocking(
    ObjectSpecification objSpec,
    SpecificationLoader specLoader,
    ServiceInjector serviceInjector) {

    public Mocking() {
        this(mock(ObjectSpecification.class), mock(SpecificationLoader.class), mock(ServiceInjector.class));
        when(objSpec.getSpecificationLoader()).thenReturn(specLoader);
        when(objSpec.getServiceInjector()).thenReturn(serviceInjector);
    }

    public ManagedObject asValue(final Object pojo) {
        when(objSpec.isPlural()).thenReturn(false);
        when(objSpec.isValue()).thenReturn(true);
        when(specLoader.specForType(pojo.getClass())).thenReturn(Optional.of(objSpec));
        return ManagedObject.value(objSpec, pojo);
    }

    public ManagedObject asViewmodel(final Object pojo) {
        when(objSpec.isPlural()).thenReturn(false);
        when(objSpec.isViewModel()).thenReturn(true);
        when(specLoader.specForType(pojo.getClass())).thenReturn(Optional.of(objSpec));
        return ManagedObject.viewmodel(objSpec, pojo, Optional.empty());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public PackedManagedObject asPacked(final Can can) {
        when(objSpec.isSingular()).thenReturn(true);
        when(objSpec.isPlural()).thenReturn(false);
        return ManagedObject.packed(objSpec, can);
    }

}
