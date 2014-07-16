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
package org.apache.isis.viewer.restfulobjects.tck;

import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.Rel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RelTest_matches {

    @Test
    public void whenDoes() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION), is(true));
    }

    @Test
    public void whenDoesNot() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION_PARAM), is(false));
    }

    @Test
    public void whenMatchesOnStr() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION.getName()), is(true));
    }

    @Test
    public void whenMatchesOnStrWithParams() throws Exception {
        assertThat(Rel.ACTION.matches(Rel.ACTION.andParam("foo", "bar")), is(true));
    }


}
