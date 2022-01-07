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
package demoapp.dom.types.isisext.markdowns.jpa;

import javax.inject.Inject;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Where;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.types.isisext.markdowns.persistence.IsisMarkdownEntity;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "IsisMarkdownJpa"
)
@EntityListeners(IsisEntityListener.class)
@DomainObject(
      logicalTypeName = "demo.IsisMarkdownEntity"
)
@NoArgsConstructor
public class IsisMarkdownJpa
        extends IsisMarkdownEntity {

//end::class[]
    public IsisMarkdownJpa(final Markdown initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Id
    @GeneratedValue
    private Long id;

    @ObjectSupport public String title() {
        return "Markdown JPA entity: " +
            bookmarkService.bookmarkForElseFail(this).getIdentifier();
    }

    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Column(nullable = false) @Lob @Basic(fetch=FetchType.LAZY)     // <.>
    @Getter @Setter
    private Markdown readOnlyProperty;

    @Property(editing = Editing.ENABLED)                            // <.>
    @PropertyLayout(hidden = Where.ALL_TABLES, fieldSetId = "editable-properties", sequence = "1")
    @Column(nullable = false) @Lob @Basic(fetch=FetchType.LAZY)
    @Getter @Setter
    private Markdown readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                   // <.>
    @PropertyLayout(hidden = Where.ALL_TABLES, fieldSetId = "optional-properties", sequence = "1")
    @Column(nullable = true) @Lob @Basic(fetch=FetchType.LAZY)      // <.>
    @Getter @Setter
    private Markdown readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(hidden = Where.ALL_TABLES, fieldSetId = "optional-properties", sequence = "2")
    @Column(nullable = true) @Lob @Basic(fetch=FetchType.LAZY)
    @Getter @Setter
    private Markdown readWriteOptionalProperty;

    @Inject @Transient
    private transient BookmarkService bookmarkService;
}
//end::class[]
