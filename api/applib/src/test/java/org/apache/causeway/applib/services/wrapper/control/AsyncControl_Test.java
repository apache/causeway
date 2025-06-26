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

import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.core.task.support.TaskExecutorAdapter;

import org.apache.causeway.applib.services.user.UserMemento;

class AsyncControl_Test {

    @Test
    public void defaults() throws Exception {

        // given
        var control = AsyncControl.defaults();

        // then
        Assertions.assertThat(control.syncControl().isSkipExecute()).isEqualTo(false);
        Assertions.assertThat(control.syncControl().isSkipRules()).isEqualTo(false);
    }

    @Test
    public void check_rules() throws Exception {
        // given
        var control = AsyncControl.defaults();

        // when
        control = control.withCheckRules();

        // then
        Assertions.assertThat(control.syncControl().isSkipExecute()).isEqualTo(false);
        Assertions.assertThat(control.syncControl().isSkipRules()).isEqualTo(false);
    }

    @Test
    public void skip_rules() throws Exception {

        // given
        var control = AsyncControl.defaults();

        // when
        control = control.withSkipRules();

        // then
        Assertions.assertThat(control.syncControl().isSkipExecute()).isEqualTo(false);
        Assertions.assertThat(control.syncControl().isSkipRules()).isEqualTo(true);
    }

    @Test
    public void user() throws Exception {

        // given
        var control = AsyncControl.defaults();

        // when
        control = control.withUser(UserMemento.ofName("fred"));

        // then
        Assertions.assertThat(control.user().name()).isEqualTo("fred");
    }

    @Test
    public void roles() throws Exception {

        // given
        var control = AsyncControl.defaults();

        // when
        control = control.withUser(UserMemento.ofNameAndRoleNames("fred", "role-1", "role-2"));

        // then
        Assertions.assertThat(control.user().streamRoleNames().collect(Collectors.toList()))
        .containsExactlyInAnyOrder("role-1", "role-2");
    }

    @Test
    public void chaining() throws Exception {

        var executorService = new ExecutorServiceAdapter(new TaskExecutorAdapter(command -> {}));
        var exceptionHandler = (ExceptionHandler) ex -> null;

        var control = AsyncControl.defaults()
                .withSkipRules()
                .withUser(UserMemento.ofNameAndRoleNames("fred", "role-1", "role-2"))
                .with(executorService)
                .withExceptionHandler(exceptionHandler);

        Assertions.assertThat(control.syncControl().isSkipExecute()).isEqualTo(false);
        Assertions.assertThat(control.syncControl().isSkipRules()).isEqualTo(true);
        Assertions.assertThat(control.executorService()).isSameAs(executorService);
        Assertions.assertThat(control.syncControl().exceptionHandler()).isSameAs(exceptionHandler);
    }

}