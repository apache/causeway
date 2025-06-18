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
package org.apache.causeway.applib.services.wrapper.control;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Casts;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @since 2.0 {@index}
 */
public class ControlAbstract<T extends ControlAbstract<T>> {

    protected ControlAbstract() {
    }

    /**
     * Set by framework; simply used for logging purposes.
     */
    @Getter(AccessLevel.PACKAGE) @Setter
    private Method method;

    /**
     * Set by framework; simply used for logging purposes.
     */
    @Getter(AccessLevel.PACKAGE) @Setter
    private Bookmark bookmark;

    @Getter
    private boolean checkRules = true;
    public T withCheckRules() {
        checkRules = true;
        return _Casts.uncheckedCast(this);
    }
    public T withSkipRules() {
        checkRules = false;
        return _Casts.uncheckedCast(this);
    }

    private ExceptionHandler exceptionHandler;

    public Optional<ExceptionHandler> getExceptionHandler() {
        return Optional.ofNullable(exceptionHandler);
    }

    public T with(final ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return _Casts.uncheckedCast(this);
    }

    /**
     * Not API.
     */
    public ImmutableEnumSet<ExecutionMode> getExecutionModes() {
        EnumSet<ExecutionMode> modes = EnumSet.noneOf(ExecutionMode.class);
        if(!checkRules) {
            modes.add(ExecutionMode.SKIP_RULE_VALIDATION);
        }
        return ImmutableEnumSet.from(modes);
    }

}
