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
package demoapp.dom.domain.objects.DomainObject.aliased;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.bookmark.BookmarkService;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;

public abstract class DomainObjectAliasedEntity
        implements
        HasAsciiDocDescription,
        ValueHolder<String> {

    public String title() {
        return value();
    }

    @Override
    public String value() {
        return getName();
    }

    public abstract String getName();
    public abstract void setName(String value);

// tag::class[]
    @Property
    public String getBookmark() {
        return bookmarkService.bookmarkFor(this).orElseThrow().stringify();
    }

    @Property
    public String getPreviousBookmark() {
        return getBookmark().replace("demo.party.", "demo.customer.");
    }

    @Inject private BookmarkService bookmarkService;
// end::class[]
}
