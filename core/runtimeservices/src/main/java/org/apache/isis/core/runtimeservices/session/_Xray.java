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

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.interaction.session.AuthenticationLayer;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class _Xray {

    static void newAuthenticationLayer(Stack<AuthenticationLayer> afterEnter) {
        
        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        // make defensive copies, so can use in another thread
        final int authStackSize = afterEnter.size();
        val interactionId = afterEnter.peek().getInteractionSession().getInteractionId();
        val threadId = _Probe.currentThreadId();
        
        XrayUi.updateModel(model->{
            
            val nodeStackId = "ia-" + interactionId.toString();
            val uiNodeStack = model.getNodeStack(nodeStackId);
            
            if(authStackSize==1) {
                val uiThreadNode = model.getThreadNode(threadId);
                
                val uiTopAuthLayerNode = model.addContainerNode(
                        uiThreadNode,
                        String.format(
                                "Interaction %s",
                                interactionId));
                
                uiNodeStack.push(uiTopAuthLayerNode);
                return;
            }
            
            val uiParentNode = uiNodeStack.peek();
            val newUiAuthLayerNode = model.addContainerNode(
                    uiParentNode, 
                    String.format(
                            "Layer",
                            interactionId));
            uiNodeStack.push(newUiAuthLayerNode);
            
        });
        
    }

    public static void closeAuthenticationLayer(Stack<AuthenticationLayer> beforeClose) {
        
        if(!XrayUi.isXrayEnabled()) {
            return;
        }
        
        val interactionId = beforeClose.peek().getInteractionSession().getInteractionId();
        
        XrayUi.updateModel(model->{
            
            val nodeStackId = "ia-" + interactionId.toString();
            val uiNodeStack = model.getNodeStack(nodeStackId);
            if(uiNodeStack.isEmpty()) {
                log.warn("inconsistent uiNodeStack size; already empty when trying to model layer exit");
                return;
            }
            uiNodeStack.pop();
        });
        
    }

}
