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
package demoapp.dom.types.javaawt.images.jdo;

import java.awt.image.BufferedImage;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import demoapp.dom.types.javaawt.images.persistence.JavaAwtBufferedImageEntity;
import lombok.Getter;
import lombok.Setter;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.JavaAwtBufferedImageEntity")
@DomainObject
public class JavaAwtBufferedImageJdo
        extends JavaAwtBufferedImageEntity
//end::class[]
// label positions not yet supported
//tag::class[]
{

//end::class[]
    public JavaAwtBufferedImageJdo(final BufferedImage initialValue) {
        this.readOnlyProperty = initialValue;
//        this.readWriteProperty = initialValue;    // editable properties not yet supported
    }

    // @Title not yet supported
    @ObjectSupport public String title() {
        return "Image JDO entity";
    }

    // @Title(prepend = "Image JDO entity: ")  // not yet supported
//tag::class[]
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false")                                   // <.>
    @Getter @Setter
    private BufferedImage readOnlyProperty;

//end::class[]
// editable properties not yet supported:
//    @Property(editing = Editing.ENABLED)                          // <.>
//    @PropertyLayout(group = "editable-properties", sequence = "1")
//    @Column(allowsNull = "false")
//    @Getter @Setter
//    private BufferedImage readWriteProperty;

//tag::class[]
    @Property(optionality = Optionality.OPTIONAL)                   // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @Column(allowsNull = "true")                                    // <.>
    @Getter @Setter
    private BufferedImage readOnlyOptionalProperty;

//end::class[]
// editable properties not yet supported:
//    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
//    @PropertyLayout(group = "optional-properties", sequence = "2")
//    @Column(allowsNull = "true")
//    @Getter @Setter
//    private BufferedImage readWriteOptionalProperty;

//tag::class[]
}
//end::class[]
