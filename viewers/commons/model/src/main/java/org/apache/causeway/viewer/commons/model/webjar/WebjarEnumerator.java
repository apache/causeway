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

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.webjars.WebJarAssetLocator;
import org.webjars.WebJarAssetLocator.WebJarInfo;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

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

    // datatables/2.3.6
    // npm/bootstrap-select/1.14.0-beta3
    // npm/inputmask/5.0.9
    // jquery-ui/1.14.1
    public record WebjarResource(
        String path,
        String version) {

        static WebjarResource from(final WebJarInfo webJarInfo) {
            return new WebjarResource(webJarInfo.getArtifactId(), webJarInfo.getVersion());
        }
    }

    // -- HELPER

    private static Map<String, WebjarResource> scanClassPath() {
        var locator = new WebJarAssetLocator();
        return locator.getAllWebJars().values().stream()
            .map(WebjarResource::from)
            .collect(Collectors.toMap(WebjarResource::path, UnaryOperator.identity()));
    }

}
