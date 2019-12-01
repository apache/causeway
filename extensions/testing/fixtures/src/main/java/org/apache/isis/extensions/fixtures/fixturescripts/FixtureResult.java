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
package org.apache.isis.extensions.fixtures.fixturescripts;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "extFixture.FixtureResult"
        )
@ViewModelLayout(paged=500)
@XmlRootElement(name = "fixtureResult")
@XmlType(
        propOrder = {
                "fixtureScriptClassName",
                "fixtureScriptQualifiedName",
                "key",
                "objectBookmark"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class FixtureResult {

    @PropertyLayout(named="Fixture script")
    @Property(optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private String fixtureScriptClassName;

    @Getter(onMethod = @__(@Programmatic)) @Setter
    private String fixtureScriptQualifiedName;

    @Title(sequence="1", append=": ")
    @Getter @Setter
    private String key;

    @Getter(onMethod = @__(@Programmatic)) @Setter
    private String objectBookmark;

    @PropertyLayout(named="Result")
    @Title(sequence="2")
    public Object getObject() {
        return bookmarkService.lookup(new Bookmark(objectBookmark));
    }

    public void setObject(Object object) {
        this.objectBookmark = bookmarkService.bookmarkFor(object).toString();
    }

    @PropertyLayout(named="Result class")
    @MemberOrder(sequence="3")
    public String getClassName() {
        return getObjectBookmark() != null? getObject().getClass().getName(): null;
    }


    @XmlTransient @Inject BookmarkService bookmarkService;


}