/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.viewmodelsupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.services.viewmodelsupport.ViewModelSupport.Memento;

public class ViewModelSupportDefaultTest {

    private ViewModelSupportDefault viewModelSupport;
    
    @Before
    public void setUp() throws Exception {
        viewModelSupport = new ViewModelSupportDefault();
    }
    
    @Test
    public void roundtrip() {
        final Memento memento = viewModelSupport.create();
        
        memento.set("someString", "a string");
        memento.set("someByte", (byte)123);
        memento.set("someShort", (short)12345);
        memento.set("someInt", 123456789);
        memento.set("someLong", 1234567890123456789L);
        memento.set("someFloat", 123.45F);
        memento.set("someDouble", 1234567890.123456);
        memento.set("someBooleanTrue", Boolean.TRUE);
        memento.set("someBooleanFalse", Boolean.FALSE);
        memento.set("someBigInteger", new BigInteger("123456789012345678901234567890"));
        memento.set("someBigDecimal", new BigDecimal("123456789012345678901234567890.123456789"));
        memento.set("someLocalDate", new LocalDate(2013,9,3));
        
        final String str = memento.asString();
        
        final Memento memento2 = viewModelSupport.parse(str);
        
        assertThat(memento2.get("someString", String.class), is("a string"));
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
        assertThat(memento2.get("someLocalDate", LocalDate.class), is(new LocalDate(2013,9,3)));
    }
}
