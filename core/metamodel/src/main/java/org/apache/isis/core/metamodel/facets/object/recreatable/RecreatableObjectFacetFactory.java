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

import javax.annotation.PostConstruct;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.NonNull;
import lombok.val;

public class RecreatableObjectFacetFactory
extends FacetFactoryAbstract
implements
    MetaModelRefiner,
    PostConstructMethodCache {

    public RecreatableObjectFacetFactory(
            final @NonNull MethodByClassMap postConstructMethodsCache) {
        super(FeatureType.OBJECTS_ONLY);
        this.postConstructMethodsCache = postConstructMethodsCache;
    }

    /**
     * We simply attach all facets we can find;
     * the {@link #refineProgrammingModel(ProgrammingModel) meta-model validation}
     * will detect if multiple interfaces/annotations have
     * been attached.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {

        val facetHolder = processClassContext.getFacetHolder();
        val type = processClassContext.getCls();
        val postConstructMethodCache = this;

        // ViewModel interface
        if (ViewModel.class.isAssignableFrom(processClassContext.getCls())) {
            FacetUtil.addFacetIfPresent(
                    new RecreatableObjectFacetForRecreatableObjectInterface(
                            facetHolder, postConstructMethodCache));
        }

        // RecreatableDomainObject interface
        if (RecreatableDomainObject.class.isAssignableFrom(type)) {
            FacetUtil.addFacetIfPresent(
                    new RecreatableObjectFacetForRecreatableDomainObjectInterface(
                            facetHolder, postConstructMethodCache));
        }

        // XmlRootElement annotation
        val xmlRootElementIfAny = processClassContext.synthesizeOnType(XmlRootElement.class);
        if(xmlRootElementIfAny.isPresent()) {
            FacetUtil.addFacetIfPresent(
                    new RecreatableObjectFacetForXmlRootElementAnnotation(
                            facetHolder, postConstructMethodCache));
        }

        // DomainObject(nature=VIEW_MODEL) is managed by the DomainObjectAnnotationFacetFactory
    }



    // //////////////////////////////////////

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec -> {

            objectSpec.lookupFacet(ViewModelFacet.class)
            .map(ViewModelFacet::getSharedFacetRankingElseFail)
            .ifPresent(facetRanking->facetRanking
                    .visitTopRankPairsSemanticDiffering(ViewModelFacet.class, (a, b)->{

                            ValidationFailure.raiseFormatted(
                                    objectSpec,
                                    "%s: has multiple incompatible annotations/interfaces indicating that " +
                                            "it is a recreatable object of some sort (%s and %s)",
                                            objectSpec.getFullIdentifier(),
                                            a.getClass().getSimpleName(),
                                            b.getClass().getSimpleName());


                    }));

        });
    }


    // //////////////////////////////////////

    private final MethodByClassMap postConstructMethodsCache;

    @Override
    public Method postConstructMethodFor(final Object pojo) {
        return MethodFinderUtils.findAnnotatedMethod(pojo, PostConstruct.class, postConstructMethodsCache);
    }



}
