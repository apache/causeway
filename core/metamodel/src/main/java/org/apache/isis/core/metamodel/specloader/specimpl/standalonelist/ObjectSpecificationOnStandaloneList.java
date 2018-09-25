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

package org.apache.isis.core.metamodel.specloader.specimpl.standalonelist;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetOnStandaloneList;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;

/**
 * A custom {@link ObjectSpecification} that is designed to treat the
 * {@link FreeStandingList} class as a "standalone" collection.
 */
public class ObjectSpecificationOnStandaloneList extends ObjectSpecificationAbstract {

    private static final String NAME = "Instances";
    private static final String DESCRIBED_AS = "Typed instances";
    private static final String ICON_NAME = "instances";

    // -- constructor

    public ObjectSpecificationOnStandaloneList(
            final ServicesInjector servicesInjector,
            final FacetProcessor facetProcessor) {
        super(FreeStandingList.class, NAME, servicesInjector, facetProcessor);
        this.specId = new ObjectSpecId(getCorrespondingClass().getName());
    }



    // -- Introspection

    @Override
    public void introspectTypeHierarchyAndMembers() {
        updateSuperclass(Object.class);

        addFacet(new CollectionFacetOnStandaloneList(this));
        addFacet(new TypeOfFacetDefaultToObject(this, getSpecificationLoader()) {
        });

        // ObjectList specific
        FacetUtil.addFacet(new NamedFacetOnStandaloneList(NAME, this));
        FacetUtil.addFacet(new PluralFacetOnStandaloneList(NAME, this));
        FacetUtil.addFacet(new DescribedAsFacetOnStandaloneList(DESCRIBED_AS, this));
        FacetUtil.addFacet(new ObjectSpecIdFacetOnStandaloneList(specId, this));
        // don't install anything for NotPersistableFacet
    }



    // -- isXxx

    @Override
    public boolean isService() {
        return false;
    }
    @Override
    public boolean isViewModel() {
        return false;
    }

    @Override
    public boolean isMixin() {
        return false;
    }

    @Override
    public boolean isViewModelCloneable(ObjectAdapter targetAdapter) {
        return false;
    }

    @Override
    public boolean isWizard() {
        return false;
    }



    // -- Associations
    /**
     * Review: is this ever called for an instance of this class? If not, then
     * no need to override.
     */
    @Override
    public ObjectAssociation getAssociation(final String id) {
        return null;
    }

    // -- Title and Icon
    
    @Override
    public String getTitle(ObjectAdapter contextAdapterIfAny, ObjectAdapter targetAdapter) {
        return ((FreeStandingList) targetAdapter.getObject()).titleString();
    }

    @Override
    public String getIconName(final Instance object) {
        return ICON_NAME;
    }

    // -- Object Actions
    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id, final List<ObjectSpecification> parameters) {
        return null;
    }

    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id) {
        return null;
    }

    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public ObjectAction getObjectAction(final String nameParmsIdentityString) {
        return null;
    }





}
