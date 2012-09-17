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

package org.apache.isis.core.progmodel.facets.object.autocomplete.annotation;

import java.util.List;

import org.apache.isis.applib.annotation.AutoComplete;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;

public class AutoCompleteAnnotationFacetFactory extends FacetFactoryAbstract {

    public AutoCompleteAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_POST_PROCESSING_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final AutoComplete annotation = Annotations.getAnnotation(processClassContext.getCls(), AutoComplete.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));
    }

    private AutoCompleteFacet create(final AutoComplete annotation, final FacetHolder holder) {
        if(annotation == null) { return null; }

        Class<?> repositoryClass = annotation.repository();
        String actionName = annotation.action();
        
        final ObjectSpecification repositorySpec = getSpecificationLookup().loadSpecification(repositoryClass);
        getSpecificationLookup().introspectIfRequired(repositorySpec);
         
        
        final List<ObjectAction> objectActions = repositorySpec.getObjectActions(ActionType.USER, Contributed.EXCLUDED, ObjectActionFilters.withId(actionName));
        
        return objectActions.size() != 1 ? null : new AutoCompleteFacetAnnotation(holder, objectActions.get(0));
    }

}
