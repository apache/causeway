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
package org.apache.isis.viewer.common.model.binding.interaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class InteractionResponse {
    
    public static enum Veto {
        NOT_FOUND,
        HIDDEN,
        FORBIDDEN,
        UNAUTHORIZED, 
    }
    
    private final static InteractionResponse SUCCESS = of(null, null);
    @Getter private final Veto veto;
    @Getter private final String failureMessage;
    
    public boolean isSuccess() {
        return veto==null;
    }
    
    public boolean isFailure() {
        return !isSuccess();
    }
    
    // -- FACTORIES
    
    public static InteractionResponse failed(@NonNull Veto veto) {
        return of(veto, "unspecified");
    }
    
    public static InteractionResponse failed(@NonNull Veto veto, String reason) {
        return of(veto, reason);
    }

    public static InteractionResponse success() {
        return SUCCESS;
    }

    
    
}
