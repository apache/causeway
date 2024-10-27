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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.ParameterTuple;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.experimental.UtilityClass;

@UtilityClass
class _MethodFacadeAutodetect {

    /**
     * Detects whether an action uses the {@link ParameterTuple} annotation on its single argument.
     * If so, we follow Parameters as Tuple (PAT) semantics.
     */
    MethodFacade autodetect(final ResolvedMethod method, final FacetHolder inspectedTypeSpec) {
        final long paramTupleCount = Stream.of(method.method().getParameters())
            .map(parameter->_Annotations.synthesize(parameter, ParameterTuple.class))
            .filter(Optional::isPresent)
            .count();
        if(paramTupleCount == 0) {
            return _MethodFacades.regular(method);
        }
        if(paramTupleCount > 1
                || method.paramCount() > 1) {
            // invalid
            ValidationFailure.raiseFormatted(inspectedTypeSpec,
                    MessageTemplate.PARAMETER_TUPLE_INVALID_USE_OF_ANNOTATION
                        .builder()
                        .addVariable("type", inspectedTypeSpec.getFeatureIdentifier().getClassName())
                        .addVariable("member", method.name())
                        .buildMessage());
        }
        var patType = method.paramType(0);
        var patConstructors = _ClassCache.getInstance().streamPublicConstructors(patType)
            .collect(Can.toCan());
        if(!patConstructors.isCardinalityOne()) {
            // invalid
            ValidationFailure.raiseFormatted(inspectedTypeSpec,
                    MessageTemplate.PARAMETER_TUPLE_TYPE_WITH_AMBIGUOUS_CONSTRUCTORS
                        .builder()
                        .addVariable("type", inspectedTypeSpec.getFeatureIdentifier().getClassName())
                        .addVariable("member", method.name())
                        .addVariable("patType", patType.getName())
                        .buildMessage());
        }
        return _MethodFacades.paramsAsTuple(method, patConstructors.getFirstElseFail());
    }

}
