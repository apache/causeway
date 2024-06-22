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
package org.apache.causeway.extensions.titlecache.jcache.dom;

import lombok.val;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.apache.causeway.extensions.titlecache.jcache.CausewayModuleExtTitlecacheJcache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;

@Service
@Named(TitleCacheSubscriber.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class TitleCacheSubscriber implements EntityTitleSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtTitlecacheJcache.NAMESPACE + ".TitleCacheSubscriber";

    private final SpecificationLoader specificationLoader;
    private final BookmarkService bookmarkService;
    private final CacheManager cacheManager;
    private final Cache titleCache;

    private final Map<String, Boolean> isEntityByLogicalTypeName = new ConcurrentHashMap<>();

    @Inject
    public TitleCacheSubscriber(
            final CacheManager cacheManager,
            final BookmarkService bookmarkService,
            final SpecificationLoader specificationLoader
            ) {
        this.bookmarkService = bookmarkService;
        this.specificationLoader = specificationLoader;
        this.cacheManager = cacheManager;
        titleCache = cacheManager.getCache(CausewayModuleExtTitlecacheJcache.TITLES_CACHE);
    }

    @Override
    public void entityTitleIs(Bookmark bookmark, String title) {
        Optional.ofNullable(bookmark)
                .filter(this::isEntity)
                .ifPresent(bkmark -> {
                    titleCache.put(bookmark, title);
                });
    }

    @EventListener(CausewayModuleApplib.TitleUiEvent.class)
    public void on(CausewayModuleApplib.TitleUiEvent<?> ev) {
        val domainObject = ev.getSource();
        if(domainObject == null) {
            return;
        }
        val bookmarkIfAny = bookmarkService.bookmarkFor(domainObject);
        bookmarkIfAny
                .filter(this::isEntity)
                .ifPresent(bookmark -> {
            Cache.ValueWrapper valueWrapper = titleCache.get(bookmark);
            Optional.ofNullable(valueWrapper)
                    .map(Cache.ValueWrapper::get)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .ifPresent(title -> {
                        ev.setTitle(title);
                    });
        });

    }

    private Boolean isEntity(Bookmark bookmark) {
        return isEntity(bookmark.getLogicalTypeName());
    }

    private Boolean isEntity(String logicalTypeName) {
        return isEntityByLogicalTypeName.computeIfAbsent(logicalTypeName, ltn -> {
            val objectSpec = specificationLoader.loadSpecification(ltn, IntrospectionState.NOT_INTROSPECTED);
            return Boolean.valueOf(objectSpec.isEntity());
        });
    }
}
