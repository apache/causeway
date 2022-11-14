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
package org.apache.causeway.viewer.restfulobjects.applib;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JsonRepresentationTest_newArray {

    @Test
    public void newArray() throws Exception {
        final JsonRepresentation jsonRepresentation = JsonRepresentation.newArray();
        assertThat(jsonRepresentation.isArray(), is(true));
        assertThat(jsonRepresentation.size(), is(0));
    }

    @Test
    public void newArrayInitialSize() throws Exception {
        final JsonRepresentation jsonRepresentation = JsonRepresentation.newArray(2);
        assertThat(jsonRepresentation.size(), is(2));
        assertThat(jsonRepresentation.arrayGet(0).isNull(), is(true));
        assertThat(jsonRepresentation.arrayGet(1).isNull(), is(true));
    }

}
