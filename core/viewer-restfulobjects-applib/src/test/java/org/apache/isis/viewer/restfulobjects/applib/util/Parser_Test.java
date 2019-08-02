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
package org.apache.isis.viewer.restfulobjects.applib.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Parser_Test {

    @Test
    public void forBoolean() {
        final Parser<Boolean> parser = Parser.forBoolean();
        for (final Boolean v : new Boolean[] { Boolean.TRUE, Boolean.FALSE }) {
            final String asString = parser.asString(v);
            final Boolean valueOf = parser.valueOf(asString);
            assertThat(v, is(equalTo(valueOf)));
        }

        final Boolean valueOf = parser.valueOf(Arrays.asList(parser.asString(Boolean.TRUE), parser.asString(Boolean.FALSE)));
        assertThat(valueOf, is(Boolean.TRUE));
    }

    @Test
    public void forString() {
        final Parser<String> parser = Parser.forString();

        for (final String v : new String[] { "", "foo", "foz" }) {
            final String asString = parser.asString(v);
            final String valueOf = parser.valueOf(asString);
            assertThat(v, is(equalTo(valueOf)));
        }
    }

    @Test
    public void forListOfStrings() {
        final Parser<List<String>> parser = Parser.forListOfStrings();

        final List<String> v = Arrays.asList("", "foo", "foz");

        final String asString = parser.asString(v);
        final List<String> valueOf = parser.valueOf(asString);

        assertThat(v, sameContentsAs(valueOf));
    }

    @Test
    public void forListOfListOfStringsDottedNotation() {
        final Parser<List<List<String>>> parser = Parser.forListOfListOfStrings();

        final List<List<String>> valueOf = parser.valueOf(Arrays.asList("a", "b.c", "d.e.f"));

        assertThat(valueOf.size(), is(3));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
        assertThat(valueOf.get(2).size(), is(3));
        assertThat(valueOf.get(2).get(0), is("d"));
        assertThat(valueOf.get(2).get(1), is("e"));
        assertThat(valueOf.get(2).get(2), is("f"));

        assertThat(parser.asString(valueOf), is("a,b.c,d.e.f"));
    }

    @Test
    public void forListOfListOfStringsCommaSeparated() {
        final Parser<List<List<String>>> parser = Parser.forListOfListOfStrings();

        final List<List<String>> valueOf = parser.valueOf("a,b.c,d.e.f");

        assertThat(valueOf.size(), is(3));
        assertThat(valueOf.get(0).size(), is(1));
        assertThat(valueOf.get(0).get(0), is("a"));
        assertThat(valueOf.get(1).size(), is(2));
        assertThat(valueOf.get(1).get(0), is("b"));
        assertThat(valueOf.get(1).get(1), is("c"));
        assertThat(valueOf.get(2).size(), is(3));
        assertThat(valueOf.get(2).get(0), is("d"));
        assertThat(valueOf.get(2).get(1), is("e"));
        assertThat(valueOf.get(2).get(2), is("f"));
    }

    //    @Test
    //    public void forGuavaMediaTypes() {
    //        final Parser<MediaType> parser = Parser.forGuavaMediaType();
    //        final MediaType mediaType = MediaType.parse("application/json");
    //        final String asString = parser.asString(mediaType);
    //        final MediaType valueOf = parser.valueOf(asString);
    //
    //        assertThat(valueOf, is(mediaType));
    //    }
    //
    //    @Test
    //    public void forListOfMediaTypes() {
    //        final Parser<List<MediaType>> parser = Parser.forListOfGuavaMediaTypes();
    //        final List<MediaType> list = Arrays.asList(MediaType.parse("application/xml"), MediaType.parse("application/json"));
    //        final String asString = parser.asString(list);
    //        final List<MediaType> valueOf = parser.valueOf(asString);
    //
    //        assertThat(list, sameContentsAs(valueOf));
    //    }
    //    
    //    @Test
    //    public void forMediaTypeJson() {
    //        final MediaType mediaType = MediaType.valueOf("application/json");
    //        assertEquals(MediaType.APPLICATION_JSON_TYPE, mediaType);
    //    }
    //
    //    @Test
    //    public void forMediaTypeXml() {
    //        final MediaType mediaType = MediaType.valueOf("application/xml");
    //        assertEquals(MediaType.APPLICATION_XML_TYPE, mediaType);
    //    }

    @Test
    public void forDate() {
        final Parser<Date> parser = Parser.forDate();

        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.MILLISECOND, 0);
        final Date nowToNearestSecond = cal.getTime();
        for (final Date v : new Date[] { nowToNearestSecond }) {
            final String asString = parser.asString(v);
            final Date valueOf = parser.valueOf(asString);
            assertThat(v, is(equalTo(valueOf)));
        }
    }

    @Test
    public void forInteger() {
        final Parser<Integer> parser = Parser.forInteger();

        for (final Integer v : new Integer[] { 1, 2, 3, -5, -100, 0, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
            final String asString = parser.asString(v);
            final Integer valueOf = parser.valueOf(asString);
            assertThat(v, is(equalTo(valueOf)));
        }
    }

    private static <T> Matcher<List<T>> sameContentsAs(final List<T> expected) {
        return new TypeSafeMatcher<List<T>>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("same sequence as " + expected);
            }

            @Override
            public boolean matchesSafely(final List<T> actual) {
                return actual.containsAll(expected) && expected.containsAll(actual);
            }
        };
    }


}
