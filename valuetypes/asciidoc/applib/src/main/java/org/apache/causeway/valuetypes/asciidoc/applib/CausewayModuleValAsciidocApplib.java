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
package org.apache.causeway.valuetypes.asciidoc.applib;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * @since 2.0 {@index}
 */
@Configuration
public class CausewayModuleValAsciidocApplib {
    public static final String NAMESPACE = "causeway.value.asciidoc";

    @Bean(NAMESPACE + ".AdocToHtmlConverter")
    @ConditionalOnMissingBean(AdocToHtmlConverter.class)
    @Qualifier("Default")
    public AdocToHtmlConverter createAdocToHtmlConverter(final CausewayConfiguration config) throws MalformedURLException {
        var asciidoctor = Asciidoctor.Factory.create();

        var krokiBaseUri = config.getValueTypes().getKroki().getBackendUrl();
        var requestTimeout = config.getValueTypes().getKroki().getRequestTimeout();

        if(krokiBaseUri!=null) {
            asciidoctor.javaExtensionRegistry().preprocessor(new OpenBlockPreProcessor());
            asciidoctor.javaExtensionRegistry().block("plantuml", new PlantumlBlockProcessor(krokiBaseUri, requestTimeout));
        }

        return new AdocToHtmlConverter(asciidoctor, org.asciidoctor.Options.builder()
                .safe(SafeMode.UNSAFE)
                .toFile(false)
                .attributes(Attributes.builder()
                        .showTitle(true)
                        .sourceHighlighter("prism")
                        .build())
                .build());
    }

    public static final class AdocToHtmlConverter {

        public static AdocToHtmlConverter instance() {
            return instance;
        }

        private static AdocToHtmlConverter instance;

        private final @NonNull Asciidoctor asciidoctor;
        private final @NonNull Options options;

        public AdocToHtmlConverter(@NonNull final Asciidoctor asciidoctor, @NonNull final Options options) {
            super();
            this.asciidoctor = asciidoctor;
            this.options = options;
            instance = this;
        }

        /**
         * For syntax highlighting to work, the client/browser needs to load specific
         * Javascript and CSS.
         * The framework supports this out of the box with its various viewers,
         * using <i>Prism</i> web-jars.
         *
         * @param adoc - formated input to be converted to HTML
         *
         * @see <a href="https://prismjs.com/">prismjs.com</a>
         */
        public String adocToHtml(final @Nullable String adoc) {
            return _Strings.isEmpty(adoc)
                    ? ""
                    : asciidoctor.convert(adoc, options);
        }

    }

    /**
     * Converts 'plantuml' blocks to 'open' blocks, such that these are recognized by the
     * {@link PlantumlBlockProcessor}. Not needed otherwise.
     */
    public static class OpenBlockPreProcessor extends Preprocessor {

        private static enum State {
            DISABLED,
            BEFORE_BLOCK_START,
            AFTER_BLOCK_STARTED;
            State next() {
                var values = State.values();
                return values[(this.ordinal() + 1) % values.length];
            }
        }

        @Override
        public Reader process(final Document document, final PreprocessorReader reader) {

            var state = State.DISABLED;

            final List<String> processedLines = new ArrayList<>();
            final List<String> lines = reader.readLines();
            for(var line : lines) {
                var trimmedLine = line.trim();

                switch(state) {
                case DISABLED: {
                    if(trimmedLine.startsWith("[plantuml")) {
                        // condition causes a state transition
                        state = state.next();
                    }
                }
                case BEFORE_BLOCK_START: {
                    if(trimmedLine.equals("----")) {
                        // condition causes a state transition
                        state = state.next();
                        processedLines.add("--");
                        continue;
                    }
                }
                case AFTER_BLOCK_STARTED: {
                    if(trimmedLine.equals("----")) {
                        // condition causes a state transition
                        state = state.next();
                        processedLines.add("--");
                        continue;
                    }
                }}
                processedLines.add(line);
            }

            reader.restoreLines(processedLines);
            return reader;
        }
    }

    @org.asciidoctor.extension.Name("plantuml")
    @org.asciidoctor.extension.Contexts({Contexts.OPEN})
    @org.asciidoctor.ast.ContentModel(org.asciidoctor.ast.ContentModel.SIMPLE)
    public static class PlantumlBlockProcessor extends BlockProcessor {

        private final @NonNull URL krokiBaseUri;
        private final @NonNull Duration requestTimeout;

        public PlantumlBlockProcessor(final URL krokiBaseUri, final Duration requestTimeout) {
            this.krokiBaseUri = krokiBaseUri;
            this.requestTimeout = requestTimeout;
        }

        @Override
        public Object process(final StructuralNode parent, final Reader reader, final Map<String, Object> attributes) {
            final String diagramSource = reader.read();
            return createBlock(parent, "pass", getPlantumlSvg(krokiBaseUri.toString(), requestTimeout, diagramSource));
        }

        // -- HELPER

        @SneakyThrows
        private static String getPlantumlSvg(final String krokiBaseUri, final Duration requestTimeout, final String diagramSource) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(krokiBaseUri + "/plantuml/svg/" + _Strings.base64UrlEncodeZlibCompressed(diagramSource)))
                    .timeout(requestTimeout)
                    .header("Content-Type", "image/svg+xml")
                    .GET()
                    .build();

            var client = HttpClient.newHttpClient();
            return client.send(request, BodyHandlers.ofString())
                .body();
        }

    }

}
