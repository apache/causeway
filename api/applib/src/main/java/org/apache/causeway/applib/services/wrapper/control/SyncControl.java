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

import java.util.EnumSet;

import org.apache.causeway.commons.collections.ImmutableEnumSet;

/**
 * Controls the way that a (synchronous) wrapper works.
 *
 * @since 2.0 {@index}
 */
public class SyncControl extends ControlAbstract<SyncControl> {

    public static SyncControl control() {
        return new SyncControl();
    }

    private SyncControl() {
        with(exception -> {
            throw exception;
        });
    }

    /**
     * Skip checking business rules (hide/disable/validate) before
     * executing the underlying property or action
     */
    @Override
    public SyncControl withSkipRules() {
        return super.withSkipRules();
    }

    /**
     * How to handle exceptions if they occur, using the provided
     * {@link ExceptionHandler}.
     *
     * <p>
     *     The default behaviour is to rethrow the exception.
     * </p>
     */
    @Override
    public SyncControl with(final ExceptionHandler exceptionHandler) {
        return super.with(exceptionHandler);
    }

    private boolean execute = true;

    /**
     * Explicitly set the action to be executed.
     */
    public SyncControl withExecute() {
        execute = true;
        return this;
    }

    /**
     * Explicitly set the action to <i>not</i >be executed, in other words a
     * &quot;dry run&quot;.
     */
    public SyncControl withNoExecute() {
        execute = false;
        return this;
    }

    /**
     * Not API.
     */
    @Override
    public ImmutableEnumSet<ExecutionMode> getExecutionModes() {
        EnumSet<ExecutionMode> modes = EnumSet.copyOf(super.getExecutionModes().toEnumSet());
        if(!execute) {
            modes.add(ExecutionMode.SKIP_EXECUTION);
        }
        return ImmutableEnumSet.from(modes);
    }

}
