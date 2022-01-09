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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.Collections;
import java.util.Map;

import org.apache.isis.applib.util.Enums;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.properties.validating.PropertyValidateFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;

import lombok.Getter;

public enum MemberType {

    PROPERTY("properties/", RepresentationType.OBJECT_PROPERTY,
            _Maps.unmodifiable(
                    "modify", MutatorSpec.of(Rel.MODIFY, PropertyValidateFacet.class, PropertySetterFacet.class, RestfulHttpMethod.PUT, BodyArgs.ONE),
                    "clear", MutatorSpec.of(Rel.CLEAR, PropertyValidateFacet.class, PropertyClearFacet.class, RestfulHttpMethod.DELETE, BodyArgs.NONE))),
    /**
     * {@link #getMutators()} are empty}
     */
    COLLECTION("collections/", RepresentationType.OBJECT_COLLECTION,
            Collections.emptyMap()
            ),
    /**
     * {@link #getMutators()} are keyed by
     */
    ACTION("actions/", RepresentationType.OBJECT_ACTION,
            _Maps.unmodifiable(
                    "invokeQueryOnly", MutatorSpec.of(Rel.INVOKE, ActionValidationFacet.class, ActionInvocationFacet.class, RestfulHttpMethod.GET, BodyArgs.MANY, "invoke"),
                    "invokeIdempotent", MutatorSpec.of(Rel.INVOKE, ActionValidationFacet.class, ActionInvocationFacet.class, RestfulHttpMethod.PUT, BodyArgs.MANY, "invoke"),
                    "invoke", MutatorSpec.of(Rel.INVOKE, ActionValidationFacet.class, ActionInvocationFacet.class, RestfulHttpMethod.POST, BodyArgs.MANY, "invoke")));

    @Getter private final String urlPart;
    @Getter private final String name;
    @Getter private final RepresentationType representationType;
    @Getter private final Map<String, MutatorSpec> mutators;

    private MemberType(final String urlPart, final RepresentationType representationType, final Map<String, MutatorSpec> mutators) {
        this.urlPart = urlPart;
        this.representationType = representationType;
        this.mutators = mutators;
        name = Enums.enumToCamelCase(this);
    }

    public boolean isProperty() {
        return this == MemberType.PROPERTY;
    }

    public boolean isCollection() {
        return this == MemberType.COLLECTION;
    }

    public boolean isAction() {
        return this == MemberType.ACTION;
    }

    public static MemberType lookup(final String memberTypeName) {
        for (final MemberType memberType : values()) {
            if (memberType.getName().equals(memberTypeName)) {
                return memberType;
            }
        }
        return null;
    }

    public static MemberType of(final ObjectMember objectMember) {
        return objectMember.isAction() ? ACTION : objectMember.isOneToOneAssociation() ? PROPERTY : COLLECTION;
    }

    public static MemberType determineFrom(final ObjectFeature objectFeature) {
        if (objectFeature instanceof ObjectAction) {
            return MemberType.ACTION;
        }
        if (objectFeature instanceof OneToOneAssociation) {
            return MemberType.PROPERTY;
        }
        if (objectFeature instanceof OneToManyAssociation) {
            return MemberType.COLLECTION;
        }
        return null;
    }

}
