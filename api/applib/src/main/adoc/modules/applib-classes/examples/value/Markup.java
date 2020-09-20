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

package org.apache.isis.applib.value;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.commons.internal.base._Strings;

/**
 * Immutable value type holding pre-rendered HTML.
 *
 */
// tag::refguide[]
// end::refguide[]
@Value(semanticsProviderName = 
        "org.apache.isis.core.metamodel.facets.value.markup.MarkupValueSemanticsProvider")
@XmlJavaTypeAdapter(Markup.JaxbToStringAdapter.class)   // for JAXB view model support
public class Markup implements HasHtml, Serializable {   // TODO: should be final

    private static final long serialVersionUID = 1L;
    private final String html;

    public Markup() {
        this(null);
    }

    public Markup(String html) {
        this.html = html!=null ? html : "";
    }

    public String asHtml() {
        return html;
    }

    public boolean isEqualTo(final Markup other) {
        return other==null ? false : this.html.equals(other.html);
    }

    public String title() {
        return "Markup[length="+html.length()+"]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return isEqualTo((Markup) obj);
    }

    @Override
    public int hashCode() {
        return html.hashCode();
    }

    @Override
    public String toString() {
        return "Markup[length="+html.length()+", html="+html+"]";
    }

    public static final class JaxbToStringAdapter extends XmlAdapter<String, Markup> {

        /**
         * Is threadsafe, see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Encoder.html">JDK8 javadocs</a>
         */
        private final Base64.Encoder encoder = Base64.getEncoder();
        /**
         * Is threadsafe, see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.Decoder.html">JDK8 javadocs</a>
         */
        private final Base64.Decoder decoder = Base64.getDecoder(); // is thread-safe ?

        @Override
        public Markup unmarshal(String v) throws Exception {
            return v != null
                    ? new Markup(_Strings.ofBytes(decoder.decode(v), StandardCharsets.UTF_8))
                    : null;
        }

        @Override
        public String marshal(Markup v) throws Exception {
            return v != null
                    ? encoder.encodeToString(_Strings.toBytes(v.asHtml(), StandardCharsets.UTF_8))
                    : null;
        }
    }

}
