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
package org.apache.isis.objectstore.jdo.datanucleus.valuetypes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.value.Date;

public class IsisDateConverterTest {

    private IsisDateConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new IsisDateConverter();
    }
    
    @Test
    public void roundTrip() {
        Date date = new Date();
        final Long value = converter.toDatastoreType(date);
        Date date2 = (Date) converter.toMemberType(value);
        
        // necessary to use dateValue() because the Isis date (rather poorly) does not
        // override equals() / hashCode()
        assertThat(date.dateValue(), is(equalTo(date2.dateValue())));
    }

    @Test
    public void toLong_whenNull() {
        assertNull(converter.toDatastoreType(null));
    }

    @Test
    public void toObject_whenNull() {
        assertNull(converter.toMemberType(null));
    }

}
