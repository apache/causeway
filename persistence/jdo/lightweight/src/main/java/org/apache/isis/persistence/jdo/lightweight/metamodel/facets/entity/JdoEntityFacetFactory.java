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
package org.apache.isis.persistence.jdo.lightweight.metamodel.facets.entity;

import javax.inject.Inject;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.persistence.jdo.applib.integration.JdoSupportService;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

public class JdoEntityFacetFactory extends FacetFactoryAbstract {

    @Inject private JdoFacetContext jdoFacetContext;
    @Inject private JdoSupportService jdoSupportService;
    
    public JdoEntityFacetFactory() {
        super(ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();

        val entityAnnotation = Annotations.getAnnotation(cls, PersistenceCapable.class);
        if (entityAnnotation == null) {
            return;
        }
        
        val facetHolder = processClassContext.getFacetHolder();
        val mmc = facetHolder.getMetaModelContext();
        val jdoEntityFacet = new JdoEntityFacet(facetHolder, cls, 
                mmc, jdoFacetContext, jdoSupportService);
            
        addFacet(jdoEntityFacet);
    }


}
