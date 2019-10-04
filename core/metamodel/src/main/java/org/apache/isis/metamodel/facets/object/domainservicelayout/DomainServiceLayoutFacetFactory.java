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
package org.apache.isis.metamodel.facets.object.domainservicelayout;

import java.util.Objects;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.object.domainservicelayout.annotation.DomainServiceLayoutFacetAnnotation;

import lombok.val;

public class DomainServiceLayoutFacetFactory extends FacetFactoryAbstract {

    public DomainServiceLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();

        val domainServiceIfAny = processClassContext.synthesize(DomainService.class);
        val domainServiceLayoutIfAny = processClassContext.synthesize(DomainServiceLayout.class);

        // either one is enough to treat this as a domain service
        val isAnyPresent = 
                domainServiceIfAny.isPresent() || 
                domainServiceLayoutIfAny.isPresent();
        
        if(!isAnyPresent) {
            return;
        }

        val menuBar = domainServiceLayoutIfAny
                .map(DomainServiceLayout::menuBar)
                .filter(mb -> mb != DomainServiceLayout.MenuBar.NOT_SPECIFIED) // redundant since _Annotations
                .orElse(DomainServiceLayout.MenuBar.PRIMARY);
        
        FacetUtil.addFacet(new DomainServiceLayoutFacetAnnotation(facetHolder, menuBar));

        val named = domainServiceLayoutIfAny
                .map(DomainServiceLayout::named)
                .map(_Strings::emptyToNull)
                .filter(Objects::nonNull)
                .orElse(null);
        
        FacetUtil.addFacet(NamedFacetForDomainServiceLayoutAnnotation.create(named, facetHolder));
    }

}
