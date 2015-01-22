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

package org.apache.isis.core.metamodel.facets.object.domainobject.recreatable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetDeclarativeAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;

public class RecreatableObjectFacetForDomainObjectAnnotation extends RecreatableObjectFacetDeclarativeAbstract {

    public static ViewModelFacet create(
            final DomainObject domainObject,
            final SpecificationLoader specificationLoader,
            final AdapterManager adapterManager,
            final ServicesInjector servicesInjector,
            final FacetHolder holder) {

        if(domainObject == null) {
            return null;
        }

        final Nature nature = domainObject.nature();

        if(nature == null || nature == Nature.JDO_ENTITY) {
            return null;
        }

        switch (nature)
        {
            case NOT_SPECIFIED:
            case JDO_ENTITY:
                // not a recreatable object, so no facet
                return null;

            case VIEW_MODEL:
                final ViewModelFacet existingFacet = holder.getFacet(ViewModelFacet.class);
                if (existingFacet != null) {
                    if (existingFacet.getArchitecturalLayer() == ArchitecturalLayer.APPLICATION) {
                        // if compatible existing facet, then just ignore
                        // (otherwise the metamodel
                        return null;
                    }
                    // otherwise, we continue through and add the new facet making the existing one the
                    // underlying.  The metamodel validator on RecreatableObjectFacetFactory will then mark the
                    // model as invalid because of incompatible semantics.
                }
                return new RecreatableObjectFacetForDomainObjectAnnotation(
                        holder,
                        ArchitecturalLayer.APPLICATION,
                        specificationLoader, adapterManager, servicesInjector);

            case EXTERNAL_ENTITY:
            case INMEMORY_ENTITY:
                return new RecreatableObjectFacetForDomainObjectAnnotation(
                        holder,
                        ArchitecturalLayer.DOMAIN,
                        specificationLoader, adapterManager, servicesInjector);
        }
        // shouldn't happen, the above switch should match all cases.
        throw new IllegalArgumentException("nature of '" + nature + "' not recognized");
    }

    private RecreatableObjectFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final ArchitecturalLayer architecturalLayer,
            final SpecificationLoader specificationLoader,
            final AdapterManager adapterManager,
            final ServicesInjector servicesInjector) {
        super(holder, architecturalLayer, specificationLoader, adapterManager, servicesInjector);
    }

}
