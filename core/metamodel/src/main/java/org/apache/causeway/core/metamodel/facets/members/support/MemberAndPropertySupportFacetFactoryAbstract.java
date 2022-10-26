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
import org.apache.causeway.core.metamodel.facets.param.support.ActionParameterSupportFacetFactoryAbstract;
import org.apache.causeway.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.NonNull;

/**
 * Specializations {@link ActionParameterSupportFacetFactoryAbstract} and
 * {@link MemberSupportFacetFactoryAbstract}
 */
public abstract class MemberAndPropertySupportFacetFactoryAbstract
extends MethodPrefixBasedFacetFactoryAbstract {

    protected final MemberSupportPrefix memberSupportPrefix;

    protected MemberAndPropertySupportFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull ImmutableEnumSet<FeatureType> featureTypes,
            final @NonNull MemberSupportPrefix memberSupportPrefix) {
        super(mmc, featureTypes, OrphanValidation.VALIDATE,
                memberSupportPrefix.getMethodNamePrefixes());
        this.memberSupportPrefix = memberSupportPrefix;
    }

}
