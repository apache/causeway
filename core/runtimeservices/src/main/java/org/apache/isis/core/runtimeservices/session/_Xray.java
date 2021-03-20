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
import org.apache.isis.core.runtime.util.XrayUtil;

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
        
        val threadId = XrayUtil.currentThreadAsMemento();
        
        XrayUi.updateModel(model->{
            
            val sequenceId = XrayUtil.sequenceId(interactionId);
            val iaLabel = String.format("Interaction-%s", interactionId);
            val iaLabelMultiline = String.format("Interaction\n%s", interactionId);
            val iaOpeningLabel = String.format("open interaction\n%s", 
                    executionContext.getUser().toString().replace(", ", ",\n"));
            
            val uiInteractionId = XrayUtil.nestedInteractionId(authStackSize);
            
            if(authStackSize==1) {
                val uiThreadNode = model.getThreadNode(threadId);
                
                //val uiTopAuthLayerNode = model.addContainerNode(uiThreadNode, iaLabel);
                
                val sequenceData = model.addDataNode(
                            uiThreadNode,//uiTopAuthLayerNode, 
                            new XrayDataModel.Sequence(sequenceId, iaLabel))
                        .getData();
                
                sequenceData.alias("thread", threadId.getMultilinelabel());
                sequenceData.alias(uiInteractionId, iaLabelMultiline);
                
                sequenceData.enter("thread", uiInteractionId, iaOpeningLabel);
                
                return;
            }
            
            model.lookupSequence(sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                sequenceData
                .enter(XrayUtil.nestedInteractionId(authStackSize-1), uiInteractionId, iaOpeningLabel);
            });
            
            
        });
        
    }

    public static void closeAuthenticationLayer(Stack<AuthenticationLayer> beforeClose) {
        
        if(!XrayUi.isXrayEnabled()) {
            return;
        }
        
        final int authStackSize = beforeClose.size();
        val interactionId = beforeClose.peek().getInteractionSession().getInteractionId();
        val sequenceId = XrayUtil.sequenceId(interactionId);
        
        XrayUi.updateModel(model->{
            
            val uiInteractionId = XrayUtil.nestedInteractionId(authStackSize);
            
            model.lookupSequence(sequenceId)
            .ifPresent(sequence->{
                val sequenceData = sequence.getData();
                
                if(authStackSize==1) {
                    sequenceData.exit(uiInteractionId, "thread", "close");
                    return;
                }
                
                sequenceData
                .exit(uiInteractionId, XrayUtil.nestedInteractionId(authStackSize-1), "close");
            });
            
        });
        
    }


}
