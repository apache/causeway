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
package org.apache.isis.core.metamodel.facets.object.domainobject.editing;

import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.immutable.EditingEnabledFacet;

import lombok.val;

/**
 *
 * @since 2.0
 *
 */
public class EditingEnabledFacetForDomainObjectAnnotation extends FacetAbstract implements EditingEnabledFacet {

    public static Optional<EditingEnabledFacetForDomainObjectAnnotation> create(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        val isEditingEnabled = domainObjectIfAny
        .map(DomainObject::editing)
        .map(editing->editing==Editing.ENABLED)
        .orElse(false);

        return isEditingEnabled
                ? Optional.of(new EditingEnabledFacetForDomainObjectAnnotation(holder))
                : Optional.empty();
    }

    protected EditingEnabledFacetForDomainObjectAnnotation(
            final FacetHolder holder) {
        super(EditingEnabledFacet.class, holder);
    }

}
