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
package org.causewayaddons.wicket.pdfjs.fixture.demoapp.demomodule.dom;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;

import org.wicketstuff.pdfjs.Scale;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberOrder;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.value.Blob;

import org.causewayaddons.wicket.pdfjs.cpt.applib.PdfJsViewer;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.DATASTORE,
        schema = "wktPdfjsFixture"
)
@javax.jdo.annotations.DatastoreIdentity(strategy= IdGeneratorStrategy.IDENTITY, column = "id")
@javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column = "version")
@DomainObject
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT,
        cssClassUiEvent = PdfJsDemoObjectWithBlob.CssClassUiEvent.class
)
public class PdfJsDemoObjectWithBlob implements Comparable<PdfJsDemoObjectWithBlob> {


    public static class CssClassUiEvent
            extends org.apache.causeway.applib.services.eventbus.CssClassUiEvent<PdfJsDemoObjectWithBlob> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Title(sequence="1")
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private String name;


    @PropertyLayout(group = "name", sequence = "1")
    public PdfJsDemoObjectWithBlob updateName(String name) {
        setName(name);
        return this;
    }
    @MemberSupport public String default0UpdateName() {
        return getName();
    }



    @javax.jdo.annotations.Column(allowsNull="true")
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private String url;



    @javax.jdo.annotations.Persistent(defaultFetchGroup="false", columns = {
            @javax.jdo.annotations.Column(name = "blob_name"),
            @javax.jdo.annotations.Column(name = "blob_mimetype"),
            @javax.jdo.annotations.Column(name = "blob_bytes", jdbcType = "BLOB", sqlType = "LONGVARBINARY")
    })
    @Property(optionality = Optionality.OPTIONAL, editing = Editing.ENABLED)
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @Setter
    private Blob blob;

    //    @Getter(onMethod = @__({ }))  throwing a compile exception :-(
    @PdfJsViewer(initialPageNum = 1, initialScale = Scale._1_00, initialHeight = 600)
    public Blob getBlob() {
        return blob;
    }


    @Action(semantics = SemanticsOf.SAFE)
    @PropertyLayout(group = "url", sequence = "1")
    public URL openUrl() throws MalformedURLException {
        return new java.net.URL(getUrl());
    }
    @MemberSupport public String disableOpenUrl() {
        if (getUrl() == null)
            return "No URL to open";
        return null;
    }



    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(final PdfJsDemoObjectWithBlob other) {
        return Ordering.natural().onResultOf(PdfJsDemoObjectWithBlob::getName).compare(this, other);
    }


}
