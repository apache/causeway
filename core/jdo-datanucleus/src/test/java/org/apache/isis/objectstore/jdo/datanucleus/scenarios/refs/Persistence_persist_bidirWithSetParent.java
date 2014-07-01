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
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.refs.BidirWithSetChildEntity;
import org.apache.isis.core.tck.dom.refs.BidirWithSetParentEntity;
import org.apache.isis.core.tck.dom.refs.BidirWithSetParentEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persist_bidirWithSetParent {

    private BidirWithSetParentEntityRepository repo = new BidirWithSetParentEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("BIDIRWITHSETCHILDENTITY"))
        .with(Utils.listenerToDeleteFrom("BIDIRWITHSETPARENTENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwoParents() throws Exception {
        iswf.beginTran();
        repo.newEntity().setName("Parent 1");
        repo.newEntity().setName("Parent 2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<BidirWithSetParentEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistTwoChildrenOfParent() throws Exception {
        iswf.beginTran();
        repo.newEntity().setName("Parent 1");
        repo.newEntity().setName("Parent 2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        BidirWithSetParentEntity retrievedEntity = repo.list().get(0);
        retrievedEntity.newChild("Child 1 of Parent 1");
        retrievedEntity.newChild("Child 2 of Parent 1");
        retrievedEntity.newChild("Child 3 of Parent 1");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        retrievedEntity = repo.list().get(0);
        Set<BidirWithSetChildEntity> children = retrievedEntity.getChildren();
        assertThat(children.size(), is(3));
        iswf.commitTran();
    }

    @Ignore("Not currently working, is an update")
    @Test
    public void updateBidirectional() throws Exception {
        iswf.beginTran();
        BidirWithSetParentEntity parent1 = repo.newEntity();
        parent1.setName("Parent 1");
        BidirWithSetParentEntity parent2 = repo.newEntity();
        parent2.setName("Parent 2");

        parent1.newChild("Child 1 of Parent 1");
        parent1.newChild("Child 2 of Parent 1");
        parent1.newChild("Child 3 of Parent 1");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        parent1 = repo.list().get(0);
        parent2 = repo.list().get(0);
        Set<BidirWithSetChildEntity> children = parent1.getChildren();
        assertThat(children.size(), is(3));
        BidirWithSetChildEntity child1 = parent1.getChildren().iterator().next();
        child1.moveTo(parent2);
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        parent1 = repo.list().get(0);
        parent2 = repo.list().get(0);
        children = parent1.getChildren();
        assertThat(children.size(), is(2));
        
        children = parent2.getChildren();
        assertThat(children.size(), is(1));
        iswf.commitTran();
    }
    

}
