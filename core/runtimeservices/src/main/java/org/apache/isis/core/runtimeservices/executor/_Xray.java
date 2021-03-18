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

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.execution.InternalInteraction;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.util.XrayUtil;

import lombok.Builder;
import lombok.NonNull;
import lombok.val;

final class _Xray {

    static Handle enterActionInvocation(
            final @NonNull InternalInteraction interaction, 
            final @NonNull ObjectAction owningAction,
            final @NonNull InteractionHead head, 
            final @NonNull Can<ManagedObject> argumentAdapters) {
        
        if(!XrayUi.isXrayEnabled()) {
            return null;
        }
        
        val participantLabel = owningAction.getIdentifier().getLogicalIdentityString("\n#")
                .replace(".","\n  ."); // poor men's line breaking
        val enteringLabel = argumentAdapters.isEmpty()
                ? "action invocation (no args)"
                : String.format("action invocation w/ %d args:\n  %s",
                        argumentAdapters.size(),
                        argumentAdapters.stream()
                        .map(ManagedObjects.UnwrapUtil::single)
                        .map(obj->"" + obj)
                        .collect(Collectors.joining(",\n  ")));
        
        return enterInvocation(interaction, participantLabel, enteringLabel);
    }
    
    public static Handle enterPropertyEdit(
            final @NonNull InternalInteraction interaction, 
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull InteractionHead head, 
            final @NonNull ManagedObject newValueAdapter) {
        
        if(!XrayUi.isXrayEnabled()) {
            return null;
        }
        
        val participantLabel = owningProperty.getIdentifier().getLogicalIdentityString("\n#")
                .replace(".","\n  ."); // poor men's line breaking
        val enteringLabel = String.format("property edit -> '%s'", 
                ManagedObjects.UnwrapUtil.single(newValueAdapter));
        
        return enterInvocation(interaction, participantLabel, enteringLabel);
    }
    
    private static Handle enterInvocation(
            final InternalInteraction interaction,
            final String participantLabel,
            final String enteringLabel) {


//        val execution = interaction.getCurrentExecution(); // XXX why not populated?
//
//        val command = interaction.getCommand();
//        if(command==null
//                || command.getCommandDto()==null
//                || command.getCommandDto().getMember()==null) {
//            return null;
//        }
//        
//        // the act/prop/coll that is interacted with
//        val memberDto = command.getCommandDto().getMember();
//        
//        val memberLogicalId = memberDto.getLogicalMemberIdentifier();
//        
//        val interactionDescription = memberDto.getInteractionType()==InteractionType.PROPERTY_EDIT
//                ? String.format("property edit -> '%s'", 
//                        CommonDtoUtils.<Object>getValue(((PropertyDto)memberDto).getNewValue()))
//                : String.format("action invocation");
        
        val handle = Handle.builder()
                .sequenceId(XrayUtil.sequenceId(interaction.getInteractionId()))
                .caller("thread")
                .callee(participantLabel)
                .build();
        
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
