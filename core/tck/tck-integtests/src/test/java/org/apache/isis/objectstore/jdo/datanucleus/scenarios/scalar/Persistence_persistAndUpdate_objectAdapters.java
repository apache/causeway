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
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persistAndUpdate_objectAdapters {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void transient_then_persistent() throws Exception {
        
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        ObjectAdapter adapter = iswf.adapterFor(entity);
        
        assertThat(adapter.isTransient(), is(true));
        assertThat(adapter.getResolveState(), is(ResolveState.TRANSIENT));
        assertThat(adapter.getOid().isTransient(), is(true));
        
        entity.setId(1);
        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        final List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(1));
        
        adapter = iswf.adapterFor(list.get(0));
        assertThat(adapter.getResolveState(), is(ResolveState.RESOLVED));
        assertThat(adapter.isTransient(), is(false));
        assertThat(adapter.getOid().enString(new OidMarshaller()), is("PRMV:i~1"));

        iswf.commitTran();
    }

    @Test
    public void updated_and_retrieved() throws Exception {

        // given persisted
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        ObjectAdapter adapter = iswf.adapterFor(entity);
        
        entity.setId(1);
        entity.setCharProperty('X');
        
        iswf.commitTran();
        
        // when update
        iswf.bounceSystem();

        iswf.beginTran();
        entity = repo.list().get(0);
        entity.setCharProperty('Y');
        iswf.commitTran();

        // then adapter's state is resolved
        iswf.bounceSystem();
        
        iswf.beginTran();
        entity = repo.list().get(0);
        assertThat(entity.getCharProperty(), is('Y'));
        
        adapter = iswf.adapterFor(entity);
        assertThat(adapter.getResolveState(), is(ResolveState.RESOLVED));
        
        iswf.commitTran();

    }

    

}
