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

package org.apache.isis.applib.services.interaction;

import java.util.UUID;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.iactn.Interaction;

import lombok.val;

public class InteractionDefaultTest_next {

    @Test
    public void test() {
        val interaction = new Interaction(UUID.randomUUID());

        assertThat(interaction.next("foo"), is(0));
        assertThat(interaction.next("foo"), is(1));
        assertThat(interaction.next("bar"), is(0));
        assertThat(interaction.next("bar"), is(1));
        assertThat(interaction.next("foo"), is(2));
        assertThat(interaction.next("bar"), is(2));
        assertThat(interaction.next("bar"), is(3));
    }

}
