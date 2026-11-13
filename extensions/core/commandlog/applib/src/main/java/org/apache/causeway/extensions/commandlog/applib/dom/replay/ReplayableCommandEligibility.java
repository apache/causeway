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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;

import lombok.experimental.UtilityClass;

/**
 * Determines whether a command-log entry is useful in general replayable-command projections.
 *
 * @since 4.0 {@index}
 */
@UtilityClass
public class ReplayableCommandEligibility {

    public boolean isEligible(
            final @Nullable CommandLogEntry entry,
            final @Nullable ApplicationFeatureRepository applicationFeatureRepository) {
        if (entry == null) {
            return false;
        }
        return !isSafeAction(entry, applicationFeatureRepository)
                || entry.getResult() != null;
    }

    private boolean isSafeAction(
            final CommandLogEntry entry,
            final @Nullable ApplicationFeatureRepository applicationFeatureRepository) {
        if (applicationFeatureRepository == null
                || entry.getCommandDto() == null
                || !(entry.getCommandDto().getMember() instanceof ActionDto)
                || entry.getLogicalMemberIdentifier() == null) {
            return false;
        }
        try {
            return Optional.of(ApplicationFeatureId.newMember(entry.getLogicalMemberIdentifier()))
                    .map(applicationFeatureRepository::findFeature)
                    .flatMap(feature -> feature != null
                            ? feature.getActionSemantics()
                            : Optional.empty())
                    .map(semantics -> semantics.isSafeInNature())
                    .orElse(false);
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
