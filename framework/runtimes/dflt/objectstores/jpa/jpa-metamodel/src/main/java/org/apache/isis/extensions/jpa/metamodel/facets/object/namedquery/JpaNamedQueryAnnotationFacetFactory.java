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
package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;


public class JpaNamedQueryAnnotationFacetFactory extends
        AnnotationBasedFacetFactoryAbstract {

    public JpaNamedQueryAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final NamedQueries namedQueriesAnnotation = getAnnotation(cls,
                NamedQueries.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        
        if (namedQueriesAnnotation != null) {
            FacetUtil.addFacet(new JpaNamedQueriesFacetAnnotation(
                    namedQueriesAnnotation.value(), facetHolder));
            return;
        }

        final NamedQuery namedQueryAnnotation = getAnnotation(cls,
                NamedQuery.class);
        if (namedQueryAnnotation != null) {
            FacetUtil.addFacet(new JpaNamedQueryFacetAnnotation(
                    namedQueryAnnotation, facetHolder));
        }
    }
}
