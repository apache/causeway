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
package org.apache.causeway.applib.value;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.commons.net.DataUri;

/**
 * Intended to be used as a read-only property, to render plain HTML.
 *
 * @since 2.0 {@index}
 */
@Named(CausewayModuleApplib.NAMESPACE + ".value.Markup")
@Value
@XmlJavaTypeAdapter(Markup.JaxbToStringAdapter.class)   // for JAXB view model support
public record Markup(String html) implements Serializable {

    // -- FACTORIES

    public static Markup valueOf(final @Nullable String html) {
        return new Markup(html);
    }

    public static Markup embeddedImage(final @Nullable DataUri dataUri) {
        return dataUri!=null
                ? new Markup("<img src=\"" + dataUri.toExternalForm() + "\"/>")
                : new Markup(null);
    }

    // -- CONSTRUCTION

    public Markup() {
        this(null);
    }

    public Markup(final String html) {
        this.html = html!=null ? html : "";
    }

    @Override
    public String toString() {
        return String.format("Markup[length=%d,content=%s]",
                html.length(), summarizeHtmlAsTitle(html));
    }

    public static String summarizeHtmlAsTitle(final String html) {
        return _Strings.ellipsifyAtEnd(
                _Text.normalize(TextUtils.readLines(html)).stream()
                .collect(Collectors.joining(" ")),
                255, "...");
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
        public Markup unmarshal(final String v) throws Exception {
            return v != null
                    ? new Markup(_Strings.ofBytes(decoder.decode(v), StandardCharsets.UTF_8))
                    : null;
        }

        @Override
        public String marshal(final Markup v) throws Exception {
            return v != null
                    ? encoder.encodeToString(_Strings.toBytes(v.html(), StandardCharsets.UTF_8))
                    : null;
        }
    }

}
