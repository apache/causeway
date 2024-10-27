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

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyNegotiationModel;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmDebugUtils {

    // -- PARAM

    @Value @Builder
    public static class ParamUpdateData {
        final String action; // feature-id
        final int index;
        final String name;
        final Can<? extends ManagedParameter> allParams;
        public String formatted() {
            return String.format("[actionId=%s,paramIndex=%d,paramName=%s]\n%s",
                    action, index, name,
                    allParams.stream().map(this::formatted)
                    .collect(Collectors.joining("\n")));
        }
        String formatted(final ManagedParameter managedParam) {
            return String.format("- param[index=%d,name=%s]: %s",
                    managedParam.getParamNr(),
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
                .action(parameterNegotiationModel.getHead().getMetaModel().getId())
                .name(param.getFriendlyName())
                .allParams(parameterNegotiationModel.getParamModels())
                .build();
    }

    // -- PROP

    @Value @Builder
    public static class PropUpdateData {
        final String property;  // feature-id
        final String name;
        final ManagedObject pendingValue;
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

    private String formatPendingValue(final @Nullable ManagedObject managedObject) {
        return ManagedObjects.isSpecified(managedObject)
                ? String.format("(%s,cls=%s) pojo=%s",
                    managedObject.getSpecialization().name(),
                    managedObject.getCorrespondingClass().getName(),
                    ""+managedObject.getPojo())
                : "(unspecified) pojo=null";
    }

}
