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
package demoapp.dom.types.causewayval.markdowns.jdo;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.applib.annotation.Where;

import org.apache.causeway.valuetypes.markdown.applib.value.Markdown;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.types.causewayval.markdowns.persistence.CausewayMarkdownEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.CausewayMarkdownEntity")
@DomainObject
public class CausewayMarkdownJdo                                          // <.>
        extends CausewayMarkdownEntity {

//end::class[]
    public CausewayMarkdownJdo(final Markdown initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "Markdown JDO entity: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @Column(allowsNull = "false", jdbcType = "CLOB")                            // <.>
    @Getter @Setter
    private Markdown readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1", hidden = Where.ALL_TABLES, multiLine = 5)
    @Column(allowsNull = "false", jdbcType = "CLOB")
    @Getter @Setter
    private Markdown readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @Column(allowsNull = "true", jdbcType = "CLOB")                             // <.>
    @Getter @Setter
    private Markdown readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2", hidden = Where.ALL_TABLES, multiLine = 5)
    @Column(allowsNull = "true", jdbcType = "CLOB")
    @Getter @Setter
    private Markdown readWriteOptionalProperty;

}
//end::class[]
