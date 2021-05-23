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
package demoapp.dom.types.isisext.asciidocs.jpa;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom.types.isisext.asciidocs.persistence.IsisAsciiDocEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "IsisAsciiDocJpa"
)
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
      objectType = "demo.IsisAsciiDocEntity"
)
@NoArgsConstructor
public class IsisAsciiDocJpa
        extends IsisAsciiDocEntity {

//end::class[]
    public IsisAsciiDocJpa(AsciiDoc initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Id
    @GeneratedValue
    private Long id;

    public String title() {
        return "AsciiDoc JPA entity: " +
            bookmarkService.bookmarkForElseFail(this).getIdentifier();
    }

    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false", jdbcType = "CLOB")                // <.>
    @Getter @Setter
    private AsciiDoc readOnlyProperty;

    @Property(editing = Editing.ENABLED)                            // <.>
    @PropertyLayout(hidden = Where.ALL_TABLES, fieldSetId = "editable-properties", sequence = "1")
    @Column(allowsNull = "false", jdbcType = "CLOB")
    @Getter @Setter
    private AsciiDoc readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                   // <.>
    @PropertyLayout(hidden = Where.ALL_TABLES, fieldSetId = "optional-properties", sequence = "1")
    @Column(allowsNull = "true")                                    // <.>
    @Getter @Setter
    private AsciiDoc readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(hidden = Where.ALL_TABLES, fieldSetId = "optional-properties", sequence = "2")
    @Column(allowsNull = "true")
    @Getter @Setter
    private AsciiDoc readWriteOptionalProperty;

    @Inject
    private BookmarkService bookmarkService;
}
//end::class[]
