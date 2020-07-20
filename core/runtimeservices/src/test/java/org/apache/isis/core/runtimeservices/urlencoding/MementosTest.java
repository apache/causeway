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

package org.apache.isis.core.runtimeservices.urlencoding;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.applib.services.urlencoding.UrlEncodingServiceUsingBaseEncodingAbstract;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.memento._Mementos;
import org.apache.isis.core.commons.internal.memento._Mementos.Memento;
import org.apache.isis.core.commons.internal.memento._Mementos.SerializingAdapter;

public class MementosTest {

    static enum DOW {
        Mon,Tue,Wed,Thu,Fri
    }

    UrlEncodingServiceWithCompression serviceWithCompression;
    UrlEncodingServiceUsingBaseEncodingAbstract serviceBaseEncoding;
    SerializingAdapter serializingAdapter;

    @Before
    public void setUp() throws Exception {
        serviceWithCompression = new UrlEncodingServiceWithCompression();
        serviceBaseEncoding = new UrlEncodingServiceUsingBaseEncodingAbstract(){};

        serializingAdapter = new SerializingAdapter() {

            @Override
            public Serializable write(Object value) {
                return (Serializable) value;
            }

            @Override
            public <T> T read(Class<T> cls, Serializable value) {
                return _Casts.castToOrElseNull(value, cls);
            }
        };

    }

    @Test
    public void roundtrip() {
        roundtrip(serviceBaseEncoding);
    }

    @Test
    public void roundtrip_with_compression() {
        roundtrip(serviceWithCompression);
    }

    private void roundtrip(UrlEncodingService codec) {
        final Memento memento = _Mementos.create(codec, serializingAdapter);

        memento.put("someString", "a string");
        memento.put("someStringWithDoubleSpaces", "a  string");
        memento.put("someByte", (byte)123);
        memento.put("someShort", (short)12345);
        memento.put("someInt", 123456789);
        memento.put("someLong", 1234567890123456789L);
        memento.put("someFloat", 123.45F);
        memento.put("someDouble", 1234567890.123456);
        memento.put("someBooleanTrue", Boolean.TRUE);
        memento.put("someBooleanFalse", Boolean.FALSE);
        memento.put("someBigInteger", new BigInteger("123456789012345678901234567890"));
        memento.put("someBigDecimal", new BigDecimal("123456789012345678901234567890.123456789"));
        memento.put("someLocalDate", LocalDate.of(2013,9,3));
        memento.put("someJavaUtilDate", new Date(300_000_000));

        memento.put("someBookmark", Bookmark.of("CUS", "12345"));
        memento.put("someNullValue", null);

        memento.put("someEnum", DOW.Wed);

        final String str = memento.asString();

        final Memento memento2 = _Mementos.parse(codec, serializingAdapter, str);

        assertThat(memento2.get("someString", String.class), is("a string"));
        assertThat(memento2.get("someStringWithDoubleSpaces", String.class), is("a  string"));
        assertThat(memento2.get("someByte", Byte.class), is((byte)123));
        assertThat(memento2.get("someShort", Short.class), is((short)12345));
        assertThat(memento2.get("someInt", Integer.class), is(123456789));
        assertThat(memento2.get("someLong", Long.class), is(1234567890123456789L));
        assertThat(memento2.get("someFloat", Float.class), is(123.45F));
        assertThat(memento2.get("someDouble", Double.class), is(1234567890.123456));
        assertThat(memento2.get("someBooleanTrue", Boolean.class), is(Boolean.TRUE));
        assertThat(memento2.get("someBooleanFalse", Boolean.class), is(Boolean.FALSE));
        assertThat(memento2.get("someBigInteger", BigInteger.class), is(new BigInteger("123456789012345678901234567890")));
        assertThat(memento2.get("someBigDecimal", BigDecimal.class), is(new BigDecimal("123456789012345678901234567890.123456789")));
        assertThat(memento2.get("someLocalDate", LocalDate.class), is(LocalDate.of(2013,9,3)));
        assertThat(memento2.get("someJavaUtilDate", Date.class), is(new Date(300_000_000)));
        assertThat(memento2.get("someBookmark", Bookmark.class), is(Bookmark.of("CUS", "12345")));

        // a nullValue can be grabbed as any type, will always succeed
        assertThat(memento2.get("someNullValue", Integer.class), is(nullValue()));
        assertThat(memento2.get("someNullValue", Bookmark.class), is(nullValue()));
        assertThat(memento2.get("someNullValue", LocalDate.class), is(nullValue()));

        assertThat(memento2.get("someEnum", DOW.class), is(DOW.Wed));

    }


}
