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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
@Deprecated // ParameterNegotiationModel has all we need
public class ManagedParameter implements ManagedFeature {
    @NonNull private final ManagedAction owningAction;
    @NonNull private final ObjectActionParameter parameter;
    
    @Deprecated //TODO use a binder instead
    @NonNull private final ManagedObject value;
    
    public ManagedObject getOwningObject() {
        return getOwningAction().getOwner();
    }
    
    public Optional<InteractionVeto> validate(ManagedObject proposedValue) {
        
        return Optional.ofNullable(
            getParameter()
                .isValid(getOwningAction().getInteractionHead(), proposedValue, InteractionInitiatedBy.USER))
        .map(reasonNotValid->InteractionVeto.actionParamInvalid(new Veto(reasonNotValid)));
    }
    
    public Optional<InteractionVeto> validate() {
        return validate(getValue());
    }

    @Override
    public Identifier getIdentifier() {
        return parameter.getIdentifier();
    }

    @Override
    public String getDisplayLabel() {
        return parameter.getName();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return parameter.getSpecification();
    }
    
}