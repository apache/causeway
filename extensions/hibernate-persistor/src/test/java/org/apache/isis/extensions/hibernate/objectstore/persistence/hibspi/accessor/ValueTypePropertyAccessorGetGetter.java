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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.hibernate.property.Getter;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.SimpleObject;



public class ValueTypePropertyAccessorGetGetter {

    private SimpleObject obj = new SimpleObject();
    private String expected = "myvalue";


    @Ignore("need to convert, was originally written for the old value holder design (TextString, etc)")
    @Test
    public void happyCase() {
        obj.setString(expected);

        ObjectPropertyAccessor accessor = new ObjectPropertyAccessor();
        Getter getter = accessor.getGetter(SimpleObject.class, "string");
        assertNotNull(getter);
        assertNull("getMethod", getter.getMethod());
        assertNull("getMethodName", getter.getMethodName());
        assertEquals("return type", String.class, getter.getReturnType());
    }

}
