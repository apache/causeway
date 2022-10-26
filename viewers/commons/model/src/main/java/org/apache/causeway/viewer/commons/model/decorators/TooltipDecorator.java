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
package org.apache.causeway.viewer.commons.model.decorators;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;

import lombok.NonNull;
import lombok.Value;

@FunctionalInterface
public interface TooltipDecorator<T> {

    void decorate(T uiComponent, TooltipDecorationModel decorationModel);

    // -- DECORATION MODEL

    @Value(staticConstructor = "of")
    public static class TooltipDecorationModel implements Serializable {

        private static final long serialVersionUID = 1L;

        final @NonNull UiPlacementDirection placementDirection;
        final @NonNull Optional<String> title;
        final @NonNull String body;

        public static TooltipDecorationModel ofBody(
                final @NonNull UiPlacementDirection uiPlacementDirection,
                final @Nullable String body) {
            return of(uiPlacementDirection, Optional.empty(), _Strings.nullToEmpty(body));
        }

        public static TooltipDecorationModel ofTitleAndBody(
                final @NonNull UiPlacementDirection uiPlacementDirection,
                final @Nullable String title,
                final @Nullable String body) {
            return of(uiPlacementDirection, Optional.ofNullable(_Strings.emptyToNull(title)), _Strings.nullToEmpty(body));
        }

        public static TooltipDecorationModel empty() {
            return ofBody(UiPlacementDirection.BOTTOM, "");
        }

        public boolean isEmpty() {
            return body.isEmpty();
        }

    }

}
