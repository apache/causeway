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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.util.stream.Stream;

import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.viewer.context.ResourceContext;

/**
 * Utility class that encapsulates the logic for updating an
 * {@link ManagedObject}'s with the
 * values of a {@link org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation}.
 *
 * <p>
 *     Used in persist or multi-property update
 * </p>
 */
public class ObjectAdapterUpdateHelper {

    private final ManagedObject objectAdapter;
    private final ResourceContext resourceContext;

    public ObjectAdapterUpdateHelper(ResourceContext resourceContext, ManagedObject objectAdapter) {
        this.objectAdapter = objectAdapter;
        this.resourceContext = resourceContext;
    }


    enum Intent {
        UPDATE_EXISTING,
        PERSISTING_NEW;
        private boolean shouldValidate() {
            return this == UPDATE_EXISTING;
        }
    }

    boolean copyOverProperties(
            final JsonRepresentation propertiesMap,
            final Intent intent) {

        final ObjectSpecification objectSpec = objectAdapter.getSpecification();
        final Stream<ObjectAssociation> properties = objectSpec.streamAssociations(Contributed.EXCLUDED)
                .filter(ObjectAssociation.Predicates.PROPERTIES);

        final boolean[] allOk = {true}; // simply a non-thread-safe value reference

        properties.forEach(association->{
            allOk[0] &= copyOverProperty(association, propertiesMap, intent);
        });

        return allOk[0];
    }

    private boolean copyOverProperty(
            final ObjectAssociation association, 
            final JsonRepresentation propertiesMap,
            final Intent intent) {

        boolean allOk = true;

        final OneToOneAssociation property = (OneToOneAssociation) association;
        final ObjectSpecification propertySpec = property.getSpecification();
        final String id = property.getId();
        final JsonRepresentation propertyRepr = propertiesMap.getRepresentation(id);
        final Consent visibility = property.isVisible(
                objectAdapter,
                resourceContext.getInteractionInitiatedBy(),
                resourceContext.getWhere());
        final Consent usability = property.isUsable(
                objectAdapter,
                resourceContext.getInteractionInitiatedBy(),
                resourceContext.getWhere()
                );

        final boolean invisible = visibility.isVetoed();
        final boolean disabled = usability.isVetoed();
        final boolean valueProvided = propertyRepr != null;

        if(!valueProvided) {

            // no value provided
            if(intent.shouldValidate()) {
                if(invisible || disabled) {
                    // that's ok, indeed expected
                    return allOk;
                }
            }
            if (!property.isMandatory()) {
                // optional, so also not a problem
                return allOk;
            }

            // otherwise, is an error.
            final String invalidReason = propertiesMap.getString("x-ro-invalidReason");
            if(invalidReason != null) {
                propertiesMap.mapPut("x-ro-invalidReason", invalidReason + "; " + property.getName());
            } else {
                propertiesMap.mapPut("x-ro-invalidReason", "Mandatory field(s) missing: " + property.getName());
            }
            allOk = false;
            return allOk;

        } else {

            if(intent.shouldValidate()) {
                // value has been provided
                if (invisible) {
                    // silently ignore; don't want to acknowledge the
                    // existence of this property to the caller
                    return allOk;
                }
                if (disabled) {
                    // not allowed to update
                    propertyRepr.mapPut("invalidReason", usability.getReason());
                    allOk = false;
                    return allOk;
                }
            }

            // ok, we have a value, and
            // (if validating) then the property is not invisible, and is not disabled
            final ManagedObject valueAdapter;
            try {
                valueAdapter = new JsonParserHelper(resourceContext, propertySpec).objectAdapterFor(propertyRepr);
            } catch(IllegalArgumentException ex) {
                propertyRepr.mapPut("invalidReason", ex.getMessage());
                allOk = false;
                return allOk;
            }
            // check if the proposed value is valid
            final Consent validity = property.isAssociationValid(objectAdapter, valueAdapter,
                    InteractionInitiatedBy.USER);
            if (validity.isAllowed()) {
                try {
                    property.set(
                            objectAdapter, valueAdapter,
                            resourceContext.getInteractionInitiatedBy());
                } catch (final IllegalArgumentException ex) {
                    propertyRepr.mapPut("invalidReason", ex.getMessage());
                    allOk = false;
                }
            } else {
                propertyRepr.mapPut("invalidReason", validity.getReason());
                allOk = false;
            }
        }
        return allOk;
    }


}
