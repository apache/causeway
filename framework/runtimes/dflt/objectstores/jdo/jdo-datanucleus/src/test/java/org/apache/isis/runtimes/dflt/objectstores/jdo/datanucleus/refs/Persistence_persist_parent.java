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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.refs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.Utils;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.refs.ParentEntity;
import org.apache.isis.tck.dom.refs.ParentEntityRepository;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class Persistence_persist_parent {

    private ParentEntityRepository repo = new ParentEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        //.with(Utils.listenerToDeleteFrom("CHILDENTITY"))
        .with(Utils.listenerToDeleteFrom("PARENTENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwo() throws Exception {
        iswf.beginTran();
        repo.newEntity().setName("Parent 1");
        repo.newEntity().setName("Parent 2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<ParentEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

//    @Test
//    public void persistAllValues() throws Exception {
//        iswf.beginTran();
//        PrimitiveValuedEntity entity = repo.newEntity();
//        entity.setId(1);
//        entity.setBooleanProperty(true);
//        entity.setByteProperty((byte)123);
//        entity.setDoubleProperty(9876543210987.0);
//        entity.setFloatProperty(123456.0f);
//        entity.setIntProperty(456);
//        entity.setLongProperty(12345678901L);
//        entity.setShortProperty((short)4567);
//        entity.setCharProperty('X');
//        
//        iswf.commitTran();
//
//        iswf.bounceSystem();
//        
//        iswf.beginTran();
//        PrimitiveValuedEntity entityRetrieved = repo.list().get(0);
//        assertThat(entityRetrieved.getBooleanProperty(), is(true));
//        assertThat(entityRetrieved.getByteProperty(), is((byte)123));
//        assertThat(entityRetrieved.getDoubleProperty(), is(9876543210987.0));
//        assertThat(entityRetrieved.getFloatProperty(), is(123456.0f));
//        assertThat(entityRetrieved.getIntProperty(), is(456));
//        assertThat(entityRetrieved.getLongProperty(), is(12345678901L));
//        assertThat(entityRetrieved.getShortProperty(), is((short)4567));
//        assertThat(entityRetrieved.getCharProperty(), is('X'));
//        
//        iswf.commitTran();
//    }

}
