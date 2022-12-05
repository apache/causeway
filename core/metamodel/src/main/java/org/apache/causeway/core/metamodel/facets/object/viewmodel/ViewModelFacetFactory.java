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
package org.apache.causeway.core.metamodel.facets.object.viewmodel;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

public class ViewModelFacetFactory
extends FacetFactoryAbstract
implements
    MetaModelRefiner {

    @Inject
    public ViewModelFacetFactory(
            final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
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

        // XmlRootElement annotation (with default precedence)
        val xmlRootElementIfAny = processClassContext.synthesizeOnType(XmlRootElement.class);
        FacetUtil
        .addFacetIfPresent(
                ViewModelFacetForXmlRootElementAnnotation
                .create(xmlRootElementIfAny, facetHolder));

        // (with high precedence)
        FacetUtil
        .addFacetIfPresent(
            // either ViewModel interface (highest precedence)
            ViewModelFacetForViewModelInterface.create(type, facetHolder)
            // or Serializable interface (if any)
            .or(()->ViewModelFacetForSerializableInterface.create(type, facetHolder)));

        // DomainObject(nature=VIEW_MODEL) is managed by the DomainObjectAnnotationFacetFactory as a fallback strategy
    }


    // //////////////////////////////////////

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec -> {

            // ensure concrete viewmodel types have a ViewModelFacet
            if(!objectSpec.isAbstract()
                    && objectSpec.getBeanSort().isViewModel()
                    && !objectSpec.viewmodelFacet().isPresent()) {
                ValidationFailure.raiseFormatted(objectSpec,
                        ProgrammingModelConstants.Violation.VIEWMODEL_MISSING_SERIALIZATION_STRATEGY
                            .builder()
                            .addVariable("type", objectSpec.getCorrespondingClass().getName())
                            .buildMessage());
            }

            objectSpec.viewmodelFacet()
            .map(ViewModelFacet::getSharedFacetRankingElseFail)
            .ifPresent(facetRanking->{
                facetRanking
                .visitTopRankPairsSemanticDiffering(ViewModelFacet.class, (a, b)->{
                    ValidationFailure.raiseFormatted(objectSpec,
                            ProgrammingModelConstants.Violation.VIEWMODEL_CONFLICTING_SERIALIZATION_STRATEGIES
                                .builder()
                                .addVariable("type", objectSpec.getFullIdentifier())
                                .addVariable("facetA", a.getClass().getSimpleName())
                                .addVariable("facetB", b.getClass().getSimpleName())
                                .buildMessage());
                });
            });
        });
    }

}
