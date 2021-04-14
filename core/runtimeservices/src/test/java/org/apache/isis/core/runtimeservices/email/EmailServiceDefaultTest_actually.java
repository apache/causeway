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
package org.apache.isis.core.runtimeservices.email;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.commons.internal.collections._Lists;

class EmailServiceDefaultTest_actually {

    @Test
    void when_null() throws Exception {

        final String[] actually = EmailServiceDefault.originalUnlessOverridden(null, null);

        assertThat(actually, is(not(nullValue())));
        assertThat(actually.length, is(0));
    }

    @Test
    void when_not_null_but_no_override() throws Exception {

        final String[] actually = EmailServiceDefault.originalUnlessOverridden(_Lists.of("joey@tribiani.com", "rachel@green.com"), null);

        assertThat(actually, is(not(nullValue())));
        assertThat(actually.length, is(2));
    }

    @Test
    void when_not_null_but_with_override() throws Exception {

        final String[] actually = EmailServiceDefault.originalUnlessOverridden(_Lists.of("joey@tribiani.com", "rachel@green.com"), "ross@geller.com");

        assertThat(actually, is(not(nullValue())));
        assertThat(actually.length, is(1));
        assertThat(actually[0], is(equalTo("ross@geller.com")));
    }

}