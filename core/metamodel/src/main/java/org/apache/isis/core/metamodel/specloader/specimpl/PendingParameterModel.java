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
package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Model used to negotiate the paramValues of an action by means of an UI dialog.
 *  
 * @since 2.0.0
 */
@Getter 
@RequiredArgsConstructor(staticName = "of")
public class PendingParameterModel {

    @NonNull private final ActionInteractionHead head;
    @NonNull private final Can<ManagedObject> paramValues;

    // -- SHORTCUTS
    
    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }

    @NonNull public ManagedObject getParamValue(int paramNum) {
        return paramValues.getElseFail(paramNum);
    }

    
}
