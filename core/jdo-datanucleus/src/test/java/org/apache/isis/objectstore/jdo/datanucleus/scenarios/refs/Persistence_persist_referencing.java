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
package org.apache.isis.objectstore.jdo.datanucleus.scenarios.refs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.refs.UnidirReferencedEntity;
import org.apache.isis.core.tck.dom.refs.UnidirReferencedEntityRepository;
import org.apache.isis.core.tck.dom.refs.UnidirReferencingEntity;
import org.apache.isis.core.tck.dom.refs.UnidirReferencingEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persist_referencing {

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


}
