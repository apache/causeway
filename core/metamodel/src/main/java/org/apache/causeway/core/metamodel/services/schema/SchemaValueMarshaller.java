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
package org.apache.causeway.core.metamodel.services.schema;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

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

    ManagedObject recoverReferenceFrom(@NonNull OidDto oidDto);

    /**
     * Recovers a property value, using {@link ValueSemanticsProvider}
     * for corresponding <i>Property</i>.
     * Cardinality {@code 0..1}
     */
    ManagedObject recoverPropertyFrom(@NonNull PropertyDto propertyDto);

    /**
     * Recovers a parameter value, using {@link ValueSemanticsProvider}
     * for corresponding <i>Action Parameter</i>. Cardinality {@code 0..n}
     * <p>
     * Packed up if non-scalar.
     */
    ManagedObject recoverParameterFrom(@NonNull Identifier paramIdentifier, @NonNull ParamDto paramDto);

    // -- RECORD VALUES INTO DTO

    /**
     * Records given result value into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Action</i>.
     */
    ActionInvocationDto recordActionResultScalar(
            @NonNull ActionInvocationDto invocationDto,
            @NonNull ObjectAction objectAction,
            @NonNull ManagedObject value);

    /**
     * Records given result values into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Action</i>.
     */
    ActionInvocationDto recordActionResultNonScalar(
            @NonNull ActionInvocationDto invocationDto,
            @NonNull ObjectAction objectAction,
            @NonNull Can<ManagedObject> values);

    /**
     * Records given property value into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Property</i>.
     */
    PropertyDto recordPropertyValue(
            @NonNull PropertyDto propertyDto,
            @NonNull OneToOneAssociation property,
            @NonNull ManagedObject value);

    /**
     * Records given parameter value into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Action Parameter</i>.
     */
    ParamDto recordParamScalar(
            @NonNull ParamDto paramDto,
            @NonNull ObjectActionParameter param,
            @NonNull ManagedObject value);

    /**
     * Records given parameter values into given DTO object,
     * using {@link ValueSemanticsProvider} for corresponding <i>Action Parameter</i>.
     */
    ParamDto recordParamNonScalar(
            @NonNull ParamDto paramDto,
            @NonNull ObjectActionParameter param,
            @NonNull Can<ManagedObject> values);

}
