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

package org.apache.isis.core.tck.dom.busrules;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.core.tck.dom.AbstractEntityRepository;

@Named("BusinessRulesEntities")
@ObjectType("BusinessRulesEntities")
@DomainService
public class BusRulesEntityRepository extends AbstractEntityRepository<BusRulesEntity> {

    public BusRulesEntityRepository() {
        super(BusRulesEntity.class, "BusinessRulesEntities");
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity findById(int id) {
        final Query<BusRulesEntity> query = 
                new QueryDefault<BusRulesEntity>(BusRulesEntity.class, BusRulesEntity.class.getName() + "#pk", "id", id);
        return this.firstMatch(query);
    }


    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity visibleAndInvocableAction(@Named("id") int id) {
        return this.findById(id);
    }

    @Disabled
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity visibleButNotInvocableAction(@Named("id") int id) {
        return this.findById(id);
    }

    @Hidden
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public BusRulesEntity invisibleAction(int id) {
        return this.findById(id);
    }

    
    
}
