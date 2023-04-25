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
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;

class ObjectAdapterUtilsTest {

    private ManagedObject mockObjectAdapter = Mockito.mock(ManagedObject.class);
    private Object underlyingDomainObject;

    @Test
    void unwrapObjectWhenNull() {
        assertNull(MmUnwrapUtils.single((ManagedObject)null));
    }

    @Test
    void unwrapObjectWhenNotNull() {
        underlyingDomainObject = new Object();
        expectAdapterWillReturn(underlyingDomainObject);
        assertEquals(underlyingDomainObject, MmUnwrapUtils.single(mockObjectAdapter));
    }

    @Test
    void unwrapStringWhenNull() {
        assertNull(MmUnwrapUtils.singleAsStringOrElse(null, null));
    }

    @Test
    void unwrapStringWhenNotNullButNotString() {
        underlyingDomainObject = new Object();
        expectAdapterWillReturn(underlyingDomainObject);
        assertNull(MmUnwrapUtils.singleAsStringOrElse(mockObjectAdapter, null));
    }

    @Test
    void unwrapStringWhenNotNullAndString() {
        underlyingDomainObject = "huzzah";
        expectAdapterWillReturn(underlyingDomainObject);
        assertEquals("huzzah", MmUnwrapUtils.singleAsStringOrElse(mockObjectAdapter, null));
    }

    private void expectAdapterWillReturn(final Object domainObject) {
        Mockito.when(mockObjectAdapter.getPojo()).thenReturn(domainObject);
    }

}
