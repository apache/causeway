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
package org.apache.isis.objectstore.jdo.datanucleus.scenarios.adaptermanager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.tck.dom.refs.UnidirReferencedEntity;
import org.apache.isis.core.tck.dom.refs.UnidirReferencedEntityRepository;
import org.apache.isis.core.tck.dom.refs.UnidirReferencingEntity;
import org.apache.isis.core.tck.dom.refs.UnidirReferencingEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_loadObject {

    private UnidirReferencingEntityRepository referencingRepo = new UnidirReferencingEntityRepository();
    private UnidirReferencedEntityRepository referencedRepo = new UnidirReferencedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("UNIDIRREFERENCINGENTITY"))
        .with(Utils.listenerToDeleteFrom("UNIDIRREFERENCEDENTITY"))
        .withServices(referencingRepo, referencedRepo)
        .build();


    @Test
    public void persist_then_update_using_persistentAdapterFor() throws Exception {
        
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

        TypedOid referencingOid2 = (TypedOid) iswf.adapterFor(referencingEntity2).getOid();

        TypedOid referencedOid1 = (TypedOid) iswf.adapterFor(referencedEntity1).getOid();
        TypedOid referencedOid2 = (TypedOid) iswf.adapterFor(referencedEntity2).getOid();


        // when ...
        iswf.bounceSystem();
        
        iswf.beginTran();

        ObjectAdapter referencingAdapter2 = iswf.getPersistor().loadObject(referencingOid2);
        referencingEntity2 = (UnidirReferencingEntity) referencingAdapter2.getObject();
        
		UnidirReferencedEntity referenced = referencingEntity2.getReferenced();
		
		ObjectAdapter referencedAdapter1 = iswf.getAdapterManager().adapterFor(referencedOid1);
		assertThat(referenced, is(referencedAdapter1.getObject()));

        // ...switch to refer to other

		ObjectAdapter referencedAdapter2 = iswf.getAdapterManager().adapterFor(referencedOid2);
		referencedEntity2 = (UnidirReferencedEntity) referencedAdapter2.getObject();

		referencingEntity2.setReferenced(referencedEntity2);
        iswf.commitTran();

    }



    
}
