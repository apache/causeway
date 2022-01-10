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
package org.apache.isis.extensions.commandlog.jdo.mixins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.extensions.commandlog.jdo.IsisModuleExtCommandLogJdo;
import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdo;
import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdoRepository;

@Collection(
    domainEvent = T_recent.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table"
)
public abstract class T_recent<T> {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogJdo.CollectionDomainEvent<T_recent, CommandJdo> { }

    private final T domainObject;
    public T_recent(final T domainObject) {
        this.domainObject = domainObject;
    }

    public List<CommandJdo> coll() {
        return findRecent();
    }

    private List<CommandJdo> findRecent() {
        return bookmarkService.bookmarkFor(domainObject)
        .map(bookmark->queryResultsCache.execute(
                () -> commandJdoRepository.findRecentByTarget(bookmark)
                , T_recent.class
                , "findRecentByTarget"
                , domainObject))
        .orElse(Collections.emptyList());
    }

    @Inject CommandJdoRepository commandJdoRepository;
    @Inject BookmarkService bookmarkService;
    @Inject QueryResultsCache queryResultsCache;

}
