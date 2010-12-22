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


package org.apache.isis.core.metamodel.spec;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectList;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;


/**
 * A custom {@link ObjectSpecification} that is designed to treat the
 * {@link ObjectList} class as a "standalone" collection.
 */
public class InstanceCollectionSpecification extends IntrospectableSpecificationAbstract {

    /**
     * Used as {@link #getShortName()}, {@link #getName()} and {@link #getPluralName()}.
     */
	private static final String NAME = "Instances";
	private static final String DESCRIBED_AS = "Typed instances";
	private static final String ICON_NAME = "instances";

    public InstanceCollectionSpecification(
    		final SpecificationLookup specificationLookup,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final ServicesProvider servicesProvider,
            final ObjectInstantiator objectInstantiator) {
		super(ObjectList.class, NAME, authenticationSessionProvider, servicesProvider, objectInstantiator, specificationLookup);
	}

    
    ///////////////////////////////////////////////////////////
    // Intrinsic to class
    ///////////////////////////////////////////////////////////
    
    /**
     * Review: wouldn't it be better to have a FacetFactory that specifically
     * recognized {@link ObjectList} as a type and installed a {@link CollectionFacet}
     * for it.
     */
    @Override
    public boolean isCollectionOrIsAggregated() {
        return true;
    }

    ///////////////////////////////////////////////////////////
    // Introspection
    ///////////////////////////////////////////////////////////

    @Override
    public void introspectTypeHierarchyAndMembers() {
        setSuperclass(Object.class);

        addFacet(new InstancesCollectionFacet(this));
        addFacet(new TypeOfFacetDefaultToObject(this, getSpecificationLookup()) {});
    }

    @Override
    public void completeIntrospection() {
        setSingularName(NAME);
        setPluralName(NAME);
        setDescribedAs(DESCRIBED_AS);
        // REVIEW... is this right?
        setPersistability(Persistability.USER_PERSISTABLE);
        
        setIntrospected(true);
    }



    ///////////////////////////////////////////////////////
    // Service
    ///////////////////////////////////////////////////////

    /**
     * No-op.
     *
     * <p>
     * Review: is this ever called for an instance of this class?  If not, then
     * no need to override.
     */
    @Override
    public void markAsService() {}

    @Override
    public boolean isService() {
        return false;
    }


    ///////////////////////////////////////////////////////
    // Associations
    ///////////////////////////////////////////////////////

    /**
     * Review: is this ever called for an instance of this class?  If not, then
     * no need to override.
     */
    @Override
    public ObjectAssociation getAssociation(final String id) {
        return null;
    }


    ///////////////////////////////////////////////////////
    // Title and Icon
    ///////////////////////////////////////////////////////

    @Override
    public String getTitle(final ObjectAdapter object) {
        return ((ObjectList) object.getObject()).titleString();
    }

    @Override
    public String getIconName(final ObjectAdapter object) {
        return ICON_NAME;
    }


    
    ///////////////////////////////////////////////////////
    // Object Actions
    ///////////////////////////////////////////////////////

    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String id,
            final List<ObjectSpecification> parameters) {
        return null;
    }

    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String id) {
        return null;
    }

    ///////////////////////////////////////////////////////
    // Service Actions
    ///////////////////////////////////////////////////////

    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public List<ObjectAction> getServiceActionsReturning(final ObjectActionType... type) {
        return Collections.emptyList();
    }


}

