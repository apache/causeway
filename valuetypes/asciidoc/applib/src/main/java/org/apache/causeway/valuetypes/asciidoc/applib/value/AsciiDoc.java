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
package org.apache.causeway.valuetypes.asciidoc.applib.value;

import java.io.Serializable;
import java.util.Objects;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.commons.internal.hardening._Hardening;
import org.apache.causeway.commons.internal.hardening._Hardening.SafelistSelect;
import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib;
import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib.AdocToHtmlConverter;
import org.apache.causeway.valuetypes.asciidoc.applib.jaxb.AsciiDocJaxbAdapter;

/**
 * Immutable value type holding pre-rendered HTML.
 *
 * @since 2.0 {@index}
 */
@Named(CausewayModuleValAsciidocApplib.NAMESPACE + ".AsciiDoc")
@org.apache.causeway.applib.annotation.Value
@XmlJavaTypeAdapter(AsciiDocJaxbAdapter.class)  // for JAXB view model support
public record AsciiDoc(String adoc, String asHtml) implements Serializable {

    public static AsciiDoc valueOf(final String adoc) {
        return new AsciiDoc(adoc, null);
    }

    public AsciiDoc {
    	adoc = adoc != null
        		? adoc
				: "";
    	asHtml = _Hardening.toSafeHtml(
    			AdocToHtmlConverter.instance().adocToHtml(adoc), SafelistSelect.ASCIIDOC);
    }

    public AsciiDoc() {
        this("", null);
    }

    public AsciiDoc(final String adoc) {
        this(adoc, null);
    }

    @Override
    public final boolean equals(final Object o) {
    	return o instanceof AsciiDoc other
			? Objects.equals(this.adoc, other.adoc)
			: false;
    }

    @Override
    public final int hashCode() {
    	return Objects.hashCode(adoc);
    }

    public boolean isEqualTo(final AsciiDoc other) {
        return Objects.equals(this, other);
    }

    @Override
    public String toString() {
        return String.format("AsciiDoc[length=%d,content=%s]",
                adoc.length(), Markup.summarizeHtmlAsTitle(adoc));
    }

    // -- DEPRECATIONS

    /**
     * @deprecated use {@link #adoc()} instead.
     */
    @Deprecated
    public String getAdoc() {
    	return adoc();
    }
}
