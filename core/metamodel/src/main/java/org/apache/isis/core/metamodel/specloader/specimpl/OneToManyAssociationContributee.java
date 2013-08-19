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

import org.apache.isis.applib.annotation.Render;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacet;
import org.apache.isis.core.metamodel.facets.members.resolve.RenderFacetAbstract;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;

public class OneToManyAssociationContributee extends OneToManyAssociationImpl implements ContributeeMember {

    private final ObjectAdapter serviceAdapter;
    private final ObjectAction objectAction;
    
    private final RenderFacet renderFacet;
    private final NotPersistedFacet notPersistedFacet;
    private final TypeOfFacet typeOfFacet; 

    private static ObjectSpecification typeOfSpec(final ObjectActionImpl objectAction, ObjectMemberContext objectMemberContext) {
        final TypeOfFacet actionTypeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        return objectMemberContext.getSpecificationLookup().loadSpecification(actionTypeOfFacet.value());
    }
    
    public OneToManyAssociationContributee(ObjectAdapter serviceAdapter, ObjectActionImpl objectAction, ObjectMemberContext objectMemberContext) {
        super(objectAction.getFacetedMethod(), typeOfSpec(objectAction, objectMemberContext), objectMemberContext);
        this.serviceAdapter = serviceAdapter;
        this.objectAction = objectAction;
        
        renderFacet = new RenderFacetAbstract(Render.Type.EAGERLY, this) {};
        notPersistedFacet = new NotPersistedFacetAbstract(this) {};
        typeOfFacet = new TypeOfFacetAbstract(getSpecification().getCorrespondingClass(), this, objectMemberContext.getSpecificationLookup()) {};
    }

    
    @Override
    public ObjectAdapter get(final ObjectAdapter ownerAdapter) {
        return objectAction.execute(serviceAdapter, new ObjectAdapter[]{ownerAdapter});
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T getFacet(Class<T> facetType) {
        if(facetType == RenderFacet.class) {
            return (T) renderFacet;
        }
        if(facetType == NotPersistedFacet.class) {
            return (T) notPersistedFacet;
        }
        if(facetType == TypeOfFacet.class) {
            return (T) typeOfFacet;
        }
        return super.getFacet(facetType);
    }
    
    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        if(facetType == RenderFacet.class) {
            return true;
        }
        if(facetType == NotPersistedFacet.class) {
            return true;
        }
        if(facetType == TypeOfFacet.class) {
            return true;
        }
        return super.containsFacet(facetType);
    }
    
    @Override
    public boolean containsDoOpFacet(Class<? extends Facet> facetType) {
        if(facetType == RenderFacet.class) {
            return true;
        }
        if(facetType == NotPersistedFacet.class) {
            return true;
        }
        if(facetType == TypeOfFacet.class) {
            return true;
        }
        return super.containsDoOpFacet(facetType);
    }

}
