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
package org.apache.isis.objectstore.jdo.datanucleus.scenarios.scalar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persistAndUpdate_primitiveValuedEntity {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwo() throws Exception {
        iswf.beginTran();
        repo.newEntity().setId(1);
        repo.newEntity().setId(2);
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persist_then_update() throws Exception {
        
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        
        entity.setBooleanProperty(false);
        entity.setByteProperty((byte)456);
        entity.setDoubleProperty(123456789876.0);
        entity.setFloatProperty(654321.0f);
        entity.setIntProperty(765);
        entity.setLongProperty(7654321012345L);
        entity.setShortProperty((short)543);
        entity.setCharProperty('A');
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        entity = repo.list().get(0);

        assertThat(entity.getBooleanProperty(), is(false));
        assertThat(entity.getByteProperty(), is((byte)456));
        assertThat(entity.getDoubleProperty(), is(123456789876.0));
        assertThat(entity.getFloatProperty(), is(654321.0f));
        assertThat(entity.getIntProperty(), is(765));
        assertThat(entity.getLongProperty(), is(7654321012345L));
        assertThat(entity.getShortProperty(), is((short)543));
        assertThat(entity.getCharProperty(), is('A'));

        
        entity.setBooleanProperty(true);
        entity.setByteProperty((byte)123);
        entity.setDoubleProperty(9876543210987.0);
        entity.setFloatProperty(123456.0f);
        entity.setIntProperty(456);
        entity.setLongProperty(12345678901L);
        entity.setShortProperty((short)4567);
        entity.setCharProperty('X');

        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        entity = repo.list().get(0);

        assertThat(entity.getBooleanProperty(), is(true));
        assertThat(entity.getByteProperty(), is((byte)123));
        assertThat(entity.getDoubleProperty(), is(9876543210987.0));
        assertThat(entity.getFloatProperty(), is(123456.0f));
        assertThat(entity.getIntProperty(), is(456));
        assertThat(entity.getLongProperty(), is(12345678901L));
        assertThat(entity.getShortProperty(), is((short)4567));
        assertThat(entity.getCharProperty(), is('X'));
        
        iswf.commitTran();
    }
}
