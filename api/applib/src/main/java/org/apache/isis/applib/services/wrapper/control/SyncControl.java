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

import java.util.EnumSet;

import org.apache.isis.core.commons.collections.ImmutableEnumSet;

/**
 */
// end::refguide[]
public class SyncControl extends ControlAbstract {

    public static SyncControl control() {
        return new SyncControl();
    }

    private SyncControl() {
        with(exception -> {
            throw exception;
        });
    }

    private boolean execute = true;
    public SyncControl withExecute() {
        execute = true;
        return this;
    }
    public SyncControl withNoExecute() {
        execute = false;
        return this;
    }

    /**
     * Not API.
     */
    public ImmutableEnumSet<ExecutionMode> getExecutionModes() {
        EnumSet<ExecutionMode> modes = EnumSet.copyOf(super.getExecutionModes().toEnumSet());
        if(!execute) {
            modes.add(ExecutionMode.SKIP_EXECUTION);
        }
        return ImmutableEnumSet.from(modes);
    }


}
// end::refguide[]
