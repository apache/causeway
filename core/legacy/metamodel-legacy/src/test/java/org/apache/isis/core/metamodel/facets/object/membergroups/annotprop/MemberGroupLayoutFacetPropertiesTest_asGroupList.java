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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.apache.isis.core.metamodel.facets.object.membergroups.annotprop.MemberGroupLayoutFacetProperties;

public class MemberGroupLayoutFacetPropertiesTest_asGroupList {

    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
    }
    
    @Test
    public void empty() throws Exception {
        assertThat(MemberGroupLayoutFacetProperties.asGroupList(properties, "left"), is(new String[]{}));
    }
    
    @Test
    public void other() throws Exception {
        properties.put("foo", "a,b,c");
        assertThat(MemberGroupLayoutFacetProperties.asGroupList(properties, "left"), is(new String[]{}));
    }

    @Test
    public void happyCase() throws Exception {
        properties.put("left", "a,b,c");
        assertThat(MemberGroupLayoutFacetProperties.asGroupList(properties, "left"), is(new String[]{"a","b","c"}));
    }
    
    @Test
    public void single() throws Exception {
        properties.put("left", "a");
        assertThat(MemberGroupLayoutFacetProperties.asGroupList(properties, "left"), is(new String[]{"a"}));
    }

    @Test
    public void trimmed() throws Exception {
        properties.put("left", " a, b ,c ");
        assertThat(MemberGroupLayoutFacetProperties.asGroupList(properties, "left"), is(new String[]{"a","b","c"}));
    }

    @Test
    public void filter() throws Exception {
        properties.put("left", "a,,c");
        assertThat(MemberGroupLayoutFacetProperties.asGroupList(properties, "left"), is(new String[]{"a","c"}));
    }
    
}
