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
package demoapp.dom.types.causeway.blobs.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.applib.value.Blob;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.types.causeway.blobs.persistence.CausewayBlobEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.CausewayBlobEntity")
@DomainObject
public class CausewayBlobJdo                                            // <.>
        extends CausewayBlobEntity {
    // ...
//end::class[]
    public CausewayBlobJdo(final Blob initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::property[]
    @Title(prepend = "Blob JDO entity: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Persistent(defaultFetchGroup="false", columns = {              // <.>
            @Column(name = "readOnlyProperty_name"),
            @Column(name = "readOnlyProperty_mimetype"),
            @Column(name = "readOnlyProperty_bytes")
    })
    @Getter @Setter
    private Blob readOnlyProperty;
//end::property[]

    @Property(editing = Editing.ENABLED,
            optionality = Optionality.MANDATORY)
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    @Persistent(defaultFetchGroup="false", columns = {
            @Column(name = "readWriteProperty_name"),
            @Column(name = "readWriteProperty_mimetype"),
            @Column(name = "readWriteProperty_bytes")
    })
    @Getter @Setter
    private Blob readWriteProperty;

//tag::optional-property[]
    @Property(optionality = Optionality.OPTIONAL)                   // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @Persistent(defaultFetchGroup="false", columns = {
            @Column(name = "readOnlyOptionalProperty_name",
                    allowsNull = "true"),                           // <.>
            @Column(name = "readOnlyOptionalProperty_mimetype",
                    allowsNull = "true"),
            @Column(name = "readOnlyOptionalProperty_bytes",
                    allowsNull = "true")
    })
    @Getter @Setter
    private Blob readOnlyOptionalProperty;
//end::optional-property[]

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2")
    @Persistent(defaultFetchGroup="false", columns = {
            @Column(name = "readWriteOptionalProperty_name",
                    allowsNull = "true"),
            @Column(name = "readWriteOptionalProperty_mimetype",
                    allowsNull = "true"),
            @Column(name = "readWriteOptionalProperty_bytes",
                    allowsNull = "true")
    })
    @Getter @Setter
    private Blob readWriteOptionalProperty;

//tag::class[]
}
//end::class[]
