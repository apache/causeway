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
package org.apache.isis.core.runtimeservices.publish;

import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.runtime.util.XrayUtil;

import lombok.Builder;
import lombok.NonNull;
import lombok.val;

final class _Xray {
    
    static Handle enterCommandPublishing(
            final @NonNull InteractionTracker iaTracker,
            final @Nullable Command command,
            final boolean canPublish,
            final @NonNull Can<CommandSubscriber> enabledSubscribers) {
        
        if(!XrayUi.isXrayEnabled()
                || !iaTracker.isInInteractionSession()) {
            return null;
        }
        
        final int authStackSize = iaTracker.getAuthenticationLayerCount();
        val conversationId = iaTracker.getConversationId().orElseThrow(_Exceptions::unexpectedCodeReach);
        
        val handle = createHandle(conversationId, authStackSize, "command-publisher");
        val enteringLabel = canPublish 
                ? String.format("publishing command to %d subscriber(s)", enabledSubscribers.size())
                : "not publishing command";
        
        XrayUi.updateModel(model->{
            model.lookupSequence(handle.sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                
                sequenceData.alias(handle.callee, "Command-\nPublisher-\n(Default)");
                sequenceData.enter(handle.caller, handle.callee, enteringLabel);
                
            });
        });
        
        return handle;
    }


    public static void exitCommandPublishing(final @Nullable Handle handle) {
        
        if(handle==null) {
            return; // x-ray is not enabled
        }
        
        XrayUi.updateModel(model->{
            model.lookupSequence(handle.sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                sequenceData.exit(handle.callee, handle.caller);
            });
        });
        
    }
    
    // -- HELPER
    
    private static Handle createHandle(
            final UUID interactionId,
            final int authStackSize,
            final String participantLabel) {

        val handle = Handle.builder()
                .sequenceId(XrayUtil.sequenceId(interactionId))
                .caller(authStackSize>0
                    ? XrayUtil.nestedInteractionId(authStackSize)
                    : "thread")
                .callee(participantLabel)
                .build();
        
        return handle;
    }
    
    @Builder
    static final class Handle {
        final @NonNull String sequenceId;
        final @NonNull String caller;
        final @NonNull String callee;
    }
    
}
