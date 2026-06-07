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
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
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

    ReplayableCommandParticipant(
            final UUID owningInteractionId,
            final Role role,
            final String parameterName,
            final Bookmark recordedBookmark,
            final Bookmark actualBookmark) {
        this.owningInteractionId = owningInteractionId;
        this.role = role;
        this.parameterName = parameterName;
        this.recordedBookmark = recordedBookmark;
        this.actualBookmark = actualBookmark;
    }

    public ReplayableCommandParticipant(
            final String memento,
            final BookmarkService bookmarkService) {
        final String[] parts = memento.split("\\|", -1);
        this.owningInteractionId = parts.length > 0 && !parts[0].isEmpty()
                ? UUID.fromString(parts[0])
                : null;
        this.role = parts.length > 1 && !parts[1].isEmpty()
                ? Role.valueOf(parts[1])
                : null;
        this.parameterName = parts.length > 2 && !parts[2].isEmpty()
                ? parts[2]
                : null;
        this.recordedBookmark = parts.length > 3 && !parts[3].isEmpty()
                ? Bookmark.parseElseFail(parts[3])
                : null;
        this.actualBookmark = parts.length > 4 && !parts[4].isEmpty()
                ? Bookmark.parseElseFail(parts[4])
                : null;
        this.bookmarkService = bookmarkService;
    }

    @ObjectSupport public String title() {
        return new TitleBuffer()
                .append(getRole())
                .append(" ")
                .append(getRecordedBookmark())
                .append(" → ")
                .append(getActualBookmark())
                .toString();
    }

    @Override
    public String viewModelMemento() {
        return String.join("|",
                owningInteractionId != null ? owningInteractionId.toString() : "",
                role != null ? role.name() : "",
                parameterName != null ? parameterName : "",
                recordedBookmark != null ? recordedBookmark.stringify() : "",
                actualBookmark != null ? actualBookmark.stringify() : "");
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "1")
    public UUID getOwningInteractionId() {
        return owningInteractionId;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "2")
    public Role getRole() {
        return role;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "3")
    public String getParameterName() {
        return parameterName;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "4")
    public Object getTarget() {
        if (role != Role.TARGET && role != Role.PARAMETER) {
            return null;
        }
        return lookupActualObject().orElse(null);
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "5")
    public Object getResult() {
        if (role != Role.RESULT) {
            return null;
        }
        return lookupActualObject().orElse(null);
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(sequence = "6")
    public Bookmark getRecordedBookmark() {
        return recordedBookmark;
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
}
