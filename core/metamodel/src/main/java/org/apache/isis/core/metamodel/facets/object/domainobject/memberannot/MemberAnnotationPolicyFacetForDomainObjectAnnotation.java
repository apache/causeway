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

package org.apache.isis.core.metamodel.facets.object.domainobject.memberannot;

import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberAnnotations;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.memberannot.MemberAnnotationPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.memberannot.MemberAnnotationPolicyFacetAbstract;

public class MemberAnnotationPolicyFacetForDomainObjectAnnotation
extends MemberAnnotationPolicyFacetAbstract {

    /**
     * If {@link DomainObject} annotation not present
     * or {@link DomainObject#memberAnnotations()}
     * is unspecified or null returns an empty Optional.
     */
    public static Optional<MemberAnnotationPolicyFacet> create(
            final Optional<DomainObject> domainObjectIfAny,
            final Class<?> correspondingClass,
            final FacetHolder holder) {

        return domainObjectIfAny
                .map(annot->annot.memberAnnotations())
                .filter(_NullSafe::isPresent)
                .filter(memberAnnotations->!memberAnnotations.isNotSpecified())
                .map(memberAnnotations -> new MemberAnnotationPolicyFacetForDomainObjectAnnotation(
                        memberAnnotations,
                        holder));
    }

    private MemberAnnotationPolicyFacetForDomainObjectAnnotation(
            final MemberAnnotations memberAnnotations,
            final FacetHolder holder) {
        super(memberAnnotations, holder);
    }

}
