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

package org.apache.isis.viewer.wicket.model.models;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.viewer.wicket.model.models.PageParameterUtil.ParamNumAndOidString;

public class ActionModelTest {

    @Test
    public void whenParseThenParses() throws Exception {
        final ParamNumAndOidString parsed = PageParameterUtil.parseParamContext("3=OBJECT_OID:123")
                .orElseThrow(()->_Exceptions.unrecoverable("parsing failed"));
        
        assertThat(parsed, is(not(nullValue())));
        assertThat(parsed.getParamNum(), is(3));
        assertThat(parsed.getOidString(), is("OBJECT_OID:123"));
    }
}
