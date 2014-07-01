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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.tck.dom.refs.PolyClassChildEntity;
import org.apache.isis.core.tck.dom.refs.PolyClassParentEntity;
import org.apache.isis.core.tck.dom.refs.PolyClassParentEntityRepository;
import org.apache.isis.objectstore.jdo.datanucleus.Utils;

public class Persistence_persist_polyclass {

    private PolyClassParentEntityRepository repo = new PolyClassParentEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("POLYCLASSSUBTYPE1ENTITY"))
        .with(Utils.listenerToDeleteFrom("POLYCLASSCHILDENTITY"))
        .with(Utils.listenerToDeleteFrom("POLYCLASSPARENTENTITY"))
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
        List<PolyClassParentEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistSixDifferentChildrenOfParent() throws Exception {
        iswf.beginTran();
        repo.newEntity().setName("Parent 1");
        repo.newEntity().setName("Parent 2");
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        PolyClassParentEntity retrievedEntity = repo.list().get(0);
        retrievedEntity.newSubtype1("Child 1 of Parent 1", 123);
        retrievedEntity.newSubtype1("Child 2 of Parent 1", 456);
        retrievedEntity.newSubtype2("Child 3 of Parent 1", "abc");
        retrievedEntity.newSubtype2("Child 4 of Parent 1", "def");
        retrievedEntity.newSubtype3("Child 5 of Parent 1", BigDecimal.ONE);
        retrievedEntity.newSubtype3("Child 6 of Parent 1", BigDecimal.TEN);
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        retrievedEntity = repo.list().get(0);
        Set<PolyClassChildEntity> children = retrievedEntity.getChildren();
        assertThat(children.size(), is(6));
        iswf.commitTran();
    }

    

}
