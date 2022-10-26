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
package org.apache.causeway.extensions.pdfjs.applib.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor
public enum Scale {
    /** predefined scaling strategies, depend on the width/height of the panel available to render in */
    AUTOMATIC("auto"),
    ACTUAL_SIZE("page-actual"),
    PAGE_FIT("page-fit"),
    PAGE_WIDTH("page-width"),
    /** 50% */
    _0_50("0.50"),
    /** 75% */
    _0_75("0.75"),
    /** 100% */
    _1_00("1.00"),
    /** 125% */
    _1_25("1.25"),
    /** 150% */
    _1_50("1.50"),
    /** 200% */
    _2_00("2.00"),
    /** 300% */
    _3_00("3.00"),
    /** 400% */
    _4_00("4.00"),;

    @Getter @JsonValue
    private final String value;

    public static Scale forValue(final String scaleValue) {
        if (scaleValue == null) {
            return null;
        }
        for (Scale scale : Scale.values()) {
            if (Objects.equals(scale.value, scaleValue)) {
                return scale;
            }
        }
        return null;
    }
}
