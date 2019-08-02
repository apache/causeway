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
package org.apache.isis.metamodel.facets.object.domainobject.auditing;

import org.junit.Assert;
import org.junit.Test;

import static org.apache.isis.metamodel.facets.object.domainobject.auditing.DefaultViewConfiguration.HIDDEN;
import static org.apache.isis.metamodel.facets.object.domainobject.auditing.DefaultViewConfiguration.TABLE;
import static org.apache.isis.metamodel.facets.object.domainobject.auditing.DefaultViewConfiguration.parseValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DefaultViewConfiguration_parseValue_Test {


    @Test
    public void when_hidden() throws Exception {
        Assert.assertThat(parseValue(null), is(equalTo(HIDDEN)));
        Assert.assertThat(parseValue(""), is(equalTo(HIDDEN)));
        Assert.assertThat(parseValue("hidden"), is(equalTo(HIDDEN)));
        Assert.assertThat(parseValue("garbage"), is(equalTo(HIDDEN)));
    }

    @Test
    public void when_table() throws Exception {
        Assert.assertThat(parseValue("table"), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("TABLE"), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("tAbLe"), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("  table  "), is(equalTo(TABLE)));
        Assert.assertThat(parseValue("  \ntable \n "), is(equalTo(TABLE)));
    }

}