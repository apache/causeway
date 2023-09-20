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
package org.apache.causeway.viewer.wicket.model.links;

import java.io.Serializable;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Can be added to drop-down menus. Either a link, a separator or a separator label.
 * @see Menuable.Kind
 */
public interface Menuable extends Serializable {

    @RequiredArgsConstructor
    public enum Kind {
        SECTION_SEPARATOR("list-separator"),
        SECTION_LABEL("list-section-label"),
        LINK("viewItem"),
        SUBMENU(null);
        @Getter private final String cssClassForLiElement;
        public boolean isSectionSeparator() { return this==SECTION_SEPARATOR;}
        public boolean isSectionLabel() { return this==SECTION_LABEL;}
        public boolean isLink() { return this==LINK;}
    }

    Kind menuableKind();

    // -- FACTORIES

    public static SectionSeparator sectionSeparator() {
        return new SectionSeparator();
    }
    public static SectionLabel sectionLabel(final @NonNull String sectionLabel) {
        return new SectionLabel(sectionLabel);
    }

    // -- IMPLEMENTATIONS

    @lombok.Value
    public static class SectionSeparator implements Menuable {
        private static final long serialVersionUID = 1L;
        @Override public Kind menuableKind() { return Menuable.Kind.SECTION_SEPARATOR; }
    }

    @lombok.Value
    public static class SectionLabel implements Menuable {
        private static final long serialVersionUID = 1L;
        final String sectionLabel;
        @Override public Kind menuableKind() { return Menuable.Kind.SECTION_LABEL; }
    }

}
