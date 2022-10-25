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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.base._NullSafe;

import lombok.val;

class SyncControl_Test {

    @Test
    public void defaults() throws Exception {

        // given
        val control = SyncControl.control();

        // then
        assertTrue(_NullSafe.isEmpty(control.getExecutionModes()));
    }

    @Test
    public void check_rules() throws Exception {
        // given
        val control = SyncControl.control();

        // when
        control.withCheckRules();

        // then
        assertTrue(_NullSafe.isEmpty(control.getExecutionModes()));
    }

    @Test
    public void skip_rules() throws Exception {

        // given
        val control = SyncControl.control();

        // when
        control.withSkipRules();

        // then
        assertTrue(control.getExecutionModes().contains(ExecutionMode.SKIP_RULE_VALIDATION));
    }

    @Test
    public void execute() throws Exception {

        // given
        val control = SyncControl.control();

        // when
        control.withExecute();

        // then
        assertTrue(_NullSafe.isEmpty(control.getExecutionModes()));
    }

    @Test
    public void no_execute() throws Exception {

        // given
        val control = SyncControl.control();

        // when
        control.withNoExecute();

        // then
        assertTrue(control.getExecutionModes().contains(ExecutionMode.SKIP_EXECUTION));
    }

    @Test
    public void chaining() throws Exception {

        ExceptionHandler exceptionHandler = ex -> null;

        val control = SyncControl.control()
                .withNoExecute()
                .withSkipRules()
                .with(exceptionHandler);

        assertTrue(control.getExecutionModes().size()==2);
        assertTrue(control.getExecutionModes().contains(ExecutionMode.SKIP_RULE_VALIDATION));
        assertTrue(control.getExecutionModes().contains(ExecutionMode.SKIP_EXECUTION));
    }

}