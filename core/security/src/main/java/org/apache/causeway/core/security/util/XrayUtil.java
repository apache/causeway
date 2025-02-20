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
package org.apache.causeway.core.security.util;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.commons.internal.debug.xray.graphics.SequenceDiagram;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

public record XrayUtil() {

    /**
     * Returns the sequence diagram data model's id, that is bound to the current thread and interaction.
     * @param interactionProvider
     */
    public static Optional<String> currentSequenceId(final @NonNull InteractionProvider interactionProvider) {
        return interactionProvider.getInteractionId()
                .map(XrayUtil::sequenceId);
    }

    public static String sequenceId(final @NonNull UUID uuid) {
        return String.format("seq-%s", uuid);
    }

    public static String nestedInteractionId(final int authenticationStackSize) {
        return "ia-" + (authenticationStackSize-1);
    }

    // -- SEQUENCE HANDLE

    public static Optional<SequenceHandle> createSequenceHandle(
            final @NonNull InteractionProvider interactionProvider,
            final String ... callees) {

        if(!interactionProvider.isInInteraction()) {
            return Optional.empty();
        }

        final int authStackSize = interactionProvider.getInteractionLayerCount();
        var interactionId = interactionProvider.getInteractionId().orElseThrow(_Exceptions::unexpectedCodeReach);

        var handle = SequenceHandle.builder()
                .sequenceId(XrayUtil.sequenceId(interactionId))
                .caller(authStackSize>0
                        ? XrayUtil.nestedInteractionId(authStackSize)
                        : "thread")
                .callees(Can.ofArray(callees))
                .build();

        return Optional.of(handle);

    }

    @Builder
    public record SequenceHandle(
            @NonNull String sequenceId,
            @NonNull String caller,
            @NonNull Can<String> callees) {

        public void submit(final Consumer<SequenceDiagram> onSubmission) {
            XrayUi.updateModel(model->{
                model.lookupSequence(sequenceId())
                .ifPresent(sequence->onSubmission.accept(sequence.getData()));
            });
        }

    }

}
