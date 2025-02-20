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
package org.apache.causeway.core.runtimeservices.executor;

import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.execution.InteractionInternal;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.security.util.XrayUtil;
import org.apache.causeway.core.security.util.XrayUtil.SequenceHandle;

import org.jspecify.annotations.NonNull;

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

        var participantLabel = owningAction.getFeatureIdentifier().getLogicalIdentityString("\n#");
        var enteringLabel = argumentAdapters.isEmpty()
                ? "action invocation (no args)"
                : String.format("action invocation w/ %d args:\n  %s",
                        argumentAdapters.size(),
                        argumentAdapters.stream()
                        .map(MmUnwrapUtils::single)
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

        var participantLabel = owningProperty.getFeatureIdentifier().getLogicalIdentityString("\n#");
        var enteringLabel = String.format("property edit -> '%s'",
                MmUnwrapUtils.single(newValueAdapter));

        return enterInvocation(iaTracker, interaction, participantLabel, enteringLabel);
    }

    private static SequenceHandle enterInvocation(
            final @NonNull InteractionLayerTracker iaTracker,
            final InteractionInternal interaction,
            final String participantLabel,
            final String enteringLabel) {

        // var execution = interaction.getCurrentExecution(); // XXX why not populated?

        var handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "executor", participantLabel);
        handleIfAny.ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("executor", "Member-\nExecutorService-\n(Default)");

                var callee1 = handle.callees().getFirstElseFail();
                var callee2 = handle.callees().getLastElseFail();

                sequenceData.enter(handle.caller(), callee1);
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

            var callee1 = handle.callees().getFirstElseFail();
            var callee2 = handle.callees().getLastElseFail();

            sequenceData.exit(callee2, callee1);
            sequenceData.deactivate(callee2);

            sequenceData.exit(callee1, handle.caller());
            sequenceData.deactivate(callee1);

        });

    }

}
