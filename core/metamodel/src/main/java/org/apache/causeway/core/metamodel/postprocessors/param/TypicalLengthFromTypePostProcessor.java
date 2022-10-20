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
package org.apache.causeway.core.metamodel.postprocessors.param;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.causeway.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterFromType;
import org.apache.causeway.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyFromType;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public class TypicalLengthFromTypePostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public TypicalLengthFromTypePostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessParameter(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction,
            final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(TypicalLengthFacet.class)) {
            return;
        }
        parameter
            .getElementType()
            .lookupNonFallbackFacet(TypicalLengthFacet.class)
            .ifPresent(typicalLengthFacet ->
                    FacetUtil.addFacet(
                            TypicalLengthFacetOnParameterFromType
                            .createWhilePostprocessing(typicalLengthFacet, parameter.getFacetHolder())));
    }

    @Override
    public void postProcessProperty(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(TypicalLengthFacet.class)) {
            return;
        }
        property
            .getElementType()
            .lookupNonFallbackFacet(TypicalLengthFacet.class)
            .ifPresent(typicalLengthFacet ->
                    FacetUtil.addFacet(
                            TypicalLengthFacetOnPropertyFromType
                            .createWhilePostprocessing(typicalLengthFacet, facetedMethodFor(property))));

    }

}
