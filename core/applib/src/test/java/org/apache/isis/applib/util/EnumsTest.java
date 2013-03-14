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

package org.apache.isis.applib.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.applib.annotation.When;

public class EnumsTest {

    @Test
    public void getFriendlyNameOf() {
        assertThat(Enums.getFriendlyNameOf(When.ALWAYS), is("Always"));
        assertThat(Enums.getFriendlyNameOf(When.ONCE_PERSISTED), is("Once Persisted"));
    }

    @Test
    public void getEnumNameFromFriendly() {
        assertThat(Enums.getEnumNameFromFriendly("Always"), is("ALWAYS"));
        assertThat(Enums.getEnumNameFromFriendly("Once Persisted"), is("ONCE_PERSISTED"));
    }

}
