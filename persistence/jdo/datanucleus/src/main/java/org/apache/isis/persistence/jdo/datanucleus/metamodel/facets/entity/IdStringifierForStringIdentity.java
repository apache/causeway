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
package org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.jdo.identity.StringIdentity;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.services.bookmark.IdStringifierForString;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.val;

@Component
@Priority(PriorityPrecedence.LATE)
public class IdStringifierForStringIdentity extends IdStringifier.Abstract<StringIdentity> {

    @Inject IdStringifierForString idStringifierForString;

    public IdStringifierForStringIdentity() {
        super(StringIdentity.class);
    }

    /**
     * for testing only
     */
    @Builder
    IdStringifierForStringIdentity(IdStringifierForString idStringifierForString) {
        this();
        this.idStringifierForString = idStringifierForString;
    }

    @Override
    public String stringify(StringIdentity value) {
        return idStringifierForString.doStringify(value.toString());
    }

    @Override
    public StringIdentity parse(String stringified, Class<?> owningEntityType) {
        val idValue = idStringifierForString.doParse(stringified, null);
        return new StringIdentity(owningEntityType, idValue);
    }
}
