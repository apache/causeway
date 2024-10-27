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
package org.apache.causeway.core.runtimeservices.transaction;

import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.security.util.XrayUtil;

final class _Xray {

    static void txBeforeCompletion(final InteractionLayerTracker iaTracker, final String txInfo) {
        // append to the current interaction if any

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        _XrayEvent.transaction(txInfo);

        //var threadId = ThreadMemento.fromCurrentThread();

        var sequenceId = XrayUtil.currentSequenceId(iaTracker)
        .orElse(null);

        XrayUi.updateModel(model->{

            var seq = model.lookupSequence(sequenceId);

            // if no sequence diagram available, that we can append to,
            // then at least add a node to the left tree
            //XXX replaced by log above
//            if(!seq.isPresent()) {
//                var uiThreadNode = model.getThreadNode(threadId);
//                model.addContainerNode(
//                        uiThreadNode,
//                        txInfo,
//                        Stickiness.CAN_DELETE_NODE);
//                return;
//            }

            seq.ifPresent(sequence->{
                var sequenceData = sequence.getData();
                sequenceData.alias("evb", "EventBus");
                sequenceData.enter("tx", "evb", txInfo);
            });

        });

    }

    static void txAfterCompletion(final InteractionLayerTracker iaTracker, final String txInfo) {
        // append to the current interaction if any

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        _XrayEvent.transaction(txInfo);

        //var threadId = ThreadMemento.fromCurrentThread();

        var sequenceId = XrayUtil.currentSequenceId(iaTracker)
                .orElse(null);

        XrayUi.updateModel(model->{

            var seq = model.lookupSequence(sequenceId);

            // if no sequence diagram available, that we can append to,
            // then at least add a node to the left tree
            //XXX replaced by log above
//            if(!seq.isPresent()) {
//                var uiThreadNode = model.getThreadNode(threadId);
//                model.addContainerNode(
//                        uiThreadNode,
//                        txInfo,
//                        Stickiness.CAN_DELETE_NODE);
//                return;
//            }

            seq.ifPresent(sequence->{
                var sequenceData = sequence.getData();
                sequenceData.enter("tx", "evb", txInfo);
            });

        });

    }

}
