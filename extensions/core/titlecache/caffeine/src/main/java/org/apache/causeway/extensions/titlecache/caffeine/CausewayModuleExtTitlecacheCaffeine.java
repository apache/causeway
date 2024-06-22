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
package org.apache.causeway.extensions.titlecache.caffeine;

import java.util.concurrent.TimeUnit;

import org.apache.causeway.extensions.titlecache.caffeine.dom.TitleCacheSubscriber;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.titlecache.applib.CausewayModuleExtTitlecacheApplib;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@Import({
        // modules
        CausewayModuleExtTitlecacheApplib.class,

        // services
        TitleCacheSubscriber.class
})
@EnableCaching
public class CausewayModuleExtTitlecacheCaffeine extends CachingConfigurerSupport {

    public static final String NAMESPACE = "causeway.ext.titlecache.jcache";
    static final String CACHE_NAME_PREFIX = NAMESPACE + ".";

    @Bean
    @Override
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(100)
        );
        return cacheManager;
    }
}
