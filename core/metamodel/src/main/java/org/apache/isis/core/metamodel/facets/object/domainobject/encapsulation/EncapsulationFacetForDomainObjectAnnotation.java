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

package org.apache.isis.core.metamodel.facets.object.domainobject.encapsulation;

import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Encapsulation;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encapsulation.EncapsulationFacetAbstract;

public class EncapsulationFacetForDomainObjectAnnotation
extends EncapsulationFacetAbstract {

    /**
     * If {@link DomainObject} annotation not present
     * or {@link DomainObject#encapsulation()}
     * is unspecified or null returns an empty Optional.
     */
    public static Optional<EncapsulationFacetAbstract> create(
            final Optional<DomainObject> domainObjectIfAny,
            final Class<?> correspondingClass,
            final FacetHolder holder) {

        return domainObjectIfAny
                .map(annot->annot.encapsulation())
                .filter(_NullSafe::isPresent)
                .filter(encapsulation->!encapsulation.isNotSpecified())
                .map(encapsulation -> new EncapsulationFacetForDomainObjectAnnotation(
                        encapsulation,
                        holder));
    }

    private EncapsulationFacetForDomainObjectAnnotation(
            final Encapsulation encapsulation,
            final FacetHolder holder) {
        super(encapsulation, holder);
    }

}
