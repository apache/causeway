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
package org.apache.isis.core.runtime.util;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayModel.ThreadMemento;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.debug.xray.sequence.SequenceDiagram;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.interaction.session.InteractionTracker;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public final class XrayUtil {

    /**
     * Returns the sequence diagram data model's id, that is bound to the current thread and interaction.
     * @param iaTracker
     */
    public static Optional<String> currentSequenceId(final @NonNull InteractionTracker iaTracker) {
        return iaTracker.getConversationId()
                .map(XrayUtil::sequenceId);
    }
    
    public static String sequenceId(final @NonNull UUID uuid) {
        return String.format("seq-%s", uuid);
    }

    public static ThreadMemento currentThreadAsMemento() {
        val ct = Thread.currentThread();
        return ThreadMemento.of(
                String.format("thread-%d-%s", ct.getId(), ct.getName()), 
                String.format("Thread-%d [%s]", ct.getId(), ct.getName()),
                String.format("Thread-%d\n%s", ct.getId(), ct.getName())); 
    }
    
    public static String nestedInteractionId(int authenticationStackSize) {
        return "ia-" + (authenticationStackSize-1);
    }
    
    // -- SEQUENCE HANDLE
    
    public static Optional<SequenceHandle> createSequenceHandle(
            final @NonNull InteractionTracker iaTracker,
            final String ... callees) {

        if(!iaTracker.isInInteractionSession()) {
            return Optional.empty();
        }
        
        final int authStackSize = iaTracker.getAuthenticationLayerCount();
        val conversationId = iaTracker.getConversationId().orElseThrow(_Exceptions::unexpectedCodeReach);
        
        val handle = SequenceHandle.builder()
                .sequenceId(XrayUtil.sequenceId(conversationId))
                .caller(authStackSize>0
                        ? XrayUtil.nestedInteractionId(authStackSize)
                        : "thread")
                .callees(Can.ofArray(callees))
                .build();
        
        return Optional.of(handle);
    }
    
    @Value @Builder
    public static final class SequenceHandle {
        final @NonNull String sequenceId;
        final @NonNull String caller;
        final @NonNull Can<String> callees;
        
        public void submit(Consumer<SequenceDiagram> onSubmission) {
            XrayUi.updateModel(model->{
                model.lookupSequence(getSequenceId())
                .ifPresent(sequence->onSubmission.accept(sequence.getData()));
            });
        }
        
    }
    
}
