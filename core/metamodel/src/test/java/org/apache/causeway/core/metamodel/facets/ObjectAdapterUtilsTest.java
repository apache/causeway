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
package org.apache.causeway.core.metamodel.facets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;

class ObjectAdapterUtilsTest {

    Mocking mocking = new Mocking();

    @Test
    void unwrapObjectWhenNull() {
        assertNull(MmUnwrapUtils.single((ManagedObject)null));
    }

    @Test
    void unwrapObjectWhenNotNull() {
        var mo = mocking.asValue(new Object());
        assertEquals(mo.getPojo(), MmUnwrapUtils.single(mo));
    }

    @Test
    void unwrapStringWhenNull() {
        assertNull(MmUnwrapUtils.singleAsStringOrElse(null, null));
    }

    @Test
    void unwrapStringWhenNotNullButNotString() {
        var mo = mocking.asValue(new Object());
        assertNull(MmUnwrapUtils.singleAsStringOrElse(mo, null));
    }

    @Test
    void unwrapStringWhenNotNullAndString() {
        var mo = mocking.asValue("huzzah");
        assertEquals("huzzah", MmUnwrapUtils.singleAsStringOrElse(mo, null));
    }

}
