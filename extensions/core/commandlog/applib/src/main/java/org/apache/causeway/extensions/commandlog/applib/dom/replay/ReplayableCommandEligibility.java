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

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;

import lombok.experimental.UtilityClass;

@UtilityClass
class ReplayableCommandEligibility {

    boolean isReplayable(
            final CommandLogEntry entry,
            final SpecificationLoader specificationLoader) {
        if (entry == null) {
            return false;
        }
        if (!isSafeAction(entry, specificationLoader)) {
            return true;
        }
        return entry.getResult() != null;
    }

    private boolean isSafeAction(
            final CommandLogEntry entry,
            final SpecificationLoader specificationLoader) {
        if (specificationLoader == null
                || entry.getCommandDto() == null
                || !(entry.getCommandDto().getMember() instanceof ActionDto)) {
            return false;
        }
        return Optional.ofNullable(entry.getLogicalMemberIdentifier())
                .flatMap(logicalMemberIdentifier -> safeActionSemantics(specificationLoader, logicalMemberIdentifier))
                .orElse(false);
    }

    private Optional<Boolean> safeActionSemantics(
            final SpecificationLoader specificationLoader,
            final String logicalMemberIdentifier) {
        try {
            final var identifier = IdentifierUtil.memberIdentifierFor(
                    specificationLoader,
                    Identifier.Type.ACTION,
                    logicalMemberIdentifier);
            return specificationLoader.loadFeature(identifier)
                    .filter(ObjectAction.class::isInstance)
                    .map(ObjectAction.class::cast)
                    .map(ObjectAction::getSemantics)
                    .map(SemanticsOf::isSafeInNature);
        } catch (final RuntimeException ex) {
            return Optional.empty();
        }
    }
}
