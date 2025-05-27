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
package org.apache.causeway.extensions.titlecache.caffeine.dom;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.apache.causeway.extensions.titlecache.applib.event.Cached;
import org.apache.causeway.extensions.titlecache.applib.event.CachedWithCacheSettings;
import org.apache.causeway.extensions.titlecache.caffeine.CausewayModuleExtTitlecacheCaffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * Holds a cache for each entity type that indicates its title should be cached.
 *
 * <p>
 *     Uses the {@link EntityTitleSubscriber} to populate the cache, and listens on the
 *     {@link org.apache.causeway.applib.CausewayModuleApplib.TitleUiEvent} events to provide the cache values.
 * </p>
 *
 * @since 2.1 {@index}
 */
@Service
@Named(TitleCacheSubscriber.LOGICAL_TYPE_NAME)
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
public class TitleCacheSubscriber implements EntityTitleSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtTitlecacheCaffeine.NAMESPACE + ".TitleCacheSubscriber";

    private final SpecificationLoader specificationLoader;
    private final BookmarkService bookmarkService;
    private final CaffeineCacheManager cacheManager;
    private final CausewayConfiguration causewayConfiguration;

    private final Map<String, Boolean> isCachedByLogicalTypeName = new ConcurrentHashMap<>();
    private final Map<String, Cache> cacheByLogicalTypeName = new ConcurrentHashMap<>();

    @Inject
    public TitleCacheSubscriber(
            final CaffeineCacheManager cacheManager,
            final BookmarkService bookmarkService,
            final SpecificationLoader specificationLoader,
            final CausewayConfiguration causewayConfiguration
    ) {
        this.bookmarkService = bookmarkService;
        this.specificationLoader = specificationLoader;
        this.cacheManager = cacheManager;
        this.causewayConfiguration = causewayConfiguration;
    }

    /**
     * Implementation of {@link EntityTitleSubscriber}, listens to any computed titles and puts them into the relevant
     * cache.
     *
     * <p>
     *     Note that the cache is only lazily created when the first request to lookup an entry (in
     *     {@link #on(CausewayModuleApplib.TitleUiEvent)}) is called, so there will always be at least one cache miss
     *     per logical type name).  (An alternative design would have been to scan for all implementations of
     *     {@link CachedWithCacheSettings} during bootstrap).
     * </p>
     *
     * @param bookmark
     * @param title (untranslated)
     */
    @Override
    public void entityTitleIs(final Bookmark bookmark, final String title) {
        var cache = cacheByLogicalTypeName.get(bookmark.logicalTypeName());
        if(cache == null) {
            return;
        }
        cache.put(bookmark, title);
    }

    /**
     * Listens on {@link org.apache.causeway.applib.CausewayModuleApplib.TitleUiEvent}s, and obtains a previously
     * computed title for the bookmark, if any.
     *
     * <p>
     *     If there is not cache, then one is created.  If possible, the configuration of that cache is obtained from
     *     the event itself (if the event class implements
     *     {@link CachedWithCacheSettings}) otherwise using default
     *     settings obtained from {@link org.apache.causeway.core.config.CausewayConfiguration.Extensions.Titlecache}.
     * </p>
     *
     * @param ev
     */
    @EventListener(CausewayModuleApplib.TitleUiEvent.class)
    public void on(final CausewayModuleApplib.TitleUiEvent<?> ev) {
        var domainObject = ev.getSource();
        if(domainObject == null) {
            return;
        }
        var bookmarkIfAny = bookmarkService.bookmarkFor(domainObject);
        bookmarkIfAny
                .filter(bookmark -> isCached(bookmark, ev))
                .ifPresent(bookmark -> {
                    var cache = cacheByLogicalTypeName.computeIfAbsent(
                        bookmark.logicalTypeName(), ltn -> addCache(ev, ltn)
                    );
                    var valueWrapper = cache.get(bookmark);
                    setTitleOnEventFromCacheValue(valueWrapper, ev, bookmark);
                });
    }

    private Cache addCache(
            final CausewayModuleApplib.TitleUiEvent<?> ev,
            final String logicalTypeName) {
        var titlecacheConfig = causewayConfiguration.getExtensions().getTitlecache().getCaffeine();
        int expiryDurationInMinutes = titlecacheConfig.getExpiryDurationInMinutes();
        int maxSizeInEntries = titlecacheConfig.getMaxSizeInEntries();
        if(ev instanceof CachedWithCacheSettings) {
            var settings = (CachedWithCacheSettings) ev;
            expiryDurationInMinutes = settings.expiryDurationInMinutes();
            maxSizeInEntries = settings.maxSizeInEntries();
        }
        return addCache(logicalTypeName, expiryDurationInMinutes, maxSizeInEntries);
    }

    private void setTitleOnEventFromCacheValue(
            final Cache.ValueWrapper valueWrapper,
            final CausewayModuleApplib.TitleUiEvent<?> ev,
            final Bookmark bookmark) {
        Optional.ofNullable(valueWrapper)
                .map(Cache.ValueWrapper::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .ifPresent(title -> {
                    if(log.isDebugEnabled()) {
                        log.debug("Cache hit for title of {}", bookmark.stringify());
                    }
                    ev.setTitle(title);
                });
    }

    Cache addCache(
            final String cacheName,
            final int expiryDurationInMinutes,
            final int maxSizeInEntries) {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .expireAfterWrite(expiryDurationInMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSizeInEntries);
        cacheManager.registerCustomCache(cacheName, caffeineBuilder.build());
        return cacheManager.getCache(cacheName);
    }

    private Boolean isCached(final Bookmark bookmark, final CausewayModuleApplib.TitleUiEvent<?> ev) {
        return isCached(bookmark.logicalTypeName(), ev);
    }

    private Boolean isCached(
            final String logicalTypeName, final CausewayModuleApplib.TitleUiEvent<?> ev) {
        return isCachedByLogicalTypeName.computeIfAbsent(logicalTypeName, ltn -> {
            if (!(ev instanceof Cached)) return false;

            return specificationLoader
                .lookupLogicalType(logicalTypeName)
                .flatMap(specificationLoader::lookupBeanSort)
                .map(BeanSort::isEntity)
                .orElse(false);
        });
    }
}
