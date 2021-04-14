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
package org.apache.isis.applib.services.i18n;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Controls whether the {@link TranslationService} is enabled or disabled, and
 * if enabled whether it is in write (learn) mode or read mode.
 *
 * @since 1.x {@index}
 */
public enum Mode {
    /**
     * Translations are disabled.
     */
    DISABLED(configValue ->
            "disable".equalsIgnoreCase(configValue) ||
            "disabled".equalsIgnoreCase(configValue)
    ),
    /**
     * Messages are being translated
     */
    READ(configValue ->
            "read".equalsIgnoreCase(configValue) ||
            "reader".equalsIgnoreCase(configValue)
    ),
    /**
     * Messages are <i>not</i> being translated, but they are being recorded
     * (such that a file of messages to be translated can be downloaded).
     */
    WRITE(configValue ->
            !(READ.matches(configValue) ||
              DISABLED.matches(configValue)));

    // -- handle values from configuration

    private final Predicate<String> matchesConfigValue;

    private Mode(Predicate<String> matchesConfigValue) {
        this.matchesConfigValue = Objects.requireNonNull(matchesConfigValue);
    }

    public boolean matches(String configValue) {
        return matchesConfigValue.test(configValue);
    }

    // -- for convenience

    public boolean isRead() {
        return this == READ;
    }

    public boolean isWrite() {
        return this == WRITE;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

}
