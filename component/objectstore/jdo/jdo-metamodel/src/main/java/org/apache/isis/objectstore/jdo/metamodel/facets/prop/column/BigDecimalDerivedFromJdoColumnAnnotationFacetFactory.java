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
package org.apache.isis.objectstore.jdo.metamodel.facets.prop.column;

import java.math.BigDecimal;

import javax.jdo.annotations.Column;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueFacet;


public class BigDecimalDerivedFromJdoColumnAnnotationFacetFactory extends FacetFactoryAbstract {

    private static final int DEFAULT_LENGTH = BigDecimalFacetFallback.DEFAULT_LENGTH;
    private static final int DEFAULT_SCALE = BigDecimalFacetFallback.DEFAULT_SCALE;

    public BigDecimalDerivedFromJdoColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Column annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Column.class);

        if(BigDecimal.class != processMethodContext.getMethod().getReturnType()) {
            return;
        } 
        final BigDecimalValueFacet facet;
        final FacetedMethod holder = processMethodContext.getFacetHolder();
        
        if (annotation == null) {
            facet = new BigDecimalFacetFallback(holder);
        } else {
            facet = new BigDecimalFacetDerivedFromJdoColumn(holder, valueElseDefault(annotation.length(), DEFAULT_LENGTH), valueElseDefault(annotation.scale(), DEFAULT_SCALE));
        }
        FacetUtil.addFacet(facet);
    }

    Integer valueElseDefault(final int value, final int defaultValue) {
        return value != -1? value: defaultValue;
    }

    
}
