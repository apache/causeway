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

package org.apache.isis.core.tck.dom.actions;

import java.util.List;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.core.tck.dom.AbstractEntityRepository;

@Named("ActionsEntities")
@ObjectType("ActionsEntities")
public class ActionsEntityRepository extends AbstractEntityRepository<ActionsEntity> {

    public ActionsEntityRepository() {
        super(ActionsEntity.class, "ActionsEntities");
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public ActionsEntity findById(int id) {
        final Query<ActionsEntity> query = 
                new QueryDefault<ActionsEntity>(ActionsEntity.class, ActionsEntity.class.getName() + "#pk", "id", id);
        return this.firstMatch(query);
    }


    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ActionsEntity> subList(@Named("from") int from, @Named("to") int to) {
        List<ActionsEntity> list = list();
        int toChecked = Math.min(to, list.size());
        int fromChecked = Math.min(from, toChecked);
        return list.subList(fromChecked, toChecked);
    }


    
    
}
