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
package org.apache.causeway.viewer.commons.prism;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

public record PrismLanguage(String languageId) {
    
    public String jsFile() {
        return "prism/components/prism-" + languageId + ".min.js";
    }

    /**
     * eg. {@code class='language-ruby'} results in {@code languageId=ruby}
     */
    public static Optional<PrismLanguage> parseFromCssClass(final @Nullable String cssClass) {
        if(!StringUtils.hasLength(cssClass)) return Optional.empty();
        int start = cssClass.indexOf("language-");
        if(start==-1) return Optional.empty();
        var languageKey = cssClass.substring(start+9);
        int w = languageKey.indexOf(" ");
        if(w>-1) languageKey = languageKey.substring(0, w);
        return Optional.of(new PrismLanguage(languageKey));
    }
    
    @Deprecated
    public static List<PrismLanguage> mostCommon() {
        //XXX order matters, eg. JAVADOCLIKE must come before JAVADOC
        //XXX future extensions might want to make that a config option
        return List.of(
            "markup",
            "css",
            "clike",
            "java",
            "javascript",
            "asciidoc",
            "javadoclike",
            "javadoc",
            "json",
            "properties",
            "xml-doc",
            "yaml")
            .stream()
            .map(PrismLanguage::new)
            .toList();
    }

}
