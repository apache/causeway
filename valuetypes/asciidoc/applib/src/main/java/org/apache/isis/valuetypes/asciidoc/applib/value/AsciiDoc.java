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
package org.apache.isis.valuetypes.asciidoc.applib.value;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.value.HasHtml;
import org.apache.isis.valuetypes.asciidoc.applib.jaxb.AsciiDocJaxbAdapter;

/**
 * Immutable value type holding pre-rendered HTML.
 *
 * @since 2.0 {@index}
 */
@org.apache.isis.applib.annotation.Value(semanticsProviderName = "org.apache.isis.valuetypes.asciidoc.metamodel.facets.AsciiDocValueSemanticsProvider")
@XmlJavaTypeAdapter(AsciiDocJaxbAdapter.class)  // for JAXB view model support
public class AsciiDoc implements HasHtml, Serializable {

    private static final long serialVersionUID = 1L;

    public static AsciiDoc valueOfAdoc(String asciiDoc) {
        return valueOfHtml(Converter.adocToHtml(asciiDoc));
    }

    public static AsciiDoc valueOfHtml(String html) {
        return new AsciiDoc(html);
    }

    private final String html;

    public AsciiDoc() {
        this(null);
    }

    public AsciiDoc(String html) {
        this.html = html!=null ? html : "";
    }

    public String title() {
        return "AsciiDoc[length="+html.length()+"]";
    }

    @Override
    public String asHtml() {
        return html;
    }

    public boolean isEqualTo(final AsciiDoc other) {
        return other != null && this.html.equals(other.html);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return isEqualTo((AsciiDoc) obj);
    }

    @Override
    public int hashCode() {
        return html.hashCode();
    }

    @Override
    public String toString() {
        return "AsciiDoc[length="+html.length()+", html="+html+"]";
    }

}
