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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;

public class RecreatableObjectFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner, PostConstructMethodCache {

    public RecreatableObjectFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    /**
     * We simply attach all facets we can find; the {@link #refineMetaModelValidator(org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite, IsisConfiguration) meta-model validation} will detect if multiple interfaces/annotations have
     * been attached.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {

        // ViewModel interface
        if (ViewModel.class.isAssignableFrom(processClassContext.getCls())) {
            final PostConstructMethodCache postConstructMethodCache = this;
            FacetUtil.addFacet(new RecreatableObjectFacetForRecreatableObjectInterface(
                    processClassContext.getFacetHolder(), postConstructMethodCache));
        }

        // ViewModel annotation
        final org.apache.isis.applib.annotation.ViewModel annotation = Annotations.getAnnotation(processClassContext.getCls(), org.apache.isis.applib.annotation.ViewModel.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));

        // XmlRootElement annotation
        final XmlRootElement xmlRootElement = Annotations.getAnnotation(processClassContext.getCls(), XmlRootElement.class);
        FacetUtil.addFacet(create(xmlRootElement, processClassContext.getFacetHolder()));

        // RecreatableDomainObject interface
        if (RecreatableDomainObject.class.isAssignableFrom(processClassContext.getCls())) {
            final PostConstructMethodCache postConstructMethodCache = this;
            FacetUtil.addFacet(new RecreatableObjectFacetForRecreatableDomainObjectInterface(
                    processClassContext.getFacetHolder(), postConstructMethodCache));
        }

        // DomainObject(nature=VIEW_MODEL) is managed by the DomainObjectFacetFactory
    }

    private ViewModelFacet create(final org.apache.isis.applib.annotation.ViewModel annotation, final FacetHolder holder) {
        final PostConstructMethodCache postConstructMethodCache = this;
        return annotation != null ? new RecreatableObjectFacetForViewModelAnnotation(holder, postConstructMethodCache) : null;
    }

    private ViewModelFacet create(final XmlRootElement annotation, final FacetHolder holder) {
        final PostConstructMethodCache postConstructMethodCache = this;
        return annotation != null
                ? new RecreatableObjectFacetForXmlRootElementAnnotation(holder, postConstructMethodCache)
                        : null;
    }

    // //////////////////////////////////////

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        programmingModel.addValidator((objectSpec, validate) -> {

            final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
            final Facet underlyingFacet = facet != null ? facet.getUnderlyingFacet() : null;
            if(underlyingFacet != null && underlyingFacet.getClass() != facet.getClass()) {
                validate.onFailure(
                        objectSpec,
                        objectSpec.getIdentifier(),
                        "%s: has multiple incompatible annotations/interfaces indicating that " +
                                "it is a recreatable object of some sort (%s and %s)",
                                objectSpec.getFullIdentifier(),
                                facet.getClass().getSimpleName(),
                                underlyingFacet.getClass().getSimpleName());
            }
            return true;
        });
    }


    // //////////////////////////////////////

    private final Map<Class<?>, Optional<Method>> postConstructMethods = _Maps.newHashMap();

    @Override
    public Method postConstructMethodFor(final Object pojo) {
        return MethodFinderUtils.findAnnotatedMethod(pojo, PostConstruct.class, postConstructMethods);
    }



}
