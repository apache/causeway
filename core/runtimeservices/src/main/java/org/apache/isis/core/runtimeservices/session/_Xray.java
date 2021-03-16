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
package org.apache.isis.core.runtimeservices.session;

import java.util.Stack;

import org.apache.isis.commons.internal.debug.xray.XrayDataModel;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.interaction.session.AuthenticationLayer;

import lombok.val;

//@Log4j2
final class _Xray {

    static void newAuthenticationLayer(Stack<AuthenticationLayer> afterEnter) {
        
        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        // make defensive copies, so can use in another thread
        final int authStackSize = afterEnter.size();
        val interactionId = afterEnter.peek().getInteractionSession().getInteractionId();
        val executionContext = afterEnter.peek().getExecutionContext();
        
        val ct = Thread.currentThread();
        val threadLabel = String.format("Thread-%d\n%s", ct.getId(), ct.getName());
        
        XrayUi.updateModel(model->{
            
            val sequenceId = String.format("seq-%s", interactionId);
            val iaLabel = String.format("Interaction\n%s", interactionId);
            val iaOpeningLabel = String.format("open interaction\n%s", 
                    executionContext.getUser().toString().replace(", ", ",\n"));
            
            if(authStackSize==1) {
                val uiThreadNode = model.getThreadNode(threadLabel);
                
                val uiTopAuthLayerNode = model.addContainerNode(uiThreadNode, iaLabel);
                
                val sequenceData = model.addDataNode(
                            uiTopAuthLayerNode, 
                            new XrayDataModel.Sequence(sequenceId, "Sequence Diagam"))
                        .getData();
                
                sequenceData.alias("thread", threadLabel);
                sequenceData.alias("ia-0", iaLabel);
                
                sequenceData.enter("thread", "ia-0", iaOpeningLabel);
                
                return;
            }
            
            model.lookupSequence(sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                sequenceData.enter("ia-" + (authStackSize-2), "ia" + (authStackSize-1), iaOpeningLabel);
            });
            
            
        });
        
    }

    public static void closeAuthenticationLayer(Stack<AuthenticationLayer> beforeClose) {
        
        if(!XrayUi.isXrayEnabled()) {
            return;
        }
        
        final int authStackSize = beforeClose.size();
        val interactionId = beforeClose.peek().getInteractionSession().getInteractionId();
        val sequenceId = String.format("seq-%s", interactionId);
        
        XrayUi.updateModel(model->{
            
            model.lookupSequence(sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                
                if(authStackSize==1) {
                    sequenceData.exit("ia-0", "thread", "close");
                    return;
                }
                
                sequenceData.exit("ia-" + (authStackSize-2) , "ia" + (authStackSize-1), "close");
            });
            
        });
        
    }

}
