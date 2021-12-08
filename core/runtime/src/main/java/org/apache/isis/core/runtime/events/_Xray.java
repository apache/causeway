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
package org.apache.isis.core.runtime.events;

import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.commons.internal.debug._XrayEvent;
import org.apache.isis.commons.internal.debug.xray.XrayDataModel;
import org.apache.isis.commons.internal.debug.xray.XrayModel.Stickiness;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.security.util.XrayUtil;

import lombok.val;

final class _Xray {

    static void addConfiguration(final ConfigurationViewService configurationService) {

        XrayUi.updateModel(model->{

            val root = model.getRootNode();

            val env = model.addDataNode(
                    root, new XrayDataModel.KeyValue("isis-env", "Environment", Stickiness.CANNOT_DELETE_NODE));
            configurationService.getEnvironmentProperties().forEach(item->{
                env.getData().put(item.getKey(), item.getValue());
            });

            val config = model.addDataNode(
                    root, new XrayDataModel.KeyValue("isis-conf", "Config", Stickiness.CANNOT_DELETE_NODE));
            configurationService.getVisibleConfigurationProperties().forEach(item->{
                config.getData().put(item.getKey(), item.getValue());
            });

        });

    }

    public static void txBeforeCompletion(final InteractionLayerTracker iaTracker, final String txInfo) {
        // append to the current interaction if any

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        _XrayEvent.transaction(txInfo);

        //val threadId = ThreadMemento.fromCurrentThread();

        val sequenceId = XrayUtil.currentSequenceId(iaTracker)
        .orElse(null);

        XrayUi.updateModel(model->{

            val seq = model.lookupSequence(sequenceId);

            // if no sequence diagram available, that we can append to,
            // then at least add a node to the left tree
            //XXX replaced by log above
//            if(!seq.isPresent()) {
//                val uiThreadNode = model.getThreadNode(threadId);
//                model.addContainerNode(
//                        uiThreadNode,
//                        txInfo,
//                        Stickiness.CAN_DELETE_NODE);
//                return;
//            }

            seq.ifPresent(sequence->{
                val sequenceData = sequence.getData();
                sequenceData.alias("evb", "EventBus");
                sequenceData.enter("tx", "evb", txInfo);
            });

        });

    }

    public static void txAfterCompletion(final InteractionLayerTracker iaTracker, final String txInfo) {
        // append to the current interaction if any

        if(!XrayUi.isXrayEnabled()) {
            return;
        }

        _XrayEvent.transaction(txInfo);

        //val threadId = ThreadMemento.fromCurrentThread();

        val sequenceId = XrayUtil.currentSequenceId(iaTracker)
                .orElse(null);

        XrayUi.updateModel(model->{

            val seq = model.lookupSequence(sequenceId);

            // if no sequence diagram available, that we can append to,
            // then at least add a node to the left tree
            //XXX replaced by log above
//            if(!seq.isPresent()) {
//                val uiThreadNode = model.getThreadNode(threadId);
//                model.addContainerNode(
//                        uiThreadNode,
//                        txInfo,
//                        Stickiness.CAN_DELETE_NODE);
//                return;
//            }

            seq.ifPresent(sequence->{
                val sequenceData = sequence.getData();
                sequenceData.enter("tx", "evb", txInfo);
            });

        });

    }

}
