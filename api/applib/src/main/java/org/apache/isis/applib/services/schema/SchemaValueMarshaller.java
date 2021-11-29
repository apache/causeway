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
package org.apache.isis.applib.services.schema;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.schema.cmd.v2.ActionDto;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.NonNull;

/**
 * Provides the runtime context for converting values
 * between their XML <i>Schema</i> and their <i>Java</i> type representation,
 * based on {@link ValueSemanticsProvider value-semantics} if specified.
 *
 * @since 2.x {@index}
 */
public interface SchemaValueMarshaller {

    // -- RECOVER IDENTIFIERS

    /**
     * Recovers an <i>Action's</i> {@link Identifier} from given DTO.
     */
    Identifier actionIdentifier(@NonNull ActionInvocationDto actionInvocationDto);

    /**
     * Recovers an <i>Action's</i> {@link Identifier} from given DTO.
     */
    Identifier actionIdentifier(@NonNull ActionDto actionDto);

    /**
     * Recovers a <i>Property's</i> {@link Identifier} from given DTO.
     */
    Identifier propertyIdentifier(@NonNull PropertyDto propertyDto);

    // -- RECOVER VALUES FROM DTO

    /**
     * Recovers a property value, using {@link ValueSemanticsProvider}
     * for corresponding <i>Property</i>.
     */
    @Nullable Object recoverValueFrom(@NonNull PropertyDto propertyDto);

    /**
     * Recovers a parameter value, using {@link ValueSemanticsProvider}
     * for corresponding <i>Action Parameter</i>.
     */
    @Nullable Object recoverValueFrom(@NonNull Identifier paramIdentifier, @NonNull ParamDto paramDto);

    // -- RECORD VALUES INTO DTO

    /**
     * Records given result value into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Action</i>.
     */
    <T> ActionInvocationDto recordActionResult(
            @NonNull ActionInvocationDto invocationDto,
            @NonNull Class<T> returnType,
            @Nullable T result);

    /**
     * Records given property value into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Property</i>.
     */
    <T> PropertyDto recordPropertyValue(
            @NonNull PropertyDto propertyDto,
            @NonNull Class<T> propertyType,
            @Nullable T valuePojo);

    /**
     * Records given parameter value into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Action Parameter</i>.
     */
    <T> ParamDto recordParamValue(
            @NonNull Identifier paramIdentifier,
            @NonNull ParamDto paramDto,
            @NonNull Class<T> paramType,
            @Nullable T valuePojo);

}
