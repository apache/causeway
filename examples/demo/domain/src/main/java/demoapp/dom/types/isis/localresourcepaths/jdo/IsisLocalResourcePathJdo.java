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
package demoapp.dom.types.isis.localresourcepaths.jdo;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.value.LocalResourcePath;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.types.isis.localresourcepaths.persistence.IsisLocalResourcePathEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        logicalTypeName = "demo.IsisLocalResourcePathEntity"
)
public class IsisLocalResourcePathJdo                                   // <.>
        extends IsisLocalResourcePathEntity {

//end::class[]
    public IsisLocalResourcePathJdo(LocalResourcePath initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "LocalResourcePath JDO entity: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false")                                       // <.>
    @Getter @Setter
    private LocalResourcePath readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                // <.>
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    @Column(allowsNull = "false")
    @Getter @Setter
    private LocalResourcePath readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                       // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @Column(allowsNull = "true")                                        // <.>
    @Getter @Setter
    private LocalResourcePath readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2")
    @NotPersistent
    // @Column(allowsNull = "true")
    @Getter @Setter
    private LocalResourcePath readWriteOptionalProperty;

}
//end::class[]
