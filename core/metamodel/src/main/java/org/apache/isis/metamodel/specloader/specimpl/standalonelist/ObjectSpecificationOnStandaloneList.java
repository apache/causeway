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

package org.apache.isis.metamodel.specloader.specimpl.standalonelist;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;
import org.apache.isis.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetOnStandaloneList;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract;

import static org.apache.isis.core.commons.internal.base._With.mapIfPresentElse;

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
            final FacetProcessor facetProcessor,
            final PostProcessor postProcessor) {
        super(FreeStandingList.class, NAME, facetProcessor, postProcessor);
        this.specId = ObjectSpecId.of(getCorrespondingClass().getName());

        FacetUtil.addFacet(
                new ObjectSpecIdFacetOnStandaloneList(specId, this));
    }



    // -- Introspection

    @Override
    protected void introspectTypeHierarchy() {
        loadSpecOfSuperclass(Object.class);

        addFacet(new CollectionFacetOnStandaloneList(this));
        addFacet(new TypeOfFacetDefaultToObject(this) {
        });

        // ObjectList specific
        FacetUtil.addFacet(new NamedFacetOnStandaloneList(NAME, this));
        FacetUtil.addFacet(new PluralFacetOnStandaloneList(NAME, this));
        FacetUtil.addFacet(new DescribedAsFacetOnStandaloneList(DESCRIBED_AS, this));
        FacetUtil.addFacet(new ObjectSpecIdFacetOnStandaloneList(specId, this));
        // don't install anything for NotPersistableFacet
    }

    @Override
    protected void introspectMembers() {
        // no-op.
    }

    @Override
    public BeanSort getBeanSort() {
        return BeanSort.COLLECTION;
    }

    // -- PREDICTATES

    @Override
    public boolean isViewModelCloneable(ManagedObject targetAdapter) {
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
    public String getTitle(ManagedObject contextAdapterIfAny, ManagedObject targetAdapter) {
        return ((FreeStandingList) targetAdapter.getPojo()).titleString();
    }

    @Override
    public String getIconName(final ManagedObject object) {
        return ICON_NAME;
    }

    // -- Object Actions
    /**
     * Review: is it necessary to override for this subclass?
     */
    @Override
    public ObjectAction getObjectAction(
            final ActionType type, 
            final String id, 
            final Can<ObjectSpecification> parameters) {
        
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

    // -- ELEMENT SPECIFICATION

    private final _Lazy<ObjectSpecification> elementSpecification = _Lazy.of(this::lookupElementSpecification); 

    @Override
    public ObjectSpecification getElementSpecification() {
        return elementSpecification.get();
    }

    private ObjectSpecification lookupElementSpecification() {
        return mapIfPresentElse(
                getFacet(TypeOfFacet.class), 
                typeOfFacet -> ElementSpecificationProvider.of(typeOfFacet).getElementType(), 
                null);
    }



    @Override
    public String getManagedBeanName() {
        return null; // not a managed-bean
    }

    // --


}
