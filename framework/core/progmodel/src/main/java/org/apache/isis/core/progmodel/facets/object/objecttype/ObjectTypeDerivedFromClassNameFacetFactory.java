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

package org.apache.isis.core.progmodel.facets.object.objecttype;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.objecttype.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutorAware;

public class ObjectTypeDerivedFromClassNameFacetFactory extends FacetFactoryAbstract implements ClassSubstitutorAware {

    private ClassSubstitutor classSubstitutor;

    public ObjectTypeDerivedFromClassNameFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final FacetHolder facetHolder = processClassContaxt.getFacetHolder();
        // don't trash existing facet
        if(facetHolder.containsDoOpFacet(ObjectSpecIdFacet.class)) {
            return;
        }
        final Class<?> originalClass = processClassContaxt.getCls();
        final Class<?> substitutedClass = classSubstitutor.getClass(originalClass);
        FacetUtil.addFacet(new ObjectSpecIdFacetDerivedFromClassName(substitutedClass.getCanonicalName(), facetHolder));
    }

    @Override
    public void setClassSubstitutor(ClassSubstitutor classSubstitutor) {
        this.classSubstitutor = classSubstitutor;
    }

}
