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
package org.apache.isis.jdo.jdosupport.mixins;

import javax.jdo.JDOHelper;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.DatastoreId;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

@Mixin(method = "prop")
public class Persistable_datanucleusIdLong {

    private final Persistable persistable;

    public Persistable_datanucleusIdLong(final Persistable persistable) {
        this.persistable = persistable;
    }

    public static class ActionDomainEvent extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Persistable_datanucleusIdLong> {}

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            contributed = Contributed.AS_ASSOCIATION
            )
    @PropertyLayout(
            named = "Id",
            hidden = Where.ALL_TABLES
            )
    @MemberOrder(name = "Metadata", sequence = "800.1")
    public Long prop() {
        final Object objectId = JDOHelper.getObjectId(persistable);
        if(objectId instanceof DatastoreId) {
            final DatastoreId datastoreId = (DatastoreId) objectId;
            final Object id = datastoreId.getKeyAsObject();
            return id != null && id instanceof Long ? (Long) id : null;
        }
        return null;
    }

    public boolean hideProp() {
        return prop() == null;
    }


}
