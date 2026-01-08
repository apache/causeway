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
package org.apache.causeway.viewer.commons.model.webjar;

import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.TextUtils;

import lombok.SneakyThrows;

/**
 * Utility to scan webjar version strings dynamically from class-path under {@code META-INF/resources/webjars}.
 *
 * <p>Removes the need to hard code version strings for webjar resource lookups.
 *
 * @since 3.6.0, 4.0
 */
public record WebjarEnumerator() {

    private final static _Lazy<Map<String, WebjarResource>> WEBJARS = _Lazy.threadSafe(WebjarEnumerator::scanClassPath);

    public static Optional<WebjarResource> lookup(final String path) {
        return Optional.ofNullable(WEBJARS.get().get(path));
    }

    public static WebjarResource lookupElseFail(final String path) {
        return lookup(path).orElseThrow(()->_Exceptions
                .noSuchElement("no webjar found on class-path under META-INF/resources/webjars matching sub-path '%s'", path));
    }

    public record WebjarResource(
        String path,
        String version) {

        static Optional<WebjarResource> parseFromURL(final String url) {
            if(url==null
                    || !url.startsWith("jar:file:")
                    || !url.contains("/org/webjars/")
                    || !url.contains(".jar!"))
                return Optional.empty();
            // datatables/2.3.6
            // npm/bootstrap-select/1.14.0-beta3
            // npm/inputmask/5.0.9
            // jquery-ui/1.14.1
            var pathAndVersion = TextUtils.cutter(url)
                .keepAfter("/org/webjars/")
                .keepBefore(".jar!")
                .keepBeforeLast("/");
            return Optional.of(new WebjarResource(
                pathAndVersion.keepBeforeLast("/").getValue(),
                pathAndVersion.keepAfterLast("/").getValue()));
        }
    }

    // -- HELPER

    private static Map<String, WebjarResource> scanClassPath() {
        return webjarURLs().stream()
            .map(WebjarResource::parseFromURL)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toMap(WebjarResource::path, UnaryOperator.identity()));
    }

    @SneakyThrows
    private static Set<String> webjarURLs() {
        Set<String> webjarURLs = new TreeSet<>();
        Enumeration<URL> resources = WebjarEnumerator.class.getClassLoader()
                .getResources("META-INF/resources/webjars");

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            webjarURLs.add(resource.toString());
        }
        return webjarURLs;
    }

}
