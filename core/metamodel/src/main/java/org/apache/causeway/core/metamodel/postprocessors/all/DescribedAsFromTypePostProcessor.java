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
package org.apache.causeway.core.metamodel.postprocessors.all;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.causeway.core.metamodel.facets.members.described.annotprop.MemberDescribedFacetFromType;
import org.apache.causeway.core.metamodel.facets.param.described.annotderived.ParamDescribedFacetFromType;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public class DescribedAsFromTypePostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public DescribedAsFromTypePostProcessor(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void postProcessParameter(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter parameter) {
        handleParam(parameter);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        handleMember(objectAction);
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        handleMember(prop);
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        handleMember(coll);
    }

    // -- HELPER

    private void handleMember(final ObjectMember member) {
        if(member.containsNonFallbackFacet(MemberDescribedFacet.class)) {
            return;
        }
        member.getElementType()
        .lookupNonFallbackFacet(ObjectDescribedFacet.class)
        .ifPresent(objectDescribedFacet ->
            FacetUtil.addFacetIfPresent(
                    MemberDescribedFacetFromType
                    .create(objectDescribedFacet, facetedMethodFor(member))));
    }

    private void handleParam(final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(ParamDescribedFacet.class)) {
            return;
        }
        parameter.getElementType()
        .lookupNonFallbackFacet(ObjectDescribedFacet.class)
        .ifPresent(objectDescribedFacet->
            FacetUtil.addFacetIfPresent(
                    ParamDescribedFacetFromType
                    .create(objectDescribedFacet, parameter.getFacetHolder())));
    }

}
