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
package org.apache.isis.viewer.json.viewer.resources.objects;

import java.util.Map;

import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.progmodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacet;
import org.apache.isis.viewer.json.viewer.representations.HttpMethod;

import com.google.common.collect.ImmutableMap;

public enum MemberType {

    PROPERTY("properties/", ImmutableMap.of(
            "modify", MutatorSpec.of(PropertyValidateFacet.class, PropertySetterFacet.class, HttpMethod.PUT, BodyArgs.ONE),
            "clear", MutatorSpec.of(PropertyValidateFacet.class, PropertyClearFacet.class, HttpMethod.DELETE, BodyArgs.NONE)
            )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            return objectMember.getSpecification();
        }
    },
    COLLECTION("collections/", ImmutableMap.of(
            "addTo", MutatorSpec.of(CollectionValidateAddToFacet.class, CollectionAddToFacet.class, HttpMethod.PUT, BodyArgs.ONE),
            "removeFrom", MutatorSpec.of(CollectionValidateRemoveFromFacet.class, CollectionRemoveFromFacet.class, HttpMethod.DELETE, BodyArgs.ONE)
            )) {
        @Override
        public ObjectSpecification specFor(ObjectMember objectMember) {
            return objectMember.getSpecification();
        }
    },
    ACTION("actions/", ImmutableMap.of(
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
    private MemberType(String urlPart, Map<String, MutatorSpec> mutators) {
        this.urlPart = urlPart;
        this.mutators = mutators;
    }

    public String key() {
        return name().toLowerCase() + "Id";
    }

    public String urlPart() {
        return urlPart;
    }
    
    public Map<String, MutatorSpec> getMutators() {
        return mutators;
    }
    
    public abstract ObjectSpecification specFor(ObjectMember objectMember);

    public boolean isProperty() {
        return this == MemberType.PROPERTY;
    }

    public boolean isCollection() {
        return this == MemberType.COLLECTION;
    }

    public boolean isAction() {
        return this == MemberType.ACTION;
    }

    public static MemberType lookup(final String memberType) {
    	return valueOf(memberType.toUpperCase());
    }

	public static MemberType of(ObjectMember objectMember) {
		return objectMember.isAction()?
				ACTION:
					objectMember.isOneToOneAssociation()?
						PROPERTY:
						COLLECTION;
	}

}
