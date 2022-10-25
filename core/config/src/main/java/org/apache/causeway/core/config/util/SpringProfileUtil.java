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
package org.apache.causeway.core.config.util;

import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SpringProfileUtil {

    private final String SPRING_PROFILES = "spring.profiles.active";

    public void addActiveProfile(final @Nullable String profile) {
        val profileIfAny = _Strings.blankToNullOrTrim(profile);
        System.setProperty(SPRING_PROFILES,
                stringify(currentActiveProfiles().add(profileIfAny)));
    }

    public void removeActiveProfile(final @Nullable String profile) {
        val profileIfAny = _Strings.blankToNullOrTrim(profile);
        System.setProperty(SPRING_PROFILES,
                stringify(currentActiveProfiles().remove(profileIfAny)));
    }

    // -- HELPER

    private Can<String> currentActiveProfiles() {
        return _Strings.splitThenStream(System.getProperty(SPRING_PROFILES), ",")
        .map(String::trim)
        .map(_Strings::emptyToNull)
        .collect(Can.toCan());
    }

    private String stringify(final Can<String> profiles) {
        return profiles.stream()
                .collect(Collectors.joining(","));
    }

}
