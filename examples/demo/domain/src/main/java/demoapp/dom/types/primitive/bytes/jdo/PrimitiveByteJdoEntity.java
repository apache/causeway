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
package demoapp.dom.types.primitive.bytes.jdo;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.primitive.bytes.holder.PrimitiveByteHolder;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        objectType = "demo.PrimitiveByteJdoEntity"
)
public class PrimitiveByteJdoEntity                                 // <.>
        implements HasAsciiDocDescription, PrimitiveByteHolder {

//end::class[]
    public PrimitiveByteJdoEntity(byte initialValue) {
        this.readOnlyProperty = initialValue;
        this.readOnlyProperty2 = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "byte (primitive) JDO entity: ")
    @Getter @Setter
    private byte readOnlyProperty;                                  // <.>

    @Property
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @Getter @Setter
    private byte readOnlyProperty2;

    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private byte readWriteProperty;

}
//end::class[]
