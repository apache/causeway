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
package demoapp.dom.annotDomain.DomainObject.entityChangePublishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyUpdatedByAction"
)
public class DomainObjectAuditingJdo_updatePropertyUsingAction implements HasAsciiDocDescription {
    // ...
//end::class[]
    private final DomainObjectAuditingJdo domainObjectAuditingJdo;

    public DomainObjectAuditingJdo_updatePropertyUsingAction(DomainObjectAuditingJdo domainObjectAuditingJdo) {
        this.domainObjectAuditingJdo = domainObjectAuditingJdo;
    }

//tag::class[]
    public DomainObjectAuditingJdo_updatePropertyUsingAction updateProperty(final String value) {
        domainObjectAuditingJdo.setPropertyUpdatedByAction(value);
        return this;
    }
    public String default0UpdateProperty() {
        return domainObjectAuditingJdo.getPropertyUpdatedByAction();
    }

}
//end::class[]
