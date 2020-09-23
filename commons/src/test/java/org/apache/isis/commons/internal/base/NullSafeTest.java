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

package org.apache.isis.commons.internal.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NullSafeTest {

    @Test
    void streamAutodetect() throws Exception {
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new boolean[] {true, false})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new byte[] {1, 2})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new char[] {'1', '2'})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new short[] {1, 2})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new float[] {1.f, 2.f})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new double[] {1., 2.})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new int[] {1, 2})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new long[] {1L, 2L})
                .count());
        
        assertEquals(2, 
                _NullSafe.streamAutodetect(new String[] {"hi", "there"})
                .count());
    }
    
    @Test
    void isEmptyString() throws Exception {
        assertThat(_NullSafe.isEmpty((String)null), is(true));
        assertThat(_NullSafe.isEmpty(""), is(true));
        assertThat(_NullSafe.isEmpty(" 12 aBc"), is(false));
    }

    @Test
    void isEmptyCollection() throws Exception {
        assertThat(_NullSafe.isEmpty((Collection<?>)null), is(true));
        assertThat(_NullSafe.isEmpty(Collections.emptyList()), is(true));
        assertThat(_NullSafe.isEmpty(Arrays.asList(new String[] {"foo", "bar"})), is(false));
    }

    @Test
    void absence() throws Exception {
        assertThat(_NullSafe.isAbsent(null), is(true));
        assertThat(_NullSafe.isAbsent(""), is(false));
    }

    @Test
    void presence() throws Exception {
        assertThat(_NullSafe.isPresent(null), is(false));
        assertThat(_NullSafe.isPresent(""), is(true));
    }


    @Test
    void emptyStreamWithArray() throws Exception {

        assertNotNull(_NullSafe.stream((String[])null));

        assertNotNull(_NullSafe.stream(_Strings.emptyArray));
        assertEquals(0L, _NullSafe.stream(_Strings.emptyArray).count());
    }

    @Test
    void streamWithArray() throws Exception {
        assertThat(
                _NullSafe.stream(new String[] {"foo", "bar"})
                .collect(Collectors.joining("|")),
                is("foo|bar"));
    }

    @Test
    void emptyStreamWithCollection() throws Exception {

        assertNotNull(_NullSafe.stream((List<?>)null));

        assertNotNull(_NullSafe.stream(Arrays.asList(_Strings.emptyArray)));
        assertEquals(0L, _NullSafe.stream(Arrays.asList(_Strings.emptyArray)).count());
    }

    @Test
    void streamWithCollection() throws Exception {
        assertThat(
                _NullSafe.stream(Arrays.asList(new String[] {"foo", "bar"}))
                .collect(Collectors.joining("|")),
                is("foo|bar"));
    }

    @Test
    void emptyStreamWithIterator() throws Exception {

        assertNotNull(_NullSafe.stream((Iterator<?>)null));

        assertNotNull(_NullSafe.stream(Arrays.asList(_Strings.emptyArray)).iterator());
        assertEquals(0L, _NullSafe.stream(Arrays.asList(_Strings.emptyArray).iterator()).count());
    }

    @Test
    void streamWithIterator() throws Exception {
        assertThat(
                _NullSafe.stream(Arrays.asList(new String[] {"foo", "bar"}).iterator())
                .collect(Collectors.joining("|")),
                is("foo|bar"));
    }



}
