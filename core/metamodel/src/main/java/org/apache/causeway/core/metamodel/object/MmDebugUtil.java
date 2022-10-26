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
package org.apache.causeway.core.metamodel.object;

import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;

import lombok.Builder;
import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmDebugUtil {

    @Value @Builder
    public static class ParamUpdateData {
        final String action;
        final int index;
        final String name;
        final ManagedObject pendingValue;
        final Can<? extends ManagedParameter> allParams;
        public String formatted() {
            return String.format("%s[%d](%s->%s)\n%s",
                    action, index, name, formatted(pendingValue),
                    allParams.stream().map(this::formatted)
                    .collect(Collectors.joining("\n")));
        }
        String formatted(final ManagedParameter managedParam) {
            return String.format("- param[%d] %s->%s",
                    managedParam.getParamNr(),
                    managedParam.getFriendlyName(),
                    formatted(managedParam.getValue().getValue()));
        }
        String formatted(final ManagedObject managedObject) {
            return String.format("(%s, cls=%s, pojo=%s)",
                    managedObject.getSpecialization().name(),
                    managedObject.getCorrespondingClass().getName(),
                    ""+managedObject.getPojo());
        }
    }

    public static ParamUpdateData paramUpdateDataFor(
            final int parameterIndex,
            final ParameterNegotiationModel parameterNegotiationModel) {
        val param = parameterNegotiationModel.getParamModels().getElseFail(parameterIndex);
        return ParamUpdateData.builder()
                .action(parameterNegotiationModel.getHead().getMetaModel().getId())
                .name(param.getFriendlyName())
                .pendingValue(param.getValue().getValue())
                .allParams(parameterNegotiationModel.getParamModels())
                .build();
    }


}
