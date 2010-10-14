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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.hibernate.property.Setter;
import org.junit.Test;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.TestObject;

public class UserAccessorGetSetter extends UserAccessorAbstractTestCase {


    @Test
    public void happyCase() {
        final UserAccessor accessor = new UserAccessor();
        final Setter setter = accessor.getSetter(TestObject.class, PropertyHelper.MODIFIED_BY);
        assertNotNull(setter);
        assertNull("getMethod", setter.getMethod());
        assertNull("getMethodName", setter.getMethodName());
    }


}
