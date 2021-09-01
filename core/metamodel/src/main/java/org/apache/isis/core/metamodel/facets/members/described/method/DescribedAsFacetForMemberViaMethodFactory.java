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

package org.apache.isis.core.metamodel.facets.members.described.method;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class DescribedAsFacetForMemberViaMethodFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.DESCRIBED_PREFIX;

    @Inject
    public DescribedAsFacetForMemberViaMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.MEMBERS, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        addDescribedFacetIfDescribedMethodIsFound(processMethodContext);
    }

    private void addDescribedFacetIfDescribedMethodIsFound(
            final ProcessMethodContext processMethodContext) {

        val cls = processMethodContext.getCls();
        //val actionOrGetter = processMethodContext.getMethod();

        val methodNameCandidates = processMethodContext.memberSupportCandidates(PREFIX);

        val describedMethod = MethodFinder.findMethod_returningText(
                MethodFinderOptions
                .memberSupport(processMethodContext.getIntrospectionPolicy()),
                cls,
                methodNameCandidates,
                NO_ARG)
                .findFirst()
                .orElse(null);
        if (describedMethod == null) {
            return;
        }
        processMethodContext.removeMethod(describedMethod);

        FacetUtil.addFacet(
                new DescribedAsFacetForMemberViaMethod(
                        describedMethod,
                        processMethodContext.getFacetHolder()));
    }

}
