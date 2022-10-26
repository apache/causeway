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
package org.apache.causeway.commons.internal.base;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class _Strings_trimmed_Test {

    @Test
    public void fits() {
        assertThat(_Strings.trimmed("abcde", 5), is("abcde"));
    }

    @Test
    public void needs_to_be_trimmed() {
        assertThat(_Strings.trimmed("abcde", 4), is("a..."));
    }

    @Test
    public void when_null() {
        assertThat(_Strings.trimmed(null, 4), nullValue());
    }

    @Test
    public void when_empty() {
        assertThat(_Strings.trimmed("", 4), is(""));
    }

}
