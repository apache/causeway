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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_namedQuery_all {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Before
    public void setUp() throws Exception {

        iswf.beginTran();

        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity.setIntProperty(111);

        entity = repo.newEntity();
        entity.setId(2);
        entity.setIntProperty(222);

        entity = repo.newEntity();
        entity.setId(3);
        entity.setIntProperty(333);

        entity = repo.newEntity();
        entity.setId(4);
        entity.setIntProperty(111);

        iswf.commitTran();
    }
    
    @Test
    public void whenOne() throws Exception {
        
        iswf.beginTran();

        List<PrimitiveValuedEntity> entities = repo.findByNamedQueryAll("prmv_findByIntProperty", ImmutableMap.of("i", (Object)222));
        assertThat(entities, is(not(nullValue())));
        assertThat(entities.size(), is(1));

        iswf.commitTran();
    }

    @Test
    public void whenTwo() throws Exception {
        
        iswf.beginTran();

        List<PrimitiveValuedEntity> entities = repo.findByNamedQueryAll("prmv_findByIntProperty", ImmutableMap.of("i", (Object)111));
        assertThat(entities, is(not(nullValue())));
        assertThat(entities.size(), is(2));

        iswf.commitTran();
    }

    @Test
    public void whenNone() throws Exception {
        
        iswf.beginTran();

        List<PrimitiveValuedEntity> entities = repo.findByNamedQueryAll("prmv_findByIntProperty", ImmutableMap.of("i", (Object)999));
        assertThat(entities, is(not(nullValue())));
        assertThat(entities.size(), is(0));

        iswf.commitTran();
    }
}
