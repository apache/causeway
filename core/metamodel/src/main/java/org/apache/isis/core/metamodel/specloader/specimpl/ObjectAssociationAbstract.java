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

package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public abstract class ObjectAssociationAbstract extends ObjectMemberAbstract implements ObjectAssociation {

    private final ObjectSpecification specification;

    public ObjectAssociationAbstract(
            final FacetedMethod facetedMethod,
            final FeatureType featureType,
            final ObjectSpecification specification) {

        super(facetedMethod, featureType);
        if (specification == null) {
            throw new IllegalArgumentException("field type for '" + getId() + "' must exist");
        }
        this.specification = specification;
    }
    
    @Override
    public FacetHolder getFacetHolder() {
        return getFacetedMethod();
    }

    @Override
    public ObjectSpecification getOnType() {
        final PropertyOrCollectionAccessorFacet facet = getFacet(PropertyOrCollectionAccessorFacet.class);
        return facet.getOnType();
    }

    /**
     * Return the specification of the object (or objects) that this field
     * holds. For a value are one-to-one reference this will be type that the
     * accessor returns. For a collection it will be the type of element, not
     * the type of collection.
     */
    @Override
    public ObjectSpecification getSpecification() {
        return specification;
    }

    @Override
    public boolean isNotPersisted() {
        return containsFacet(NotPersistedFacet.class);
    }

    @Override
    public boolean hasChoices() {
        return containsFacet(PropertyChoicesFacet.class);
    }

    @Override
    public boolean isMandatory() {
        final MandatoryFacet mandatoryFacet = getFacet(MandatoryFacet.class);
        return mandatoryFacet != null && !mandatoryFacet.isInvertedSemantics();
    }

    @Override
    public abstract boolean isEmpty(final ManagedObject adapter, final InteractionInitiatedBy interactionInitiatedBy);

    @Override
    public boolean isOneToOneAssociation() {
        return !isOneToManyAssociation();
    }


}
