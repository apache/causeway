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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import org.apache.causeway.commons.internal.base._Strings;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RoSpec {
    ARGS_SIMPLE("2.9.1", "section-a/chapter-02.html#_2-9-1-simple-arguments"),
    ARGS_PASSING("2.10", "section-a/chapter-02.html#_2-10-passing-arguments-to-resources"),
    CACHE_CONTROL("2.13", "section-a/chapter-02.html#_2-13-caching-cache-control-and-other-headers"),
    HOMEPAGE_GET("5.1", "section-b/chapter-05.html#_5_1_http_get"),
    HOMEPAGE_REPR("5.2", "section-b/chapter-05.html#_5_2_representation"),
    USER_GET("6.1", "section-b/chapter-06.html#_6-1-http-get"),
    USER_REPR("6.2", "section-b/chapter-06.html#_6_2_representation"),
    DOMAIN_SERVICES_GET("7.1", "section-b/chapter-07.html#_7-1-http-get"),
    DOMAIN_SERVICES_REPR("7.2", "section-b/chapter-07.html#_7_2_representation"),
    VERSION_GET("8.1", "section-b/chapter-08.html#_8_1_http_get"),
    VERSION_REPR("8.2", "section-b/chapter-08.html#_8_2_representation"),
    DOMAIN_OBJECT_GET("14.1", "section-c/chapter-14.html#_14_1_http_get"),
    DOMAIN_SERVICE_GET("15.1", "section-c/chapter-15.html#_15_1_http_get"),
    DOMAIN_SERVICE_GET_SUCCESS("15.1.2", "section-c/chapter-15.html#_15-1-2-success-response"),
    COLLECTION_GET("17.1", "section-c/chapter-17.html#_17_1_http_get"),
    ACTION_INVOKE_GET("19.1", "section-c/chapter-19.html#_19_1_http_get")
    ;

    private static String RO_SPEC_ROOT_URL = "https://www.restfulobjects.org/spec/1.0/";

    private final String section;
    private final String urlRelPath;

    public String onlineUrl() {
        return RO_SPEC_ROOT_URL + urlRelPath;
    }

    /**
     * Fully qualified section, prefixed with RO version.
     */
    public String fqSection() {
        //return String.format("RO Spec v1.0, section %s", section); // no link
        return String.format("RO Spec v1.0, section <a href=\"%s\" target=\"_blank\">%s</a>", onlineUrl(), section);
    }

    public String fqSection(final String suffix) {
        return fqSection() + (_Strings.isNotEmpty(suffix)
                ? (": " + suffix)
                : "");
    }

}
