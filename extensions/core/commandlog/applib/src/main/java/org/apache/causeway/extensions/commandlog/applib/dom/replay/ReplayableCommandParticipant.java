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
import java.util.UUID;

import javax.inject.Named;

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
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.util.TitleBuffer;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

/**
 * Viewmodel row that represents a replay bookmark participant remapping.
 */
@DomainObject(introspection = Introspection.ANNOTATION_REQUIRED)
@DomainObjectLayout
@Named(ReplayableCommandParticipant.LOGICAL_TYPE_NAME)
public final class ReplayableCommandParticipant implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE
            + ".ReplayableCommandParticipant";

    public enum Role {
        TARGET,
        PARAMETER,
        RESULT
    }

    private final UUID owningInteractionId;
    private final Role role;
    private final String parameterName;
    private final Bookmark recordedBookmark;
    private final Bookmark actualBookmark;

    BookmarkService bookmarkService;
    ReplayContext replayContext;

    ReplayableCommandParticipant(
            final UUID owningInteractionId,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark,
            final BookmarkService bookmarkService,
            final ReplayContext replayContext) {
        this.owningInteractionId = owningInteractionId;
        this.role = role;
        this.parameterName = parameterName;
        this.recordedBookmark = recordedBookmark;
        this.actualBookmark = actualBookmark;
        this.bookmarkService = bookmarkService;
        this.replayContext = replayContext;
    }

    ReplayableCommandParticipant(
            final UUID owningInteractionId,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark,
            final BookmarkService bookmarkService) {
        this(owningInteractionId, role, parameterName, recordedBookmark, actualBookmark, bookmarkService, null);
    }

    ReplayableCommandParticipant(
            final UUID owningInteractionId,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark) {
        this(owningInteractionId, role, parameterName, recordedBookmark, actualBookmark, null);
    }

    public ReplayableCommandParticipant(
            final String memento,
            final BookmarkService bookmarkService,
            final ReplayContext replayContext) {
        final Memento parsedMemento = Memento.parse(memento);
        final Optional<ReplayableCommandParticipant> derivedParticipant = parsedMemento
                .participantFrom(replayContext);
        this.owningInteractionId = parsedMemento.owningInteractionId;
        this.role = parsedMemento.role;
        this.parameterName = parsedMemento.parameterName;
        this.recordedBookmark = derivedParticipant
                .map(ReplayableCommandParticipant::getRecordedBookmark)
                .orElse(null);
        this.actualBookmark = derivedParticipant
                .map(ReplayableCommandParticipant::getActualBookmark)
                .orElse(null);
        this.bookmarkService = bookmarkService;
        this.replayContext = replayContext;
    }

    @ObjectSupport public String title() {
        final TitleBuffer title = new TitleBuffer()
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
        switch (role) {
            case TARGET:
                return owningInteractionId + "--target";
            case PARAMETER:
                return owningInteractionId + "--parameter--" + (parameterName != null ? parameterName : "");
            case RESULT:
                return owningInteractionId + "--result";
            default:
                return "";
        }
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "0")
    public ReplayableCommand getReplayableCommand() {
        return owningInteractionId != null && replayContext != null
                ? new ReplayableCommand(owningInteractionId, replayContext)
                : null;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "0.1", hidden = Where.OBJECT_FORMS)
    public UUID getOwningInteractionId() {
        return owningInteractionId;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "1")
    public Role getRole() {
        return role;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "2")
    public String getParameterName() {
        return parameterName;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "3")
    public Bookmark getRecordedBookmark() {
        return recordedBookmark;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "4")
    public Object getTarget() {
        if (role != Role.TARGET) {
            return null;
        }
        return lookupActualObject().orElse(null);
    }

    @MemberSupport
    public boolean hideTarget() {
        return role != Role.TARGET;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "5")
    public Object getArgument() {
        if (role != Role.PARAMETER) {
            return null;
        }
        return lookupActualObject().orElse(null);
    }

    @MemberSupport
    public boolean hideArgument() {
        return role != Role.PARAMETER;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "6")
    public Object getResult() {
        if (role != Role.RESULT) {
            return null;
        }
        return lookupActualObject().orElse(null);
    }

    @MemberSupport
    public boolean hideResult() {
        return role != Role.RESULT;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "7")
    public Bookmark getActualBookmark() {
        return actualBookmark;
    }

    @Programmatic
    Optional<Object> lookupActualObject() {
        return bookmarkService != null && actualBookmark != null
                ? bookmarkService.lookup(actualBookmark)
                : Optional.empty();
    }

    private static final class Memento {

        private final UUID owningInteractionId;
        private final Role role;
        private final String parameterName;

        private Memento(
                final UUID owningInteractionId,
                final Role role,
                final String parameterName) {
            this.owningInteractionId = owningInteractionId;
            this.role = role;
            this.parameterName = parameterName;
        }

        static Memento parse(final String memento) {
            final String[] parts = memento.split("--", 3);
            final UUID owningInteractionId = parts.length > 0 && !parts[0].isEmpty()
                    ? UUID.fromString(parts[0])
                    : null;
            final Role role = parts.length > 1 && !parts[1].isEmpty()
                    ? Role.valueOf(parts[1].toUpperCase())
                    : null;
            final String parameterName = role == Role.PARAMETER && parts.length > 2 && !parts[2].isEmpty()
                    ? parts[2]
                    : null;
            return new Memento(owningInteractionId, role, parameterName);
        }

        Optional<ReplayableCommandParticipant> participantFrom(final ReplayContext replayContext) {
            if (owningInteractionId == null || role == null || replayContext == null) {
                return Optional.empty();
            }
            return new ReplayableCommand(owningInteractionId, replayContext)
                    .getParticipants()
                    .stream()
                    .filter(this::matches)
                    .findFirst();
        }

        private boolean matches(final ReplayableCommandParticipant participant) {
            if (participant.getRole() != role) {
                return false;
            }
            return role != Role.PARAMETER
                    || java.util.Objects.equals(participant.getParameterName(), parameterName);
        }
    }
}
