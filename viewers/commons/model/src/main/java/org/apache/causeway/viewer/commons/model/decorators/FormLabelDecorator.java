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

import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@FunctionalInterface
public interface FormLabelDecorator<T> {

    void decorate(T uiComponent, FormLabelDecorationModel decorationModel);

    // -- DECORATION MODEL

    @Getter
    @RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    public static class FormLabelDecorationModel implements Serializable {

        private static final long serialVersionUID = 1L;

        public static FormLabelDecorationModel mandatory(final boolean mandatory) {
            return of(mandatory, ":");
        }

        public static FormLabelDecorationModel mandatorySuffixed(
                final boolean mandatory,
                final @Nullable String suffix) {
            return of(mandatory, suffix);
        }

        final boolean mandatoryMarker;
        final @Nullable String suffix;

    }

}
