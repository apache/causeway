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
package org.apache.causeway.core.runtimeservices.session;

import java.util.Stack;

import org.apache.causeway.applib.services.iactnlayer.InteractionLayer;
import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayDataModel;
import org.apache.causeway.commons.internal.debug.xray.XrayModel.ThreadMemento;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.security.util.XrayUtil;

//@Log4j2
final class _Xray {

    static void newInteractionLayer(final Stack<InteractionLayer> afterEnter) {

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        // make defensive copies, so can use in another thread
        final int authStackSize = afterEnter.size();
        var interactionId = afterEnter.peek().interaction().getInteractionId();
        var executionContext = afterEnter.peek().interactionContext();

        _XrayEvent.interactionOpen("open interaction %s", interactionId);

        var threadId = ThreadMemento.fromCurrentThread();

        XrayUi.updateModel(model->{

            var sequenceId = XrayUtil.sequenceId(interactionId);
            var iaLabel = String.format("Interaction-%s", interactionId);
            var iaLabelMultiline = String.format("Interaction\n%s", interactionId);
            var iaOpeningLabel = String.format("open interaction\n%s",
                    executionContext.getUser().toString().replace(", ", ",\n"));

            var uiInteractionId = XrayUtil.nestedInteractionId(authStackSize);

            if(authStackSize==1) {
                var uiThreadNode = model.getThreadNode(threadId);

                //var uiTopAuthLayerNode = model.addContainerNode(uiThreadNode, iaLabel);

                var sequenceData = model.addDataNode(
                            uiThreadNode,//uiTopAuthLayerNode,
                            new XrayDataModel.Sequence(sequenceId, iaLabel))
                        .getData();

                sequenceData.alias("thread", threadId.multilinelabel());
                sequenceData.alias(uiInteractionId, iaLabelMultiline);

                sequenceData.enter("thread", uiInteractionId, iaOpeningLabel);
                sequenceData.activate(uiInteractionId);

                return;
            }

            model.lookupSequence(sequenceId)
            .ifPresent(sequence->{
                var sequenceData = sequence.getData();
                sequenceData
                .enter(XrayUtil.nestedInteractionId(authStackSize-1), uiInteractionId, iaOpeningLabel);
                sequenceData.activate(uiInteractionId);
            });

        });

    }

    public static void closeInteractionLayer(final Stack<InteractionLayer> beforeClose) {

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        final int authStackSize = beforeClose.size();
        var interactionId = beforeClose.peek().interaction().getInteractionId();
        var sequenceId = XrayUtil.sequenceId(interactionId);

        _XrayEvent.interactionClose("close interaction %s", interactionId);

        XrayUi.updateModel(model->{

            var uiInteractionId = XrayUtil.nestedInteractionId(authStackSize);

            model.lookupSequence(sequenceId)
            .ifPresent(sequence->{
                var sequenceData = sequence.getData();

                if(authStackSize==1) {
                    sequenceData.exit(uiInteractionId, "thread", "close");
                    return;
                }

                sequenceData
                .exit(uiInteractionId, XrayUtil.nestedInteractionId(authStackSize-1), "close");
                sequenceData.deactivate(uiInteractionId);
            });

        });

    }

}
