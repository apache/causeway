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
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacet;
import org.apache.isis.viewer.json.applib.util.Enums;
import org.apache.isis.viewer.json.viewer.representations.HttpMethod;

import com.google.common.collect.ImmutableMap;

public enum MemberType {

    OBJECT_PROPERTY("properties/", "propertyId", "propertyDetails", 
            ImmutableMap.of(
                "modify", MutatorSpec.of(PropertyValidateFacet.class, PropertySetterFacet.class, HttpMethod.PUT, BodyArgs.ONE),
                "clear", MutatorSpec.of(PropertyValidateFacet.class, PropertyClearFacet.class, HttpMethod.DELETE, BodyArgs.NONE)
                )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            return objectMember.getSpecification();
        }
    },
    /**
     * {@link #getMutators()} are keyed by {@link CollectionSemantics#getAddToKey()}
     */
    OBJECT_COLLECTION("collections/", "collectionId", "collectionDetails", 
            ImmutableMap.of(
                "addToSet", MutatorSpec.of(CollectionValidateAddToFacet.class, CollectionAddToFacet.class, HttpMethod.PUT, BodyArgs.ONE),
                "addToList", MutatorSpec.of(CollectionValidateAddToFacet.class, CollectionAddToFacet.class, HttpMethod.POST, BodyArgs.ONE),
                "removeFrom", MutatorSpec.of(CollectionValidateRemoveFromFacet.class, CollectionRemoveFromFacet.class, HttpMethod.DELETE, BodyArgs.ONE)
                )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            return objectMember.getSpecification();
        }
    },
    /**
     * {@link #getMutators()} are keyed by {@link ActionSemantics#getInvokeKey()}
     */
    OBJECT_ACTION("actions/", "actionId", "actionDetails",
            ImmutableMap.of(
                "invokeQueryOnly", MutatorSpec.of(ActionValidationFacet.class, ActionInvocationFacet.class, HttpMethod.GET, BodyArgs.MANY, "invoke"),
                "invokeIdempotent", MutatorSpec.of(ActionValidationFacet.class, ActionInvocationFacet.class, HttpMethod.PUT, BodyArgs.MANY, "invoke"),
                "invoke", MutatorSpec.of(ActionValidationFacet.class, ActionInvocationFacet.class, HttpMethod.POST, BodyArgs.MANY, "invoke")
            )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            ObjectAction objectAction = (ObjectAction) objectMember;
            return objectAction.getReturnType();
        }
    };

    private final Map<String, MutatorSpec> mutators;
    
    private final String urlPart;
    private final String detailsRel;
    private final String name;
    private final String jsProp;
    
    private MemberType(String urlPart, String jsProp, String detailsRel, Map<String, MutatorSpec> mutators) {
        this.urlPart = urlPart;
        this.jsProp = jsProp;
        this.detailsRel = detailsRel;
        this.mutators = mutators;
        name = Enums.enumToCamelCase(this);
    }

    public String getJsProp() {
        return jsProp;
    }

    public String urlPart() {
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

    public String getDetailsRel() {
        return detailsRel;
    }

    public String getName() {
        return name;
    }

    public static MemberType determineFrom(ObjectMember objectMember) {
        if (objectMember instanceof ObjectAction) {
            return MemberType.OBJECT_ACTION;
        }
        if (objectMember instanceof OneToOneAssociation) {
            return MemberType.OBJECT_PROPERTY;
        }
        return MemberType.OBJECT_COLLECTION;
    }


}
