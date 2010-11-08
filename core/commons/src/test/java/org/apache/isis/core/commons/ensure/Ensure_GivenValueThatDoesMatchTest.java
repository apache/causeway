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


package org.apache.isis.core.commons.ensure;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.apache.isis.core.commons.ensure.Ensure;


public class Ensure_GivenValueThatDoesMatchTest {

    @Test
    public void whenCallEnsureThatArgThenShouldReturnOriginalObject() {
        String object = "foo";
        String returnedObject = Ensure.ensureThatArg(object, is(not(nullValue(String.class))));
        assertThat(returnedObject, sameInstance(object));
    }

    @Test
    public void whenCallEnsureThatArgWithOverloadedShouldReturnOriginalObject() {
        String object = "foo";
        String returnedObject = Ensure.ensureThatArg(object, is(not(nullValue(String.class))), "some message");
        assertThat(returnedObject, sameInstance(object));
    }

    @Test
    public void whenCallEnsureThatStateThenShouldReturnOriginalObject() {
        String object = "foo";
        String returnedObject = Ensure.ensureThatState(object, is(not(nullValue(String.class))));
        assertThat(returnedObject, sameInstance(object));
    }

    @Test
    public void whenCallEnsureThatStateWithOverloadedShouldReturnOriginalObject() {
        String object = "foo";
        String returnedObject = Ensure.ensureThatState(object, is(not(nullValue(String.class))), "some message");
        assertThat(returnedObject, sameInstance(object));
    }

    @Test
    public void whenCallEnsureThatContextThenShouldReturnOriginalObject() {
        String object = "foo";
        String returnedObject = Ensure.ensureThatContext(object, is(not(nullValue(String.class))));
        assertThat(returnedObject, sameInstance(object));
    }

    @Test
    public void whenCallEnsureThatContextWithOverloadedShouldReturnOriginalObject() {
        String object = "foo";
        String returnedObject = Ensure.ensureThatContext(object, is(not(nullValue(String.class))), "some message");
        assertThat(returnedObject, sameInstance(object));
    }


}
