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
package org.apache.causeway.applib.services.metamodel;

import java.util.List;
import java.util.function.BiConsumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.collections._Multimaps;

import lombok.val;

/**
 *
 * @since 1.x {@index}
 */
public interface DomainModel {

    List<DomainMember> getDomainMembers();

    // -- SHORTCUTS

    default void forEachLogicalType(final @Nullable BiConsumer<? super String, ? super List<DomainMember>> consumer) {
        if(consumer==null) {
            return;
        }

        val membersByLogicalType = _Multimaps.<String, DomainMember>newListMultimap();

        getDomainMembers().forEach(member->
            membersByLogicalType.putElement(member.getLogicalTypeName(), member));

        membersByLogicalType.forEach(consumer);
    }

}
