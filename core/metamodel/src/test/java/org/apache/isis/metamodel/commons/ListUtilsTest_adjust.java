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
package org.apache.isis.metamodel.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ListUtilsTest_adjust {
    
    
    @Test
    public void sameLength() throws Exception {
        final List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE);
        ListExtensions.adjust(list, 3);
        
        assertThat(list.size(), is(3));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
        assertThat(list.get(2), is(Integer.MIN_VALUE));
    }
    
    @Test
    public void ifLonger() throws Exception {
        final List<Integer> list = new ArrayList<>(Arrays.asList(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE));
        ListExtensions.adjust(list, 4);
        
        assertThat(list.size(), is(4));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
        assertThat(list.get(2), is(Integer.MIN_VALUE));
        assertThat(list.get(3), is(nullValue()));
    }
    
    @Test
    public void ifShorter() throws Exception {
        final List<Integer> list = new ArrayList<>(Arrays.asList(Integer.valueOf(0), Integer.MAX_VALUE, Integer.MIN_VALUE));
        ListExtensions.adjust(list, 2);
        
        assertThat(list.size(), is(2));
        assertThat(list.get(0), is(Integer.valueOf(0)));
        assertThat(list.get(1), is(Integer.MAX_VALUE));
    }

}
