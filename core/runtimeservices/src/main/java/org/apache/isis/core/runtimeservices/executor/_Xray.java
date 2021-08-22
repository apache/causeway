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

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.execution.InteractionInternal;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.security.util.XrayUtil;
import org.apache.isis.core.security.util.XrayUtil.SequenceHandle;

import lombok.NonNull;
import lombok.val;

final class _Xray {

    static SequenceHandle enterActionInvocation(
            final @NonNull InteractionLayerTracker iaTracker,
            final @NonNull InteractionInternal interaction,
            final @NonNull ObjectAction owningAction,
            final @NonNull InteractionHead head,
            final @NonNull Can<ManagedObject> argumentAdapters) {

        if(!XrayUi.isXrayEnabled()) {
            return null;
        }

        val participantLabel = owningAction.getFeatureIdentifier().getLogicalIdentityString("\n#");
        val enteringLabel = argumentAdapters.isEmpty()
                ? "action invocation (no args)"
                : String.format("action invocation w/ %d args:\n  %s",
                        argumentAdapters.size(),
                        argumentAdapters.stream()
                        .map(ManagedObjects.UnwrapUtil::single)
                        .map(obj->"" + obj)
                        .collect(Collectors.joining(",\n  ")));

        return enterInvocation(iaTracker, interaction, participantLabel, enteringLabel);
    }

    public static SequenceHandle enterPropertyEdit(
            final @NonNull InteractionLayerTracker iaTracker,
            final @NonNull InteractionInternal interaction,
            final @NonNull OneToOneAssociation owningProperty,
            final @NonNull InteractionHead head,
            final @NonNull ManagedObject newValueAdapter) {

        if(!XrayUi.isXrayEnabled()) {
            return null;
        }

        val participantLabel = owningProperty.getFeatureIdentifier().getLogicalIdentityString("\n#");
        val enteringLabel = String.format("property edit -> '%s'",
                ManagedObjects.UnwrapUtil.single(newValueAdapter));

        return enterInvocation(iaTracker, interaction, participantLabel, enteringLabel);
    }

    private static SequenceHandle enterInvocation(
            final @NonNull InteractionLayerTracker iaTracker,
            final InteractionInternal interaction,
            final String participantLabel,
            final String enteringLabel) {

        // val execution = interaction.getCurrentExecution(); // XXX why not populated?

        val handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "executor", participantLabel);
        handleIfAny.ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("executor", "Member-\nExecutorService-\n(Default)");

                val callee1 = handle.getCallees().getFirstOrFail();
                val callee2 = handle.getCallees().getLastOrFail();

                sequenceData.enter(handle.getCaller(), callee1);
                sequenceData.activate(callee1);

                sequenceData.enter(callee1, callee2, enteringLabel);
                sequenceData.activate(callee2);
            });

        });

        return handleIfAny.orElse(null);

    }

    static void exitInvocation(final @Nullable SequenceHandle handle) {
        if(handle==null) {
            return; // x-ray is not enabled
        }

        handle.submit(sequenceData->{

            val callee1 = handle.getCallees().getFirstOrFail();
            val callee2 = handle.getCallees().getLastOrFail();

            sequenceData.exit(callee2, callee1);
            sequenceData.deactivate(callee2);

            sequenceData.exit(callee1, handle.getCaller());
            sequenceData.deactivate(callee1);

        });

    }

}
