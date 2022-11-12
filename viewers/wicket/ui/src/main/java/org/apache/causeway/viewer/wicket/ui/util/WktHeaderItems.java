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
package org.apache.causeway.viewer.wicket.ui.util;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WktHeaderItems {

    // -- FACTORIES

    /**
     * Reading JS from source, it (hardcoded) skips 18 license header lines
     * and any single line comments as well as empty lines.
     * @apiNote that could be done by the yui-compressor maven plugin as well,
     *      but at the time of writing did not look into it
     */
    public OnDomReadyHeaderItem forScriptReferenceAsOnDomReady(
            final @NonNull Class<?> resourceLocation,
            final @NonNull String resourceName) {
        return OnDomReadyHeaderItem.forScript(readJsResource(resourceLocation, resourceName));
    }

    public JavaScriptReferenceHeaderItem forScriptReference(
            final @NonNull Class<?> resourceLocation,
            final @NonNull String resourceName) {
        return JavaScriptReferenceHeaderItem.forReference(
                new JavaScriptResourceReference(resourceLocation, resourceName));
    }

    @RequiredArgsConstructor
    public class HeaderContributor
    implements IHeaderContributor {
        private static final long serialVersionUID = 1L;
        @Getter private final HeaderItem headerItem;
        @Override public void renderHead(final IHeaderResponse response) {
            response.render(headerItem);
        }
    }

    // -- HELPER

    /** skips 18 license header lines and any single line comments as well as empty lines */
    @SneakyThrows
    private String readJsResource(
            final @NonNull Class<?> resourceLocation,
            final @NonNull String resourceName) {
        return _Text.readLinesFromResource(
                resourceLocation, resourceName, StandardCharsets.UTF_8)
                .filter(_Strings::isNotEmpty)
                .filter(line->!line.trim().startsWith("//"))
                .stream()
                .skip(18) // skip license header
                .collect(Collectors.joining("\n"));
    }

}
