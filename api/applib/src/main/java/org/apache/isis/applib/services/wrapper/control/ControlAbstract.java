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
package org.apache.isis.applib.services.wrapper.control;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.commons.internal.base._Casts;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

// tag::refguide[]
public class ControlAbstract<T extends ControlAbstract<T>> {

    // end::refguide[]
    protected ControlAbstract() {
    }

    /**
     * Set by framework.
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private Method method;

    /**
     * Set by framework.
     */
    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private Bookmark bookmark;

    // tag::refguide[]
    private boolean checkRules = true;                          // <.>
    public T withCheckRules() {
        // end::refguide[]
        checkRules = true;
        return _Casts.uncheckedCast(this);
        // tag::refguide[]
        // ...
    }
    public T withSkipRules() {
        // end::refguide[]
        checkRules = false;
        return _Casts.uncheckedCast(this);
        // tag::refguide[]
        // ...
    }
    
    private ExceptionHandler exceptionHandler;
   
    public Optional<ExceptionHandler> getExceptionHandler() { // <.>
        return Optional.ofNullable(exceptionHandler);
    }
                      
    public T with(ExceptionHandler exceptionHandler) {
        // end::refguide[]
        this.exceptionHandler = exceptionHandler;
        return _Casts.uncheckedCast(this);
        // tag::refguide[]
        // ...
    }

    // end::refguide[]
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

    // tag::refguide[]
}
// end::refguide[]
