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
package demoapp.dom.types.javatime.javatimeoffsetdatetime.jdo;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.javatime.javatimeoffsetdatetime.holder.JavaTimeOffsetDateTimeHolder3;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        objectType = "demo.JavaTimeOffsetDateTimeJdo"
)
public class JavaTimeOffsetDateTimeJdo                                          // <.>
        implements HasAsciiDocDescription, JavaTimeOffsetDateTimeHolder3 {

//end::class[]
    public JavaTimeOffsetDateTimeJdo(java.time.OffsetDateTime initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "java.time.OffsetDateTime JDO entity: ")
    @PropertyLayout(fieldSet = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false")                                               // <.>
    @Getter @Setter
    private java.time.OffsetDateTime readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @PropertyLayout(fieldSet = "editable-properties", sequence = "1")
    @Column(allowsNull = "false")
    @Getter @Setter
    private java.time.OffsetDateTime readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @PropertyLayout(fieldSet = "optional-properties", sequence = "1")
    @Column(allowsNull = "true")                                                // <.>
    @Getter @Setter
    private java.time.OffsetDateTime readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSet = "optional-properties", sequence = "2")
    @Column(allowsNull = "true")
    @Getter @Setter
    private java.time.OffsetDateTime readWriteOptionalProperty;

}
//end::class[]
