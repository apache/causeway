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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;
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

@AllArgsConstructor(onConstructor_ = {@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)})
@lombok.Value
public class FontAwesomeLayers implements Serializable {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static FontAwesomeLayers empty() {
        return new FontAwesomeLayers(IconType.SINGLE, null, null, null, null, null);
    }

    public static FontAwesomeLayers blank() {
        return singleIcon("fa-blank");
    }

    public static FontAwesomeLayers singleIcon(final String faClasses) {
        return new FontAwesomeLayers(IconType.SINGLE, null, null, null,
                List.of(new IconEntry(normalizeCssClasses(faClasses, "fa"), null)),
                null);
    }

    public static FontAwesomeLayers iconStack(
            final @Nullable String containerCssClasses,
            final @Nullable String containerCssStyle,
            final @NonNull IconEntry baseEntry,
            final @NonNull IconEntry overlayEntry,
            final IconEntry ...additionalOverlayEntries) {
        var iconEntries = Stream.concat(
                    Stream.of(baseEntry, overlayEntry),
                    Can.ofArray(additionalOverlayEntries).stream() // does not collect nulls
                )
                .collect(Collectors.toList());
        return new FontAwesomeLayers(
                IconType.STACKED,
                null,
                normalizeCssClasses(containerCssClasses, "fa-stack"),
                containerCssStyle,
                iconEntries,
                null);
    }

    public static FontAwesomeLayers fromJson(final @Nullable String json) {
        return FontAwesomeJsonParser.parse(json);
    }

    /**
     * Example:
     * <pre>
     * solid person-walking-arrow-right .my-color,
     * solid scale-balanced .my-color .bottom-right-overlay</pre>
     */
    public static FontAwesomeLayers fromQuickNotation(final @Nullable String quickNotation) {
        return FontAwesomeQuickNotationParser.parse(quickNotation);
    }

    // -- BUILDER

    @AllArgsConstructor
    public static class StackBuilder {
        private IconType iconType;
        @Setter @Accessors(fluent=true, chain = true) private CssClassFaPosition postition;
        @Setter @Accessors(fluent=true, chain = true) private String containerCssClasses;
        @Setter @Accessors(fluent=true, chain = true) private String containerCssStyle;
        private List<IconEntry> iconEntries;

        public FontAwesomeLayers build() {
            switch (iconType) {
            case STACKED:{
                return new FontAwesomeLayers(
                    IconType.STACKED,
                    null,
                    normalizeCssClasses(containerCssClasses, "fa-stack"),
                    containerCssStyle,
                    Can.ofCollection(iconEntries).toList(),
                    null);
            }
            default:
                throw _Exceptions.unmatchedCase(iconType);
            }
        }
        public StackBuilder addIconEntry(final @NonNull String cssClasses) {
            return addIconEntry(cssClasses, null);
        }
        public StackBuilder addIconEntry(final @NonNull String cssClasses, final @Nullable String cssStyle) {
            iconEntries.add(new IconEntry(normalizeCssClasses(cssClasses), cssStyle));
            return this;
        }
    }

    public static StackBuilder stackBuilder() {
        return new StackBuilder(IconType.STACKED, null,
                null, null, new ArrayList<FontAwesomeLayers.IconEntry>());
    }

    // -- UTILITIES

    public static String normalizeCssClasses(final String cssClasses, final String... mandatory) {
        var elements = _Strings.splitThenStream(cssClasses, " ")
            .map(String::trim)
            .filter(_Strings::isNotEmpty)
            .collect(Collectors.toCollection(TreeSet::new));
        _NullSafe.stream(mandatory)
            .forEach(elements::add);
        return elements.stream()
                //TODO[CAUSEWAY-3646] filter out malformed names (hardening)
                .collect(Collectors.joining(" "));
    }

    // --

    public enum IconType {
        SINGLE,
        STACKED,
        LAYERED
    }

    @lombok.Value
    public static class IconEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        @Nullable String cssClasses;
        @Nullable String cssStyle;
        public IconEntry(final String cssClasses, final String cssStyle) {
            this.cssClasses = normalizeCssClasses(cssClasses);
            this.cssStyle = cssStyle;
        }
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

    @lombok.Value
    public static class SpanEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        @Nullable String cssClasses;
        @Nullable String cssStyle;
        @Nullable String transform;
        @Nullable String text;
    }

    // -- CONSTRUCTION

    @NonNull IconType iconType;
    /**
     * Position of <i>Font Awesome</i> icon, relative to its accompanied title.
     */
    @With
    @Nullable CssClassFaPosition position;
    @Nullable String containerCssClasses;
    @Nullable String containerCssStyle;
    @Nullable List<IconEntry> iconEntries;
    @Nullable List<SpanEntry> spanEntries;

    public String toHtml() {
        var iconEntries = Can.ofCollection(getIconEntries());
        if(iconEntries.isEmpty()) {
            return "";
        }
        if(iconEntries.isCardinalityOne()) {
            // use simple rendering (not a stack nor layered)
            return faSpanHtml(iconEntries.getFirstElseFail().toHtml(), null, null);
        }
        var sb = new StringBuilder();
        iconEntries.forEach(iconEntry->sb.append(iconEntry.toHtml()));
        return faSpanHtml(sb.toString(), containerCssClasses, containerCssStyle);
    }

    public String toJson() {
        return JsonUtils.toStringUtf8(this, JsonUtils::indentedOutput);
    }

    /**
     * If this instance was not created from a quick-notation,
     * the result may loose style information.
     */
    public String toQuickNotation() {
        return FontAwesomeQuickNotationGenerator.generate(this);
    }

    // -- UTILITY

    //TODO[CAUSEWAY-3646] how to determine position when empty
    public FontAwesomeLayers emptyToBlank() {
        return _NullSafe.size(this.getIconEntries())>0
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
