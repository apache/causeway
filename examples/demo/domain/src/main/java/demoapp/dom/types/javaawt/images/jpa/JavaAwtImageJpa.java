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
package demoapp.dom.types.javaawt.images.jpa;

import java.awt.image.BufferedImage;

import javax.jdo.annotations.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

import demoapp.dom.types.javaawt.images.persistence.JavaAwtImageEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "JavaAwtImageJpa"
)
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
      objectType = "demo.JavaAwtImageEntity"
)
@NoArgsConstructor
public class JavaAwtImageJpa
        extends JavaAwtImageEntity
//end::class[]
// label positions not yet supported
//tag::class[]
{

//end::class[]
    public JavaAwtImageJpa(BufferedImage initialValue) {
        this.readOnlyProperty = initialValue;
//        this.readWriteProperty = initialValue;    // editable properties not yet supported
    }

    // @Title not yet supported
    public String title() {
        return "Image JPA entity";
    }

    // @Title(prepend = "Image JDO entity: ")  // not yet supported
//tag::class[]
    @Id
    @GeneratedValue
    private Long id;

    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false")                                   // <.>
    @Getter @Setter
    private BufferedImage readOnlyProperty;

//end::class[]

    @Override // once JavaAwtImageHolder2 is implemented by this class, move this up to JavaAwtImageEntity
    public java.awt.image.BufferedImage value() {
        return getReadOnlyProperty();
    }

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
