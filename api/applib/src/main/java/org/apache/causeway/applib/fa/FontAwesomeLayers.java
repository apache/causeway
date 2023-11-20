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
package org.apache.causeway.applib.fa;

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * <h1>EXPERIMENTAL FEATURE WARNING</h1>
 * This class is still subject to changes without warning or notice!
 * <p>
 * Model for a single or multiple (layered) <i>Font Awesome</i> icon(s).
 *
 * @since 2.0 {@index}
 * @see <a href="https://fontawesome.com/docs/web/style/layer">Font Awesome Layers</a>
 */
@lombok.Value @Accessors(fluent=true) // record candidate
public class FontAwesomeLayers implements Serializable {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static FontAwesomeLayers empty() {
        return new FontAwesomeLayers(null, null, null, null);
    }

    public static FontAwesomeLayers blank() {
        return singleIcon("fa-blank");
    }

    public static FontAwesomeLayers fallback() {
        return singleIcon("fa-cube");
    }

    public static FontAwesomeLayers singleIcon(final String faClasses) {
        return new FontAwesomeLayers(null, null, List.of(new IconEntry(normalizeCssClasses(faClasses, "fa"), null)), null);
    }

    public static FontAwesomeLayers iconStack(
            final @Nullable String containerCssClasses,
            final @Nullable String containerCssStyle,
            final @NonNull IconEntry baseEntry,
            final @NonNull IconEntry overlayEntry) {
        return new FontAwesomeLayers(
                normalizeCssClasses(containerCssClasses, "fa-stack"),
                containerCssStyle,
                List.of(baseEntry, overlayEntry), null);
    }

    // -- UTILITIES

    public static String normalizeCssClasses(final String cssClasses, final String... mandatory) {
        var elements = _Strings.splitThenStream(cssClasses, " ")
            .map(String::trim)
            .filter(_Strings::isNotEmpty)
            .collect(Collectors.toCollection(TreeSet::new));
        _NullSafe.stream(mandatory)
            .forEach(elements::add);
        return elements.stream().collect(Collectors.joining(" "));
    }

    // --

    @lombok.Value @Accessors(fluent=true) // record candidate
    public static class IconEntry {
        @Nullable String cssClasses;
        @Nullable String cssStyle;
        public String toHtml() {
            return faIconHtml(cssClasses, cssStyle);
        }
        // -- HELPER
        private static String faIconHtml(final @Nullable String faClasses) {
            if(_Strings.isEmpty(faClasses)) return "";
            return String.format("<i class=\"%s\"></i>", faClasses);
        }
        private static String faIconHtml(final @Nullable String faClasses, final @Nullable String faStyle) {
            if(_Strings.isEmpty(faStyle)) return faIconHtml(faClasses);
            return String.format("<i class=\"%s\" style=\"%s\"></i>", faClasses, faStyle);
        }
    }

    @lombok.Value @Accessors(fluent=true) // record candidate
    public static class SpanEntry {
        @Nullable String cssClasses;
        @Nullable String cssStyle;
        @Nullable String transform;
        @Nullable String text;
    }

    @Nullable String containerCssClasses;
    @Nullable String containerCssStyle;
    @Nullable List<IconEntry> iconEntries;
    @Nullable List<SpanEntry> spanEntries;

    /**
     * Position of <i>Font Awesome</i> icon, relative to its accompanied title.
     */
    @Nullable CssClassFaPosition postition = CssClassFaPosition.LEFT;

    public String toHtml() {
        var iconEntries = Can.ofCollection(iconEntries());
        if(iconEntries.isEmpty()) {
            // fallback to cube icon
            return fallback().toHtml();
        }
        if(iconEntries.isCardinalityOne()) {
            // use simple rendering (not a stack nor layered)
            return faSpanHtml(iconEntries.getFirstElseFail().toHtml(), null, null);
        }
        var sb = new StringBuilder();
        iconEntries.forEach(iconEntry->sb.append(iconEntry.toHtml()));
        return faSpanHtml(sb.toString(), containerCssClasses, containerCssStyle);
    }

    // -- UTILITY

    //TODO[CAUSEWAY-3646] how to determine position when empty
    public FontAwesomeLayers emptyToBlank() {
        return _NullSafe.size(this.iconEntries())>0
                ? this
                : FontAwesomeLayers.blank();
    }

    // -- HELPER

    private static String faSpanHtml(
            final @Nullable String innerHtml,
            final @Nullable String cssClasses,
            final @Nullable String cssStyle) {
        if(_Strings.isEmpty(innerHtml)) return "";
        var attrClass = _Strings.nonEmpty(cssClasses)
                .map(s->String.format(" class=\"%s\"", s))
                .orElse("");
        var attrStyle = _Strings.nonEmpty(cssStyle)
                .map(s->String.format(" style=\"%s\"", s))
                .orElse("");
        return String.format("<span%s%s>%s</span>", attrClass, attrStyle, innerHtml);
    }
}
