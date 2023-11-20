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

import lombok.experimental.Accessors;

/**
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

    public static FontAwesomeLayers singleIcon(final String faClasses) {
        return new FontAwesomeLayers(null, null, List.of(new IconEntry(normalizeFaClasses(faClasses), null)), null);
    }

    public static FontAwesomeLayers iconStack(final IconEntry baseEntry, final IconEntry overlayEntry) {
        return new FontAwesomeLayers(null, null, List.of(baseEntry, overlayEntry), null);
    }

    // -- UTILITIES

    public static String normalizeFaClasses(final String faClasses) {
        var elements = _Strings.splitThenStream(faClasses, " ")
                .map(String::trim)
                .filter(_Strings::isNotEmpty)
                .sorted((a, b)->a.compareTo(b))
                .collect(Collectors.toCollection(TreeSet::new));
        elements.add("fa"); // mandatory
        return elements.stream().collect(Collectors.joining(" "));
    }

    // --

    @lombok.Value @Accessors(fluent=true) // record candidate
    public static class IconEntry {
        @Nullable String cssClasses;
        @Nullable String cssStyle;
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
            return faIcon("fa-cube");
        }
        if(iconEntries.isCardinalityOne()) {
            // use simple rendering (not a stack nor layered)
            return faIcon(iconEntries.getFirstElseFail().cssClasses());
        }
        var sb = new StringBuilder();
        iconEntries.forEach(iconEntry->sb.append(faIcon(iconEntry.cssClasses())));
        return "<span class=\"fa-stack\">"
                + sb.toString()
                + "</span>";
    }

    // -- UTILITY

    //TODO[CAUSEWAY-3646] how to determine position when empty
    public FontAwesomeLayers emptyToBlank() {
        return _NullSafe.size(this.iconEntries())>0
                ? this
                : FontAwesomeLayers.blank();
    }

    // -- HELPER

    private String faIcon(final String faClasses) {
        return String.format("<i class=\"%s\"></i>", faClasses);
    }

}
