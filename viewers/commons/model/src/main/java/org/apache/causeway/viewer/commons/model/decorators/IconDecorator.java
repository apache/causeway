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

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.CssClassFaFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @param <T> UI component type to decorate
 * @param <R> resulting UI component type
 */
@FunctionalInterface
public interface IconDecorator<T, R> {

    /**
     * corresponds to the webjars path as provided by the maven artifact
     * {@code org.webjars:font-awesome}
     */
    public static final String FONTAWESOME_RESOURCE = "font-awesome/6.4.2/css/all.min.css";

    R decorate(T uiComponent, Optional<FontAwesomeDecorationModel> decorationModel);

    // -- DECORATION MODEL

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class FontAwesomeDecorationModel implements Serializable {

        private static final long serialVersionUID = 1L;

        @Getter
        private final @NonNull FontAwesomeLayers fontAwesomeLayers;

        @Getter
        private final @NonNull CssClassFaPosition position;

        /**
         * @param forceAlignmentOnIconAbsence enforced in drop-dows,
         *      but not in horizontal action panels,
         *      where e.g. LinkAndLabel correspond to a UI button.
         */
        public static Optional<FontAwesomeDecorationModel> create(
                final @Nullable CssClassFaFactory cssClassFaFactoryIfAny,
                final boolean forceAlignmentOnIconAbsence) {

            return Optional.ofNullable(cssClassFaFactoryIfAny)
                .map(cssClassFaFactory->new FontAwesomeDecorationModel(
                        forceAlignmentOnIconAbsence
                            ? emptyToBlank(cssClassFaFactory.getLayers())
                            : cssClassFaFactory.getLayers(),
                        Optional.ofNullable(cssClassFaFactory.getPosition())
                            .orElse(CssClassFaPosition.LEFT)));

        }

        private static FontAwesomeLayers emptyToBlank(final FontAwesomeLayers faLayers) {
            return _NullSafe.size(faLayers.iconEntries())>0
                    ? faLayers
                    : FontAwesomeLayers.blank();
        }

    }

}
