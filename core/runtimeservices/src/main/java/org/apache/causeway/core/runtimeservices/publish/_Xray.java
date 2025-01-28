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
package org.apache.causeway.core.runtimeservices.publish;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.publishing.spi.CommandSubscriber;
import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.security.util.XrayUtil;
import org.apache.causeway.core.security.util.XrayUtil.SequenceHandle;

import org.jspecify.annotations.NonNull;

final class _Xray {

    // -- COMMAND

    static SequenceHandle enterCommandReadyPublishing(
            final @NonNull InteractionLayerTracker iaTracker,
            final @Nullable Command command,
            final @NonNull Can<CommandSubscriber> enabledSubscribers,
            final @NonNull Supplier<String> cannotPublishReasonSupplier) {

        return enterCommandPublishing(iaTracker, command, enabledSubscribers, cannotPublishReasonSupplier, "created");
    }

    static SequenceHandle enterCommandStartedPublishing(
            final @NonNull InteractionLayerTracker iaTracker,
            final @Nullable Command command,
            final @NonNull Can<CommandSubscriber> enabledSubscribers,
            final @NonNull Supplier<String> cannotPublishReasonSupplier) {

        return enterCommandPublishing(iaTracker, command, enabledSubscribers, cannotPublishReasonSupplier, "started");

    }

    static SequenceHandle enterCommandCompletedPublishing(
            final @NonNull InteractionLayerTracker iaTracker,
            final @Nullable Command command,
            final @NonNull Can<CommandSubscriber> enabledSubscribers,
            final @NonNull Supplier<String> cannotPublishReasonSupplier) {

        return enterCommandPublishing(iaTracker, command, enabledSubscribers, cannotPublishReasonSupplier, "completed");
    }

    private static SequenceHandle enterCommandPublishing(InteractionLayerTracker iaTracker, Command command, Can<CommandSubscriber> enabledSubscribers, Supplier<String> cannotPublishReasonSupplier, String verb) {
        if(!XrayUi.isXrayEnabled()) {
            return null;
        }

        var cannotPublishReason = cannotPublishReasonSupplier.get();
        var canPublish = cannotPublishReason==null;
        var enteringLabel = canPublish
                ? String.format("publishing command %s to %d subscriber(s):\n%s",
                        verb,
                        enabledSubscribers.size(),
                        toText(command))
                : String.format("not publishing command %s:\n%s", verb, cannotPublishReason);

        var handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "cmd-publisher");
        handleIfAny.ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("cmd-publisher", "Command-\nPublisher-\n(Default)");

                if(!canPublish) {
                    sequenceData.setConnectionArrowColor(Color.GRAY);
                    sequenceData.setConnectionLabelColor(Color.GRAY);
                }

                var callee = handle.getCallees().getFirstElseFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                sequenceData.activate(callee);
            });

        });

        return handleIfAny.orElse(null);
    }

    // -- EXECUTION

    public static SequenceHandle enterExecutionPublishing(
            final @NonNull InteractionLayerTracker iaTracker,
            final @Nullable Execution<?, ?> execution,
            final @NonNull Can<ExecutionSubscriber> enabledSubscribers,
            final @NonNull Supplier<String> cannotPublishReasonSupplier) {

        if(!XrayUi.isXrayEnabled()) {
            return null;
        }

        var cannotPublishReason = cannotPublishReasonSupplier.get();
        var canPublish = cannotPublishReason==null;
        var enteringLabel = canPublish
                ? String.format("publishing execution to %d subscriber(s):\n%s",
                        enabledSubscribers.size(),
                        toText(execution))
                : String.format("not publishing execution:\n%s", cannotPublishReason);

        var handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "exec-publisher");
        handleIfAny.ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("exec-publisher", "Execution-\nPublisher-\n(Default)");

                if(!canPublish) {
                    sequenceData.setConnectionArrowColor(Color.GRAY);
                    sequenceData.setConnectionLabelColor(Color.GRAY);
                }

                var callee = handle.getCallees().getFirstElseFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                sequenceData.activate(callee);
            });

        });

        return handleIfAny.orElse(null);

    }

    // -- ENTITY CHANGES

    public static SequenceHandle enterEntityChangesPublishing(
            final @NonNull InteractionLayerTracker iaTracker,
            final @NonNull Optional<EntityChanges> payload,
            final @NonNull Can<EntityChangesSubscriber> enabledSubscribers,
            final @NonNull Supplier<String> cannotPublishReasonSupplier) {

        if(!XrayUi.isXrayEnabled()) {
            return null;
        }

        var cannotPublishReason = cannotPublishReasonSupplier.get();
        var canPublish = cannotPublishReason==null;
        var enteringLabel = canPublish
                ? String.format("publishing entity-changes to %d subscriber(s):\n%s",
                        enabledSubscribers.size(),
                        payload.map(x->toText(x)).orElse("null"))
                : String.format("not publishing entity-changes:\n%s", cannotPublishReason);

        var handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "ec-publisher");
        handleIfAny.ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("ec-publisher", "EntityChanges-\nPublisher-\n(Default)");

                if(!canPublish) {
                    sequenceData.setConnectionArrowColor(Color.GRAY);
                    sequenceData.setConnectionLabelColor(Color.GRAY);
                }

                var callee = handle.getCallees().getFirstElseFail();
                sequenceData.enter(handle.getCaller(), callee, enteringLabel);
                sequenceData.activate(callee);
            });

        });

        return handleIfAny.orElse(null);

    }

    // -- ENTITY PROPERTY CHANGES

    public static SequenceHandle enterEntityPropertyChangePublishing(
            final @NonNull InteractionLayerTracker iaTracker,
            final @NonNull Can<EntityPropertyChange> payload,
            final @NonNull Can<EntityPropertyChangeSubscriber> enabledSubscribers,
            final @NonNull Supplier<String> cannotPublishReasonSupplier) {

        if(!XrayUi.isXrayEnabled()) {
            return null;
        }

        var cannotPublishReason = cannotPublishReasonSupplier.get();
        var canPublish = cannotPublishReason==null;
        var enteringLabel = canPublish
                ? String.format("publishing entity-property-changes to %d subscriber(s):\n%s",
                        enabledSubscribers.size(),
                        toText(payload))
                : String.format("not publishing entity-property-changes:\n%s", cannotPublishReason);

        var handleIfAny = XrayUtil.createSequenceHandle(iaTracker, "epc-publisher");
        handleIfAny.ifPresent(handle->{

            handle.submit(sequenceData->{

                sequenceData.alias("epc-publisher", "EntityProperty-\nChanges-Publisher-\n(Default)");

                if(!canPublish) {
                    sequenceData.setConnectionArrowColor(Color.GRAY);
                    sequenceData.setConnectionLabelColor(Color.GRAY);
                }

                var callee = handle.getCallees().getFirstElseFail();
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
            var callee = handle.getCallees().getFirstElseFail();
            sequenceData.exit(callee, handle.getCaller());
            sequenceData.deactivate(callee);

            sequenceData.setConnectionArrowColor(null);
            sequenceData.setConnectionLabelColor(null);
        });

    }

    // -- HELPER

    static String toText(Command command) {
        return _Text.breakLines(Can.of(command.toString()), 80)
                .stream()
                .collect(Collectors.joining("\n "));
    }

    static String toText(Execution<?, ?> execution) {
        //TODO
        return execution.toString();
    }

    static String toText(EntityChanges changes) {
        //TODO
        return changes.toString();
    }

    static String toText(Can<EntityPropertyChange> changes) {
        //TODO
        return changes.toString();
    }

}
