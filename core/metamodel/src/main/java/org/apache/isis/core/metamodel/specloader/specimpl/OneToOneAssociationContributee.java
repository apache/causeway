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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;

public class OneToOneAssociationContributee extends OneToOneAssociationImpl implements ContributeeMember {

    private final ObjectAdapter serviceAdapter;
    private final ObjectAction objectAction;
    
    private final NotPersistedFacet notPersistedFacet;

    public OneToOneAssociationContributee(
            final ObjectAdapter serviceAdapter, 
            final ObjectActionImpl objectAction, 
            final ObjectMemberContext objectMemberContext) {
        super(objectAction.getFacetedMethod(), objectAction.getReturnType(), objectMemberContext);
        this.serviceAdapter = serviceAdapter;
        this.objectAction = objectAction;
        
        notPersistedFacet = new NotPersistedFacetAbstract(this) {};
    }

    @Override
    public ObjectAdapter get(final ObjectAdapter ownerAdapter) {
        return objectAction.execute(serviceAdapter, new ObjectAdapter[]{ownerAdapter});
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T getFacet(Class<T> cls) {
        if(cls == NotPersistedFacet.class) {
            return (T) notPersistedFacet;
        }
        return super.getFacet(cls);
    }

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        if(facetType == NotPersistedFacet.class) {
            return true;
        }
        return super.containsFacet(facetType);
    }
    
    @Override
    public boolean containsDoOpFacet(Class<? extends Facet> facetType) {
        if(facetType == NotPersistedFacet.class) {
            return true;
        }
        return super.containsDoOpFacet(facetType);
    }


}
