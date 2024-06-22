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
package org.apache.causeway.extensions.titlecache.jcache;

import lombok.val;

import java.util.Arrays;

import org.apache.causeway.extensions.titlecache.jcache.dom.TitleCacheSubscriber;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.titlecache.applib.CausewayModuleExtTitlecacheApplib;

@Configuration
@Import({
        // modules
        CausewayModuleExtTitlecacheApplib.class,

        // services
        TitleCacheSubscriber.class
})
@EnableCaching
public class CausewayModuleExtTitlecacheJcache {

    public static final String NAMESPACE = "causeway.ext.titlecache.jcache";
    static final String CACHE_NAME_PREFIX = NAMESPACE + ".";

    public static final String TITLES_CACHE = CACHE_NAME_PREFIX + "titles";

    @Bean
    public CacheManager cacheManager() {
        val cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache(TITLES_CACHE)));
        return cacheManager;
    }

//    @Bean
//    public JCacheManagerCustomizer cacheConfigurationCustomizer() {
//        return cm -> cm.createCache(CACHE_NAME_TITLES, cacheConfiguration());
//    }
//
//    /**
//     * Create a simple configuration that enable statistics via the JCache programmatic
//     * configuration API.
//     * <p>
//     * Within the configuration object that is provided by the JCache API standard, there
//     * is only a very limited set of configuration options. The really relevant
//     * configuration options (like the size limit) must be set via a configuration
//     * mechanism that is provided by the selected JCache implementation.
//     */
//    private javax.cache.configuration.Configuration<Object, Object> cacheConfiguration() {
//        return new MutableConfiguration<>().setStatisticsEnabled(true);
//    }
}
