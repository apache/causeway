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
package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ExecutionDispatch;
import org.apache.isis.applib.annotation.Title;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.annotDomain.DomainObject.publishing.DomainObjectPublishingJdo;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.DomainObjectPublishingEnabledMetaAnnotation;
import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.DomainObjectPublishingDisabledMetaAnnotation;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObjectPublishingDisabledMetaAnnotation                   // <.>
@DomainObject(
        nature=Nature.JDO_ENTITY
        , objectType = "demo.DomainObjectPublishingEnabledMetaAnnotOverriddenJdo"
        , publishing = ExecutionDispatch.ENABLED                       // <.>
        , bounding = Bounding.BOUNDED
)
@DomainObjectLayout(
    describedAs =
        "@DomainObjectPublishingDisabledMetaAnnotation " +
        "@DomainObject(publishing=ENABLED)"
)
public class DomainObjectPublishingEnabledMetaAnnotOverriddenJdo implements DomainObjectPublishingJdo {
    // ...
//end::class[]

    public DomainObjectPublishingEnabledMetaAnnotOverriddenJdo(String initialValue) {
        this.property = initialValue;
        this.propertyUpdatedByAction = initialValue;
    }

    @Title(sequence = "1.0")
    @Getter @Setter
    private String property;

    @Getter @Setter
    @Title(sequence = "2.0", prepend = " / ")
    private String propertyUpdatedByAction;

//tag::class[]
}
//end::class[]
