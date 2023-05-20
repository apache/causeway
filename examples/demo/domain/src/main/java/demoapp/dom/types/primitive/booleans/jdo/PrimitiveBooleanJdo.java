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
package demoapp.dom.types.primitive.booleans.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Title;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.types.primitive.booleans.persistence.PrimitiveBooleanEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.PrimitiveBooleanEntity")
@DomainObject
public class PrimitiveBooleanJdo                                          // <.>
        extends PrimitiveBooleanEntity {

//end::class[]
    public PrimitiveBooleanJdo(final boolean initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "boolean (primitive) JDO entity: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Getter @Setter
    private boolean readOnlyProperty;                             // <.>

    @Property(editing = Editing.ENABLED)
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    @Getter @Setter
    private boolean readWriteProperty;

}
//end::class[]
