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

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyNegotiationModel;

import lombok.Builder;

public record MmDebugUtils() {

    // -- PARAM

    @Builder
    public record ParamUpdateData(
            String action, // feature-id
            int index,
            String name,
            Can<? extends ManagedParameter> allParams) {
        public String formatted() {
            return String.format("[actionId=%s,paramIndex=%d,paramName=%s]\n%s",
                    action, index, name,
                    allParams.stream().map(this::formatted)
                    .collect(Collectors.joining("\n")));
        }
        String formatted(final ManagedParameter managedParam) {
            return String.format("- param[index=%d,name=%s]: %s",
                    managedParam.paramIndex(),
                    managedParam.getFriendlyName(),
                    formatPendingValue(managedParam.getValue().getValue()));
        }
    }

    public static ParamUpdateData paramUpdateDataFor(
            final int parameterIndex,
            final ParameterNegotiationModel parameterNegotiationModel) {
        var param = parameterNegotiationModel.getParamModels().getElseFail(parameterIndex);
        return ParamUpdateData.builder()
                .index(parameterIndex)
                .action(parameterNegotiationModel.act().getId())
                .name(param.getFriendlyName())
                .allParams(parameterNegotiationModel.getParamModels())
                .build();
    }

    // -- PROP

    @Builder
    public record PropUpdateData(
            String property,  // feature-id
            String name,
            ManagedObject pendingValue
            ) {
        public String formatted() {
            return String.format("[propertyId=%s,propertyName=%s] -> %s)",
                    property, name, formatPendingValue(pendingValue));
        }
    }

    public static PropUpdateData propUpdateDataFor(final PropertyNegotiationModel propertyNegotiationModel) {
        var prop = propertyNegotiationModel.getManagedProperty();
        return PropUpdateData.builder()
                .property(prop.getIdentifier().toString())
                .name(prop.getFriendlyName())
                .build();
    }

    // -- HELPER

    private static String formatPendingValue(final @Nullable ManagedObject managedObject) {
        return ManagedObjects.isSpecified(managedObject)
                ? String.format("(%s,cls=%s) pojo=%s",
                    managedObject.specialization().name(),
                    managedObject.getCorrespondingClass().getName(),
                    ""+managedObject.getPojo())
                : "(unspecified) pojo=null";
    }

}
