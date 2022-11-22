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
package org.apache.causeway.core.config.metamodel.facets;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public final class CollectionLayoutConfigOptions {

    //@RequiredArgsConstructor XXX[ISIS-3287] don't use lombok here: hibernate-validation bug
    public enum TableDecoration {

        /**
         * If this option declares that the collection's table representation should not be decorated.
         */
        NONE(null),

        /**
         * If this option declares that the collection's table representation be decorated using
         * <a href="https://datatables.net>datatables.net</a> for client-side paging and filtering.
         */
        DATATABLES_NET("table-decoration");

        @Getter @Accessors(fluent = true)
        private final @Nullable String cssClass;

        public boolean isNone() { return this == NONE; }
        public boolean isDataTablesNet() { return this == DATATABLES_NET; }

        private TableDecoration(final String cssClass) {
            this.cssClass = cssClass;
        }
    }

    // -- FACTORIES

    public static TableDecoration tableDecoration(
            final @NonNull CausewayConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getCollectionLayout().getTableDecoration();
    }


    public enum DefaultView {
        HIDDEN(),
        TABLE();

        public String toNameLower() {
            return name().toLowerCase();
        }

        public static DefaultView from(final CausewayConfiguration configuration) {
            return configuration.getApplib().getAnnotation().getCollectionLayout().getDefaultView();
        }


    }
}
