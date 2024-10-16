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

import lombok.EqualsAndHashCode;

import java.util.Objects;

import javax.inject.Named;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib;
import org.apache.causeway.valuetypes.asciidoc.applib.jaxb.AsciiDocJaxbAdapter;

/**
 * Immutable value type holding XML rendered using an <a href="https://docs.asciidoctor.org/asciidoc/latest/verbatim/source-blocks/">Asciidoc source block</a>
 *
 * @since 2.0 {@index}
 */
@Named(CausewayModuleValAsciidocApplib.NAMESPACE + ".AsciiDocXml")
@org.apache.causeway.applib.annotation.Value
@EqualsAndHashCode
@XmlJavaTypeAdapter(AsciiDocJaxbAdapter.class)  // for JAXB view model support
public final class AsciiDocXml extends AsciiDocAbstract {

    public static AsciiDocXml valueOf(final String adoc) {
        return new AsciiDocXml(adoc);
    }

    public AsciiDocXml() {
        this(null);
    }

    public AsciiDocXml(final String adoc) {
        super(adoc, "xml");
    }

    public boolean isEqualTo(final AsciiDocXml other) {
        return Objects.equals(this, other);
    }

    @Override
    public String toString() {
        return String.format("AsciiDocXml[length=%d,content=%s]",
                adoc.length(), Markup.summarizeHtmlAsTitle(adoc));
    }

}
