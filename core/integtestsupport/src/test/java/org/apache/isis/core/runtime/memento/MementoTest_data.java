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

package org.apache.isis.core.runtime.memento;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.tck.dom.scalars.WrapperValuedEntity;

public class MementoTest_data {

    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder().build();

    private ObjectAdapter originalAdapter1, originalAdapter2;
    private Oid oid1, oid2;

    private Memento memento1, memento2;
    private Data data1, data2;

    @Before
    public void setUpSystem() throws Exception {
        iswf.fixtures.wve1.setStringProperty("Fred");
        
        originalAdapter1 = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(iswf.fixtures.wve1);
        oid1 = originalAdapter1.getOid();
        memento1 = new Memento(originalAdapter1);
        data1 = memento1.getData();
        
        iswf.fixtures.wve2.setStringProperty("Harry");
        iswf.container.persist(iswf.fixtures.wve2);
        
        originalAdapter2 = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(iswf.fixtures.wve2);
        oid2 = originalAdapter2.getOid();
        memento2 = new Memento(originalAdapter2);
        data2 = memento2.getData();
    }

    
    @Test
    public void data_whenNull() throws Exception {
        final Memento memento = new Memento(null);
        Data data = memento.getData();

        assertEquals(null, data);
    }


    @Test
    public void data_getOid_equal() throws Exception {
        assertEquals(oid1, data1.getOid());
    }



    @Test
    public void data_getClassName() throws Exception {
        assertEquals(WrapperValuedEntity.class.getName(), data1.getClassName());
    }
    
    
    @Test
    public void data_getEntry_forStringField() throws Exception {
        assertEquals(ObjectData.class, data1.getClass());
        final ObjectData objectData = (ObjectData) data1;
        assertEquals("Fred", objectData.getEntry("stringProperty"));
    }
    
}
