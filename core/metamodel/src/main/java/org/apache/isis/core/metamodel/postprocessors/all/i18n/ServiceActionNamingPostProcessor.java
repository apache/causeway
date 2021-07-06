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
package org.apache.isis.core.metamodel.postprocessors.all.i18n;

import javax.inject.Inject;

import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.layout.MemberDescribedFacetForMenuBarXml;
import org.apache.isis.core.metamodel.facets.actions.layout.MemberNamedFacetForMenuBarXml;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.val;

public class ServiceActionNamingPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public ServiceActionNamingPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {

        if(!(objectSpecification.getBeanSort().isManagedBeanContributing())) {
            return;
        }

        // installs MemberNamedFacet(s) and MemberDescribedFacet(s) for MenuBar entries

        val layoutData = getMenuBarsService().lookupLayout(objectAction.getFeatureIdentifier()).orElse(null);

        FacetUtil.addFacetIfPresent(
                MemberNamedFacetForMenuBarXml
                .create(layoutData, objectAction));

        FacetUtil.addFacetIfPresent(
                MemberDescribedFacetForMenuBarXml
                .create(layoutData, objectAction));

    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter param) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
    }

    // -- HELEPR

    @Getter(lazy = true)
    private final MenuBarsService menuBarsService = getMenuBarsServiceAndReloadXml();

    private final MenuBarsService getMenuBarsServiceAndReloadXml() {
        val menuBarsService = getServiceRegistry()
                .lookupServiceElseFail(MenuBarsService.class);
        menuBarsService.menuBars(); // as a side-effect reloads XML resource if supported
        return menuBarsService;
    }

}
