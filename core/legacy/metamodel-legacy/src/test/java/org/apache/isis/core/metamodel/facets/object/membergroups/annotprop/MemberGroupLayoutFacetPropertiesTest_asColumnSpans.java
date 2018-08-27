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

package org.apache.isis.core.metamodel.facets.object.membergroups.annotprop;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class MemberGroupLayoutFacetPropertiesTest_asColumnSpans {

    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
    }
    
    @Test
    public void empty() throws Exception {
        assertThat(MemberGroupLayoutFacetProperties.asColumnSpans(properties), is(nullValue()));
    }
    
    @Test
    public void other() throws Exception {
        properties.put("foo", "a,b,c");
        assertThat(MemberGroupLayoutFacetProperties.asColumnSpans(properties), is(nullValue()));
    }

    @Test
    public void happyCase() throws Exception {
        properties.put("columnSpans", "2,3,0,7");
        assertThat(MemberGroupLayoutFacetProperties.asColumnSpans(properties), is(ColumnSpans.asSpans(2,3,0,7)));
    }
    
    @Test
    public void trimmed() throws Exception {
        properties.put("columnSpans", " 2,3,0,7 ");
        assertThat(MemberGroupLayoutFacetProperties.asColumnSpans(properties), is(ColumnSpans.asSpans(2,3,0,7)));
    }
    
    @Test
    public void invalid() throws Exception {
        properties.put("columnSpans", "x,x,x,x");
        assertThat(MemberGroupLayoutFacetProperties.asColumnSpans(properties), is(nullValue()));
    }

}
