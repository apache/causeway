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
package org.apache.causeway.applib.services.iactnlayer;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.val;

class InteractionContext_combine_Test {

    @Test
    void happy_case() {

        val mappers = Stream.<UnaryOperator<String>>of(
                s -> s + "bar",
                s -> "[" + s + "]",
                s -> s + s);
        val result = InteractionContext.combine(mappers).apply("foo");

        Assertions.assertThat(result).isEqualTo("[foobar][foobar]");
    }

}
