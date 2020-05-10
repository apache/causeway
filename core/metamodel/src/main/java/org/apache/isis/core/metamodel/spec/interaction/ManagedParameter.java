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
package org.apache.isis.core.metamodel.spec.interaction;

import java.util.Optional;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.interactions.InteractionContext.Head;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
public class ManagedParameter {
    @NonNull private final ManagedAction owningAction;
    @NonNull private final ObjectActionParameter parameter;
    @NonNull private final ManagedObject value;
    
    public ManagedObject getOwningObject() {
        return getOwningAction().getOwner();
    }
    
    public Optional<InteractionVeto> validate() {
        
        val head2 = owningAction.getAction().newPendingParameterModelHead(getOwningObject());
        val head = Head.of(head2.getActionOwner(), head2.getActionTarget());
        
        return Optional.ofNullable(
            getParameter()
                .isValid(head, getValue(), InteractionInitiatedBy.USER))
        .map(reasonNotValid->InteractionVeto.actionParamInvalid(new Veto(reasonNotValid)));
    }
    
}