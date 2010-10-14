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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.hibernate.PropertyNotFoundException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.SimpleObject;


public class ValueTypePropertyAccessorGetValueHolder {

    private ObjectPropertyAccessor accessor;

    @Before
    public void setUp() {
        accessor = new ObjectPropertyAccessor();
    }
    
    @Ignore("need to convert, was originally written for the old value holder design (TextString, etc)")
    @Test
    public void happyCaseWhenPropertyExists() {

        Method m = accessor.getValueHolderMethod(SimpleObject.class, "string");
        assertEquals("getString", m.getName());
    }

    @Test(expected=PropertyNotFoundException.class)
    public void shouldThrowExceptionIfPropertyDoesNotExist() {
        accessor.getValueHolderMethod(SimpleObject.class, "missing");
    }

}
