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
 *
 */
package org.apache.causeway.core.metamodel.facets.param.parameter.depdef;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.commons.internal.base._Optionals;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.ParameterConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.SingleValueFacet;

/**
 * Determines how dependent parameter values should be updated,
 * if one of the earlier parameter values is changed.
 * <p>
 * Corresponds to annotating the action method {@link Parameter#dependentDefaultsPolicy()}.
 *
 * @since 2.0
 */
public interface ParameterDependentDefaultsFacet
extends SingleValueFacet<ParameterConfigOptions.DependentDefaultsPolicy> {

    static Optional<ParameterDependentDefaultsFacet> create(
            final Optional<Parameter> parameterIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder) {

        final ParameterConfigOptions.DependentDefaultsPolicy defaultPolicyFromConfig =
                ParameterConfigOptions.dependentDefaultsPolicy(configuration);

        return _Optionals.orNullable(

        parameterIfAny
        .map(Parameter::dependentDefaultsPolicy)
        .<ParameterDependentDefaultsFacet>map(policy -> {
            switch (policy) {
            case PRESERVE_CHANGES:
                return new ParameterDependentDefaultsFacetForParameterAnnotation(
                        ParameterConfigOptions.DependentDefaultsPolicy.PRESERVE_CHANGES, holder);
            case UPDATE_DEPENDENT:
                return new ParameterDependentDefaultsFacetForParameterAnnotation(
                        ParameterConfigOptions.DependentDefaultsPolicy.UPDATE_DEPENDENT, holder);
            case NOT_SPECIFIED:
            case AS_CONFIGURED:
                return new ParameterDependentDefaultsFacetForParameterAnnotation(defaultPolicyFromConfig, holder);
            default:
            }
            throw new IllegalStateException("dependentDefaultsPolicy '" + policy + "' not recognised");
        })
        ,
        () -> new ParameterDependentDefaultsFacetFromConfiguration(defaultPolicyFromConfig, holder));
    }
}
