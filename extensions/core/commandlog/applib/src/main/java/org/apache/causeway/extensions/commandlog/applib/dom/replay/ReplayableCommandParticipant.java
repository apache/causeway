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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

/**
 * Derived view-model row describing one bookmark-bearing participant in a replayable command.
 *
 * @since 4.0 {@index}
 */
@Named(ReplayableCommandParticipant.LOGICAL_TYPE_NAME)
@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout
public final class ReplayableCommandParticipant
implements ViewModel, CommandRecordingSuppressed {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE
            + ".ReplayableCommandParticipant";

    public enum Role {
        TARGET,
        PARAMETER,
        RESULT
    }

    private final @Nullable UUID owningInteractionId;
    private final @Nullable Role role;
    private final @Nullable String parameterName;
    private final @Nullable Bookmark recordedBookmark;
    private final @Nullable Bookmark actualBookmark;
    private final ReplayContext replayContext;

    ReplayableCommandParticipant(
            final UUID owningInteractionId,
            final Role role,
            final @Nullable String parameterName,
            final Bookmark recordedBookmark,
            final @Nullable Bookmark actualBookmark,
            final ReplayContext replayContext) {
        this.owningInteractionId = owningInteractionId;
        this.role = role;
        this.parameterName = parameterName;
        this.recordedBookmark = recordedBookmark;
        this.actualBookmark = actualBookmark;
        this.replayContext = replayContext;
    }

    @Inject
    public ReplayableCommandParticipant(
            final String memento,
            final ReplayContext replayContext) {
        var parsed = Memento.parse(memento);
        var derived = parsed.participantFrom(replayContext);
        this.owningInteractionId = parsed.owningInteractionId;
        this.role = parsed.role;
        this.parameterName = parsed.parameterName;
        this.recordedBookmark = derived
                .map(ReplayableCommandParticipant::getRecordedBookmark)
                .orElse(null);
        this.actualBookmark = derived
                .map(ReplayableCommandParticipant::getActualBookmark)
                .orElse(null);
        this.replayContext = replayContext;
    }

    @ObjectSupport
    public String title() {
        var title = new TitleBuffer()
                .append("Replay participant")
                .append(" - ")
                .append(getRole());
        if (parameterName != null) {
            title.append(" ").append(parameterName);
        }
        return title
                .append(" ")
                .append(getRecordedBookmark())
                .append(" → ")
                .append(getActualBookmark())
                .toString();
    }

    @Override
    public String viewModelMemento() {
        if (owningInteractionId == null || role == null) {
            return "";
        }
        return switch (role) {
            case TARGET -> owningInteractionId + "--target";
            case PARAMETER -> owningInteractionId + "--parameter--"
                    + (parameterName != null ? parameterName : "");
            case RESULT -> owningInteractionId + "--result";
        };
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "0")
    public @Nullable ReplayableCommand getReplayableCommand() {
        return owningInteractionId != null
                ? new ReplayableCommand(owningInteractionId, replayContext)
                : null;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "0.1", hidden = Where.OBJECT_FORMS)
    public @Nullable UUID getOwningInteractionId() {
        return owningInteractionId;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "1")
    public @Nullable Role getRole() {
        return role;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "2")
    public @Nullable String getParameterName() {
        return parameterName;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "3")
    public @Nullable Bookmark getRecordedBookmark() {
        return recordedBookmark;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "4")
    public @Nullable Object getTarget() {
        return role == Role.TARGET ? lookupActualObject().orElse(null) : null;
    }

    @MemberSupport
    public boolean hideTarget() {
        return role != Role.TARGET;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "5")
    public @Nullable Object getArgument() {
        return role == Role.PARAMETER ? lookupActualObject().orElse(null) : null;
    }

    @MemberSupport
    public boolean hideArgument() {
        return role != Role.PARAMETER;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "6")
    public @Nullable Object getResult() {
        return role == Role.RESULT ? lookupActualObject().orElse(null) : null;
    }

    @MemberSupport
    public boolean hideResult() {
        return role != Role.RESULT;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "7")
    public @Nullable Bookmark getActualBookmark() {
        return actualBookmark;
    }

    @Programmatic
    Optional<Object> lookupActualObject() {
        return actualBookmark != null && replayContext.bookmarkService() != null
                ? replayContext.bookmarkService().lookup(actualBookmark)
                : Optional.empty();
    }

    private record Memento(
            @Nullable UUID owningInteractionId,
            @Nullable Role role,
            @Nullable String parameterName) {

        private static Memento parse(final String memento) {
            var parts = memento.split("--", 3);
            var interactionId = parts.length > 0 && !parts[0].isEmpty()
                    ? UUID.fromString(parts[0])
                    : null;
            var role = parts.length > 1 && !parts[1].isEmpty()
                    ? Role.valueOf(parts[1].toUpperCase())
                    : null;
            var parameterName = role == Role.PARAMETER && parts.length > 2 && !parts[2].isEmpty()
                    ? parts[2]
                    : null;
            return new Memento(interactionId, role, parameterName);
        }

        private Optional<ReplayableCommandParticipant> participantFrom(final ReplayContext replayContext) {
            if (owningInteractionId == null || role == null) {
                return Optional.empty();
            }
            return new ReplayableCommand(owningInteractionId, replayContext)
                    .getParticipants()
                    .stream()
                    .filter(this::matches)
                    .findFirst();
        }

        private boolean matches(final ReplayableCommandParticipant participant) {
            return participant.getRole() == role
                    && (role != Role.PARAMETER
                        || Objects.equals(participant.getParameterName(), parameterName));
        }
    }
}
