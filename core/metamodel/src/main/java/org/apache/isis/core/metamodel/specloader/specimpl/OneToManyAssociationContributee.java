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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacet;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacetAbstract;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.progmodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.DisabledFacetImpl;

public class OneToManyAssociationContributee extends OneToManyAssociationImpl implements ContributeeMember {

    private final ObjectAdapter serviceAdapter;
    private final ObjectAction objectAction;
    

    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();
    
    private final Identifier identifier;

    private static ObjectSpecification typeOfSpec(final ObjectActionImpl objectAction, ObjectMemberContext objectMemberContext) {
        final TypeOfFacet actionTypeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        return objectMemberContext.getSpecificationLookup().loadSpecification(actionTypeOfFacet.value());
    }
    
    public OneToManyAssociationContributee(
            final ObjectAdapter serviceAdapter, 
            final ObjectActionImpl serviceAction,
            final ObjectSpecification contributeeType,
            final ObjectMemberContext objectMemberContext) {
        super(serviceAction.getFacetedMethod(), typeOfSpec(serviceAction, objectMemberContext), objectMemberContext);
        this.serviceAdapter = serviceAdapter;
        this.objectAction = serviceAction;

        // copy over facets from contributed to own.
        FacetUtil.copyFacets(serviceAction.getFacetedMethod(), facetHolder);
        
        final RenderFacet renderFacet = new RenderFacetAbstract(Render.Type.EAGERLY, this) {};
        final NotPersistedFacet notPersistedFacet = new NotPersistedFacetAbstract(this) {};
        final DisabledFacet disabledFacet = disabledFacet();
        final TypeOfFacet typeOfFacet = new TypeOfFacetAbstract(getSpecification().getCorrespondingClass(), this, objectMemberContext.getSpecificationLookup()) {}; 
        
        FacetUtil.addFacet(renderFacet);
        FacetUtil.addFacet(notPersistedFacet);
        FacetUtil.addFacet(disabledFacet);
        FacetUtil.addFacet(typeOfFacet);
        
        // calculate the identifier
        final Identifier contributorIdentifier = serviceAction.getFacetedMethod().getIdentifier();
        final String memberName = contributorIdentifier.getMemberName();
        List<String> memberParameterNames = contributorIdentifier.getMemberParameterNames();
        
        identifier = Identifier.actionIdentifier(contributeeType.getCorrespondingClass().getName(), memberName, memberParameterNames);
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null && 
            originalFacet.when() == When.ALWAYS && 
            originalFacet.where() == Where.ANYWHERE) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetImpl(When.ALWAYS, Where.ANYWHERE, "Contributed collection", this);
    }

    @Override
    public ObjectAdapter get(final ObjectAdapter ownerAdapter) {
        return objectAction.execute(serviceAdapter, new ObjectAdapter[]{ownerAdapter});
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }
    
    // //////////////////////////////////////
    // FacetHolder
    // //////////////////////////////////////
    
    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return facetHolder.getFacetTypes();
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> cls) {
        return facetHolder.getFacet(cls);
    }
    
    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        return facetHolder.containsFacet(facetType);
    }

    @Override
    public boolean containsDoOpFacet(java.lang.Class<? extends Facet> facetType) {
        return facetHolder.containsDoOpFacet(facetType);
    }

    @Override
    public List<Facet> getFacets(Filter<Facet> filter) {
        return facetHolder.getFacets(filter);
    }

    @Override
    public void addFacet(Facet facet) {
        facetHolder.addFacet(facet);
    }

    @Override
    public void addFacet(MultiTypedFacet facet) {
        facetHolder.addFacet(facet);
    }
    
    @Override
    public void removeFacet(Facet facet) {
        facetHolder.removeFacet(facet);
    }

    @Override
    public void removeFacet(Class<? extends Facet> facetType) {
        facetHolder.removeFacet(facetType);
    }

}
