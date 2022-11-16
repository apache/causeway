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
package org.apache.causeway.extensions.commandlog.applib.contributions;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

/**
 * This (abstract) mixin contributes a <tt>recentBackgroundCommands</tt> collection to any domain object.
 *
 * <p>
 *     To surface this collection, create a trivial subclass for the target domain class.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Collection
@RequiredArgsConstructor
public abstract class T_recentBackgroundCommands<T> {

    private final T domainObject;

    public static class ActionDomainEvent extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<T_recentBackgroundCommands> { }
    @Action(
            domainEvent = ActionDomainEvent.class
    )
    @MemberSupport public List<? extends CommandLogEntry> coll() {
        return bookmarkService.bookmarkFor(domainObject)
                .map(bookmark -> queryResultsCache.execute(
                        () -> commandLogEntryRepository.findRecentBackgroundByTarget(bookmark),
                                T_recentBackgroundCommands.class, "T_recentBackgroundCommands",
                                bookmark))
                .orElse(Collections.emptyList());
    }

    @Inject CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;
    @Inject BookmarkService bookmarkService;
    @Inject QueryResultsCache queryResultsCache;
}
