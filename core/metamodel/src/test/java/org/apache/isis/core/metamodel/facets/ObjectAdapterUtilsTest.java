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

package org.apache.isis.core.metamodel.facets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class ObjectAdapterUtilsTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_ONLY);

    @Mock
    private ObjectAdapter mockObjectAdapter;

    private Object underlyingDomainObject;

    @Test
    public void testUnwrapObjectWhenNull() {
        assertNull(ObjectAdapter.Util.unwrapPojo((ObjectAdapter)null));
    }

    @Test
    public void testUnwrapObjectWhenNotNull() {
        underlyingDomainObject = new Object(); 
        expectAdapterWillReturn(underlyingDomainObject);
        assertEquals(underlyingDomainObject, ObjectAdapter.Util.unwrapPojo(mockObjectAdapter));
    }

    @Test
    public void testUnwrapStringWhenNull() {
        assertNull(ObjectAdapter.Util.unwrapPojoStringElse(null, null));
    }

    @Test
    public void testUnwrapStringWhenNotNullButNotString() {
        underlyingDomainObject = new Object(); 
        expectAdapterWillReturn(underlyingDomainObject);
        assertNull(ObjectAdapter.Util.unwrapPojoStringElse(mockObjectAdapter, null));
    }

    @Test
    public void testUnwrapStringWhenNotNullAndString() {
        underlyingDomainObject = "huzzah";
        expectAdapterWillReturn(underlyingDomainObject);
        assertEquals("huzzah", ObjectAdapter.Util.unwrapPojoStringElse(mockObjectAdapter, null));
    }

    private void expectAdapterWillReturn(final Object domainObject) {
        context.checking(new Expectations() {
            {
                allowing(mockObjectAdapter).getPojo();
                will(returnValue(domainObject));
            }
        });
    }
    

}
