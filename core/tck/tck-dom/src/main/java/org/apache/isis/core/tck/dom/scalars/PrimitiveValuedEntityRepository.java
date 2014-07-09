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

package org.apache.isis.core.tck.dom.scalars;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.core.tck.dom.AbstractEntityRepository;

@Named("PrimitiveValuedEntities")
@ObjectType("PrimitiveValuedEntities")
@DomainService
public class PrimitiveValuedEntityRepository extends AbstractEntityRepository<PrimitiveValuedEntity> {

    public PrimitiveValuedEntityRepository() {
        super(PrimitiveValuedEntity.class, "PrimitiveValuedEntities");
    }

    @QueryOnly
    @MemberOrder(sequence = "1")
    public PrimitiveValuedEntity findById(int id) {
        final Query<PrimitiveValuedEntity> query = 
                new QueryDefault<PrimitiveValuedEntity>(PrimitiveValuedEntity.class, PrimitiveValuedEntity.class.getName() + "#pk", "id", id);
        return this.firstMatch(query);
    }
}
