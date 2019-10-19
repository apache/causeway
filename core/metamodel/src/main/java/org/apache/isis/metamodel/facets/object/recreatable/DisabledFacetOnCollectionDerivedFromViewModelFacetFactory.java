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

package org.apache.isis.metamodel.facets.object.recreatable;

import java.lang.reflect.Method;

import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public class DisabledFacetOnCollectionDerivedFromViewModelFacetFactory extends FacetFactoryAbstract {

    public DisabledFacetOnCollectionDerivedFromViewModelFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Class<?> declaringClass = method.getDeclaringClass();
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(declaringClass);

        if (!spec.containsDoOpFacet(ViewModelFacet.class)) {
            return;
        }
        final ViewModelFacet facet = spec.getFacet(ViewModelFacet.class);
        final DisabledFacetAbstract.Semantics semantics = Util.inferSemanticsFrom(facet);

        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        super.addFacet(new DisabledFacetOnCollectionDerivedFromRecreatableObject(facetHolder, semantics));
    }


}
