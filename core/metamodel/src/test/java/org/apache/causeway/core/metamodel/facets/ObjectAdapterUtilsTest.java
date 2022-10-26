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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;

@ExtendWith(MockitoExtension.class)
class ObjectAdapterUtilsTest {

    @Mock private ManagedObject mockObjectAdapter;
    private Object underlyingDomainObject;

    @Test
    public void testUnwrapObjectWhenNull() {
        assertNull(MmUnwrapUtil.single((ManagedObject)null));
    }

    @Test
    public void testUnwrapObjectWhenNotNull() {
        underlyingDomainObject = new Object();
        expectAdapterWillReturn(underlyingDomainObject);
        assertEquals(underlyingDomainObject, MmUnwrapUtil.single(mockObjectAdapter));
    }

    @Test
    public void testUnwrapStringWhenNull() {
        assertNull(MmUnwrapUtil.singleAsStringOrElse(null, null));
    }

    @Test
    public void testUnwrapStringWhenNotNullButNotString() {
        underlyingDomainObject = new Object();
        expectAdapterWillReturn(underlyingDomainObject);
        assertNull(MmUnwrapUtil.singleAsStringOrElse(mockObjectAdapter, null));
    }

    @Test
    public void testUnwrapStringWhenNotNullAndString() {
        underlyingDomainObject = "huzzah";
        expectAdapterWillReturn(underlyingDomainObject);
        assertEquals("huzzah", MmUnwrapUtil.singleAsStringOrElse(mockObjectAdapter, null));
    }

    private void expectAdapterWillReturn(final Object domainObject) {
        Mockito.when(mockObjectAdapter.getPojo()).thenReturn(domainObject);
    }

}
