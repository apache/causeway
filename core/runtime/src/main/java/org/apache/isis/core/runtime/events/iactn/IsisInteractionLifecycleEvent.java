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
package org.apache.isis.core.runtime.events.iactn;

import org.apache.isis.core.runtime.iactn.InteractionSession;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;

@Value(staticConstructor="of") @ToString(of = "eventType")
public class IsisInteractionLifecycleEvent {

    public enum EventType {
        HAS_STARTED,
        IS_ENDING,
        FLUSH_REQUEST, 
    }

    @Getter String conversationId;
    @Getter InteractionSession interactionSession;
    @Getter EventType eventType;

}
