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

package org.apache.isis.applib.fixturescripts;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class ExecutionContextTest_roundUp {

    @Test
    public void happyCase() throws Exception {
        Assert.assertThat(FixtureScript.ExecutionContext.roundup(5, 20), is(20));
        Assert.assertThat(FixtureScript.ExecutionContext.roundup(19, 20), is(20));
        Assert.assertThat(FixtureScript.ExecutionContext.roundup(20, 20), is(40));
        Assert.assertThat(FixtureScript.ExecutionContext.roundup(21, 20), is(40));
        Assert.assertThat(FixtureScript.ExecutionContext.roundup(39, 20), is(40));
        Assert.assertThat(FixtureScript.ExecutionContext.roundup(40, 20), is(60));
    }

}
