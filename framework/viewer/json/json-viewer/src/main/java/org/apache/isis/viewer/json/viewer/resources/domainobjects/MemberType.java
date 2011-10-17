/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.util.Map;

import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacet;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.util.Enums;
import org.apache.isis.viewer.json.viewer.representations.HttpMethod;
import org.apache.isis.viewer.json.viewer.representations.Rel;

import com.google.common.collect.ImmutableMap;

public enum MemberType {

    OBJECT_PROPERTY("properties/", "propertyId", Rel.DETAILS, RepresentationType.OBJECT_PROPERTY, 
            ImmutableMap.of(
                "modify", MutatorSpec.of(Rel.MODIFY, PropertyValidateFacet.class, PropertySetterFacet.class, HttpMethod.PUT, BodyArgs.ONE),
                "clear", MutatorSpec.of(Rel.CLEAR, PropertyValidateFacet.class, PropertyClearFacet.class, HttpMethod.DELETE, BodyArgs.NONE)
                )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            return objectMember.getSpecification();
        }
    },
    /**
     * {@link #getMutators()} are keyed by {@link CollectionSemantics#getAddToKey()}
     */
    OBJECT_COLLECTION("collections/", "collectionId", Rel.DETAILS, RepresentationType.OBJECT_COLLECTION, 
            ImmutableMap.of(
                "addToSet", MutatorSpec.of(Rel.ADD_TO, CollectionValidateAddToFacet.class, CollectionAddToFacet.class, HttpMethod.PUT, BodyArgs.ONE),
                "addToList", MutatorSpec.of(Rel.ADD_TO, CollectionValidateAddToFacet.class, CollectionAddToFacet.class, HttpMethod.POST, BodyArgs.ONE),
                "removeFrom", MutatorSpec.of(Rel.REMOVE_FROM, CollectionValidateRemoveFromFacet.class, CollectionRemoveFromFacet.class, HttpMethod.DELETE, BodyArgs.ONE)
                )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            return objectMember.getSpecification();
        }
    },
    /**
     * {@link #getMutators()} are keyed by {@link ActionSemantics#getInvokeKey()}
     */
    OBJECT_ACTION("actions/", "actionId", Rel.DETAILS, RepresentationType.OBJECT_ACTION,
            ImmutableMap.of(
                "invokeQueryOnly", MutatorSpec.of(Rel.INVOKE, ActionValidationFacet.class, ActionInvocationFacet.class, HttpMethod.GET, BodyArgs.MANY, "invoke"),
                "invokeIdempotent", MutatorSpec.of(Rel.INVOKE, ActionValidationFacet.class, ActionInvocationFacet.class, HttpMethod.PUT, BodyArgs.MANY, "invoke"),
                "invoke", MutatorSpec.of(Rel.INVOKE, ActionValidationFacet.class, ActionInvocationFacet.class, HttpMethod.POST, BodyArgs.MANY, "invoke")
            )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            ObjectAction objectAction = (ObjectAction) objectMember;
            return objectAction.getReturnType();
        }
    };

    private final String urlPart;
    private final String jsProp;
    private final Rel detailsRel;
    private final String name;
    private final RepresentationType representationType;

    private final Map<String, MutatorSpec> mutators;
    

    private MemberType(String urlPart, String jsProp, Rel detailsRel, RepresentationType representationType, Map<String, MutatorSpec> mutators) {
        this.urlPart = urlPart;
        this.jsProp = jsProp;
        this.detailsRel = detailsRel;
        this.representationType = representationType;
        this.mutators = mutators;
        name = Enums.enumToCamelCase(this);
    }

    public String getJsProp() {
        return jsProp;
    }

    public String getUrlPart() {
        return urlPart;
    }
    
    public Map<String, MutatorSpec> getMutators() {
        return mutators;
    }
    
    public abstract ObjectSpecification specFor(ObjectMember objectMember);

    public boolean isProperty() {
        return this == MemberType.OBJECT_PROPERTY;
    }

    public boolean isCollection() {
        return this == MemberType.OBJECT_COLLECTION;
    }

    public boolean isAction() {
        return this == MemberType.OBJECT_ACTION;
    }

    public static MemberType lookup(final String memberTypeName) {
        for (MemberType memberType : values()) {
            if(memberType.getName().equals(memberTypeName)) {
                return memberType;
            }
        }
    	return null;
    }

	public static MemberType of(ObjectMember objectMember) {
		return objectMember.isAction()?
				OBJECT_ACTION:
					objectMember.isOneToOneAssociation()?
						OBJECT_PROPERTY:
						OBJECT_COLLECTION;
	}

    public RepresentationType getRepresentationType() {
        return representationType;
    }

    public Rel getDetailsRel() {
        return detailsRel;
    }

    public String getName() {
        return name;
    }

    public static MemberType determineFrom(ObjectFeature objectFeature) {
        if (objectFeature instanceof ObjectAction) {
            return MemberType.OBJECT_ACTION;
        }
        if (objectFeature instanceof OneToOneAssociation) {
            return MemberType.OBJECT_PROPERTY;
        }
        if (objectFeature instanceof OneToManyAssociation) {
            return MemberType.OBJECT_COLLECTION;
        }
        return null;
    }



}
