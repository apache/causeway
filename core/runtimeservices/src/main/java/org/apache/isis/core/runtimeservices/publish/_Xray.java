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

import javax.annotation.Nullable;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.runtime.util.XrayUtil;
import org.apache.isis.core.runtime.util.XrayUtil.SequenceHandle;

import lombok.NonNull;
import lombok.val;

final class _Xray {
    
    // -- COMMAND
    
    static SequenceHandle enterCommandPublishing(
            final @NonNull InteractionTracker iaTracker,
            final @Nullable Command command,
            final boolean canPublish,
            final @NonNull Can<CommandSubscriber> enabledSubscribers) {
        
        if(!XrayUi.isXrayEnabled()) {
            return null;
        }
        
        val enteringLabel = canPublish 
                ? String.format("publishing command to %d subscriber(s)", enabledSubscribers.size())
                : "not publishing command";
        
        val handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "cmd-publisher");
        handleIfAny.ifPresent(handle->{
           
            handle.submit(sequenceData->{
                
                sequenceData.alias("cmd-publisher", "Command-\nPublisher-\n(Default)");
                
                val callee = handle.getCallees().getFirstOrFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                sequenceData.activate(callee);
            });
            
        });
        
        return handleIfAny.orElse(null);
        
    }
    
    // -- EXECUTION
    
    public static SequenceHandle enterExecutionPublishing(
            final @NonNull InteractionTracker iaTracker,
            final @Nullable Execution<?, ?> command,
            final boolean canPublish,
            final @NonNull Can<ExecutionSubscriber> enabledSubscribers) {
        
        if(!XrayUi.isXrayEnabled()) {
            return null;
        }
        
        val enteringLabel = canPublish 
                ? String.format("publishing execution to %d subscriber(s)", enabledSubscribers.size())
                : "not publishing execution";
        
        val handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "exec-publisher");
        handleIfAny.ifPresent(handle->{
            
            handle.submit(sequenceData->{
                
                sequenceData.alias("exec-publisher", "Execution-\nPublisher-\n(Default)");
                
                val callee = handle.getCallees().getFirstOrFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                sequenceData.activate(callee);
            });
            
        });
        
        return handleIfAny.orElse(null);
        
    }
    
    // -- EXIT
    
    public static void exitPublishing(final @Nullable SequenceHandle handle) {
        
        if(handle==null) {
            return; // x-ray is not enabled
        }
        
        handle.submit(sequenceData->{
            val callee = handle.getCallees().getFirstOrFail();
            sequenceData.exit(callee, handle.getCaller());
            sequenceData.deactivate(callee);
        });
        
    }
    
}
