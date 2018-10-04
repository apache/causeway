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
package org.apache.isis.core.metamodel.facets.object.domainservicelayout;

import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceMenuOrder;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.annotation.DomainServiceLayoutFacetAnnotation;

public class DomainServiceLayoutFacetFactory extends FacetFactoryAbstract {

    public DomainServiceLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final DomainService domainService = Annotations.getAnnotation(cls, DomainService.class);
        final List<DomainServiceLayout> domainServiceLayouts = Annotations.getAnnotations(cls, DomainServiceLayout.class);

        if (domainService == null && domainServiceLayouts == null) {
            return;
        }

        final String domainServiceMenuOrder =
                domainService != null && !domainService.menuOrder().equals("" + (Integer.MAX_VALUE - 100))
                ? domainService.menuOrder()
                        : null;
                final String domainServiceLayoutMenuOrder =domainServiceLayouts.stream()
                        .map(DomainServiceLayout::menuOrder)
                        .filter(menuOrder -> !menuOrder.equals("" + (Integer.MAX_VALUE - 100)))
                        .findFirst()
                        .orElse(null);

                final String menuOrder = DomainServiceMenuOrder.minimumOf(domainServiceLayoutMenuOrder, domainServiceMenuOrder);

                DomainServiceLayout.MenuBar menuBar =
                        domainServiceLayouts.stream()
                        .map(DomainServiceLayout::menuBar)
                        .filter(mb -> mb != DomainServiceLayout.MenuBar.NOT_SPECIFIED)
                        .findFirst()
                        .orElse(DomainServiceLayout.MenuBar.PRIMARY);

                FacetUtil.addFacet(
                        new DomainServiceLayoutFacetAnnotation(
                                facetHolder,
                                menuBar, menuOrder));

                FacetUtil.addFacet(NamedFacetForDomainServiceLayoutAnnotation.create(domainServiceLayouts, facetHolder));
    }

}
