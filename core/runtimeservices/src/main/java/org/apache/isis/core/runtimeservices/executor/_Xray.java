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
package org.apache.isis.core.runtimeservices.executor;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.runtime.util.XrayUtil;

import lombok.Builder;
import lombok.NonNull;
import lombok.val;

final class _Xray {

    static Handle enterInvocation(Interaction interaction) {
        if(!XrayUi.isXrayEnabled()
                || interaction.getCurrentExecution()==null) {
            return null;
        }
        
        val mId = interaction.getCurrentExecution().getMemberIdentifier();
        
        val command = interaction.getCommand();
        
        val handle = Handle.builder()
                .sequenceId(XrayUtil.sequenceId(interaction.getInteractionId()))
                .caller("thread")
                .callee(mId.getLogicalIdentityString("#"))
                .build();
        
        val enteringLabel = String.format("invoking");

        XrayUi.updateModel(model->{
            model.lookupSequence(handle.sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                sequenceData.enter(handle.caller, handle.callee, enteringLabel);
            });
        });
        
        return handle;
    }

    static void exitInvocation(final @Nullable Handle handle) {
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
    
    @Builder
    static final class Handle {
        final @NonNull String sequenceId;
        final @NonNull String caller;
        final @NonNull String callee;
    }

}
