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
package demoapp.dom.types.jodatime.jodalocaldatetime.jdo;

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
import demoapp.dom.types.jodatime.jodalocaldatetime.holder.JodaLocalDateTimeHolder3;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        objectType = "demo.JodaLocalDateTimeJdo"
)
public class JodaLocalDateTimeJdo                                          // <.>
        implements HasAsciiDocDescription, JodaLocalDateTimeHolder3 {

//end::class[]
    public JodaLocalDateTimeJdo(org.joda.time.LocalDateTime initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "org.joda.time.LocalDateTime JDO entity: ")
    @PropertyLayout(group = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false")                                               // <.>
    @Getter @Setter
    private org.joda.time.LocalDateTime readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @PropertyLayout(group = "editable-properties", sequence = "1")
    @Column(allowsNull = "false")
    @Getter @Setter
    private org.joda.time.LocalDateTime readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @PropertyLayout(group = "optional-properties", sequence = "1")
    @Column(allowsNull = "true")                                                // <.>
    @Getter @Setter
    private org.joda.time.LocalDateTime readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(group = "optional-properties", sequence = "2")
    @Column(allowsNull = "true")
    @Getter @Setter
    private org.joda.time.LocalDateTime readWriteOptionalProperty;

}
//end::class[]
