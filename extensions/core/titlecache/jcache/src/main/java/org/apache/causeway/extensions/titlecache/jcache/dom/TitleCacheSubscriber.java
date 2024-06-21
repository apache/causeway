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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.services.title.TitleService;
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
public class TitleCacheSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtTitlecacheJcache.NAMESPACE + ".TitleCacheSubscriber";

    private final CacheManager cacheManager;
    private final Cache titleCache;

    @Inject
    public TitleCacheSubscriber(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        titleCache = cacheManager.getCache(CausewayModuleExtTitlecacheJcache.CACHE_NAME_TITLES);
    }


    @EventListener(CausewayModuleApplib.TitleUiEvent.class)
    public void on(CausewayModuleApplib.TitleUiEvent ev) {
        Object source = ev.getSource();
        Cache.ValueWrapper valueWrapper = titleCache.get(source);
    }

}
