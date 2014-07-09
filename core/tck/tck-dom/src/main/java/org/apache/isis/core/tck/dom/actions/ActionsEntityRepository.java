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

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.tck.dom.AbstractEntityRepository;

@Named("ActionsEntities")
@ObjectType("ActionsEntities")
@DomainService
public class ActionsEntityRepository extends AbstractEntityRepository<ActionsEntity> {

    public ActionsEntityRepository() {
        super(ActionsEntity.class, "ActionsEntities");
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public ActionsEntity findById(@Named("id") int id) {
        return findByIdIfAny(id);
    }

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public ActionsEntity findByIdIdempotent(@Named("id") int id) {
        return findByIdIfAny(id);
    }

    @ActionSemantics(Of.NON_IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public ActionsEntity findByIdNotIdempotent(@Named("id") int id) {
        return findByIdIfAny(id);
    }

    private ActionsEntity findByIdIfAny(int id) {
        List<ActionsEntity> subList = subList(id, id+1);
        return subList.isEmpty()?null:subList.get(0);
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ActionsEntity> subList(
            @MustSatisfy(IntegerCannotBeNegative.class)
            @Named("from") int from, 
            @MustSatisfy(IntegerCannotBeNegative.class)
            @Named("to") int to) {
        List<ActionsEntity> list = list();
        int toChecked = Math.min(to, list.size());
        int fromChecked = Math.min(from, toChecked);
        return list.subList(fromChecked, toChecked);
    }
    public String validateSubList(final int from, final int to) {
        if(from > to) {
            return "'from' cannot be larger than 'to'";
        }
        return null;
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public boolean contains(@Named("searchFor") ActionsEntity entity, @Named("from") int from, @Named("to") int to) {
        List<ActionsEntity> list = subList(from, to);
        return list.contains(entity);
    }

    public static class IntegerCannotBeNegative implements Specification {
        @Override
        public String satisfies(Object obj) {
            if(!(obj instanceof Integer)) {
                return null;
            } 
            Integer integer = (Integer) obj;
            return integer.intValue() < 0? "Cannot be less than zero": null;
        }
    }


    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ActionsEntity> subListWithOptionalRange(
            @Optional
            @MustSatisfy(IntegerCannotBeNegative.class)
            @Named("from") Integer from, 
            @Optional
            @MustSatisfy(IntegerCannotBeNegative.class)
            @Named("to") Integer to) {
        return subList(valueElseDefault(from, 0), valueElseDefault(to, Integer.MAX_VALUE));
    }

    public String validateSubListWithOptionalRange(final Integer from, final Integer to) {
        return validateSubList(valueElseDefault(from, 0), valueElseDefault(to, Integer.MAX_VALUE));
    }

    private static int valueElseDefault(Integer value, int i) {
        return value != null? value: i;
    }
    
    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(sequence = "90")
    public String concatenate(@Named("str1") String str1, @Named("str2") String str2) {
        return str1 + str2;
    }


}
