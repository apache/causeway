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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_openTarget.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "0.3", associateWith = "target",
        describedAs = "Opens the recorded or actual target"
)
@RequiredArgsConstructor
public class ReplayableCommand_openTarget {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_openTarget> {
    }

    public enum TargetType {
        RECORDED,
        ACTUAL
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Object act(final TargetType targetType) {
        final var bookmarkStr = targetType == TargetType.RECORDED
                ? replayableCommand.getTarget()
                : replayableCommand.getActualTarget();
        return domainObject(targetType, bookmarkStr);
    }

    @MemberSupport
    public String disableAct() {
        return choicesTargetType().isEmpty() ? "No targets found." : null;
    }

    @MemberSupport
    public List<TargetType> choicesTargetType() {
        var choices = new ArrayList<TargetType>();
        if (replayableCommand.getTarget() != null) {
            choices.add(TargetType.RECORDED);
        }
        if (replayableCommand.getActualTarget() != null) {
            choices.add(TargetType.ACTUAL);
        }
        return choices;
    }

    @MemberSupport
    public TargetType defaultTargetType() {
        return choicesTargetType().stream().findFirst().orElse(TargetType.RECORDED);
    }

    private Object domainObject(final TargetType targetType, final String bookmarkStr) {
        final var domainObjectIfAny = Optional.ofNullable(bookmarkStr)
                .flatMap(Bookmark::parse)
                .flatMap(bookmark -> bookmarkService.lookup(bookmark));
        if (domainObjectIfAny.isPresent()) {
            return domainObjectIfAny.get();
        }
        messageService.informUser(String.format(
                "Unable to open %s target '%s'", targetType.name().toLowerCase(), bookmarkStr));
        return null;
    }

    @Inject private BookmarkService bookmarkService;
    @Inject private MessageService messageService;

}
