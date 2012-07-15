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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;
import org.apache.isis.tck.dom.scalars.WrapperValuedEntity;
import org.apache.isis.tck.dom.scalars.WrapperValuedEntityRepository;

public class Persistence_persist_wrapperValuedEntity {

    private WrapperValuedEntityRepository repo = new WrapperValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("WRAPPERVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwo() throws Exception {
        iswf.beginTran();
        repo.newEntity().setStringProperty("1");
        repo.newEntity().setStringProperty("2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<WrapperValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistAllValues() throws Exception {
        iswf.beginTran();
        WrapperValuedEntity entity = repo.newEntity();
        entity.setStringProperty("1");
        entity.setBooleanProperty(true);
        entity.setByteProperty((byte)123);
        entity.setDoubleProperty(9876543210987.0);
        entity.setFloatProperty(123456.0f);
        entity.setIntegerProperty(456);
        entity.setLongProperty(12345678901L);
        entity.setShortProperty((short)4567);
        entity.setCharacterProperty('X');
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        WrapperValuedEntity entityRetrieved = repo.list().get(0);
        assertThat(entityRetrieved.getStringProperty(), is("1"));
        assertThat(entityRetrieved.getBooleanProperty(), is(true));
        assertThat(entityRetrieved.getByteProperty(), is((byte)123));
        assertThat(entityRetrieved.getDoubleProperty(), is(9876543210987.0));
        assertThat(entityRetrieved.getFloatProperty(), is(123456.0f));
        assertThat(entityRetrieved.getIntegerProperty(), is(456));
        assertThat(entityRetrieved.getLongProperty(), is(12345678901L));
        assertThat(entityRetrieved.getShortProperty(), is((short)4567));
        assertThat(entityRetrieved.getCharacterProperty(), is('X'));
        
        iswf.commitTran();
    }

}
