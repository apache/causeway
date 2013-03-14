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
import org.apache.isis.core.tck.dom.scalars.WrapperValuedEntity;
import org.apache.isis.core.tck.dom.scalars.WrapperValuedEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persistAndUpdate_wrapperValuedEntity {

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
    public void persist_then_update() throws Exception {
        iswf.beginTran();
        WrapperValuedEntity entity = repo.newEntity();
        entity.setStringProperty("1");
        entity.setBooleanProperty(false);
        entity.setByteProperty((byte)321);
        entity.setDoubleProperty(123456768723429.0);
        entity.setFloatProperty(654321.0f);
        entity.setIntegerProperty(543);
        entity.setLongProperty(90876512345L);
        entity.setShortProperty((short)7654);
        entity.setCharacterProperty('A');
        
        iswf.commitTran();

        iswf.bounceSystem();

        iswf.beginTran();
        entity = repo.list().get(0);
        assertThat(entity.getStringProperty(), is("1"));
        assertThat(entity.getBooleanProperty(), is(false));
        assertThat(entity.getByteProperty(), is((byte)321));
        assertThat(entity.getDoubleProperty(), is(123456768723429.0));
        assertThat(entity.getFloatProperty(), is(654321.0f));
        assertThat(entity.getIntegerProperty(), is(543));
        assertThat(entity.getLongProperty(), is(90876512345L));
        assertThat(entity.getShortProperty(), is((short)7654));
        assertThat(entity.getCharacterProperty(), is('A'));
        
        
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
        entity = repo.list().get(0);
        assertThat(entity.getBooleanProperty(), is(true));
        assertThat(entity.getByteProperty(), is((byte)123));
        assertThat(entity.getDoubleProperty(), is(9876543210987.0));
        assertThat(entity.getFloatProperty(), is(123456.0f));
        assertThat(entity.getIntegerProperty(), is(456));
        assertThat(entity.getLongProperty(), is(12345678901L));
        assertThat(entity.getShortProperty(), is((short)4567));
        assertThat(entity.getCharacterProperty(), is('X'));
        
        iswf.commitTran();
    }

}
