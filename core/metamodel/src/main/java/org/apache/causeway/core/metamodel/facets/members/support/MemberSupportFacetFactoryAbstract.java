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
package org.apache.causeway.core.metamodel.facets.members.support;

import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.methods.MethodFinder;

import lombok.NonNull;
import lombok.val;

public abstract class MemberSupportFacetFactoryAbstract
extends MemberAndPropertySupportFacetFactoryAbstract {

    private final boolean mixinSupportExplicitlyAdded;

    protected MemberSupportFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull ImmutableEnumSet<FeatureType> featureTypes,
            final @NonNull MemberSupportPrefix memberSupportPrefix) {
        super(mmc, addMixinSupport(featureTypes), memberSupportPrefix);
        this.mixinSupportExplicitlyAdded = !featureTypes.contains(FeatureType.ACTION);
    }

    private static ImmutableEnumSet<FeatureType> addMixinSupport(
            final ImmutableEnumSet<FeatureType> featureTypes) {
        return featureTypes.add(FeatureType.ACTION);
    }

    @Override
    public final void process(final ProcessMethodContext processMethodContext) {

        // optimization step, not strictly required
        if(mixinSupportExplicitlyAdded
                && !processMethodContext.isMixinMain()) {
            // stop processing if it is not an allowed property or collection
            val isProp = getFeatureTypes().contains(FeatureType.PROPERTY)
                    && processMethodContext.getFeatureType().isProperty();
            val isColl = getFeatureTypes().contains(FeatureType.COLLECTION)
                    && processMethodContext.getFeatureType().isCollection();
            if(!(isProp
                    || isColl)) {
                return; // skip
            }
        }

        val getterMethod = processMethodContext.getMethod();
        val elementType = getterMethod.getReturnType(); // in case of an action, is never used

        val methodNameCandidates = memberSupportPrefix.getMethodNamePrefixes()
                .flatMap(processMethodContext::memberSupportCandidates);

        search(processMethodContext,
                MethodFinder
                .memberSupport(processMethodContext.getCls(),
                        methodNameCandidates,
                        processMethodContext.getIntrospectionPolicy())
                .withReturnTypeAnyOf(memberSupportPrefix
                        .getSupportMethodReturnType().matchingTypes(elementType))
                );
    }

    protected abstract void search(
            ProcessMethodContext processMethodContext,
            MethodFinder methodFinder);

}
