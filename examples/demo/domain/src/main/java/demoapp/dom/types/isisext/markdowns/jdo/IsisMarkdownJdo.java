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
package demoapp.dom.types.isisext.markdowns.jdo;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.isisext.markdowns.holder.IsisMarkdownHolder2;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        objectType = "demo.IsisMarkdownJdo"
)
public class IsisMarkdownJdo                                          // <.>
        implements HasAsciiDocDescription, IsisMarkdownHolder2 {

//end::class[]
    public IsisMarkdownJdo(Markdown initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    public String title() {
        return "Markdown JDO entity: " +
            bookmarkService.bookmarkFor(this).getIdentifier();
}

    @MemberOrder(name = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false", jdbcType = "CLOB")                // <.>
    @Getter @Setter
    private Markdown readOnlyProperty;

    @Property(editing = Editing.ENABLED)                            // <.>
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @MemberOrder(name = "editable-properties", sequence = "1")
    @Column(allowsNull = "false", jdbcType = "CLOB")
    @Getter @Setter
    private Markdown readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                   // <.>
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @MemberOrder(name = "optional-properties", sequence = "1")
    @Column(allowsNull = "true")                                    // <.>
    @Getter @Setter
    private Markdown readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @MemberOrder(name = "optional-properties", sequence = "2")
    @Column(allowsNull = "true")
    @Getter @Setter
    private Markdown readWriteOptionalProperty;

    @Inject
    private BookmarkService bookmarkService;
}
//end::class[]
