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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        objectType = "demo.JavaAwtImageJdo"
)
public class JavaAwtImageJdo                                          // <.>
        implements HasAsciiDocDescription
//end::class[]
// label positions not yet supported
//        , JavaAwtImageHolder2
//tag::class[]
{

//end::class[]
    public JavaAwtImageJdo(BufferedImage initialValue) {
        this.readOnlyProperty = initialValue;
//        this.readWriteProperty = initialValue;    // editable properties not yet supported
    }

    // @Title not yet supported
    public String title() {
        return "Image JDO entity";
    }

    // @Title(prepend = "Image JDO entity: ")  // not yet supported
//tag::class[]
    @PropertyLayout(group = "read-only-properties", sequence = "1")
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
    @PropertyLayout(group = "optional-properties", sequence = "1")
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
