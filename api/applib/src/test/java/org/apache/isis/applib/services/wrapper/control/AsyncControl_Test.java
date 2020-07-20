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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.task.support.TaskExecutorAdapter;

import lombok.val;

public class AsyncControl_Test {

    @Test
    public void defaults() throws Exception {

        // given
        val control = AsyncControl.control();

        // then
        Assertions.assertThat(control.getExecutionModes()).isEmpty();
    }

    @Test
    public void check_rules() throws Exception {
        // given
        val control = AsyncControl.control();

        // when
        control.withCheckRules();

        // then
        Assertions.assertThat(control.getExecutionModes()).isEmpty();
    }

    @Test
    public void skip_rules() throws Exception {

        // given
        val control = AsyncControl.control();

        // when
        control.withSkipRules();

        // then
        Assertions.assertThat(control.getExecutionModes()).contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

    @Test
    public void user() throws Exception {

        // given
        val control = AsyncControl.control();

        // when
        control.withUser("fred");

        // then
        Assertions.assertThat(control.getUser()).isEqualTo("fred");
    }

    @Test
    public void roles() throws Exception {

        // given
        val control = AsyncControl.control();

        // when
        control.withRoles("role-1", "role-2");

        // then
        Assertions.assertThat(control.getRoles()).containsExactlyInAnyOrder("role-1", "role-2");
    }

    @Test
    public void chaining() throws Exception {

        val executorService = new ExecutorServiceAdapter(new TaskExecutorAdapter(new Executor() {
            @Override
            public void execute(Runnable command) {
            }
        }));
        ExceptionHandler exceptionHandler = ex -> null;

        val control = AsyncControl.control(String.class)
                .withSkipRules()
                .withUser("fred")
                .withRoles("role-1", "role-2")
                .with(executorService)
                .with(exceptionHandler);

        Assertions.assertThat(control.getExecutionModes())
                .containsExactlyInAnyOrder(ExecutionMode.SKIP_RULE_VALIDATION);
        Assertions.assertThat(control.getExecutorService())
                .isSameAs(executorService);
        Assertions.assertThat(control.getExceptionHandler().orElse(null))
                .isSameAs(exceptionHandler);
    }

}