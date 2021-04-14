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
package org.apache.isis.persistence.jpa.metamodel.facets.prop.column;

import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import lombok.val;

public class MandatoryFromJpaColumnAnnotationFacetFactory
extends FacetFactoryAbstract {

    public MandatoryFromJpaColumnAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        //val cls = processMethodContext.getCls();

        final Optional<Boolean> nullable1 = processMethodContext.synthesizeOnMethod(JoinColumn.class)
                .map(JoinColumn::nullable);
        
        final Optional<Boolean> nullable2 = processMethodContext.synthesizeOnMethod(Column.class)
                .map(Column::nullable);
        
        if(!nullable1.isPresent() 
                && !nullable2.isPresent()) {
            return;
        }
        
        val nullable = nullable1.orElseGet(nullable2::get);
        
        val facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new MandatoryFacetDerivedFromJpaColumn(
                facetHolder,
                !nullable));
    }
    
    
}
