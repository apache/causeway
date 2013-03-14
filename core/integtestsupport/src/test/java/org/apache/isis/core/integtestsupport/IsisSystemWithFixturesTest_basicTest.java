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

package org.apache.isis.core.integtestsupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.tck.dom.scalars.JdkValuedEntity;

public class IsisSystemWithFixturesTest_basicTest {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();
    
    @Test
    public void savePojo() throws Exception {

        iswf.beginTran();
        assertThat(iswf.container.allInstances(JdkValuedEntity.class).size(), is(0));
        
        iswf.container.persist(iswf.fixtures.jve1);
        
        assertThat(iswf.container.allInstances(JdkValuedEntity.class).size(), is(1));
        iswf.commitTran();
    }

}
