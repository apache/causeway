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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.Utils;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.refs.UnidirReferencedEntity;
import org.apache.isis.tck.dom.refs.UnidirReferencedEntityRepository;
import org.apache.isis.tck.dom.refs.UnidirReferencingEntity;
import org.apache.isis.tck.dom.refs.UnidirReferencingEntityRepository;

public class Persistence_persistAndUpdate_referencing {

    private UnidirReferencingEntityRepository referencingRepo = new UnidirReferencingEntityRepository();
    private UnidirReferencedEntityRepository referencedRepo = new UnidirReferencedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("UNIDIRREFERENCINGENTITY"))
        .with(Utils.listenerToDeleteFrom("UNIDIRREFERENCEDENTITY"))
        .withServices(referencingRepo, referencedRepo)
        .build();

    @Test
    public void persist() throws Exception {
        
        iswf.beginTran();
        referencedRepo.newEntity().setName("Referenced 1");
        referencedRepo.newEntity().setName("Referenced 2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        UnidirReferencedEntity referencedEntity1 = referencedRepo.list().get(0);
        UnidirReferencedEntity referencedEntity2 = referencedRepo.list().get(1);
        
        UnidirReferencingEntity referencingEntity1 = referencingRepo.newEntity();
        referencingEntity1.setName("Referencing 1");
        referencingEntity1.setReferenced(referencedEntity1);
        
        UnidirReferencingEntity referencingEntity2 = referencingRepo.newEntity();
        referencingEntity2.setName("Referencing 2");
        referencingEntity2.setReferenced(referencedEntity1);

        UnidirReferencingEntity referencingEntity3 = referencingRepo.newEntity();
        referencingEntity3.setName("Referencing 3");
        referencingEntity3.setReferenced(referencedEntity2);

        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        List<UnidirReferencingEntity> list = referencingRepo.list();
        referencingEntity1 = list.get(0);
        referencingEntity2 = list.get(1);
        referencingEntity3 = list.get(2);
        
        assertThat(referencingEntity1.getReferenced(), is(not(nullValue())));
        assertThat(referencingEntity2.getReferenced(), is(not(nullValue())));
        assertThat(referencingEntity3.getReferenced(), is(not(nullValue())));
        
        assertThat(referencingEntity1.getReferenced(), is(referencingEntity1.getReferenced()));
        assertThat(referencingEntity1.getReferenced(), is(not(referencingEntity3.getReferenced())));
        iswf.commitTran();
    }

    @Test
    public void persistAGraphOfObjects() throws Exception {
        
        iswf.beginTran();
        UnidirReferencedEntity referencedEntity1 = referencedRepo.newEntity();
        referencedEntity1.setName("Referenced 1");
        UnidirReferencedEntity referencedEntity2 = referencedRepo.newEntity();
        referencedEntity2.setName("Referenced 2");

        UnidirReferencingEntity referencingEntity1 = referencingRepo.newEntity();
        referencingEntity1.setName("Referencing 1");
        referencingEntity1.setReferenced(referencedEntity1);
        UnidirReferencingEntity referencingEntity2 = referencingRepo.newEntity();
        referencingEntity2.setName("Referencing 2");
        referencingEntity2.setReferenced(referencedEntity1);
        UnidirReferencingEntity referencingEntity3 = referencingRepo.newEntity();
        referencingEntity3.setName("Referencing 3");
        referencingEntity3.setReferenced(referencedEntity2);
        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        List<UnidirReferencingEntity> list = referencingRepo.list();
        referencingEntity1 = list.get(0);
        referencingEntity2 = list.get(1);
        referencingEntity3 = list.get(2);
        
        assertThat(referencingEntity1.getReferenced(), is(not(nullValue())));
        assertThat(referencingEntity2.getReferenced(), is(not(nullValue())));
        assertThat(referencingEntity3.getReferenced(), is(not(nullValue())));
        
        assertThat(referencingEntity1.getReferenced(), is(referencingEntity1.getReferenced()));
        assertThat(referencingEntity1.getReferenced(), is(not(referencingEntity3.getReferenced())));
        iswf.commitTran();
    }

    @Test
    public void persist_then_update() throws Exception {
        
        iswf.beginTran();
        UnidirReferencedEntity referencedEntity1 = referencedRepo.newEntity();
        referencedEntity1.setName("Referenced 1");
        UnidirReferencedEntity referencedEntity2 = referencedRepo.newEntity();
        referencedEntity2.setName("Referenced 2");

        UnidirReferencingEntity referencingEntity1 = referencingRepo.newEntity();
        referencingEntity1.setName("Referencing 1");
        referencingEntity1.setReferenced(referencedEntity1);
        UnidirReferencingEntity referencingEntity2 = referencingRepo.newEntity();
        referencingEntity2.setName("Referencing 2");
        referencingEntity2.setReferenced(referencedEntity1);
        UnidirReferencingEntity referencingEntity3 = referencingRepo.newEntity();
        referencingEntity3.setName("Referencing 3");
        referencingEntity3.setReferenced(referencedEntity2);
        iswf.commitTran();

        // when ...
        iswf.bounceSystem();
        
        iswf.beginTran();
        List<UnidirReferencingEntity> referencingList = referencingRepo.list();
        referencingEntity1 = referencingList.get(0);
        referencingEntity2 = referencingList.get(1);
        referencingEntity3 = referencingList.get(2);

        List<UnidirReferencedEntity> referencedList = referencedRepo.list();
        referencedEntity1 = referencedList.get(0);
        referencedEntity2 = referencedList.get(1);

        assertThat(referencingEntity2.getReferenced(), is(referencedEntity1));

        // ...switch to refer to other
        referencingEntity2.setReferenced(referencedEntity2);
        iswf.commitTran();

        // then...
        iswf.bounceSystem();
        
        iswf.beginTran();
        referencingList = referencingRepo.list();
        referencingEntity1 = referencingList.get(0);
        referencingEntity2 = referencingList.get(1);
        referencingEntity3 = referencingList.get(2);

        referencedList = referencedRepo.list();
        referencedEntity1 = referencedList.get(0);
        referencedEntity2 = referencedList.get(1);

        // ...is switched
        assertThat(referencingEntity2.getReferenced(), is(referencedEntity2));
        iswf.commitTran();
    }

    
    @Test
    public void lazyLoading_and_adapters() throws Exception {
        iswf.beginTran();
        referencedRepo.newEntity().setName("Referenced 1");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        UnidirReferencedEntity referencedEntity1 = referencedRepo.list().get(0);
        
        UnidirReferencingEntity referencingEntity1 = referencingRepo.newEntity();
        referencingEntity1.setName("Referencing 1");
        referencingEntity1.setReferenced(referencedEntity1);
        
        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        List<UnidirReferencingEntity> list = referencingRepo.list();
        referencingEntity1 = list.get(0);
        
        assertThat(referencingEntity1.referenced, is(nullValue())); // lazy loading
        UnidirReferencedEntity referenced = referencingEntity1.getReferenced();
        ObjectAdapter referencedAdapter = iswf.adapterFor(referenced);
        assertThat(referencedAdapter.getResolveState(), is(ResolveState.RESOLVED));
        assertThat(referenced, is(not(nullValue())));
        
        iswf.commitTran();
    }

}
