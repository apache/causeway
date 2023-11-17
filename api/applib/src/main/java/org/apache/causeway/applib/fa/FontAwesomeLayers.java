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

import java.util.List;

import org.springframework.lang.Nullable;

import lombok.experimental.Accessors;

/**
 * Model for a single or multiple (layered) <i>Font Awesome</i> icon(s).
 *
 * @since 2.0 {@index}
 * @see <a href="https://fontawesome.com/docs/web/style/layer">Font Awesome Layers</a>
 */
@lombok.Value @Accessors(fluent=true) // record candidate
public class FontAwesomeLayers {

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

    // -- FACTORIES

    public static FontAwesomeLayers singleIcon(final String faClasses) {
        return new FontAwesomeLayers(null, null, List.of(new IconEntry(faClasses, null)), null);
    }

    //TODO[CAUSEWAY-3646] design more factories as we write the tests

}
