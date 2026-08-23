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
package demoapp.dom.domain.properties.Property.commandPublishing;

import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;

//tag::class[]
public abstract class PropertyCommandPublishingEntity
//end::class[]
        implements
        HasAsciiDocDescription,
        ValueHolder<String>
//tag::class[]
{
    // ...
//end::class[]

    public String title() {
        return value();
    }

    @Override
    public String value() {
        return getName();
    }

//tag::published[]
    @Property(
        editing = Editing.ENABLED,                  // <.>
        commandPublishing = Publishing.ENABLED      // <.>
    )
    @PropertyLayout(
        describedAs = "This property has command publishing enabled"
    )
    public abstract String getName();
    public abstract void setName(String value);
//end::published[]

//tag::not-published[]
    @Property(
        editing = Editing.ENABLED,                  // <.>
        commandPublishing = Publishing.DISABLED     // <.>
    )
    @PropertyLayout(
        describedAs = "This property does NOT have command publishing enabled",
        multiLine = 5
    )
    public abstract String getNotes();
    public abstract void setNotes(String value);
//end::not-published[]

//tag::class[]
}
//end::class[]
