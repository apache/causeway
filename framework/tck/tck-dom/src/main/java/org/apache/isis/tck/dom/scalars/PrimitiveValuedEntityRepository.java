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

package org.apache.isis.tck.dom.scalars;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;

@Named("PrimitiveValuedEntities")
@ObjectType("PrimitiveValuedEntities")
public class PrimitiveValuedEntityRepository extends AbstractFactoryAndRepository {

    @Override
    public String getId() {
        return "PrimitiveValuedEntities";
    }

    @QueryOnly
    @MemberOrder(sequence = "1")
    public List<PrimitiveValuedEntity> list() {
        return allInstances(PrimitiveValuedEntity.class);
    }

    @QueryOnly
    @MemberOrder(sequence = "1")
    public PrimitiveValuedEntity findById(int id) {
        final Query<PrimitiveValuedEntity> query = new QueryDefault<PrimitiveValuedEntity>(PrimitiveValuedEntity.class, PrimitiveValuedEntity.class.getName() + "#pk", "id", id);
        return this.firstMatch(query);
    }

    @MemberOrder(sequence = "2")
    public PrimitiveValuedEntity newEntity() {
        final PrimitiveValuedEntity entity = newTransientInstance(PrimitiveValuedEntity.class);
        persist(entity);
        return entity;
    }
    
    @Programmatic
    public PrimitiveValuedEntity findByNamedQueryFirstOnly(String queryName, Map<String, Object> argumentByParameterName) {
        final Query<PrimitiveValuedEntity> query = new QueryDefault<PrimitiveValuedEntity>(PrimitiveValuedEntity.class, queryName, argumentByParameterName); 
        return this.firstMatch(query);
    }

    @Programmatic
    public List<PrimitiveValuedEntity> findByNamedQueryAll(String queryName, Map<String, Object> argumentByParameterName) {
        final Query<PrimitiveValuedEntity> query = new QueryDefault<PrimitiveValuedEntity>(PrimitiveValuedEntity.class, queryName, argumentByParameterName); 
        return this.allMatches(query);
    }
    
}
